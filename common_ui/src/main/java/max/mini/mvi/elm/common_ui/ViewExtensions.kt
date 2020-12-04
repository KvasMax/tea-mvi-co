package max.mini.mvi.elm.common_ui

import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.changeVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.changeVisibilityLeavingView(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE

fun Resources.getColorWithId(@ColorRes id: Int) = ResourcesCompat.getColor(
    this,
    id,
    null
)

fun RecyclerView.addLoadMoreListener(
    reversed: Boolean = false,
    threshold: Int = 5,
    onLoadMore: () -> Unit
) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                return
            }
            val adapter = recyclerView.adapter ?: return
            if (adapter.itemCount == 0) {
                return
            }
            if (reversed) {
                val firstVisiblePosition = if (recyclerView.childCount != 0) {
                    recyclerView.getChildViewHolder(recyclerView.getChildAt(0)).adapterPosition
                } else RecyclerView.NO_POSITION
                if (firstVisiblePosition == RecyclerView.NO_POSITION) {
                    return
                }
                if (firstVisiblePosition <= threshold
                    || adapter.itemCount < threshold
                ) {
                    onLoadMore()
                }
            } else {
                val lastVisiblePosition = if (recyclerView.childCount != 0) {
                    recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.childCount - 1))
                } else RecyclerView.NO_POSITION
                if (lastVisiblePosition == RecyclerView.NO_POSITION) {
                    return
                }
                if (lastVisiblePosition >= adapter.itemCount - threshold
                    || adapter.itemCount < threshold
                ) {
                    onLoadMore()
                }
            }
        }
    })

}


inline fun <reified F, reified ARG> createFragmentArgumentsPacker(
): FragmentArgumentsPacker<F, ARG> where F : Fragment, ARG : Any {
    return object : FragmentArgumentsPacker<F, ARG> {

        private val key = "${F::class.java.name}.argument"

        override fun setArgument(
            fragment: F,
            argument: ARG
        ) {
            val bundle = fragment.arguments ?: Bundle()
            when (argument) {
                is Int -> bundle.putInt(key, argument)
                is Long -> bundle.putLong(key, argument)
                is String -> bundle.putString(key, argument)
                is Parcelable -> bundle.putParcelable(key, argument)
                is Serializable -> bundle.putSerializable(key, argument)
                else -> throw IllegalArgumentException("Type ${argument::class.java.name} is not supported")
            }
            fragment.arguments = bundle
        }

        override fun getArgument(fragment: F): ARG {
            return fragment.arguments!!.let { bundle ->
                when (ARG::class) {
                    Int::class -> bundle.getInt(key) as ARG
                    Long::class -> bundle.getLong(key) as ARG
                    String::class -> bundle.getString(key) as ARG
                    Parcelable::class -> bundle.getParcelable<Parcelable>(key) as ARG
                    Serializable::class -> bundle.getSerializable(key) as ARG
                    else -> throw IllegalArgumentException("Type ${ARG::class.java.name} is not supported")
                }
            }
        }
    }
}

interface FragmentArgumentsPacker<F : Fragment, ARG> {

    fun setArgument(
        fragment: F,
        argument: ARG
    )

    fun getArgument(
        fragment: F
    ): ARG

}