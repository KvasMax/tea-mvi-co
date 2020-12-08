package max.mini.mvi.elm.common_ui

import android.os.Parcelable
import com.spotify.mobius.Next
import com.spotify.mobius.extras.patterns.InnerUpdate

fun <M, E, F, LI> listStateUpdater(
    listStateExtractor: M.() -> ParcelableListState<LI>,
    refreshEventMapper: (E) -> ListAction.Refresh<LI>?,
    pageLoadedEventMapper: (E) -> ListAction.PageLoaded<LI>?,
    pageLoadFailedEventMapper: (E) -> ListAction.PageLoadFailed<LI>?,
    loadMoreEventMapper: (E) -> ListAction.LoadMore<LI>?,
    modelUpdater: M.(ParcelableListState<LI>) -> M,
    loadPageEffectMapper: (ListSideEffect.LoadPage) -> F,
    emitErrorEffectMapper: (ListSideEffect.EmitError) -> F
) where LI : Parcelable = InnerUpdate.builder<
        M,
        E,
        F,
        ParcelableListState<LI>,
        ListAction<LI>,
        ListSideEffect
        >()
    .modelExtractor { listStateExtractor.invoke(it) }
    .eventExtractor {
        refreshEventMapper.invoke(it)
            ?: pageLoadedEventMapper.invoke(it)
            ?: pageLoadFailedEventMapper.invoke(it)
            ?: loadMoreEventMapper.invoke(it)
            ?: error("Not supported event passed: $it")
    }
    .innerUpdate { listState, event ->
        val (newListState, effects) = listState.reduce(event)
        if (listState == newListState) {
            Next.dispatch(effects)
        } else {
            Next.next(newListState, effects)
        }
    }
    .modelUpdater { model, listState -> modelUpdater.invoke(model, listState) }
    .innerEffectHandler { model, modelUpdated, effects ->
        val mappedEffects = effects.map {
            when (it) {
                is ListSideEffect.LoadPage -> loadPageEffectMapper.invoke(it)
                is ListSideEffect.EmitError -> emitErrorEffectMapper.invoke(it)
            }
        }.toSet()
        if (modelUpdated) {
            Next.next(
                model,
                mappedEffects
            )
        } else {
            Next.dispatch(mappedEffects)
        }

    }
    .build()