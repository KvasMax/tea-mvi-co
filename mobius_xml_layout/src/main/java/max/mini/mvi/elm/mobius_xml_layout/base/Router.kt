package max.mini.mvi.elm.mobius_xml_layout.base

import com.github.terrakok.cicerone.Screen

interface Router {
    fun navigateTo(screen: Screen, clearContainer: Boolean = true)
    fun newRootScreen(screen: Screen)
    fun replaceScreen(screen: Screen)
    fun newChain(vararg screens: Screen, showOnlyTopScreenView: Boolean = true)
    fun newRootChain(vararg screens: Screen, showOnlyTopScreenView: Boolean = true)
    fun finishChain()
    fun exit()
}

interface FlowRouter : Router
interface RootRouter : Router

