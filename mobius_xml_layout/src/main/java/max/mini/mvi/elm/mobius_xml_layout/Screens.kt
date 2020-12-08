package max.mini.mvi.elm.mobius_xml_layout

import com.github.terrakok.cicerone.androidx.FragmentScreen
import max.mini.mvi.elm.mobius_xml_layout.user.detail.UserInfoFragment
import max.mini.mvi.elm.mobius_xml_layout.user.list.UserListFragment

object Screens {
    fun UserList() = FragmentScreen { UserListFragment() }
    fun UserInfo(userId: Int) = FragmentScreen {
        UserInfoFragment().also {
            userInfoArgumentsPacker.setArgument(it, userId)
        }
    }
}