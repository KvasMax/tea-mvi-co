package max.mini.mvi.elm.mobius_xml_layout.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer

abstract class ControllerFragment<VB : ViewBinding, VM, E>
    : ViewBindingFragment<VB>(),
    Connectable<VM, E> {

    var controllerDelegate: FragmentControllerDelegate<VM, E>? = null

    private var eventConsumer: Consumer<E>? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        controllerDelegate?.let {
            renderViewModel(
                it.getDefaultModel(
                    savedInstanceState
                )
            )
            it.onViewCreated(
                savedInstanceState,
                this
            )
        }
    }

    override fun onResume() {
        super.onResume()
        controllerDelegate?.onAppear()
    }

    override fun onPause() {
        super.onPause()
        controllerDelegate?.onDisappear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controllerDelegate?.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controllerDelegate?.onSaveInstanceState(outState)
    }

    override fun connect(output: Consumer<E>): Connection<VM> {

        eventConsumer = output

        return object : Connection<VM> {
            override fun accept(value: VM) {
                renderViewModel(value)
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