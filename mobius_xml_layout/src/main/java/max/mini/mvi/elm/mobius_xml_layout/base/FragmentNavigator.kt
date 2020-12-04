package max.mini.mvi.elm.mobius_xml_layout.base

import androidx.fragment.app.Fragment

interface FragmentNavigator {

    fun navigateTo(fragment: Fragment)

    fun back()

}