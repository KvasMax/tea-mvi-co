package max.mini.mvi.elm.common_ui

import androidx.recyclerview.widget.RecyclerView

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