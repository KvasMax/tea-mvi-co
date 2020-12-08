package max.mini.mvi.elm.mobius_xml_layout.user.list

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import max.mini.mvi.elm.common_ui.addLoadMoreListener
import max.mini.mvi.elm.common_ui.changeVisibility
import max.mini.mvi.elm.common_ui.createDifferAdapter
import max.mini.mvi.elm.mobius_xml_layout.base.ControllerFragment
import max.mini.mvi.elm.mobius_xml_layout.databinding.FragmentUserListBinding
import max.mini.mvi.elm.mobius_xml_layout.databinding.ItemLoadMoreBinding
import max.mini.mvi.elm.mobius_xml_layout.databinding.ItemUserBinding
import max.mini.mvi.elm.mobius_xml_layout.utils.createDifferAdapterDelegate

class UserListFragment :
    ControllerFragment<FragmentUserListBinding, UserListViewModel, UserListEvent>() {

    private val adapter = createDifferAdapter(
        usersAdapterDelegate {
            sendEvent(UserListEvent.UserWithPositionClick(it))
        },
        loadMoreAdapterDelegate<ListItem.LoadMore, ListItem>()
    )

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentUserListBinding.inflate(
        inflater,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.let {
            it.list.adapter = adapter
            it.list.addLoadMoreListener {
                sendEvent(UserListEvent.LoadNextPage)
            }
            it.swipeRefresh.setOnRefreshListener {
                sendEvent(UserListEvent.RefreshRequest)
            }
        }
    }

    override fun renderViewModel(
        viewModel: UserListViewModel
    ) {
        viewBinding?.let {
            it.progressBar.changeVisibility(viewModel.loading)
            it.swipeRefresh.isRefreshing = viewModel.refreshing
            adapter.items = viewModel.listItems
        }
    }

}

private sealed class ListItem {
    data class User(
        val user: UserViewModel
    ) : ListItem()

    object LoadMore : ListItem()
}

private fun usersAdapterDelegate(
    clickListener: (Int) -> Unit
) =
    createDifferAdapterDelegate<ListItem.User, ListItem, ItemUserBinding>(
        viewBinding = { layoutInflater, root ->
            ItemUserBinding.inflate(
                layoutInflater,
                root,
                false
            )
        },
        viewHolderBinding = {

            binding.root.setOnClickListener {
                clickListener.invoke(adapterPosition)
            }

            bind {
                binding.name.text = item.user.name
                binding.email.text = item.user.email
                binding.container.setBackgroundColor(
                    if (item.user.picked) Color.GRAY else Color.TRANSPARENT
                )
            }
        },
        areItemsTheSame = { oldItem, newItem ->
            oldItem.user.id == newItem.user.id
        },
        areContentsTheSame = { oldItem, newItem ->
            oldItem == newItem
        }
    )

private inline fun <reified C : P, P : Any> loadMoreAdapterDelegate() =
    createDifferAdapterDelegate<C, P, ItemLoadMoreBinding>(
        viewBinding = { layoutInflater, root ->
            ItemLoadMoreBinding.inflate(
                layoutInflater,
                root,
                false
            )
        },
        viewHolderBinding = {},
        areItemsTheSame = { _, _ -> true },
        areContentsTheSame = { _, _ -> true }
    )

private val UserListViewModel.listItems: List<ListItem>
    get() = users.map {
        ListItem.User(it)
    }.let {
        if (loadingMore) it.plus(ListItem.LoadMore)
        else it
    }