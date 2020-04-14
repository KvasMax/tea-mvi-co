package max.mini.mvi.elm.test.base

import androidx.fragment.app.Fragment

interface FragmentNavigator {

    fun navigateTo(fragment: Fragment)

    fun back()

}