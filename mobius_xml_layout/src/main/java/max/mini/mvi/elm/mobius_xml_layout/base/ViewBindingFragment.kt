package max.mini.mvi.elm.mobius_xml_layout.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class ViewBindingFragment<V : ViewBinding> : Fragment() {

    var viewBinding: V? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = createViewBinding(
        inflater,
        container
    ).also {
        viewBinding = it
    }.root

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): V

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

}