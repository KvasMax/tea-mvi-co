package max.mini.mvi.elm.test.user.detail

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
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.mobius_common.ResultEmitter
import max.mini.mvi.elm.utils.Either
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

object UserInfoLogic {

    fun init(
        model: UserInfoDataModel
    ): First<UserInfoDataModel, UserInfoEffect> {
        return when (model.userInfo) {
            null -> First.first(
                model.copy(loading = true),
                setOf(
                    UserInfoEffect.Refresh(model.userId)
                )
            )
            else -> First.first(model)
        }
    }

    fun update(
        model: UserInfoDataModel,
        event: UserInfoEvent
    ): Next<UserInfoDataModel, UserInfoEffect> {
        return when (event) {
            is UserInfoEvent.RefreshRequest -> Next.next(
                model.copy(refreshing = true),
                setOf(UserInfoEffect.Refresh(model.userId))
            )
            is UserInfoEvent.UserInfoLoaded -> {
                Next.next(
                    model.copy(
                        userInfo = event.userInfo.model,
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
                            userId = model.userId
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
    private val resultEmitter: ResultEmitter<UserInfoResult>,
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

sealed class UserInfoResult {
    class Picked(val userId: Int) : UserInfoResult()
}

sealed class UserInfoEvent {
    // ui
    object RefreshRequest : UserInfoEvent()
    object Pick : UserInfoEvent()

    // model
    class UserInfoLoaded(val userInfo: UserInfoDto) : UserInfoEvent()
    class UserInfoLoadFailed(val error: Throwable) : UserInfoEvent()
}

sealed class UserInfoEffect {
    class Refresh(val userId: Int) : UserInfoEffect()
    class ShowError(val message: String) : UserInfoEffect()
    class Pick(val userId: Int) : UserInfoEffect()
}

@Parcelize
data class UserInfoDataModel(
    val userId: Int,
    val userInfo: UserInfo? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
) : Parcelable

@Parcelize
data class UserInfo(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null
) : Parcelable

data class UserInfoViewModel(
    val name: String? = null,
    val email: String? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)

val UserInfoDataModel.mapped: UserInfoViewModel
    get() = UserInfoViewModel(
        name = userInfo?.name,
        email = userInfo?.email,
        loading = loading,
        refreshing = refreshing
    )

val UserInfoDto.model: UserInfo
    get() = UserInfo(
        id = id,
        name = arrayOf(firstName, lastName)
            .filterNotNull()
            .joinToString(separator = " "),
        email = email
    )