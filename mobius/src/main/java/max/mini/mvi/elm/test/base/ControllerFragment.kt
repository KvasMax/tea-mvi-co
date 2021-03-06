package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import dev.inkremental.Inkremental
import dev.inkremental.renderable
import javax.inject.Inject

abstract class ControllerFragment<VM, M : Parcelable, E, F>
    : BaseFragment(),
    Connectable<VM, E> {

    @Inject
    lateinit var controllerDelegate: FragmentControllerDelegate<VM, M, E, F>

    private var eventConsumer: Consumer<E>? = null
    private var lastModel: VM? = null

    override fun createView(
        savedInstanceState: Bundle?
    ): View {
        lastModel = controllerDelegate.getDefaultModel(
            savedInstanceState
        )
        return renderable(requireContext()) {
            lastModel?.let {
                renderViewModel(it)
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        controllerDelegate.onViewCreated(
            savedInstanceState,
            this
        )
    }

    override fun onResume() {
        super.onResume()
        controllerDelegate.onAppear()
    }

    override fun onPause() {
        super.onPause()
        controllerDelegate.onDisappear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controllerDelegate.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controllerDelegate.onSaveInstanceState(outState)
    }

    override fun connect(output: Consumer<E>): Connection<VM> {

        eventConsumer = output

        return object : Connection<VM> {
            override fun accept(value: VM) {
                lastModel = value
                Inkremental.render(view)
            }

            override fun dispose() {
                eventConsumer = null
            }
        }
    }

    fun sendEvent(
        event: E
    ) {
        eventConsumer?.accept(event)
    }

    abstract fun renderViewModel(viewModel: VM)
}