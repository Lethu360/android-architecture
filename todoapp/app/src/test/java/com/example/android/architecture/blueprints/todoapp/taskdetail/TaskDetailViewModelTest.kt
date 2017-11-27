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

package com.example.android.architecture.blueprints.todoapp.taskdetail


import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.res.Resources

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.wrap

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.mockito.Matchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Unit tests for the implementation of {@link TaskDetailViewModel}
 */
class TaskDetailViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val TITLE_TEST = "title"

        private const val DESCRIPTION_TEST = "description"

        private const val NO_DATA_STRING = "NO_DATA_STRING"

        private const val NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING"
    }


    @Mock
    private lateinit var mTasksRepository: TasksRepository

    @Mock
    private lateinit var mContext: Application

    @Mock
    private lateinit var mRepositoryCallback: TasksDataSource.GetTaskCallback

    @Mock
    private lateinit var mViewModelCallback: TasksDataSource.GetTaskCallback

    @Captor
    private lateinit var mGetTaskCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>

    private lateinit var mTaskDetailViewModel: TaskDetailViewModel

    private lateinit var mTask: Task

    @Before
    fun setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        setupContext()

        mTask = Task(TITLE_TEST, DESCRIPTION_TEST)

        // Get a reference to the class under test
        mTaskDetailViewModel = TaskDetailViewModel(mContext, mTasksRepository)
    }

    private fun setupContext() {
        `when`(mContext.applicationContext).thenReturn(mContext)
        `when`(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING)
        `when`(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING)
        `when`(mContext.resources).thenReturn(mock(Resources::class.java))
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback()

        // Then verify that the view was notified
        assertEquals(mTaskDetailViewModel.task.get().title, mTask.title)
        assertEquals(mTaskDetailViewModel.task.get().description, mTask.description)
    }

    @Test
    fun deleteTask() {
        setupViewModelRepositoryCallback()

        // When the deletion of a task is requested
        mTaskDetailViewModel.deleteTask()

        // Then the repository is notified
        verify(mTasksRepository).deleteTask(mTask.id)
    }

    @Test
    fun completeTask() {
        setupViewModelRepositoryCallback()

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(true)

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).completeTask(mTask)
        assertThat(mTaskDetailViewModel.getSnackbarMessage().value,
                `is`(R.string.task_marked_complete))
    }

    @Test
    fun activateTask() {
        setupViewModelRepositoryCallback()

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(false)

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(mTask)
        assertThat(mTaskDetailViewModel.getSnackbarMessage().value,
                `is`(R.string.task_marked_active))
    }

    @Test
    fun TaskDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active task
        mViewModelCallback = mock(TasksDataSource.GetTaskCallback::class.java)

        mTaskDetailViewModel.start(mTask.id)

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(wrap(eq(mTask.id)), wrap(mGetTaskCallbackCaptor.capture()))

        // When the repository returns an error
        mGetTaskCallbackCaptor.value.onDataNotAvailable() // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable())
    }

    @Test
    fun TaskDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback()

        // When the repository returns a null task
        mGetTaskCallbackCaptor.value.onTaskLoaded(null) // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable())

        // Then task detail UI is shown
        assertThat(mTaskDetailViewModel.task.get(), `is`(nullValue()))
    }

    private fun setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active task
        mViewModelCallback = mock(TasksDataSource.GetTaskCallback::class.java)

        mTaskDetailViewModel.start(mTask.id)

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(wrap(eq(mTask.id)), wrap(mGetTaskCallbackCaptor.capture()))

        mGetTaskCallbackCaptor.value.onTaskLoaded(mTask) // Trigger callback
    }

    @Test
    fun updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        val snackbarText = mTaskDetailViewModel.getSnackbarMessage()

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText.value, `is`(nullValue()))
    }
}
