package moe.caa.fabric.hadesgame.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import net.minecraft.util.thread.ReentrantBlockableEventLoop
import kotlin.coroutines.CoroutineContext

class ThreadExecutorDispatcher<T : Runnable>(private val threadExecutor: ReentrantBlockableEventLoop<T>) :
    MainCoroutineDispatcher() {
    private object ImmediateBukkitDispatcher : MainCoroutineDispatcher() {
        override val immediate: MainCoroutineDispatcher get() = this

        override fun isDispatchNeeded(context: CoroutineContext): Boolean = false
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Dispatchers.Unconfined.dispatch(context, block)
        }

        override fun toString(): String {
            return "ThreadExecutorDispatcher.immediate"
        }
    }

    override val immediate: MainCoroutineDispatcher = ImmediateBukkitDispatcher

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        threadExecutor.execute(block)
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !threadExecutor.isSameThread

    override fun toString(): String {
        return "ThreadExecutorDispatcher"
    }
}