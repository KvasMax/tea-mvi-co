package max.mini.mvi.elm.test

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import max.mini.mvi.elm.api.repo.Repository
import max.mini.mvi.elm.test.base.FragmentNavigator

interface RootDependencies {
    fun getNavigator(): FragmentNavigator
    fun getContext(): Context
    fun getRepository(): Repository
}

@Component(
    dependencies = [AppDependencies::class],
    modules = [RootModule::class]
)
interface RootComponent : RootDependencies {

    @Component.Factory
    interface Factory {
        fun create(
            appDependencies: AppDependencies,
            @BindsInstance rootActivity: RootActivity
        ): RootComponent
    }

}

@Module
interface RootModule {

    @Binds
    fun bindNavigator(rootActivity: RootActivity): FragmentNavigator
}