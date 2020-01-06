package max.mini.mvi.elm.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import max.mini.mvi.elm.test.user.UsersFragment

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.contentContainer, UsersFragment())
                .commit()
        }

    }
}
