package max.mini.mvi.elm.test.user

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.item_user.view.*
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

class UsersFragment(
    controllerDelegate: FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect>
) : ControllerFragment<UserListModel, UserListEvent, UserListEffect>(controllerDelegate) {

    override val layoutResId: Int = R.layout.fragment_users

    private val adapter = ListDelegationAdapter<List<Any>>(
        userAdapterDelegate()
    )

    override fun initViews() {
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
    }

    override fun setupListeners(output: Consumer<UserListEvent>) {
        swipeRefresh.setOnRefreshListener {
            output.accept(UserListEvent.RefreshRequest)
        }
    }

    override fun resetListeners() {
        swipeRefresh.setOnRefreshListener(null)
    }

    override fun renderViewModel(viewModel: UserListModel) {
        adapter.items = viewModel.users
        adapter.notifyDataSetChanged()

        progressBar.visibility = if (viewModel.loading) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = viewModel.refreshing
    }

}

fun userAdapterDelegate() = adapterDelegateLayoutContainer<UserEntity, Any>(R.layout.item_user) {
    bind {
        itemView.name.text = item.name
        itemView.email.text = item.email
    }
}