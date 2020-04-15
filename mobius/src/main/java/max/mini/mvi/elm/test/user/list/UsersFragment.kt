package max.mini.mvi.elm.test.user.list

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.item_user.view.*
import max.mini.mvi.elm.common_ui.DifferAdapterDelegate
import max.mini.mvi.elm.common_ui.addLoadMoreListener
import max.mini.mvi.elm.common_ui.createDifferAdapter
import max.mini.mvi.elm.common_ui.createItemComparator
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment
import max.mini.mvi.elm.test.base.ListenerMapper

class UsersFragment : ControllerFragment<UserListModel, UserListEvent, UserListEffect>() {

    override val layoutResId: Int = R.layout.fragment_users

    private val userWithPositionClickListener =
        ListenerMapper<Int, UserListEvent> {
            UserListEvent.UserWithPositionClick(
                it
            )
        }

    private val adapter = createDifferAdapter(
        userAdapterDelegate(userWithPositionClickListener.listener)
    )

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
        list.addLoadMoreListener {
            output.accept(UserListEvent.LoadNextPage)
        }
    }

    override fun resetListeners() {
        swipeRefresh.setOnRefreshListener(null)
        userWithPositionClickListener.setOutput(null)
        list.clearOnScrollListeners()
    }

    override fun renderViewModel(viewModel: UserListModel) {
        progressBar.visibility = if (viewModel.loading) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = viewModel.refreshing

        adapter.items = viewModel.users
    }

}

fun userAdapterDelegate(
    onUserClick: ((Int) -> Unit)
) = DifferAdapterDelegate(
    adapterDelegateLayoutContainer<UserEntity, Any>(R.layout.item_user) {

        itemView.setOnClickListener {
            onUserClick.invoke(adapterPosition)
        }

        bind {
            itemView.name.text = item.name
            itemView.email.text = item.email
            itemView.setBackgroundColor(if (item.picked) Color.LTGRAY else Color.TRANSPARENT)
        }
    }, createItemComparator<UserEntity, Any>(
        areContentsTheSame = { oldItem, newItem ->
            oldItem.id == newItem.id
        }, areItemsTheSame = { oldItem, newItem ->
            oldItem == newItem
        }
    )
)
