package max.mini.mvi.elm.test

import android.app.Application
import android.content.Context
import dagger.*
import max.mini.mvi.elm.api.RepositoryFactory
import max.mini.mvi.elm.api.repo.Repository

interface ContextProvider {
    fun getContext(): Context
}

interface RepositoryProvider {
    fun getRepository(): Repository
}

@Component(modules = [AppModule::class])
interface AppComponent
    : ContextProvider,
    RepositoryProvider {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }

}

@Module(includes = [AppModule.Binding::class])
class AppModule {

    @Provides
    @Reusable
    fun provideRepository(): Repository = RepositoryFactory.createRepository()

    @Module
    interface Binding {

        @Binds
        fun bindContext(app: Application): Context

    }

}