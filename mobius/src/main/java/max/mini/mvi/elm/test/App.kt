package max.mini.mvi.elm.test

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import max.mini.mvi.elm.test.user.detail.UserInfoFragment
import max.mini.mvi.elm.test.user.list.UsersFragment

class App : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        DependencyManager.create(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        when (activity) {
            is RootActivity -> DependencyManager.getInstance().inject(activity)
        }
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreAttached(
                        fragmentManager: FragmentManager,
                        fragment: Fragment,
                        context: Context
                    ) {
                        when (fragment) {
                            is UsersFragment -> DependencyManager.getInstance().inject(fragment)
                            is UserInfoFragment -> DependencyManager.getInstance().inject(fragment)
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