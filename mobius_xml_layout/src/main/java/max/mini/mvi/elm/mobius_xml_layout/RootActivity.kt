package max.mini.mvi.elm.mobius_xml_layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import max.mini.mvi.elm.mobius_xml_layout.base.OnBackPressedListener
import max.mini.mvi.elm.mobius_xml_layout.databinding.ActivityRootBinding
import max.mini.mvi.elm.mobius_xml_layout.user.UserFlowFragment

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    binding.root.id,
                    UserFlowFragment()
                )
                .commit()
        }

    }

    override fun onBackPressed() {
        val backPressedListener = supportFragmentManager.fragments.lastOrNull {
            it is OnBackPressedListener
        } as? OnBackPressedListener

        if (backPressedListener != null) {
            backPressedListener.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

}