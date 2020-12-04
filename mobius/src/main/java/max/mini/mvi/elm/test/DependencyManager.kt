package max.mini.mvi.elm.test

import android.app.Application
import androidx.annotation.MainThread
import max.mini.mvi.elm.common_ui.createFragmentArgumentsPacker
import max.mini.mvi.elm.test.user.detail.DaggerUserInfoComponent
import max.mini.mvi.elm.test.user.detail.UserInfoFragment
import max.mini.mvi.elm.test.user.list.DaggerUserListComponent
import max.mini.mvi.elm.test.user.list.UsersFragment

class DependencyManager private constructor(app: Application) {

    companion object {

        private var instance: DependencyManager? = null

        @MainThread
        fun create(app: Application): DependencyManager {
            return instance ?: app.let {
                DependencyManager(it).also { instance = it }
            }
        }

        @MainThread
        fun getInstance(): DependencyManager =
            instance ?: throw IllegalStateException("Method \"create\" was not called before")

    }

    private val appComponent: AppComponent = DaggerAppComponent.factory().create(app)
    private var rootComponent: RootComponent? = null

    private val userInfoFragmentArgumentsPacker by lazy {
        createFragmentArgumentsPacker<UserInfoFragment, Int>()
    }

    @MainThread
    fun inject(activity: RootActivity) {
        rootComponent = DaggerRootComponent.factory().create(
            appComponent,
            appComponent,
            activity
        )
    }

    @MainThread
    fun assembleUsersFragment(): UsersFragment {
        return UsersFragment()
    }

    @MainThread
    fun inject(fragment: UsersFragment) {
        val rootComponent = this.rootComponent ?: error("RootComponent is not created")
        val component = DaggerUserListComponent.factory().create(
            rootComponent,
            rootComponent,
            rootComponent,
            rootComponent
        )
        component.inject(fragment)
    }

    @MainThread
    fun assembleUserInfoFragment(
        userId: Int
    ): UserInfoFragment {
        val fragment = UserInfoFragment()
        userInfoFragmentArgumentsPacker.setArgument(fragment, userId)
        return fragment
    }

    @MainThread
    fun inject(fragment: UserInfoFragment) {
        val rootComponent = this.rootComponent ?: error("RootComponent is not created")
        val userId = userInfoFragmentArgumentsPacker.getArgument(fragment)
        val component = DaggerUserInfoComponent.factory().create(
            rootComponent,
            rootComponent,
            rootComponent,
            rootComponent,
            userId
        )
        component.inject(fragment)
    }

}
