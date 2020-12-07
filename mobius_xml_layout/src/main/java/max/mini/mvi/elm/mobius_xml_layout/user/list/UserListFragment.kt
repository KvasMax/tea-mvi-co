package max.mini.mvi.elm.mobius_xml_layout.user.list

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
    ControllerFragment<UserListViewModel, UserListEvent>() {

    private val adapter = createDifferAdapter(
        usersAdapterDelegate(),
        loadMoreAdapterDelegate<ListItem.LoadMore, ListItem>()
    )

    private var binding: FragmentUserListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentUserListBinding.inflate(
        inflater,
        container,
        false
    ).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
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
        binding?.let {
            it.progressBar.changeVisibility(viewModel.loading)
            it.swipeRefresh.isRefreshing = viewModel.refreshing
            adapter.items = viewModel.listItems
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}

private sealed class ListItem {
    data class User(
        val user: UserViewModel
    ) : ListItem()

    object LoadMore : ListItem()
}

private fun usersAdapterDelegate() =
    createDifferAdapterDelegate<ListItem.User, ListItem, ItemUserBinding>(
        viewBinding = { layoutInflater, root ->
            ItemUserBinding.inflate(
                layoutInflater,
                root,
                false
            )
        },
        viewHolderBinding = {
            bind {
                binding.name.text = item.user.name
                binding.email.text = item.user.email
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