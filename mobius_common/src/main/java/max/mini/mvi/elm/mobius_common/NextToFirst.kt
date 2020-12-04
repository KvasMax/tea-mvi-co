package max.mini.mvi.elm.mobius_common

import com.spotify.mobius.First
import com.spotify.mobius.Next

val <M, F> Next<M, F>.toFirst: First<M, F>
    get() = First.first(this.modelUnsafe(), this.effects())