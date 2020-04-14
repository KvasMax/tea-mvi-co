package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusAndroid

class FragmentControllerDelegate<M : Parcelable, E, F>(
    private val loop: MobiusLoop.Builder<M, E, F>,
    private val defaultStateProvider: () -> M
) {

    private val keyModel = "KEY_MODEL"

    private var controller: MobiusLoop.Controller<M, E>? = null
    private var modelBeforeExit: M? = null

    fun onViewCreated(
        savedInstanceState: Bundle?,
        view: Connectable<M, E>,
        initialStateRender: (M) -> Unit
    ) {
        val model: M = modelBeforeExit
            ?: savedInstanceState?.getParcelable(keyModel)
            ?: defaultStateProvider.invoke()

        initialStateRender.invoke(model)

        controller = MobiusAndroid.controller(
            loop,
            model
        ).also {
            it.connect(view)
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