package max.mini.mvi.elm.mobius_xml_layout.base

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.extras.Connectables.contramap

interface FragmentControllerDelegate<VM, E> {
    fun getDefaultModel(
        savedInstanceState: Bundle?
    ): VM

    fun onViewCreated(
        savedInstanceState: Bundle?,
        view: Connectable<VM, E>
    )

    fun onSaveInstanceState(outState: Bundle)
    fun onAppear()
    fun onDisappear()
    fun onDestroyView()
}

class RealFragmentControllerDelegate<VM, M : Parcelable, E, F>(
    private val loop: MobiusLoop.Builder<M, E, F>,
    private val initialState: Init<M, F>,
    private val defaultStateProvider: () -> M,
    private val modelMapper: (M) -> VM
) : FragmentControllerDelegate<VM, E> {

    private val keyModel = "KEY_MODEL"

    private var controller: MobiusLoop.Controller<M, E>? = null
    private var modelBeforeExit: M? = null

    private fun retrieveDefaultModel(
        savedInstanceState: Bundle?
    ): M = modelBeforeExit
        ?: savedInstanceState?.getParcelable(keyModel)
        ?: defaultStateProvider.invoke()

    override fun getDefaultModel(
        savedInstanceState: Bundle?
    ): VM = modelMapper.invoke(
        retrieveDefaultModel(savedInstanceState)
    )

    override fun onViewCreated(
        savedInstanceState: Bundle?,
        view: Connectable<VM, E>
    ) {
        val controller = this.controller ?: MobiusAndroid.controller(
            loop,
            retrieveDefaultModel(savedInstanceState),
            initialState
        ).also {
            this.controller = it
        }

        controller.also {
            it.connect(
                contramap(
                    { modelMapper.invoke(it) },
                    view
                )
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(keyModel, controller?.model)
    }

    override fun onAppear() {
        modelBeforeExit = null
        controller?.start()
    }

    override fun onDisappear() {
        modelBeforeExit = controller?.model
        controller?.stop()
    }

    override fun onDestroyView() {
        controller?.disconnect()
    }

}