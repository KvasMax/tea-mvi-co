package max.mini.mvi.elm.mobius_xml_layout.user.detail

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import com.spotify.mobius.First
import com.spotify.mobius.Next
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.mobius_common.ResultEmitter
import max.mini.mvi.elm.mobius_xml_layout.base.CoroutineScopeEffectHandler
import max.mini.mvi.elm.mobius_xml_layout.base.FlowRouter
import max.mini.mvi.elm.utils.Either

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
                Next.dispatch(
                    setOf(
                        UserInfoEffect.Pick(
                            userId = model.userId
                        )
                    )
                )
            }
            is UserInfoEvent.Exit -> {
                Next.dispatch(
                    setOf(UserInfoEffect.Exit)
                )
            }
        }
    }

}

fun userInfoEffectHandler(
    repository: Repository,
    context: Context,
    resultEmitter: ResultEmitter<UserInfoResult>,
    flowRouter: FlowRouter
) = CoroutineScopeEffectHandler<UserInfoEffect, UserInfoEvent> { value, output ->
    when (value) {
        is UserInfoEffect.Refresh -> {
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
        is UserInfoEffect.ShowError -> {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, value.message, Toast.LENGTH_LONG).show()
            }
        }
        is UserInfoEffect.Pick -> {
            resultEmitter.emit(UserInfoResult.Picked(value.userId))
            withContext(Dispatchers.Main) {
                flowRouter.exit()
            }
        }
        is UserInfoEffect.Exit -> {
            withContext(Dispatchers.Main) {
                flowRouter.exit()
            }
        }
    }
}

sealed class UserInfoEvent {
    // ui
    object RefreshRequest : UserInfoEvent()
    object Pick : UserInfoEvent()
    object Exit : UserInfoEvent()

    // model
    class UserInfoLoaded(val userInfo: UserInfoDto) : UserInfoEvent()
    class UserInfoLoadFailed(val error: Throwable) : UserInfoEvent()
}

sealed class UserInfoEffect {
    class Refresh(val userId: Int) : UserInfoEffect()
    class ShowError(val message: String) : UserInfoEffect()
    class Pick(val userId: Int) : UserInfoEffect()
    object Exit : UserInfoEffect()
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

sealed class UserInfoResult {
    class Picked(val userId: Int) : UserInfoResult()
}