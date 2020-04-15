package max.mini.mvi.elm.test.user.detail

import androidx.annotation.UiThread
import max.mini.mvi.elm.test.base.FragmentNavigator
import javax.inject.Inject

interface UserInfoCoordinator {

    @UiThread
    fun onPickUserWithId(
        userId: Int
    )

}

class RealUserInfoCoordinator @Inject constructor(
    private val navigator: FragmentNavigator
) : UserInfoCoordinator {

    override fun onPickUserWithId(userId: Int) {
        navigator.back()
    }
}