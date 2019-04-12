package com.gmail.alsubbota.liveevent

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class EventHolder<out T>(private val content: T) {

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

private class ConsumerWrapper<T>(private val consumer: (T)->Unit) : Observer<EventHolder<T>>{
    override fun onChanged(t: EventHolder<T>?) {
        t?.getContentIfNotHandled()?.let{ consumer(it) }
    }
}

interface Consumer<T>{
    fun onArrived(event: T?)
}

open class LiveEvent<T>{
    private val mData = MutableLiveData<EventHolder<T>>()
    private val mMap = HashMap<(T)->Unit, ConsumerWrapper<T>>()
    fun sendEvent(value : T){
        mData.value = EventHolder(value)
    }
    fun postEvent(value: T){
        mData.postValue(EventHolder(value))
    }


    fun observe(owner: LifecycleOwner, consumer: (T)->Unit) {
        mData.observe(owner, ConsumerWrapper(consumer))
    }
    fun observe(owner: LifecycleOwner, observer: Observer<EventHolder<T>>){
        mData.observe(owner, observer)
    }

    fun observeForever(consumer: (T)->Unit) {
        val wrapper = ConsumerWrapper(consumer)
        mMap.put(consumer, wrapper)
        mData.observeForever(wrapper)
    }
    fun observeForever(observer: Observer<EventHolder<T>>) {
        mData.observeForever(observer)
    }

    fun removeObserver(consumer: (T)->Unit) {
        val wrapper = mMap.remove(consumer)
        if (wrapper != null)
            mData.removeObserver(wrapper)
    }
    fun removeObserver(observer: Observer<EventHolder<T>>) {
        mData.removeObserver(observer)
    }
}

