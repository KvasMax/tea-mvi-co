package max.mini.mvi.elm.mobius_xml_layout.base

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CoroutineScopeEffectHandler<F, E>(
    private val handler: suspend (value: F, Consumer<E>) -> Unit
) : Connectable<F, E>, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun connect(
        output: Consumer<E>
    ): Connection<F> {
        return object : Connection<F> {

            override fun accept(value: F) {
                launch {
                    handler.invoke(value, output)
                }
            }

            override fun dispose() {
                job.cancelChildren()
            }
        }
    }
}