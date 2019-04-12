package com.gmail.alsubbota.liveevent

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
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
        event.observe(lifecycle, object: Consumer<Int>() {
            override fun onArrived(event: Int) {
                fail<Unit>()
            }
        })
    }
    @Test
    fun `event passed to consumer`(){
        val event = LiveEvent<Int>()
        var count = 0
        event.observe(lifecycle, object: Consumer<Int>() {
            override fun onArrived(event: Int) {
                count++
            }
        })
        event.sendEvent(1)
        assertEquals(1, count)
    }
    @Test
    fun `event passed to only one consumer`(){
        val event = LiveEvent<Int>()
        var count = 0
        event.observe(lifecycle, object: Consumer<Int>() {
            override fun onArrived(event: Int) {
                count++
            }
        })
        event.observe(lifecycle, object: Consumer<Int>() {
            override fun onArrived(event: Int) {
                count++
            }
        })
        event.sendEvent(1)
        assertEquals(1, count)
    }

    @Test
    fun `several events passed in right order`(){
        val event = LiveEvent<Int>()
        val expected = listOf<Int>(0,10,-20,35,44,95)
        val actual = ArrayList<Int>()
        event.observe(lifecycle, object: Consumer<Int>() {
            override fun onArrived(event: Int) {
                actual.add(event)
            }
        })
        for(e in expected)
            event.sendEvent(e)
        assertArrayEquals(expected.toIntArray(), actual.toIntArray())
    }
}