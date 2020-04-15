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
import max.mini.mvi.elm.utils.Either
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

object UserListLogic {

    fun init(model: UserListModel): First<UserListModel, UserListEffect> {
        return if (model.users.isEmpty()) {
            First.first(model.copy(loading = true), setOf(UserListEffect.Refresh))
        } else {
            First.first(model)
        }
    }

    fun update(
        model: UserListModel,
        event: UserListEvent
    ): Next<UserListModel, UserListEffect> {
        return when (event) {
            is UserListEvent.RefreshRequest -> {
                if (model.loading) {
                    Next.next<UserListModel, UserListEffect>(
                        model
                    )
                } else {
                    Next.next<UserListModel, UserListEffect>(
                        model.copy(
                            currentPage = 0,
                            refreshing = true
                        ),
                        setOf(UserListEffect.Refresh)
                    )
                }
            }
            is UserListEvent.UserInitialListLoaded -> Next.next(
                model.copy(
                    loading = false,
                    refreshing = false,
                    users = event.users
                )
            )
            is UserListEvent.UserNextListLoaded -> Next.next(
                model.copy(
                    loading = false,
                    refreshing = false,
                    users = model.users.plus(
                        event.users
                    )
                )
            )
            is UserListEvent.UserListLoadFailed -> Next.next(
                model.copy(
                    loading = false,
                    refreshing = false,
                    users = emptyList()
                ),
                setOf(
                    UserListEffect.ShowError(
                        event.error.message ?: event.error.localizedMessage
                    )
                )
            )
            is UserListEvent.UserWithPositionClick -> Next.next(
                model,
                setOf(
                    UserListEffect.OpenUserInfoById(
                        model.users[event.position].id
                    )
                )
            )
            is UserListEvent.Picked -> Next.next(
                model.copy(
                    users = model.users.map {
                        if (it.id == event.userId) {
                            it.copy(
                                picked = true
                            )
                        } else {
                            it
                        }
                    }
                )
            )
            is UserListEvent.LoadNextPage -> {
                if (model.loading) {
                    Next.noChange<UserListModel, UserListEffect>()
                } else {
                    val newModel = model.copy(
                        loading = true,
                        currentPage = model.currentPage + 1
                    )
                    Next.next<UserListModel, UserListEffect>(
                        newModel,
                        setOf(
                            UserListEffect.LoadPage(
                                newModel.currentPage
                            )
                        )
                    )
                }

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
                    is UserListEffect.Refresh -> {
                        launch {
                            val event = when (val response = loadPage(0)) {
                                is Either.Left -> UserListEvent.UserInitialListLoaded(
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
                    is UserListEffect.LoadPage -> {
                        launch {
                            val event = when (val response = loadPage(value.page)) {
                                is Either.Left -> UserListEvent.UserNextListLoaded(
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
    ): Either<List<UserEntity>, Throwable> {
        val response = repository.getUsersForPage(page)
        return when (response) {
            is Either.Left -> Either.Left(
                response.left.map {
                    UserEntity(
                        id = checkNotNull(it.id),
                        name = it.name ?: "",
                        email = it.email ?: "",
                        picked = false
                    )
                }
            )
            is Either.Right -> Either.Right(response.right)
        }
    }
}

sealed class UserListEvent {
    object RefreshRequest : UserListEvent()
    object LoadNextPage : UserListEvent()
    class UserInitialListLoaded(val users: List<UserEntity>) : UserListEvent()
    class UserNextListLoaded(val users: List<UserEntity>) : UserListEvent()
    class UserListLoadFailed(val error: Throwable) : UserListEvent()
    class UserWithPositionClick(val position: Int) : UserListEvent()
    class Picked(val userId: Int) : UserListEvent()
}

sealed class UserListEffect {
    object Refresh : UserListEffect()
    class LoadPage(val page: Int) : UserListEffect()
    class ShowError(val message: String) : UserListEffect()
    class OpenUserInfoById(val userId: Int) : UserListEffect()
}

@Parcelize
data class UserListModel(
    val users: List<UserEntity> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val currentPage: Int = 0
) : Parcelable

@Parcelize
data class UserEntity(
    val id: Int,
    val name: String,
    val email: String,
    val picked: Boolean
) : Parcelable