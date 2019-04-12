package com.gmail.alsubbota.liveevent

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


class LifecycleOwnerStub() : LifecycleOwner {
    private val registryLifecycle = LifecycleRegistry(this)
    override fun getLifecycle() = registryLifecycle
    fun setCurrentState(state: Lifecycle.State){
        registryLifecycle.markState(state)
    }
}

@ExtendWith(InstantTaskExecutorExtension::class)
class LiveEventTest {
    private val lifecycle = LifecycleOwnerStub()
    @BeforeEach
    fun initLifecycle()    {
        lifecycle.setCurrentState(Lifecycle.State.RESUMED)
    }


    @Test
    fun `empty LiveEvent does not call consumers`(){
        val event = LiveEvent<Int>()
        event.observe(lifecycle) {
            fail<Unit>()
        }
    }
    @Test
    fun `event passed to consumer`(){
        val event = LiveEvent<Int>()
        var count = 0
        event.observe(lifecycle){
            count++
        }
        event.sendEvent(1)
        assertEquals(1, count)
    }
    @Test
    fun `event passed to only one consumer`(){
        val event = LiveEvent<Int>()
        var count = 0
        event.observe(lifecycle){
            count++
        }
        event.observe(lifecycle){
            count++
        }
        event.sendEvent(1)
        assertEquals(1, count)
    }

    @Test
    fun `several events passed in right order`(){
        val event = LiveEvent<Int>()
        val expected = listOf<Int>(0,10,-20,35,44,95)
        val actual = ArrayList<Int>()
        event.observe(lifecycle) {actual.add(it)}
        for(e in expected)
            event.sendEvent(e)
        assertArrayEquals(expected.toIntArray(), actual.toIntArray())
    }
    @Test
    fun `to observe is working`(){
        val event = LiveEvent<Int>()
        val expected = listOf<Int>(0,10,-20,35,44,95)
        val actual = ArrayList<Int>()
        event.observe(lifecycle, Observer {
            it?.apply{
                if (peekContent() % 2 == 0)
                    actual.add(getContentIfNotHandled() ?: return@apply)
            }
        })
        event.observe(lifecycle, Observer<EventHolder<Int>> {
            it?.apply{
                if (peekContent() % 2 != 0)
                    actual.add(getContentIfNotHandled() ?: return@apply)
            }
        })
        for(e in expected)
            event.sendEvent(e)
        assertArrayEquals(expected.toIntArray(), actual.toIntArray())
    }
    @Test
    fun `event does not passed after removeObserver`(){
        val event = LiveEvent<Int>()
        var count = 0
        val observer = { t :Int ->
            count++
            Unit
        }
        event.observeForever(observer)
        event.sendEvent(1)
        event.removeObserver(observer)
        event.sendEvent(1)
        assertEquals(1, count)
    }
}