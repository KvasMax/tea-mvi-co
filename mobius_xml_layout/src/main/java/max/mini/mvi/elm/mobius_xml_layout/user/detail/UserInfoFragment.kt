package max.mini.mvi.elm.mobius_xml_layout.user.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import max.mini.mvi.elm.common_ui.changeVisibility
import max.mini.mvi.elm.mobius_xml_layout.base.ControllerFragment
import max.mini.mvi.elm.mobius_xml_layout.databinding.FragmentUserInfoBinding

class UserInfoFragment :
    ControllerFragment<FragmentUserInfoBinding, UserInfoViewModel, UserInfoEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentUserInfoBinding.inflate(
        inflater,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.let {
            it.swipeRefresh.setOnRefreshListener {
                sendEvent(UserInfoEvent.RefreshRequest)
            }
            it.info.setOnClickListener {
                sendEvent(UserInfoEvent.Pick)
            }
            it.toolbar.setNavigationOnClickListener {
                sendEvent(UserInfoEvent.Exit)
            }
        }
    }

    override fun renderViewModel(
        viewModel: UserInfoViewModel
    ) {
        viewBinding?.let {
            it.progressBar.changeVisibility(viewModel.loading)
            it.swipeRefresh.isRefreshing = viewModel.refreshing
            it.info.text = arrayOf(
                viewModel.name,
                viewModel.email,
                viewModel.phoneNumber,
                viewModel.website
            ).filterNotNull()
                .joinToString(separator = "\n")
        }
    }

}