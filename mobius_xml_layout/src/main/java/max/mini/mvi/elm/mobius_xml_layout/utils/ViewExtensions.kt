package max.mini.mvi.elm.mobius_xml_layout.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import max.mini.mvi.elm.common_ui.DifferAdapterDelegate
import max.mini.mvi.elm.common_ui.createItemComparator

inline fun <reified C : P, P : Any, V : ViewBinding> createDifferAdapterDelegate(
    noinline viewBinding: (LayoutInflater, ViewGroup) -> V,
    noinline viewHolderBinding: AdapterDelegateViewBindingViewHolder<C, V>.() -> Unit,
    noinline areContentsTheSame: (C, C) -> Boolean,
    noinline areItemsTheSame: (C, C) -> Boolean
) = DifferAdapterDelegate(
    adapterDelegate = adapterDelegateViewBinding<C, P, V>(
        viewBinding = viewBinding,
        block = viewHolderBinding
    ),
    itemComparator = createItemComparator(
        areContentsTheSame = areContentsTheSame,
        areItemsTheSame = areItemsTheSame
    )
)


inline fun <reified L : Any> Fragment.getImplementation(): L? {
    return getImplementation(L::class.java)
}

fun <L : Any> Fragment.getImplementation(klass: Class<L>): L? {
    val activity = this.activity
    val parentFragment = this.parentFragment
    val targetFragment = this.targetFragment

    return when {
        klass.isInstance(parentFragment) -> parentFragment as L
        klass.isInstance(targetFragment) -> targetFragment as L
        klass.isInstance(activity) && parentFragment == null -> activity as L
        else -> parentFragment?.getImplementation(klass)
    }
}