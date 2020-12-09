package max.mini.mvi.elm.mobius_xml_layout.user

import com.github.terrakok.cicerone.Screen
import max.mini.mvi.elm.mobius_xml_layout.*
import max.mini.mvi.elm.mobius_xml_layout.base.FlowFragment

class UserFlowFragment : FlowFragment(),
    UserListDependenciesProvider,
    UserInfoDependenciesProvider {

    var dependencies: UserFlowDependencies? = null

    override fun getUserListDependencies(): UserListDependencies = dependencies!!

    override fun getUserInfoDependencies(): UserInfoDependencies = dependencies!!

    override val initialScreen: Screen
        get() = Screens.UserList()
}
