package max.mini.mvi.elm.test.user

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.item_user.view.*
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.BaseFragment
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

class UsersFragment(
    private val controllerDelegate: FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect>
) : BaseFragment(),
    Connectable<UserListModel, UserListEvent> {

    override val layoutResId: Int = R.layout.fragment_users

    private val adapter = ListDelegationAdapter<List<Any>>(
        userAdapterDelegate()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        controllerDelegate.onViewCreated(savedInstanceState, this)
    }

    override fun onPause() {
        controllerDelegate.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        controllerDelegate.onResume()
    }

    override fun onDestroyView() {
        controllerDelegate.onDestroyView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controllerDelegate.onSaveInstanceState(outState)
    }

    override fun connect(
        output: Consumer<UserListEvent>
    ): Connection<UserListModel> {

        swipeRefresh.setOnRefreshListener {
            output.accept(UserListEvent.RefreshRequest)
        }

        return object : Connection<UserListModel> {
            override fun accept(value: UserListModel) {

                adapter.items = value.users
                adapter.notifyDataSetChanged()

                progressBar.visibility = if (value.loading) View.VISIBLE else View.GONE
                swipeRefresh.isRefreshing = value.refreshing
            }

            override fun dispose() {
                swipeRefresh.setOnRefreshListener(null)
            }
        }
    }

}

fun userAdapterDelegate() = adapterDelegateLayoutContainer<UserEntity, Any>(R.layout.item_user) {
    bind {
        itemView.name.text = item.name
        itemView.email.text = item.email
    }
}