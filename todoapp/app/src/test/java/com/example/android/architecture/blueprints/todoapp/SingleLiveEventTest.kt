/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import org.mockito.Matchers.anyInt
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


class SingleLiveEventTest {

    // Execute tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    // The class that has the lifecycle
    @Mock
    private lateinit var mOwner: LifecycleOwner

    // The observer of the event under test
    @Mock
    private lateinit var mEventObserver: Observer<Int>

    // Defines the Android Lifecycle of an object, used to trigger different events
    private lateinit var mLifecycle: LifecycleRegistry

    // Event object under test
    private val mSingleLiveEvent = SingleLiveEvent<Int>()

    @Before
    @Throws(Exception::class)
    fun setUpLifecycles() {
        MockitoAnnotations.initMocks(this)

        // Link custom lifecycle owner with the lifecyle register.
        mLifecycle = LifecycleRegistry(mOwner)
        `when`(mOwner.lifecycle).thenReturn(mLifecycle)

        // Start observing
        mSingleLiveEvent.observe(mOwner, mEventObserver)

        // Start in a non-active state
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @Test
    fun valueNotSet_onFirstOnResume() {
        // On resume
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // no update should be emitted because no value has been set
        verify(mEventObserver, never()).onChanged(anyInt())
    }

    @Test
    fun singleUpdate_onSecondOnResume_updatesOnce() {
        // After a value is set
        mSingleLiveEvent.value = 42

        // observers are called once on resume
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // on second resume, no update should be emitted.
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Check that the observer is called once
        verify(mEventObserver, times(1)).onChanged(anyInt())
    }

    @Test
    fun twoUpdates_updatesTwice() {
        // After a value is set
        mSingleLiveEvent.value = 42

        // observers are called once on resume
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // when the value is set again, observers are called again.
        mSingleLiveEvent.value = 42

        // Check that the observer has been called twice
        verify(mEventObserver, times(2)).onChanged(anyInt())
    }

    @Test
    fun twoUpdates_noUpdateUntilActive() {
        // Set a value
        mSingleLiveEvent.value = 42

        // which doesn't emit a change
        verify(mEventObserver, never()).onChanged(42)

        // and set it again
        mSingleLiveEvent.value = 42

        // observers are called once on resume.
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Check that the observer is called only once
        verify(mEventObserver, times(1)).onChanged(anyInt())
    }
}
