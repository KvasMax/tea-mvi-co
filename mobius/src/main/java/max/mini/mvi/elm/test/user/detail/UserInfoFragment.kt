package max.mini.mvi.elm.test.user.detail

import android.view.View
import kotlinx.android.synthetic.main.fragment_user_info.*
import max.mini.mvi.elm.test.base.ControllerFragment
import javax.inject.Inject

class UserInfoFragment @Inject constructor(
) : ControllerFragment<UserInfoModel, UserInfoEvent, UserInfoEffect>() {

    override fun renderViewModel(
        viewModel: UserInfoModel
    ) {
        swipeRefresh.isRefreshing = viewModel.refreshing
        progressBar.visibility = if (viewModel.loading) View.VISIBLE else View.GONE

        text.text = arrayOf(
            viewModel.name,
            viewModel.email,
            viewModel.phoneNumber,
            viewModel.website
        ).filterNotNull()
            .joinToString(separator = "\n")
    }

}