package max.mini.mvi.elm.test

import dagger.*
import max.mini.mvi.elm.mobius_common.ResultEmitter
import max.mini.mvi.elm.mobius_common.ResultEventSource
import max.mini.mvi.elm.test.base.FragmentNavigator
import max.mini.mvi.elm.test.user.detail.UserInfoResult
import max.mini.mvi.elm.test.user.list.UserListEvent
import javax.inject.Singleton

interface FragmentNavigatorProvider {
    fun getFragmentNavigator(): FragmentNavigator
}

interface UserInfoResultEventSourceProvider {
    fun getUserInfoResultEventSource(): ResultEventSource<UserInfoResult, UserListEvent>
}

interface UserInfoResultEmitterProvider {
    fun getUserInfoResultEmitter(): ResultEmitter<UserInfoResult>
}

@Singleton
@Component(
    dependencies = [
        ContextProvider::class,
        RepositoryProvider::class
    ],
    modules = [RootModule::class]
)
interface RootComponent
    : ContextProvider,
    RepositoryProvider,
    FragmentNavigatorProvider,
    UserInfoResultEventSourceProvider,
    UserInfoResultEmitterProvider {

    @Component.Factory
    interface Factory {
        fun create(
            contextProvider: ContextProvider,
            repositoryProvider: RepositoryProvider,
            @BindsInstance rootActivity: RootActivity
        ): RootComponent
    }

}

@Module(includes = [RootModule.Binding::class])
class RootModule {

    @Singleton
    @Provides
    fun provideUserInfoResultEventSource(): ResultEventSource<UserInfoResult, UserListEvent> {
        return ResultEventSource {
            when (it) {
                is UserInfoResult.Picked -> UserListEvent.Picked(it.userId)
            }
        }
    }

    @Module
    interface Binding {

        @Binds
        fun bindNavigator(rootActivity: RootActivity): FragmentNavigator

        @Binds
        fun bindUserInfoResultEmitter(eventSource: ResultEventSource<UserInfoResult, UserListEvent>): ResultEmitter<UserInfoResult>
    }

}