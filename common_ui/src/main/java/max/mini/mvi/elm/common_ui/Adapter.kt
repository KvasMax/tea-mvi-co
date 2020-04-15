package max.mini.mvi.elm.common_ui

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer

class DifferAdapterDelegate<P>(
    val adapterDelegate: AdapterDelegate<List<P>>,
    val itemComparator: TypedItemComparator<out P, P>
)

inline fun <reified C : P, P : Any> createContentStaticAdapterDelegate(
    @LayoutRes layoutId: Int,
    crossinline onInit: View.() -> Unit = {},
    crossinline onBindItem: View.(C) -> Unit = {},
    crossinline onViewAttachedToWindow: View.() -> Unit = {},
    crossinline onViewDetachedFromWindow: View.() -> Unit = {}
) = DifferAdapterDelegate(
    adapterDelegateLayoutContainer<C, P>(
        layoutId
    ) {

        onInit.invoke(itemView)

        bind {
            onBindItem.invoke(itemView, item)
        }
        onViewAttachedToWindow {
            onViewAttachedToWindow.invoke(itemView)
        }
        onViewDetachedFromWindow {
            onViewDetachedFromWindow.invoke(itemView)
        }
    }, createItemComparator<C, P>(
        areItemsTheSame = { _, _ -> true },
        areContentsTheSame = { _, _ -> true }
    ))

fun <P : Any> createDifferAdapter(
    vararg items: DifferAdapterDelegate<P>
): AsyncListDifferDelegationAdapter<P> {
    val delegates = items.map { it.adapterDelegate }.toTypedArray()
    val diffCallback = diffCallback(*items.map { it.itemComparator }.toTypedArray())
    return AsyncListDifferDelegationAdapter(
        diffCallback,
        *delegates
    )
}

fun <P : Any> diffCallback(
    vararg itemComparators: TypedItemComparator<out P, P>
): DiffUtil.ItemCallback<P> {

    return object : DiffUtil.ItemCallback<P>() {

        override fun areItemsTheSame(oldItem: P, newItem: P): Boolean {
            if (oldItem::class.java.isAssignableFrom(newItem::class.java)) {
                val comparator =
                    itemComparators.firstOrNull { it.klass.isInstance(newItem) }
                if (comparator != null) {
                    return comparator.areItemsTheSame(oldItem, newItem)
                }
            } else {
                return false
            }
            error("Can not find a comparator inside areItemsTheSame for ${newItem.javaClass.name} and ${oldItem.javaClass.name}")
        }

        override fun areContentsTheSame(oldItem: P, newItem: P): Boolean {
            if (oldItem::class.java.isAssignableFrom(newItem::class.java)) {
                val comparator =
                    itemComparators.firstOrNull { it.klass.isInstance(newItem) }
                if (comparator != null) {
                    return comparator.areContentsTheSame(oldItem, newItem)
                }
            } else {
                return false
            }
            error("Can not find a comparator inside areContentsTheSame for ${newItem.javaClass.name} and ${oldItem.javaClass.name}")
        }
    }
}

inline fun <reified C : P, P : Any> createItemComparator(
    crossinline areItemsTheSame: (oldItem: C, newItem: C) -> Boolean,
    crossinline areContentsTheSame: (oldItem: C, newItem: C) -> Boolean
) = object : TypedItemComparator<C, P> {

    override val klass: Class<C> = C::class.java

    override fun areItemsTheSame(oldItem: P, newItem: P): Boolean =
        areItemsTheSame.invoke(oldItem as C, newItem as C)

    override fun areContentsTheSame(oldItem: P, newItem: P): Boolean =
        areContentsTheSame.invoke(oldItem as C, newItem as C)

}

interface TypedItemComparator<C : P, P> : ItemComparator<P> {
    val klass: Class<C>
}

interface ItemComparator<in C> {
    fun areItemsTheSame(oldItem: C, newItem: C): Boolean
    fun areContentsTheSame(oldItem: C, newItem: C): Boolean
}