package max.mini.mvi.elm.mobius_xml_layout

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import max.mini.mvi.elm.mobius_xml_layout.base.OnBackPressedListener
import max.mini.mvi.elm.mobius_xml_layout.databinding.ActivityRootBinding
import max.mini.mvi.elm.mobius_xml_layout.user.UserFlowFragment

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    private var firstFragment: UserFlowFragment? = null
    private var secondFragment: UserFlowFragment? = null

    private var currentTab: Tab = Tab.FIRST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager
                .beginTransaction()

            firstFragment = UserFlowFragment().also {
                transaction
                    .add(binding.container.id, it, "FIRST")
                    .detach(it)
            }

            secondFragment = UserFlowFragment().also {
                transaction
                    .add(binding.container.id, it, "SECOND")
                    .detach(it)
            }

            transaction.commit()
        } else {
            firstFragment = supportFragmentManager.findFragmentByTag("FIRST") as UserFlowFragment
            secondFragment = supportFragmentManager.findFragmentByTag("SECOND") as UserFlowFragment
            currentTab = savedInstanceState.getSerializable("Tab") as Tab
        }

        binding.first.setOnClickListener {
            currentTab = Tab.FIRST
            refreshTabs()
        }
        binding.second.setOnClickListener {
            currentTab = Tab.SECOND
            refreshTabs()
        }

        refreshTabs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("Tab", currentTab)
    }

    private fun refreshTabs() {

        val fragmentToShow = when (currentTab) {
            Tab.FIRST -> firstFragment
            Tab.SECOND -> secondFragment
        } ?: return

        binding.first.setBackgroundColor(if (currentTab == Tab.FIRST) Color.GRAY else Color.TRANSPARENT)
        binding.second.setBackgroundColor(if (currentTab == Tab.SECOND) Color.GRAY else Color.TRANSPARENT)

        supportFragmentManager
            .beginTransaction()
            .attach(fragmentToShow)
            .apply {
                supportFragmentManager.fragments
                    .forEach {
                        detach(it)
                    }
            }
            .commit()
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

private enum class Tab {
    FIRST, SECOND
}