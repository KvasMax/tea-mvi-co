package max.mini.mvi.elm.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import max.mini.mvi.elm.test.user.UsersFragment

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return when (loadFragmentClass(classLoader, className)) {
                    UsersFragment::class.java -> Injector.getInstance().assembleUsersFragment()
                    else -> super.instantiate(classLoader, className)
                }
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.contentContainer,
                    supportFragmentManager.fragmentFactory.instantiate(
                        classLoader,
                        UsersFragment::class.java.name
                    )
                )
                .commit()
        }

    }
}
