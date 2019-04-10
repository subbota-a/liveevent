package com.gmail.alsubbota.liveevent

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

abstract class Consumer<T> : Observer<Event<T>>{
    override fun onChanged(t: Event<T>?) {
        t?.getContentIfNotHandled()?.let{ onArrived(it) }
    }
    abstract fun onArrived(event: T)
}

open class LiveEvent<T>{
    val mData = MutableLiveData<Event<T>>()
    fun sendEvent(value : T){
        mData.setValue(Event(value))
    }
    fun postEvent(value: T){
        mData.postValue(Event(value))
    }

    fun observe(owner: LifecycleOwner, consumer: Consumer<in T>) {
        mData.observe(owner, consumer)
    }

    fun observeForever(consumer: Consumer<in T>) {
        mData.observeForever(consumer)
    }

    fun removeObserver(consumer: Consumer<in T>) {
        mData.removeObserver(consumer)
    }
}