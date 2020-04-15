package max.mini.mvi.elm.test.user.list

import androidx.annotation.UiThread
import max.mini.mvi.elm.test.DependencyManager
import max.mini.mvi.elm.test.base.FragmentNavigator
import javax.inject.Inject

interface UserListCoordinator {

    @UiThread
    fun onPickUserWithId(
        userId: Int
    )

}

class RealUserListCoordinator @Inject constructor(
    private val navigator: FragmentNavigator
) : UserListCoordinator {

    override fun onPickUserWithId(userId: Int) {
        navigator.navigateTo(DependencyManager.getInstance().assembleUserInfoFragment(userId))
    }
}