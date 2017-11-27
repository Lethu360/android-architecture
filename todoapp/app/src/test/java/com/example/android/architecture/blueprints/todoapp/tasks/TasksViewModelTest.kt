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

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.content.res.Resources

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.google.common.collect.Lists

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import com.example.android.architecture.blueprints.todoapp.R.string.successfully_deleted_task_message
import com.example.android.architecture.blueprints.todoapp.TEST_OBSERVER
import com.example.android.architecture.blueprints.todoapp.wrap
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Matchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Unit tests for the implementation of {@link TasksViewModel}
 */
class TasksViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    companion object {
        private lateinit var TASKS: List<Task>
    }

    @Mock
    private lateinit var mTasksRepository: TasksRepository

    @Mock
    private lateinit var mContext: Application

    @Captor
    private lateinit var mLoadTasksCallbackCaptor: ArgumentCaptor<LoadTasksCallback>

    private lateinit var mTasksViewModel: TasksViewModel

    @Before
    fun setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        setupContext()

        // Get a reference to the class under test
        mTasksViewModel = TasksViewModel(mContext, mTasksRepository)

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(Task("Title1", "Description1"),
                Task("Title2", "Description2", true), Task("Title3", "Description3", true))

        mTasksViewModel.getSnackbarMessage().removeObservers(TEST_OBSERVER)

    }

    private fun setupContext() {
        `when`(mContext.applicationContext).thenReturn(mContext)
        `when`(mContext.getString(R.string.successfully_saved_task_message))
                .thenReturn("EDIT_RESULT_OK")
        `when`(mContext.getString(R.string.successfully_added_task_message))
                .thenReturn("ADD_EDIT_RESULT_OK")
        `when`(mContext.getString(successfully_deleted_task_message))
                .thenReturn("DELETE_RESULT_OK")

        `when`(mContext.resources).thenReturn(mock(Resources::class.java))
    }

    @Test
    fun loadAllTasksFromRepository_dataLoaded() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
        mTasksViewModel.loadTasks(true)

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))


        // Then progress indicator is shown
        assertTrue(mTasksViewModel.dataLoading.get())
        mLoadTasksCallbackCaptor.value.onTasksLoaded(TASKS)

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.dataLoading.get())

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty())
        assertTrue(mTasksViewModel.items.size == 3)
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
        mTasksViewModel.loadTasks(true)

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))
        mLoadTasksCallbackCaptor.value.onTasksLoaded(TASKS)

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.dataLoading.get())

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty())
        assertTrue(mTasksViewModel.items.size == 1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
        mTasksViewModel.loadTasks(true)

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(wrap(mLoadTasksCallbackCaptor.capture()))
        mLoadTasksCallbackCaptor.value.onTasksLoaded(TASKS)

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.dataLoading.get())

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty())
        assertTrue(mTasksViewModel.items.size == 2)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun clickOnFab_ShowsAddTaskUi() {

        val observer = mock(Observer::class.java) as Observer<Void>

        mTasksViewModel.getNewTaskEvent().observe(TEST_OBSERVER, observer)

        // When adding a new task
        mTasksViewModel.addNewTask()

        // Then the event is triggered
        verify(observer).onChanged(null)
    }

    @Test
    fun clearCompletedTasks_ClearsTasks() {
        // When completed tasks are cleared
        mTasksViewModel.clearCompletedTasks()

        // Then repository is called and the view is notified
        verify(mTasksRepository).clearCompletedTasks()
        verify(mTasksRepository).getTasks(wrap(any(LoadTasksCallback::class.java)))
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun handleActivityResult_editOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock(Observer::class.java) as Observer<Int>

        mTasksViewModel.getSnackbarMessage().observe(TEST_OBSERVER, observer)

        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.EDIT_RESULT_OK)

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_saved_task_message)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun handleActivityResult_addEditOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock(Observer::class.java) as Observer<Int>

        mTasksViewModel.getSnackbarMessage().observe(TEST_OBSERVER, observer)

        // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, AddEditTaskActivity.ADD_EDIT_RESULT_OK)

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_added_task_message)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun handleActivityResult_deleteOk() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock(Observer::class.java) as Observer<Int>

        mTasksViewModel.getSnackbarMessage().observe(TEST_OBSERVER, observer)

        // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.DELETE_RESULT_OK)

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_deleted_task_message)
    }

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Then the "Add task" action is visible
        assertThat(mTasksViewModel.tasksAddViewVisible.get(), `is`(true))
    }
}
