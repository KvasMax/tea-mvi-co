package max.mini.mvi.elm.test.base

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewScrollPositionRestorer {

    private companion object {
        const val KEY = "RECYCLER_VIEW_LAYOUT_MANAGER_STATE_"
    }

    private var oneShotSavedState: Parcelable? = null

    @MainThread
    fun onViewCreated(
        savedInstanceState: Bundle?,
        recyclerView: RecyclerView
    ) {
        if (savedInstanceState == null) {
            return
        }
        oneShotSavedState = savedInstanceState.getParcelable(recyclerView.storeKey)
    }

    @MainThread
    fun onPause(
        recyclerView: RecyclerView
    ) {
        oneShotSavedState = recyclerView.requireLayoutManager().onSaveInstanceState()
    }

    @MainThread
    fun onSaveInstanceState(
        outState: Bundle,
        recyclerView: RecyclerView
    ) {
        outState.putParcelable(
            recyclerView.storeKey,
            recyclerView.requireLayoutManager().onSaveInstanceState()
        )
    }

    @MainThread
    fun applySavedStateTo(
        recyclerView: RecyclerView
    ) {
        oneShotSavedState?.let {
            recyclerView.postDelayed({
                recyclerView.requireLayoutManager().onRestoreInstanceState(it)
            }, 0)
        }
        oneShotSavedState = null
    }

    private val RecyclerView.storeKey: String
        get() {
            val recyclerViewId = this.id
            if (recyclerViewId == View.NO_ID) {
                throw IllegalArgumentException("Was passed a recycler view without id")
            }
            return "$KEY$recyclerViewId}"
        }

    private fun RecyclerView.requireLayoutManager(): RecyclerView.LayoutManager {
        return this.layoutManager
            ?: throw IllegalArgumentException("Was passed a recycler view without a set layout manager")
    }

}