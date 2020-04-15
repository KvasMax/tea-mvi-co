package max.mini.mvi.elm.test.base

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext


interface ResultEmitter<R> {
    fun emit(
        result: R
    )
}

class ResultEventSource<R, E>(
    private val mapper: (R) -> E
) : EventSource<E>,
    ResultEmitter<R>,
    CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + Dispatchers.Default

    private val channel = Channel<R>()

    override fun subscribe(
        eventConsumer: Consumer<E>
    ): Disposable {
        launch {
            for (event in channel) {
                eventConsumer.accept(
                    mapper.invoke(event)
                )
            }
        }
        return Disposable {
            job.cancelChildren()
        }
    }

    override fun emit(
        result: R
    ) {
        launch {
            channel.send(result)
        }
    }

}