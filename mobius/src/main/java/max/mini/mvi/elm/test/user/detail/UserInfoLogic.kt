package max.mini.mvi.elm.test.user.detail

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import com.spotify.mobius.*
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.test.user.list.UserListEvent
import max.mini.mvi.elm.utils.Either
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

object UserInfoLogic {

    fun init(
        model: UserInfoModel
    ): First<UserInfoModel, UserInfoEffect> {
        return when (model.name) {
            null -> First.first(
                model.copy(loading = true),
                setOf(
                    UserInfoEffect.Refresh(model.id)
                )
            )
            else -> First.first(model)
        }
    }

    fun update(
        model: UserInfoModel,
        event: UserInfoEvent
    ): Next<UserInfoModel, UserInfoEffect> {
        return when (event) {
            is UserInfoEvent.RefreshRequest -> Next.next(
                model.copy(refreshing = true),
                setOf(UserInfoEffect.Refresh(model.id))
            )
            is UserInfoEvent.UserInfoLoaded -> {
                Next.next(
                    model.copy(
                        name = event.userInfo.name,
                        email = event.userInfo.email,
                        phoneNumber = event.userInfo.phone,
                        website = event.userInfo.website,
                        loading = false,
                        refreshing = false
                    )
                )
            }
            is UserInfoEvent.UserInfoLoadFailed -> {
                Next.next(
                    model.copy(
                        loading = false,
                        refreshing = false
                    ),
                    setOf(
                        UserInfoEffect.ShowError(
                            event.error.message ?: event.error.localizedMessage
                        )
                    )
                )
            }
            is UserInfoEvent.Pick -> {
                Next.next(
                    model,
                    setOf(
                        UserInfoEffect.Pick(
                            userId = model.id
                        )
                    )
                )
            }
        }
    }

}

class UserInfoEffectHandler @Inject constructor(
    private val repository: Repository,
    private val context: Context,
    private val resultEmitter: UserInfoResultEmitter,
    private val coordinator: UserInfoCoordinator
) : Connectable<UserInfoEffect, UserInfoEvent>,
    CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun connect(output: Consumer<UserInfoEvent>): Connection<UserInfoEffect> {

        return object : Connection<UserInfoEffect> {

            override fun accept(value: UserInfoEffect) {
                when (value) {
                    is UserInfoEffect.Refresh -> {
                        launch {
                            val response = repository.getUserInfo(value.userId)
                            when (response) {
                                is Either.Left -> output.accept(
                                    UserInfoEvent.UserInfoLoaded(
                                        response.left
                                    )
                                )
                                is Either.Right -> output.accept(
                                    UserInfoEvent.UserInfoLoadFailed(
                                        response.right
                                    )
                                )
                            }
                        }
                    }
                    is UserInfoEffect.ShowError -> {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, value.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    is UserInfoEffect.Pick -> {
                        resultEmitter.emit(UserInfoResult.Picked(value.userId))
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

@Singleton
class UserInfoResultEmitter @Inject constructor(
) : EventSource<UserListEvent>, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + Dispatchers.Default

    private val channel = Channel<UserInfoResult>()

    override fun subscribe(
        eventConsumer: Consumer<UserListEvent>
    ): Disposable {
        launch {
            for (event in channel) {
                eventConsumer.accept(
                    when (event) {
                        is UserInfoResult.Picked -> UserListEvent.Picked(event.userId)
                    }
                )
            }
        }
        return Disposable {
            job.cancelChildren()
        }
    }

    fun emit(
        result: UserInfoResult
    ) {
        launch {
            channel.send(result)
        }
    }

}

sealed class UserInfoResult {
    class Picked(val userId: Int) : UserInfoResult()
}

sealed class UserInfoEvent {
    object RefreshRequest : UserInfoEvent()
    object Pick : UserInfoEvent()
    class UserInfoLoaded(val userInfo: UserInfoDto) : UserInfoEvent()
    class UserInfoLoadFailed(val error: Throwable) : UserInfoEvent()
}

sealed class UserInfoEffect {
    class Refresh(val userId: Int) : UserInfoEffect()
    class ShowError(val message: String) : UserInfoEffect()
    class Pick(val userId: Int) : UserInfoEffect()
}

@Parcelize
data class UserInfoModel(
    val id: Int,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val website: String? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
) : Parcelable