package max.mini.mvi.elm.test.user.detail

import android.view.View
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_user_info.*
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment
import javax.inject.Inject

class UserInfoFragment @Inject constructor(
) : ControllerFragment<UserInfoModel, UserInfoEvent, UserInfoEffect>() {

    override val layoutResId: Int = R.layout.fragment_user_info

    override fun initViews() {
    }

    override fun setupListeners(
        output: Consumer<UserInfoEvent>
    ) {
        swipeRefresh.setOnRefreshListener {
            output.accept(UserInfoEvent.RefreshRequest)
        }
    }

    override fun resetListeners() {
        swipeRefresh.setOnRefreshListener(null)
    }

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
        ).joinToString(separator = "\n")
    }

}