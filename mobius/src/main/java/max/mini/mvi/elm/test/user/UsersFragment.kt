package max.mini.mvi.elm.test.user

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.spotify.mobius.*
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.item_user.view.*
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.BaseFragment

class UsersFragment : BaseFragment(),
    Connectable<UserListModel, UserListEvent> {

    override val layoutResId: Int = R.layout.fragment_users

    private val keyModel = "KEY_MODEL"

    private var controller: MobiusLoop.Controller<UserListModel, UserListEvent>? = null

    private val adapter = ListDelegationAdapter<List<Any>>(
        userAdapterDelegate()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        val model = savedInstanceState?.let {
            it.getParcelable<UserListModel>(keyModel)
        } ?: UserListModel()

        controller = createController(model, requireContext()).also {
            it.connect(this)
        }
    }

    override fun onPause() {
        controller?.stop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        controller?.start()
    }

    override fun onDestroyView() {
        controller?.disconnect()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(keyModel, controller?.model)
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

fun createController(
    defaultModel: UserListModel,
    context: Context
): MobiusLoop.Controller<UserListModel, UserListEvent> {
    val loop = Mobius.loop(Update<UserListModel, UserListEvent, UserListEffect> { model, event ->
        UserListLogic.update(
            model,
            event
        )
    }, UserListEffectHandler(context))
        .init(UserListLogic::init)
        .logger(AndroidLogger.tag("UserList"))

    return MobiusAndroid.controller(
        loop,
        defaultModel
    )
}

fun userAdapterDelegate() = adapterDelegateLayoutContainer<UserEntity, Any>(R.layout.item_user) {
    bind {
        itemView.name.text = item.name
        itemView.email.text = item.email
    }
}