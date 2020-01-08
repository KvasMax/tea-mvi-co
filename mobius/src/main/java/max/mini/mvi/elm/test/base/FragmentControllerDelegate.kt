package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusAndroid

abstract class FragmentControllerDelegate<M : Parcelable, E, F>(
    private val loop: MobiusLoop.Builder<M, E, F>
) {

    private val keyModel = "KEY_MODEL"

    private var controller: MobiusLoop.Controller<M, E>? = null

    abstract fun createDefaultModel(): M

    fun onViewCreated(
        savedInstanceState: Bundle?,
        view: Connectable<M, E>
    ) {
        val model = savedInstanceState?.let {
            it.getParcelable<M>(keyModel)
        } ?: createDefaultModel()
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

    fun onResume() {
        controller?.start()
    }

    fun onPause() {
        controller?.stop()
    }

    fun onDestroyView() {
        controller?.disconnect()
    }

}