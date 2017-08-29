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

package com.example.android.architecture.blueprints.todoapp.addedittask


import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.mockito.Matchers.any
import org.mockito.Matchers.eq
import org.mockito.Mockito.verify

import com.example.android.architecture.blueprints.todoapp.wrap

/**
 * Unit tests for the implementation of {@link AddEditTaskViewModel}.
 */
class AddEditTaskViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mApplication: Application

    @Mock
    private lateinit var mTasksRepository: TasksRepository

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private lateinit var mGetTaskCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>

    private lateinit var mAddEditTaskViewModel: AddEditTaskViewModel

    @Before
    fun setupAddEditTaskViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        mAddEditTaskViewModel = AddEditTaskViewModel(mApplication, mTasksRepository)
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() {
        // When the ViewModel is asked to save a task
        mAddEditTaskViewModel.description.set("Some Task Description")
        mAddEditTaskViewModel.title.set("New Task Title")
        mAddEditTaskViewModel.saveTask()

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(wrap(any(Task::class.java))) // saved to the model
    }

    @Test
    fun populateTask_callsRepoAndUpdatesView() {
        val testTask = Task("TITLE", "DESCRIPTION", "1")

        // Get a reference to the class under test
        mAddEditTaskViewModel = AddEditTaskViewModel(mApplication, mTasksRepository)


        // When the ViewModel is asked to populate an existing task
        mAddEditTaskViewModel.start(testTask.id)

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getTask(wrap(eq(testTask.id)), wrap(mGetTaskCallbackCaptor.capture()))

        // Simulate callback
        mGetTaskCallbackCaptor.value.onTaskLoaded(testTask)

        // Verify the fields were updated
        assertThat(mAddEditTaskViewModel.title.get(), `is`(testTask.title))
        assertThat(mAddEditTaskViewModel.description.get(), `is`(testTask.description))
    }


}
