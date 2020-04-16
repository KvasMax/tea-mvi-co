package max.mini.mvi.elm.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.inkremental.renderableContentView
import max.mini.mvi.elm.test.base.FragmentNavigator

class RootActivity : AppCompatActivity(), FragmentNavigator {

    private val rootViewId = 666

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        renderableContentView {
        }.id = rootViewId

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(
                rootViewId,
                DependencyManager.getInstance().assembleUsersFragment()
            ).commit()
        }

    }

    override fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(rootViewId, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun back() {
        onBackPressed()
    }

}
