package max.mini.mvi.elm.mobius_xml_layout.user.list

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.UiThread
import com.spotify.mobius.First
import com.spotify.mobius.Next
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.common_ui.ListAction
import max.mini.mvi.elm.common_ui.ParcelableListState
import max.mini.mvi.elm.common_ui.listStateUpdater
import max.mini.mvi.elm.common_ui.loadedItems
import max.mini.mvi.elm.mobius_common.toFirst
import max.mini.mvi.elm.mobius_xml_layout.base.CoroutineScopeEffectHandler
import max.mini.mvi.elm.utils.Either

object UserListLogic {

    fun init(model: UserListDataModel): First<UserListDataModel, UserListEffect> {
        return if (model.isInitialized) {
            First.first(model)
        } else {
            listUpdater.update(
                model,
                UserListEvent.RefreshRequest
            ).toFirst
        }
    }

    fun update(
        model: UserListDataModel,
        event: UserListEvent
    ): Next<UserListDataModel, UserListEffect> {
        return when (event) {
            is UserListEvent.RefreshRequest,
            is UserListEvent.UserListLoaded,
            is UserListEvent.UserListLoadFailed,
            is UserListEvent.LoadNextPage -> {
                listUpdater.update(
                    model,
                    event
                )
            }
            is UserListEvent.UserWithPositionClick -> Next.next(
                model,
                setOf(
                    UserListEffect.OpenUserInfoById(
                        model.listState.loadedItems()[event.position].id
                    )
                )
            )
            is UserListEvent.Picked -> Next.next(
                model.copy(
                    pickedUsers = model.pickedUsers.plus(event.userId)
                )
            )
        }
    }


    private val listUpdater = listStateUpdater<
            UserListDataModel,
            UserListEvent,
            UserListEffect,
            UserDataModel>(
        listStateExtractor = { listState },
        refreshEventMapper = { if (it is UserListEvent.RefreshRequest) ListAction.Refresh() else null },
        pageLoadedEventMapper = { if (it is UserListEvent.UserListLoaded) ListAction.PageLoaded(it.users) else null },
        pageLoadFailedEventMapper = {
            if (it is UserListEvent.UserListLoadFailed) ListAction.PageLoadFailed(it.error)
            else null
        },
        loadMoreEventMapper = { if (it is UserListEvent.LoadNextPage) ListAction.LoadMore() else null },
        modelUpdater = { copy(listState = it) },
        loadPageEffectMapper = { UserListEffect.LoadPage(it.page) },
        emitErrorEffectMapper = {
            UserListEffect.ShowError(
                it.error.message ?: it.error.localizedMessage
            )
        }
    )

}

fun userListEffectHandler(
    context: Context,
    repository: Repository,
    coordinator: UserListCoordinator
) = CoroutineScopeEffectHandler<UserListEffect, UserListEvent> { value, output ->
    when (value) {
        is UserListEffect.LoadPage -> {
            val event = when (val response = loadPage(
                repository,
                value.page
            )) {
                is Either.Left -> UserListEvent.UserListLoaded(
                    response.left
                )
                is Either.Right -> UserListEvent.UserListLoadFailed(
                    response.right
                )
            }
            output.accept(
                event
            )
        }
        is UserListEffect.ShowError -> {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, value.message, Toast.LENGTH_LONG).show()
            }
        }
        is UserListEffect.OpenUserInfoById -> {
            withContext(Dispatchers.Main) {
                coordinator.onPickUserWithId(value.userId)
            }
        }
    }
}

private suspend fun loadPage(
    repository: Repository,
    page: Int
): Either<List<UserDataModel>, Throwable> {
    val response = repository.getUsersForPage(page)
    return when (response) {
        is Either.Left -> Either.Left(
            response.left.map {
                UserDataModel(
                    id = checkNotNull(it.id),
                    name = arrayOf(it.firstName, it.lastName)
                        .filterNotNull()
                        .joinToString(separator = " "),
                    email = it.email ?: ""
                )
            }
        )
        is Either.Right -> Either.Right(response.right)
    }
}

sealed class UserListEvent {
    // ui
    object RefreshRequest : UserListEvent()
    object LoadNextPage : UserListEvent()
    class UserWithPositionClick(val position: Int) : UserListEvent()
    class Picked(val userId: Int) : UserListEvent()

    // model
    class UserListLoaded(val users: List<UserDataModel>) : UserListEvent()
    class UserListLoadFailed(val error: Throwable) : UserListEvent()
}

sealed class UserListEffect {
    class LoadPage(val page: Int) : UserListEffect()
    class ShowError(val message: String) : UserListEffect()
    class OpenUserInfoById(val userId: Int) : UserListEffect()
}

@Parcelize
data class UserListDataModel(
    val listState: ParcelableListState<UserDataModel>,
    val pickedUsers: Set<Int>
) : Parcelable {

    companion object {
        val initial: UserListDataModel
            get() = UserListDataModel(
                listState = ParcelableListState.NotInitialized(),
                pickedUsers = emptySet()
            )
    }

    val isInitialized: Boolean
        get() = listState !is ParcelableListState.NotInitialized

}

@Parcelize
data class UserDataModel(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable

data class UserListViewModel(
    val users: List<UserViewModel>,
    val loading: Boolean,
    val refreshing: Boolean,
    val loadingMore: Boolean
)

data class UserViewModel(
    val id: Int,
    val name: String,
    val email: String,
    val picked: Boolean
)

val UserListDataModel.viewModel
    get() = UserListViewModel(
        users = this.listState.loadedItems().map { user ->
            UserViewModel(
                id = user.id,
                name = user.name,
                email = user.email,
                picked = this.pickedUsers.any { user.id == it }
            )
        },
        loading = this.listState is ParcelableListState.EmptyProgress,
        refreshing = this.listState is ParcelableListState.Refreshing,
        loadingMore = this.listState is ParcelableListState.NextPageLoading
    )


interface UserListCoordinator {

    @UiThread
    fun onPickUserWithId(
        userId: Int
    )

}