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

package com.example.android.architecture.blueprints.todoapp.data.source

import android.content.Context

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.wrap
import com.google.common.collect.Lists

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.mockito.Matchers.any
import org.mockito.Matchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
class TasksRepositoryTest {

    companion object {
        private const val TASK_TITLE = "title"

        private const val TASK_TITLE2 = "title2"

        private const val TASK_TITLE3 = "title3"

        private val TASKS = Lists.newArrayList(
                Task("Title1", "Description1"), Task("Title2", "Description2"))
    }


    private lateinit var mTasksRepository: TasksRepository

    @Mock
    private lateinit var mTasksRemoteDataSource: TasksDataSource

    @Mock
    private lateinit var mTasksLocalDataSource: TasksDataSource

    @Mock
    private lateinit var mContext: Context

    @Mock
    private lateinit var mGetTaskCallback: TasksDataSource.GetTaskCallback

    @Mock
    private lateinit var mLoadTasksCallback: TasksDataSource.LoadTasksCallback

    @Captor
    private lateinit var mTasksCallbackCaptor: ArgumentCaptor<TasksDataSource.LoadTasksCallback>

    @Captor
    private lateinit var mTaskCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>

    @Before
    fun setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        mTasksRepository = TasksRepository.getInstance(
                mTasksRemoteDataSource, mTasksLocalDataSource)
    }

    @After
    fun destroyRepositoryInstance() {
        TasksRepository.destroyInstance()
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the tasks repository
        twoTasksLoadCallsToRepository(mLoadTasksCallback)

        // Then tasks were only requested once from Service API
        verify(mTasksRemoteDataSource).getTasks(wrap(any(TasksDataSource.LoadTasksCallback::class.java)))
    }

    @Test
    fun getTasks_requestsAllTasksFromLocalDataSource() {
        // When tasks are requested from the tasks repository
        mTasksRepository.getTasks(mLoadTasksCallback)

        // Then tasks are loaded from the local data source
        verify(mTasksLocalDataSource).getTasks(wrap(any(TasksDataSource.LoadTasksCallback::class.java)))
    }

    @Test
    fun saveTask_savesTaskToServiceAPI() {
        // Given a stub task with title and description
        val newTask = Task(TASK_TITLE, "Some Task Description")

        // When a task is saved to the tasks repository
        mTasksRepository.saveTask(newTask)

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).saveTask(newTask)
        verify(mTasksLocalDataSource).saveTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description")
        mTasksRepository.saveTask(newTask)

        // When a task is completed to the tasks repository
        mTasksRepository.completeTask(newTask)

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newTask)
        verify(mTasksLocalDataSource).completeTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask.id)?.isActive(), `is`(false))
    }

    @Test
    fun completeTaskId_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description")
        mTasksRepository.saveTask(newTask)

        // When a task is completed using its id to the tasks repository
        mTasksRepository.completeTask(newTask.id)

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newTask)
        verify(mTasksLocalDataSource).completeTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask.id)?.isActive(), `is`(false))
    }

    @Test
    fun activateTask_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description", true)
        mTasksRepository.saveTask(newTask)

        // When a completed task is activated to the tasks repository
        mTasksRepository.activateTask(newTask)

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newTask)
        verify(mTasksLocalDataSource).activateTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask.id)?.isActive(), `is`(true))
    }

    @Test
    fun activateTaskId_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description", true)
        mTasksRepository.saveTask(newTask)

        // When a completed task is activated with its id to the tasks repository
        mTasksRepository.activateTask(newTask.id)

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newTask)
        verify(mTasksLocalDataSource).activateTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask.id)?.isActive(), `is`(true))
    }

    @Test
    fun getTask_requestsSingleTaskFromLocalDataSource() {
        // When a task is requested from the tasks repository
        mTasksRepository.getTask(TASK_TITLE, mGetTaskCallback)

        // Then the task is loaded from the database
        verify(mTasksLocalDataSource).getTask(wrap(eq(TASK_TITLE)), wrap(any(
                TasksDataSource.GetTaskCallback::class.java)))
    }

    @Test
    fun deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description", true)
        mTasksRepository.saveTask(newTask)
        val newTask2 = Task(TASK_TITLE2, "Some Task Description")
        mTasksRepository.saveTask(newTask2)
        val newTask3 = Task(TASK_TITLE3, "Some Task Description", true)
        mTasksRepository.saveTask(newTask3)

        // When a completed tasks are cleared to the tasks repository
        mTasksRepository.clearCompletedTasks()


        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).clearCompletedTasks()
        verify(mTasksLocalDataSource).clearCompletedTasks()

        assertThat(mTasksRepository.mCachedTasks?.size, `is`(1))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask2.id)?.isActive(), `is`(true))
        assertThat(mTasksRepository.mCachedTasks?.get(newTask2.id)?.title, `is`(TASK_TITLE2))
    }

    @Test
    fun deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description", true)
        mTasksRepository.saveTask(newTask)
        val newTask2 = Task(TASK_TITLE2, "Some Task Description")
        mTasksRepository.saveTask(newTask2)
        val newTask3 = Task(TASK_TITLE3, "Some Task Description", true)
        mTasksRepository.saveTask(newTask3)

        // When all tasks are deleted to the tasks repository
        mTasksRepository.deleteAllTasks()

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteAllTasks()
        verify(mTasksLocalDataSource).deleteAllTasks()

        assertThat(mTasksRepository.mCachedTasks?.size, `is`(0))
    }

    @Test
    fun deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given a task in the repository
        val newTask = Task(TASK_TITLE, "Some Task Description", true)
        mTasksRepository.saveTask(newTask)
        assertThat(mTasksRepository.mCachedTasks?.containsKey(newTask.id), `is`(true))

        // When deleted
        mTasksRepository.deleteTask(newTask.id)

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteTask(newTask.id)
        verify(mTasksLocalDataSource).deleteTask(newTask.id)

        // Verify it's removed from repository
        assertThat(mTasksRepository.mCachedTasks?.containsKey(newTask.id), `is`(false))
    }

    @Test
    fun getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getTasks in the repository with dirty cache
        mTasksRepository.refreshTasks()
        mTasksRepository.getTasks(mLoadTasksCallback)

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS)

        // Verify the tasks from the remote data source are returned, not the local
        verify(mTasksLocalDataSource, never()).getTasks(mLoadTasksCallback)
        verify(mLoadTasksCallback).onTasksLoaded(TASKS)
    }

    @Test
    fun getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getTasks in the repository
        mTasksRepository.getTasks(mLoadTasksCallback)

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource)

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS)

        // Verify the tasks from the local data source are returned
        verify(mLoadTasksCallback).onTasksLoaded(TASKS)
    }

    @Test
    fun getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getTasks in the repository
        mTasksRepository.getTasks(mLoadTasksCallback)

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource)

        // And the remote data source has no data available
        setTasksNotAvailable(mTasksRemoteDataSource)

        // Verify no data is returned
        verify(mLoadTasksCallback).onDataNotAvailable()
    }

    @Test
    fun getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        val taskId = "123"

        // When calling getTask in the repository
        mTasksRepository.getTask(taskId, mGetTaskCallback)

        // And the local data source has no data available
        setTaskNotAvailable(mTasksLocalDataSource, taskId)

        // And the remote data source has no data available
        setTaskNotAvailable(mTasksRemoteDataSource, taskId)

        // Verify no data is returned
        verify(mGetTaskCallback).onDataNotAvailable()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mTasksRepository.refreshTasks()

        // When calling getTasks in the repository
        mTasksRepository.getTasks(mLoadTasksCallback)

        // Make the remote data source return data
        setTasksAvailable(mTasksRemoteDataSource, TASKS)

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTasksLocalDataSource, times(TASKS.size)).saveTask(wrap(any(Task::class.java)))
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    private fun twoTasksLoadCallsToRepository(callback: TasksDataSource.LoadTasksCallback) {
        // When tasks are requested from repository
        mTasksRepository.getTasks(callback) // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mTasksLocalDataSource).getTasks(wrap(mTasksCallbackCaptor.capture()))

        // Local data source doesn't have data yet
        mTasksCallbackCaptor.value.onDataNotAvailable()


        // Verify the remote data source is queried
        verify(mTasksRemoteDataSource).getTasks(wrap(mTasksCallbackCaptor.capture()))

        // Trigger callback so tasks are cached
        mTasksCallbackCaptor.value.onTasksLoaded(TASKS)

        mTasksRepository.getTasks(callback) // Second call to API
    }

    private fun setTasksNotAvailable(dataSource: TasksDataSource) {
        verify(dataSource).getTasks(wrap(mTasksCallbackCaptor.capture()))
        mTasksCallbackCaptor.value.onDataNotAvailable()
    }

    private fun setTasksAvailable(dataSource: TasksDataSource, tasks: List<Task>) {
        verify(dataSource).getTasks(wrap(mTasksCallbackCaptor.capture()))
        mTasksCallbackCaptor.value.onTasksLoaded(tasks)
    }

    private fun setTaskNotAvailable(dataSource: TasksDataSource, taskId: String) {
        verify(dataSource).getTask(wrap(eq(taskId)), wrap(mTaskCallbackCaptor.capture()))
        mTaskCallbackCaptor.value.onDataNotAvailable()
    }

    private fun setTaskAvailable(dataSource: TasksDataSource, task: Task) {
        verify(dataSource).getTask(wrap(eq(task.id)), wrap(mTaskCallbackCaptor.capture()))
        mTaskCallbackCaptor.value.onTaskLoaded(task)
    }
}
