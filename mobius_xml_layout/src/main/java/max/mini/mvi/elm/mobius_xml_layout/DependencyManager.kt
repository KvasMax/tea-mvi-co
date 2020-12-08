package max.mini.mvi.elm.mobius_xml_layout

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.spotify.mobius.EventSource
import com.spotify.mobius.Mobius
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import max.mini.mvi.elm.api.RepositoryFactory
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.common_ui.createFragmentArgumentsPacker
import max.mini.mvi.elm.mobius_common.ResultEmitter
import max.mini.mvi.elm.mobius_common.ResultEventSource
import max.mini.mvi.elm.mobius_xml_layout.base.FlowRouter
import max.mini.mvi.elm.mobius_xml_layout.base.RealFragmentControllerDelegate
import max.mini.mvi.elm.mobius_xml_layout.user.UserFlowFragment
import max.mini.mvi.elm.mobius_xml_layout.user.detail.*
import max.mini.mvi.elm.mobius_xml_layout.user.list.*
import max.mini.mvi.elm.mobius_xml_layout.utils.getImplementation

class DependencyManager(
    private val app: Application
) : Application.ActivityLifecycleCallbacks {

    private val repository by lazy { RepositoryFactory.createRepository() }

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
                            is UserFlowFragment -> setup(fragment)
                            is UserListFragment -> {
                                setup(
                                    fragment = fragment,
                                    context = app,
                                    repository = repository,
                                    flowRouter = fragment.getImplementation()!!,
                                    dependencies = fragment.getImplementation(
                                        UserListDependenciesProvider::class.java
                                    )!!.getUserListDependencies()
                                )
                            }
                            is UserInfoFragment -> {
                                setup(
                                    fragment = fragment,
                                    userId = userInfoArgumentsPacker.getArgument(
                                        fragment
                                    ),
                                    context = app,
                                    repository = repository,
                                    flowRouter = fragment.getImplementation()!!,
                                    dependencies = fragment.getImplementation(
                                        UserInfoDependenciesProvider::class.java
                                    )!!.getUserInfoDependencies()
                                )
                            }
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

class UserFlowDependencies : UserListDependencies, UserInfoDependencies {

    private val resultEventSource = ResultEventSource<UserInfoResult, UserListEvent> {
        when (it) {
            is UserInfoResult.Picked -> UserListEvent.Picked(
                it.userId
            )
        }
    }
    override val userInfoResultEventSource: EventSource<UserListEvent>
        get() = resultEventSource
    override val userInfoResultEmitter: ResultEmitter<UserInfoResult>
        get() = resultEventSource

}

interface UserListDependencies {
    val userInfoResultEventSource: EventSource<UserListEvent>
}

interface UserInfoDependencies {
    val userInfoResultEmitter: ResultEmitter<UserInfoResult>
}

interface UserListDependenciesProvider {
    fun getUserListDependencies(): UserListDependencies
}

interface UserInfoDependenciesProvider {
    fun getUserInfoDependencies(): UserInfoDependencies
}

private fun setup(
    fragment: UserFlowFragment
) {
    fragment.dependencies = UserFlowDependencies()
}

private fun setup(
    fragment: UserListFragment,
    context: Context,
    repository: Repository,
    flowRouter: FlowRouter,
    dependencies: UserListDependencies
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
            userListEffectHandler(
                context,
                repository,
                object : UserListCoordinator {
                    override fun onPickUserWithId(userId: Int) {
                        flowRouter.navigateTo(Screens.UserInfo(userId))
                    }
                }
            )
        )
            .eventSource(dependencies.userInfoResultEventSource)
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

private fun setup(
    fragment: UserInfoFragment,
    userId: Int,
    context: Context,
    repository: Repository,
    flowRouter: FlowRouter,
    dependencies: UserInfoDependencies
) {
    fragment.controllerDelegate = RealFragmentControllerDelegate<
            UserInfoViewModel,
            UserInfoDataModel,
            UserInfoEvent,
            UserInfoEffect>(
        loop = Mobius.loop(
            Update<UserInfoDataModel, UserInfoEvent, UserInfoEffect> { model, event ->
                UserInfoLogic.update(
                    model,
                    event
                )
            },
            userInfoEffectHandler(
                repository,
                context,
                dependencies.userInfoResultEmitter,
                flowRouter
            )
        )
            .logger(AndroidLogger.tag("UserInfo")),
        initialState = {
            UserInfoLogic.init(it)
        },
        defaultStateProvider = {
            UserInfoDataModel(userId = userId)
        },
        modelMapper = {
            it.mapped
        }
    )
}

val userInfoArgumentsPacker get() = createFragmentArgumentsPacker<UserInfoFragment, Int>()