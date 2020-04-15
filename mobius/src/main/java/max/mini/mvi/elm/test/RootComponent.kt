package max.mini.mvi.elm.test

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import max.mini.mvi.elm.test.base.FragmentNavigator
import max.mini.mvi.elm.test.user.detail.UserInfoResultEmitter
import javax.inject.Singleton

interface FragmentNavigatorProvider {
    fun getFragmentNavigator(): FragmentNavigator
}

interface UserInfoResultEmitterProvider {
    fun getUserInfoResultEmitter(): UserInfoResultEmitter
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

@Module
interface RootModule {

    @Binds
    fun bindNavigator(rootActivity: RootActivity): FragmentNavigator
}