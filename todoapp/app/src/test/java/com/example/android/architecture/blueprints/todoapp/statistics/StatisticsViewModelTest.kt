/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.statistics


import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.wrap
import com.google.common.collect.Lists

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertEquals
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    companion object {
        private lateinit var TASKS: MutableList<Task>
    }

    @Mock
    private lateinit var mTasksRepository: TasksRepository

    @Captor
    private lateinit var mLoadTasksCallbackCaptor: ArgumentCaptor<TasksDataSource.LoadTasksCallback>

    private lateinit var mStatisticsViewModel: StatisticsViewModel

    @Before
    fun setupStatisticsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        mStatisticsViewModel = StatisticsViewModel(mock(Application::class.java), mTasksRepository)

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(Task("Title1", "Description1"),
                Task("Title2", "Description2", true), Task("Title3", "Description3", true))
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() {
        // Given an initialized StatisticsViewModel with no tasks
        TASKS.clear()

        // When loading of Tasks is requested
        mStatisticsViewModel.loadStatistics()

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS)

        // Then the results are empty
        assertThat(mStatisticsViewModel.empty.get(), `is`(true))
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // When loading of Tasks is requested
        mStatisticsViewModel.loadStatistics()

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS)

        // Then the results are empty
        assertThat(mStatisticsViewModel.empty.get(), `is`(false))
    }


    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        mStatisticsViewModel.loadStatistics()

        // And tasks data isn't available
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable()

        // Then an error message is shown
        assertEquals(mStatisticsViewModel.empty.get(), true)
        assertEquals(mStatisticsViewModel.error.get(), true)
    }
}
