package max.mini.mvi.elm.test

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }

    fun getContext(): Context

}

@Module
interface AppModule {

    @Binds
    fun bindContext(app: Application): Context

}