package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.extras.Connectables.contramap
import com.spotify.mobius.functions.Function

class FragmentControllerDelegate<VM, M : Parcelable, E, F>(
    private val loop: MobiusLoop.Builder<M, E, F>,
    private val initialState: Init<M, F>,
    private val defaultStateProvider: () -> M,
    private val modelMapper: (M) -> VM
) {

    private val keyModel = "KEY_MODEL"

    private var controller: MobiusLoop.Controller<M, E>? = null
    private var modelBeforeExit: M? = null

    private fun retrieveDefaultModel(
        savedInstanceState: Bundle?
    ): M = modelBeforeExit
        ?: savedInstanceState?.getParcelable(keyModel)
        ?: defaultStateProvider.invoke()

    fun getDefaultModel(
        savedInstanceState: Bundle?
    ) = modelMapper.invoke(
        retrieveDefaultModel(savedInstanceState)
    )

    fun onViewCreated(
        savedInstanceState: Bundle?,
        view: Connectable<VM, E>
    ) {
        controller = MobiusAndroid.controller(
            loop,
            retrieveDefaultModel(savedInstanceState),
            initialState
        ).also {
            it.connect(
                contramap(
                    Function { modelMapper.invoke(it) },
                    view
                )
            )
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(keyModel, controller?.model)
    }

    fun onAppear() {
        modelBeforeExit = null
        controller?.start()
    }

    fun onDisappear() {
        modelBeforeExit = controller?.model
        controller?.stop()
    }

    fun onDestroyView() {
        controller?.disconnect()
    }

}