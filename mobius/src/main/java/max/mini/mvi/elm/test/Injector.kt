package max.mini.mvi.elm.test

import android.app.Application
import androidx.annotation.MainThread
import max.mini.mvi.elm.test.user.DaggerUserListComponent
import max.mini.mvi.elm.test.user.UserListComponent
import max.mini.mvi.elm.test.user.UsersFragment

class Injector private constructor(app: Application) {

    companion object {

        private var instance: Injector? = null

        @MainThread
        fun create(app: Application): Injector {
            return instance ?: app.let {
                Injector(it).also { instance = it }
            }
        }

        @MainThread
        fun getInstance(): Injector =
            instance ?: throw IllegalStateException("Method \"create\" was not called before")

    }

    private val appComponent: AppComponent = DaggerAppComponent.factory().create(app)

    private var userListComponent: UserListComponent? = null

    @MainThread
    fun assembleUsersFragment(): UsersFragment {
        val component = userListComponent ?: DaggerUserListComponent.factory().create(
            appComponent
        ).also {
            userListComponent = it
        }
        return component.getFragment()
    }

}