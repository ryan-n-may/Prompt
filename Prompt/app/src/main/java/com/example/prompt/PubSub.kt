@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.prompt

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    suspend fun publish(message: String) {
        _events.emit(message)
    }
}

class EventSubscriber(
    private val name: String,
    private val scope: CoroutineScope,
) {
    private val subscriptions = mutableMapOf<String, Job>()

    fun <R> subscribe(
        id: String,
        callback: (String) -> R,
    ) {
        // Cancel previous subscription for the same ID if it exists
        subscriptions[id]?.cancel()

        val job =
            scope.launch {
                EventBus.events.collect { message ->
                    println("[$name][$id] received: $message")
                    callback(message)
                }
            }

        subscriptions[id] = job
    }

    fun unsubscribe(id: String) {
        subscriptions[id]?.cancel()
        subscriptions.remove(id)
        println("[$name][$id] unsubscribed")
    }

    fun unsubscribeAll() {
        subscriptions.values.forEach { it.cancel() }
        subscriptions.clear()
        println("[$name] unsubscribed from all")
    }
}
