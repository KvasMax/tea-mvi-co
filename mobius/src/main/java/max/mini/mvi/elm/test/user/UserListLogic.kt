package max.mini.mvi.elm.test.user

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
import max.mini.mvi.elm.api.RepositoryFactory
import max.mini.mvi.elm.utils.Either
import kotlin.coroutines.CoroutineContext

object UserListLogic {

    fun init(model: UserListModel): First<UserListModel, UserListEffect> {
        return First.first(model.copy(loading = true), setOf(UserListEffect.Refresh))
    }

    fun update(
        model: UserListModel,
        event: UserListEvent
    ): Next<UserListModel, UserListEffect> {
        return when (event) {
            is UserListEvent.RefreshRequest -> Next.next(
                model.copy(loading = true),
                setOf(UserListEffect.Refresh)
            )
            is UserListEvent.UserListLoaded -> Next.next(
                model.copy(loading = false, users = event.users)
            )
            is UserListEvent.UserListLoadFailed -> Next.next(
                model.copy(loading = false, users = emptyList()),
                setOf(
                    UserListEffect.ShowError(
                        event.error.message ?: event.error.localizedMessage
                    )
                )
            )
        }
    }

}

class UserListEffectHandler(
    private val context: Context
) : Connectable<UserListEffect, UserListEvent> {

    override fun connect(
        output: Consumer<UserListEvent>
    ): Connection<UserListEffect> {

        return object : Connection<UserListEffect>, CoroutineScope {

            private val repository = RepositoryFactory.createRepository()

            private val job = SupervisorJob()

            override val coroutineContext: CoroutineContext = job + Dispatchers.IO

            override fun accept(value: UserListEffect) {
                when (value) {
                    is UserListEffect.Refresh -> {
                        launch {
                            val response = repository.getUsers()
                            when (response) {
                                is Either.Left -> output.accept(
                                    UserListEvent.UserListLoaded(
                                        response.left.map {
                                            UserEntity(
                                                it.name ?: "",
                                                it.email ?: ""
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
                        Toast.makeText(context, value.message, Toast.LENGTH_LONG).show()
                    }
                }

            }

            override fun dispose() {
                cancel()
            }
        }
    }
}

sealed class UserListEvent {
    object RefreshRequest : UserListEvent()
    class UserListLoaded(val users: List<UserEntity>) : UserListEvent()
    class UserListLoadFailed(val error: Throwable) : UserListEvent()
}

sealed class UserListEffect {
    object Refresh : UserListEffect()
    class ShowError(val message: String) : UserListEffect()
}

@Parcelize
data class UserListModel(
    val users: List<UserEntity> = emptyList(),
    val loading: Boolean = false
) : Parcelable

@Parcelize
data class UserEntity(
    val name: String,
    val email: String
) : Parcelable