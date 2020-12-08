package max.mini.mvi.elm.mobius_xml_layout.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.androidx.AppNavigator
import max.mini.mvi.elm.mobius_xml_layout.databinding.FragmentFlowBinding

abstract class FlowFragment :
    ViewBindingFragment<FragmentFlowBinding>(),
    FlowRouter,
    OnBackPressedListener {

    private val cicerone = Cicerone.create()
    private val router get() = cicerone.router
    private val navigatorHolder get() = cicerone.getNavigatorHolder()
    private var navigator: AppNavigator? = null

    abstract val initialScreen: Screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            router.replaceScreen(initialScreen)
        }
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFlowBinding.inflate(
        inflater,
        container,
        false
    ).also {
        navigator = AppNavigator(
            activity = requireActivity(),
            containerId = it.container.id,
            fragmentManager = childFragmentManager
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navigator = null
    }

    override fun onResume() {
        super.onResume()
        navigator?.let {
            navigatorHolder.setNavigator(it)
        }
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun navigateTo(screen: Screen, clearContainer: Boolean) {
        router.navigateTo(screen, clearContainer)
    }

    override fun newRootScreen(screen: Screen) {
        router.newRootScreen(screen)
    }

    override fun replaceScreen(screen: Screen) {
        router.replaceScreen(screen)
    }

    override fun newChain(vararg screens: Screen, showOnlyTopScreenView: Boolean) {
        router.newChain(*screens, showOnlyTopScreenView = showOnlyTopScreenView)
    }

    override fun newRootChain(vararg screens: Screen, showOnlyTopScreenView: Boolean) {
        router.newRootChain(*screens, showOnlyTopScreenView = showOnlyTopScreenView)
    }

    override fun finishChain() {
        router.finishChain()
    }

    override fun exit() {
        router.exit()
    }

    override fun onBackPressed() {
        val fragment = childFragmentManager.fragments.lastOrNull()
        (fragment as? OnBackPressedListener)?.onBackPressed() ?: router.exit()
    }
}