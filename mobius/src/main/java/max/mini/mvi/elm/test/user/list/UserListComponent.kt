package max.mini.mvi.elm.test.user.list

import com.spotify.mobius.*
import com.spotify.mobius.android.AndroidLogger
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import max.mini.mvi.elm.test.ContextProvider
import max.mini.mvi.elm.test.FragmentNavigatorProvider
import max.mini.mvi.elm.test.RepositoryProvider
import max.mini.mvi.elm.test.UserInfoResultEventSourceProvider
import max.mini.mvi.elm.test.base.FragmentControllerDelegate
import max.mini.mvi.elm.test.base.ResultEventSource
import max.mini.mvi.elm.test.user.detail.UserInfoResult
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        ContextProvider::class,
        RepositoryProvider::class,
        FragmentNavigatorProvider::class,
        UserInfoResultEventSourceProvider::class
    ],
    modules = [UserListModule::class]
)
interface UserListComponent {

    @Component.Factory
    interface Factory {
        fun create(
            contextProvider: ContextProvider,
            repositoryProvider: RepositoryProvider,
            fragmentNavigatorProvider: FragmentNavigatorProvider,
            userInfoResultEventSourceProvider: UserInfoResultEventSourceProvider
        ): UserListComponent
    }

    fun inject(fragment: UsersFragment)

}

@Module(includes = [UserListModule.Bindings::class])
class UserListModule {

    @Provides
    fun provideLoop(
        effectHandler: Connectable<UserListEffect, UserListEvent>,
        userInfoResultEventSource: ResultEventSource<UserInfoResult, UserListEvent>
    ): MobiusLoop.Builder<UserListDataModel, UserListEvent, UserListEffect> {
        val loop =
            Mobius.loop(
                Update<UserListDataModel, UserListEvent, UserListEffect> { model, event ->
                    UserListLogic.update(
                        model,
                        event
                    )
                },
                effectHandler
            ).eventSource(userInfoResultEventSource)
                .logger(AndroidLogger.tag("UserList"))

        return loop
    }

    @Provides
    fun provideDelegate(
        loop: MobiusLoop.Builder<UserListDataModel, UserListEvent, UserListEffect>
    ): FragmentControllerDelegate<UserListViewModel, UserListDataModel, UserListEvent, UserListEffect> {
        return FragmentControllerDelegate(
            loop = loop,
            initialState = Init {
                UserListLogic.init(it)
            },
            defaultStateProvider = {
                UserListDataModel()
            },
            modelMapper = {
                it.viewModel
            }
        )
    }

    @Module
    interface Bindings {

        @Binds
        fun bindEffectHandler(effectHandler: UserListEffectHandler): Connectable<UserListEffect, UserListEvent>

        @Binds
        fun bindCoordinator(coordinator: RealUserListCoordinator): UserListCoordinator

    }

}