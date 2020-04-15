package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import javax.inject.Inject

abstract class ControllerFragment<M : Parcelable, E, F>
    : BaseFragment(),
    Connectable<M, E> {

    @Inject
    lateinit var controllerDelegate: FragmentControllerDelegate<M, E, F>

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        controllerDelegate.onViewCreated(
            savedInstanceState,
            this
        ) {
            renderViewModel(it)
        }
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

    override fun connect(output: Consumer<E>): Connection<M> {

        setupListeners(output)

        return object : Connection<M> {
            override fun accept(value: M) {
                renderViewModel(value)
            }

            override fun dispose() {
                resetListeners()
            }
        }
    }

    abstract fun initViews()

    abstract fun setupListeners(output: Consumer<E>)

    abstract fun resetListeners()

    abstract fun renderViewModel(viewModel: M)
}