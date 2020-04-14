package max.mini.mvi.elm.test.user.list

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.item_user.view.*
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment
import max.mini.mvi.elm.test.base.ListenerMapper
import max.mini.mvi.elm.test.base.RecyclerViewScrollPositionRestorer

class UsersFragment : ControllerFragment<UserListModel, UserListEvent, UserListEffect>() {

    override val layoutResId: Int = R.layout.fragment_users

    private val userWithPositionClickListener =
        ListenerMapper<Int, UserListEvent> {
            UserListEvent.UserWithPositionClick(
                it
            )
        }

    private val adapter = ListDelegationAdapter<List<Any>>(
        userAdapterDelegate(userWithPositionClickListener.listener)
    )

    private val userListScrollRestorer = RecyclerViewScrollPositionRestorer()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListScrollRestorer.onViewCreated(savedInstanceState, list)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        userListScrollRestorer.onSaveInstanceState(outState, list)
    }

    override fun onPause() {
        super.onPause()
        userListScrollRestorer.onPause(list)
    }

    override fun initViews() {
        list.setHasFixedSize(true)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun setupListeners(output: Consumer<UserListEvent>) {
        swipeRefresh.setOnRefreshListener {
            output.accept(UserListEvent.RefreshRequest)
        }
        userWithPositionClickListener.setOutput(output)
    }

    override fun resetListeners() {
        swipeRefresh.setOnRefreshListener(null)
        userWithPositionClickListener.setOutput(null)
    }

    override fun renderViewModel(viewModel: UserListModel) {
        progressBar.visibility = if (viewModel.loading) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = viewModel.refreshing

        adapter.items = viewModel.users
        adapter.notifyDataSetChanged()
        userListScrollRestorer.applySavedStateTo(list)

    }

}

fun userAdapterDelegate(
    onUserClick: ((Int) -> Unit)
) = adapterDelegateLayoutContainer<UserEntity, Any>(R.layout.item_user) {

    itemView.setOnClickListener {
        onUserClick.invoke(adapterPosition)
    }

    bind {
        itemView.name.text = item.name
        itemView.email.text = item.email
    }
}
