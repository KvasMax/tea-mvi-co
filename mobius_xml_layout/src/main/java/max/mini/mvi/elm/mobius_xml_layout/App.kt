package max.mini.mvi.elm.mobius_xml_layout

import android.app.Application

class App : Application() {

    private var dependencyManager: DependencyManager? = null

    override fun onCreate() {
        super.onCreate()
        dependencyManager = DependencyManager(this).also {
            registerActivityLifecycleCallbacks(it)
        }
    }

}