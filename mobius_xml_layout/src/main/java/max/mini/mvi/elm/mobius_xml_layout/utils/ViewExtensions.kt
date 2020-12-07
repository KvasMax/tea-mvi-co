package max.mini.mvi.elm.mobius_xml_layout.utils

import android.view.LayoutInflater
import android.view.ViewGroup
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
