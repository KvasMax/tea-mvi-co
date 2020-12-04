package max.mini.mvi.elm.mobius_xml_layout

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.spotify.mobius.Mobius
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import max.mini.mvi.elm.api.RepositoryFactory
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.mobius_xml_layout.base.RealFragmentControllerDelegate
import max.mini.mvi.elm.mobius_xml_layout.user.list.*

class DependencyManager(
    private val app: Application
) : Application.ActivityLifecycleCallbacks {

    private val repository by lazy { RepositoryFactory.createRepository() }

    fun setup(
        fragment: UserListFragment
    ) {
        fragment.controllerDelegate = RealFragmentControllerDelegate<
                UserListViewModel,
                UserListDataModel,
                UserListEvent,
                UserListEffect
                >(
            loop = Mobius.loop(
                Update<UserListDataModel, UserListEvent, UserListEffect> { model, event ->
                    UserListLogic.update(
                        model,
                        event
                    )
                },
                UserListEffectHandler(
                    app,
                    repository,
                    object : UserListCoordinator {
                        override fun onPickUserWithId(userId: Int) {
                            //nothing
                        }
                    }
                )
            )
                .logger(AndroidLogger.tag("UserList")),
            initialState = {
                UserListLogic.init(it)
            },
            defaultStateProvider = {
                UserListDataModel.initial
            },
            modelMapper = {
                it.viewModel
            }

        )
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
//        when (activity) {
//            is RootActivity -> //nothing for now
//        }
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreAttached(
                        fragmentManager: FragmentManager,
                        fragment: Fragment,
                        context: Context
                    ) {
                        when (fragment) {
                            is UserListFragment -> setup(fragment)
                        }
                    }
                },
                true
            )
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    override fun onActivityStopped(activity: Activity) {}

}

interface ContextProvider {
    fun getContext(): Context
}

interface RepositoryProvider {
    fun getRepository(): Repository
}