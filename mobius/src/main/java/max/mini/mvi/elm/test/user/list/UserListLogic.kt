package max.mini.mvi.elm.test.user.list

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.functions.Consumer
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.test.base.*
import max.mini.mvi.elm.utils.Either
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

object UserListLogic {

    fun init(model: UserListDataModel): First<UserListDataModel, UserListEffect> {
        return if (model.listState.isInitialized().not()) {
            val (listState, sideEffects) = model.listState.reduce(
                ListAction.Refresh()
            )
            First.first(
                model.copy(
                    listState = listState
                ),
                sideEffects.mapped
            )
        } else {
            First.first(model)
        }
    }

    fun update(
        model: UserListDataModel,
        event: UserListEvent
    ): Next<UserListDataModel, UserListEffect> {
        return when (event) {
            is UserListEvent.RefreshRequest -> {
                val (listState, sideEffects) = model.listState.reduce(
                    ListAction.Refresh()
                )
                Next.next(
                    model.copy(
                        listState = listState
                    ),
                    sideEffects.mapped
                )
            }
            is UserListEvent.UserListLoaded -> {
                val (listState, sideEffects) = model.listState.reduce(
                    if (event.users.isEmpty()) ListAction.EmptyPageLoaded()
                    else ListAction.PageLoaded(event.users)
                )
                Next.next(
                    model.copy(
                        listState = listState
                    ),
                    sideEffects.mapped
                )
            }
            is UserListEvent.UserListLoadFailed -> {
                val (listState, sideEffects) = model.listState.reduce(
                    ListAction.PageLoadFailed(event.error)
                )
                Next.next(
                    model.copy(
                        listState = listState
                    ),
                    sideEffects.mapped
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
            is UserListEvent.LoadNextPage -> {
                val (listState, sideEffects) = model.listState.reduce(
                    ListAction.LoadMore()
                )
                Next.next(
                    model.copy(
                        listState = listState
                    ),
                    sideEffects.mapped
                )
            }
        }
    }

}

class UserListEffectHandler @Inject constructor(
    private val context: Context,
    private val repository: Repository,
    private val coordinator: UserListCoordinator
) : Connectable<UserListEffect, UserListEvent> {

    override fun connect(
        output: Consumer<UserListEvent>
    ): Connection<UserListEffect> {

        return object : Connection<UserListEffect>, CoroutineScope {

            private val job = SupervisorJob()

            override val coroutineContext: CoroutineContext = job + Dispatchers.IO

            override fun accept(value: UserListEffect) {
                when (value) {
                    is UserListEffect.LoadPage -> {
                        launch {
                            val event = when (val response = loadPage(value.page)) {
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
                    }
                    is UserListEffect.ShowError -> {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, value.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    is UserListEffect.OpenUserInfoById -> {
                        launch(Dispatchers.Main) {
                            coordinator.onPickUserWithId(value.userId)
                        }
                    }
                }

            }

            override fun dispose() {
                job.cancelChildren()
            }
        }
    }

    private suspend fun loadPage(
        page: Int
    ): Either<List<UserDataModel>, Throwable> {
        val response = repository.getUsersForPage(page)
        return when (response) {
            is Either.Left -> Either.Left(
                response.left.map {
                    UserDataModel(
                        id = checkNotNull(it.id),
                        name = it.name ?: "",
                        email = it.email ?: ""
                    )
                }
            )
            is Either.Right -> Either.Right(response.right)
        }
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
    val listState: ParcelableListState<UserDataModel> = ParcelableListState.NotInitialized(),
    val pickedUsers: Set<Int> = emptySet()
) : Parcelable

@Parcelize
data class UserDataModel(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable

data class UserListViewModel(
    val users: List<UserViewModel> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false
)

data class UserViewModel(
    val name: String,
    val email: String,
    val picked: Boolean
)

val UserListDataModel.viewModel
    get() = UserListViewModel(
        users = this.listState.loadedItems().map { user ->
            UserViewModel(
                name = user.name,
                email = user.email,
                picked = this.pickedUsers.any { user.id == it }
            )
        },
        loading = this.listState is ParcelableListState.EmptyProgress,
        refreshing = this.listState is ParcelableListState.Refreshing
    )

private val Set<ListSideEffect>.mapped: Set<UserListEffect>
    get() = this.map {
        when (it) {
            is ListSideEffect.LoadPage -> UserListEffect.LoadPage(it.page)
            is ListSideEffect.EmitError -> UserListEffect.ShowError(
                it.error.message ?: it.error.localizedMessage
            )
        }
    }.toSet()
