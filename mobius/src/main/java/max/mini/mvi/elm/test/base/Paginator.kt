package max.mini.mvi.elm.test.base

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ListState<T> {

    class NotInitialized<T> : ListState<T>()
    class Empty<T> : ListState<T>()
    class EmptyProgress<T> : ListState<T>()
    class EmptyError<T> : ListState<T>()

    data class Data<T>(
        val pageCount: Int,
        val items: List<T>
    ) : ListState<T>()

    data class Refreshing<T>(
        val pageCount: Int,
        val items: List<T>
    ) : ListState<T>()

    data class NextPageLoading<T>(
        val pageCount: Int,
        val items: List<T>
    ) : ListState<T>()

    data class AllData<T>(
        val pageCount: Int,
        val items: List<T>
    ) : ListState<T>()
}

sealed class ListAction<T> {

    // ui
    class Refresh<T> : ListAction<T>()
    class Restart<T> : ListAction<T>()
    class LoadMore<T> : ListAction<T>()

    // model
    data class PageLoaded<T>(
        val items: List<T>
    ) : ListAction<T>()

    class EmptyPageLoaded<T> : ListAction<T>()

    data class PageLoadFailed<T>(
        val error: Throwable
    ) : ListAction<T>()

}

sealed class ListSideEffect {
    data class LoadPage(
        val page: Int
    ) : ListSideEffect()

    data class EmitError(
        val error: Throwable
    ) : ListSideEffect()
}

fun <T> ListState<T>.reduce(
    action: ListAction<T>
): Pair<ListState<T>, Set<ListSideEffect>> = when (this) {
    is ListState.NotInitialized -> {
        when (action) {
            is ListAction.Refresh -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            else -> this to emptySet()
        }
    }
    is ListState.Empty -> {
        when (action) {
            is ListAction.Refresh -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            else -> this to emptySet()
        }
    }
    is ListState.EmptyProgress -> {
        when (action) {
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.PageLoaded -> {
                ListState.Data(
                    pageCount = 1,
                    items = action.items
                ) to emptySet()
            }
            is ListAction.EmptyPageLoaded -> {
                ListState.Empty<T>() to emptySet()
            }
            is ListAction.PageLoadFailed -> {
                ListState.EmptyError<T>() to setOf(ListSideEffect.EmitError(action.error))
            }
            else -> this to emptySet()
        }
    }
    is ListState.EmptyError -> {
        when (action) {
            is ListAction.Refresh -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            else -> this to emptySet()
        }
    }
    is ListState.Data -> {
        when (action) {
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.Refresh -> {
                ListState.Refreshing(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.LoadMore -> {
                ListState.NextPageLoading(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.LoadPage(this.pageCount))
            }
            else -> this to emptySet()
        }
    }
    is ListState.AllData -> {
        when (action) {
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.Refresh -> {
                ListState.Refreshing(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.LoadPage(0))
            }
            else -> this to emptySet()
        }
    }
    is ListState.Refreshing -> {
        when (action) {
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.EmptyPageLoaded -> {
                ListState.Empty<T>() to emptySet()
            }
            is ListAction.PageLoaded -> {
                ListState.Data(
                    pageCount = 1,
                    items = action.items
                ) to emptySet()
            }
            is ListAction.PageLoadFailed -> {
                ListState.Data(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.EmitError(action.error))
            }
            else -> this to emptySet()
        }
    }
    is ListState.NextPageLoading -> {
        when (action) {
            is ListAction.Restart -> {
                ListState.EmptyProgress<T>() to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.Refresh -> {
                ListState.Refreshing(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.LoadPage(0))
            }
            is ListAction.PageLoaded -> {
                ListState.Data(
                    pageCount = this.pageCount + 1,
                    items = this.items.plus(action.items)
                ) to emptySet()
            }
            is ListAction.EmptyPageLoaded -> {
                ListState.AllData(
                    pageCount = this.pageCount,
                    items = this.items
                ) to emptySet()
            }
            is ListAction.PageLoadFailed -> {
                ListState.Data(
                    pageCount = this.pageCount,
                    items = this.items
                ) to setOf(ListSideEffect.EmitError(action.error))
            }
            else -> this to emptySet()
        }
    }

}

fun <T> ListState<T>.loadedItems(): List<T> = when (this) {
    is ListState.NotInitialized -> emptyList()
    is ListState.Empty -> emptyList()
    is ListState.EmptyProgress -> emptyList()
    is ListState.EmptyError -> emptyList()
    is ListState.Data -> this.items
    is ListState.AllData -> this.items
    is ListState.NextPageLoading -> this.items
    is ListState.Refreshing -> this.items
}

fun <T> ListState<T>.isInitialized(): Boolean = this !is ListState.NotInitialized

sealed class ParcelableListState<T : Parcelable> : Parcelable {
    @Parcelize
    class NotInitialized<T : Parcelable> : ParcelableListState<T>()

    @Parcelize
    class Empty<T : Parcelable> : ParcelableListState<T>()

    @Parcelize
    class EmptyProgress<T : Parcelable> : ParcelableListState<T>()

    @Parcelize
    class EmptyError<T : Parcelable> : ParcelableListState<T>()

    @Parcelize
    data class Data<T : Parcelable>(
        val pageCount: Int,
        val items: List<T>
    ) : ParcelableListState<T>()

    @Parcelize
    data class Refreshing<T : Parcelable>(
        val pageCount: Int,
        val items: List<T>
    ) : ParcelableListState<T>()

    @Parcelize
    data class NextPageLoading<T : Parcelable>(
        val pageCount: Int,
        val items: List<T>
    ) : ParcelableListState<T>()

    @Parcelize
    data class AllData<T : Parcelable>(
        val pageCount: Int,
        val items: List<T>
    ) : ParcelableListState<T>()
}

fun <T> ParcelableListState<T>.loadedItems(): List<T> where T : Parcelable =
    this.plainState.loadedItems()

fun <T> ParcelableListState<T>.reduce(
    action: ListAction<T>
): Pair<ParcelableListState<T>, Set<ListSideEffect>> where T : Parcelable =
    this.plainState.reduce(action).let {
        val (state, effects) = it
        state.parcelableState to effects
    }

fun <T> ParcelableListState<T>.isInitialized(): Boolean where T : Parcelable =
    this.plainState.isInitialized()

val <T> ListState<T>.parcelableState: ParcelableListState<T> where T : Parcelable
    get() = when (this) {
        is ListState.NotInitialized -> ParcelableListState.NotInitialized()
        is ListState.Empty -> ParcelableListState.Empty()
        is ListState.EmptyProgress -> ParcelableListState.EmptyProgress()
        is ListState.EmptyError -> ParcelableListState.EmptyProgress()
        is ListState.Data -> ParcelableListState.Data(
            pageCount = this.pageCount,
            items = this.items
        )
        is ListState.AllData -> ParcelableListState.AllData(
            pageCount = this.pageCount,
            items = this.items
        )
        is ListState.NextPageLoading -> ParcelableListState.NextPageLoading(
            pageCount = this.pageCount,
            items = this.items
        )
        is ListState.Refreshing -> ParcelableListState.Refreshing(
            pageCount = this.pageCount,
            items = this.items
        )
    }

val <T> ParcelableListState<T>.plainState: ListState<T> where T : Parcelable
    get() = when (this) {
        is ParcelableListState.NotInitialized -> ListState.NotInitialized()
        is ParcelableListState.Empty -> ListState.Empty()
        is ParcelableListState.EmptyProgress -> ListState.EmptyProgress()
        is ParcelableListState.EmptyError -> ListState.EmptyProgress()
        is ParcelableListState.Data -> ListState.Data(
            pageCount = this.pageCount,
            items = this.items
        )
        is ParcelableListState.AllData -> ListState.AllData(
            pageCount = this.pageCount,
            items = this.items
        )
        is ParcelableListState.NextPageLoading -> ListState.NextPageLoading(
            pageCount = this.pageCount,
            items = this.items
        )
        is ParcelableListState.Refreshing -> ListState.Refreshing(
            pageCount = this.pageCount,
            items = this.items
        )
    }