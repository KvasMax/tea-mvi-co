package max.mini.mvi.elm.test.base

import com.spotify.mobius.functions.Consumer

class ListenerMapper<P, E>(
    private val mapper: (P) -> E
) {

    private var output: Consumer<E>? = null

    val listener: (P) -> Unit = {
        val event = mapper.invoke(it)
        output?.accept(event)
    }

    fun setOutput(output: Consumer<E>?) {
        this.output = output
    }

}