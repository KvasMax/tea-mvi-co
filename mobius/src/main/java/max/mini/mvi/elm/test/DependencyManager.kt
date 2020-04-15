package max.mini.mvi.elm.test

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import max.mini.mvi.elm.test.user.detail.DaggerUserInfoComponent
import max.mini.mvi.elm.test.user.detail.UserInfoFragment
import max.mini.mvi.elm.test.user.list.DaggerUserListComponent
import max.mini.mvi.elm.test.user.list.UsersFragment
import java.io.Serializable

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

inline fun <reified F, reified ARG> createFragmentArgumentsPacker(
): FragmentArgumentsPacker<F, ARG> where F : Fragment, ARG : Any {
    return object : FragmentArgumentsPacker<F, ARG> {

        private val key = "${F::class.java.name}.argument"

        override fun setArgument(
            fragment: F,
            argument: ARG
        ) {
            val bundle = fragment.arguments ?: Bundle()
            when (argument) {
                is Int -> bundle.putInt(key, argument)
                is Long -> bundle.putLong(key, argument)
                is String -> bundle.putString(key, argument)
                is Parcelable -> bundle.putParcelable(key, argument)
                is Serializable -> bundle.putSerializable(key, argument)
                else -> throw IllegalArgumentException("Type ${argument::class.java.name} is not supported")
            }
            fragment.arguments = bundle
        }

        override fun getArgument(fragment: F): ARG {
            return fragment.arguments!!.let { bundle ->
                when (ARG::class) {
                    Int::class -> bundle.getInt(key) as ARG
                    Long::class -> bundle.getLong(key) as ARG
                    String::class -> bundle.getString(key) as ARG
                    Parcelable::class -> bundle.getParcelable<Parcelable>(key) as ARG
                    Serializable::class -> bundle.getSerializable(key) as ARG
                    else -> throw IllegalArgumentException("Type ${ARG::class.java.name} is not supported")
                }
            }
        }
    }
}

interface FragmentArgumentsPacker<F : Fragment, ARG> {

    fun setArgument(
        fragment: F,
        argument: ARG
    )

    fun getArgument(
        fragment: F
    ): ARG

}