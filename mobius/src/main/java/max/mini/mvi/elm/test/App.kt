package max.mini.mvi.elm.test

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.create(this)
    }

}