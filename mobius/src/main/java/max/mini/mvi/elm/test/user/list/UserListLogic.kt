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
            is UserListEvent.RefreshRequest -> Next.next(
                model.copy(
                    refreshing = true
                ),
                setOf(UserListEffect.Refresh)
            )
            is UserListEvent.UserListLoaded -> Next.next(
                model.copy(
                    loading = false,
                    refreshing = false,
                    users = event.users
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
                            val response = repository.getUsersForPage(0)
                            when (response) {
                                is Either.Left -> output.accept(
                                    UserListEvent.UserListLoaded(
                                        response.left.map {
                                            UserEntity(
                                                id = checkNotNull(it.id),
                                                name = it.name ?: "",
                                                email = it.email ?: "",
                                                picked = false
                                            )
                                        })
                                )
                                is Either.Right -> output.accept(
                                    UserListEvent.UserListLoadFailed(
                                        response.right
                                    )
                                )
                            }
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
}

sealed class UserListEvent {
    object RefreshRequest : UserListEvent()
    class UserListLoaded(val users: List<UserEntity>) : UserListEvent()
    class UserListLoadFailed(val error: Throwable) : UserListEvent()
    class UserWithPositionClick(val position: Int) : UserListEvent()
    class Picked(val userId: Int) : UserListEvent()
}

sealed class UserListEffect {
    object Refresh : UserListEffect()
    class ShowError(val message: String) : UserListEffect()
    class OpenUserInfoById(val userId: Int) : UserListEffect()
}

@Parcelize
data class UserListModel(
    val users: List<UserEntity> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false
) : Parcelable

@Parcelize
data class UserEntity(
    val id: Int,
    val name: String,
    val email: String,
    val picked: Boolean
) : Parcelable