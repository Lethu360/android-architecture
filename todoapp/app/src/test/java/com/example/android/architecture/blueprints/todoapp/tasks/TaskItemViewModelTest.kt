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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context
import android.content.res.Resources

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.wrap

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.mockito.Matchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Unit tests for the implementation of {@link TasksViewModel}
 */
class TaskItemViewModelTest {
    companion object {
        private const val NO_DATA_STRING = "NO_DATA_STRING"

        private const val NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING"
    }

    @Mock
    private lateinit var mTasksRepository: TasksRepository

    @Mock
    private lateinit var mContext: Context

    @Mock
    private lateinit var mTaskItemNavigator: TasksActivity

    @Captor
    private lateinit var mLoadTasksCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>

    private lateinit var mTaskItemViewModel: TaskItemViewModel

    private lateinit var mTask: Task

    @Before
    fun setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        setupContext()

        // Get a reference to the class under test
        mTaskItemViewModel = TaskItemViewModel(mContext, mTasksRepository)
        mTaskItemViewModel.setNavigator(mTaskItemNavigator)

    }

    private fun setupContext() {
        `when`(mContext.applicationContext).thenReturn(mContext)
        `when`(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING)
        `when`(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING)

        `when`(mContext.resources).thenReturn(mock(Resources::class.java))
    }

    @Test
    fun clickOnTask_ShowsDetailUi() {
        loadTaskIntoViewModel()

        mLoadTasksCallbackCaptor.value.onTaskLoaded(mTask) // Trigger callback

        // Then task detail UI is shown
        assertEquals(mTaskItemViewModel.title.get(), mTask.title)
        assertEquals(mTaskItemViewModel.description.get(), mTask.description)
    }

    @Test
    fun nullTask_showsNoData() {
        loadTaskIntoViewModel()

        // Load something different from null first (otherwise the change callback doesn't run)
        mLoadTasksCallbackCaptor.value.onTaskLoaded(mTask)
        mLoadTasksCallbackCaptor.value.onTaskLoaded(null) // Trigger callback

        // Then task detail UI is shown
        assertEquals(mTaskItemViewModel.title.get(), NO_DATA_STRING)
        assertEquals(mTaskItemViewModel.description.get(), NO_DATA_DESC_STRING)
    }

    @Test
    fun completeTask_ShowsTaskMarkedComplete() {
        loadTaskIntoViewModel()

        mLoadTasksCallbackCaptor.value.onTaskLoaded(mTask) // Trigger callback

        // When task is marked as complete
        mTaskItemViewModel.setCompleted(true)

        // Then repository is called
        verify(mTasksRepository).completeTask(mTask)
    }

    @Test
    fun activateTask_ShowsTaskMarkedActive() {
        loadTaskIntoViewModel()

        mLoadTasksCallbackCaptor.value.onTaskLoaded(mTask) // Trigger callback

        // When task is marked as complete
        mTaskItemViewModel.setCompleted(false)

        // Then repository is called
        verify(mTasksRepository).activateTask(mTask)
    }

    @Test
    fun unavailableTasks_ShowsError() {
        loadTaskIntoViewModel()

        mLoadTasksCallbackCaptor.value.onDataNotAvailable() // Trigger callback

        // Then repository is called
        assertFalse(mTaskItemViewModel.isDataAvailable())
    }

    private fun loadTaskIntoViewModel() {
        // Given a stubbed active task
        mTask = Task("Details Requested", "For this task")

        // When open task details is requested
        mTaskItemViewModel.start(mTask.id)

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(wrap(eq(mTask.id)), wrap(mLoadTasksCallbackCaptor.capture()))
    }
}
