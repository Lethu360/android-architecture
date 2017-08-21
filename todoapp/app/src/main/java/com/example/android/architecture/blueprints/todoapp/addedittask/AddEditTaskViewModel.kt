/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.annotation.Nullable

import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
class AddEditTaskViewModel constructor(context: Application, tasksRepository: TasksRepository)
    : AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    @JvmField
    val title = ObservableField<String>()

    @JvmField
    val description = ObservableField<String>()

    val dataLoading = ObservableBoolean(false)

    private val mSnackbarText = SnackbarMessage()

    private val mTaskUpdated = SingleLiveEvent<Void>()

    private val mTasksRepository = tasksRepository

    private var mTaskId: String? = null

    private var mIsNewTask = false

    private var mIsDataLoaded = false

    private var mTaskCompleted = false

    fun start(taskId: String?) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return
        }
        mTaskId = taskId
        if (taskId == null) {
            // No need to populate, it's a new task
            mIsNewTask = true
            return
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return
        }
        mIsNewTask = false
        dataLoading.set(true)

        mTasksRepository.getTask(taskId, this)
    }

    override fun onTaskLoaded(task: Task?) {
        task?.let {
            title.set(it.title)
            description.set(it.description)
            mTaskCompleted = it.isCompleted
        }
        dataLoading.set(false)
        mIsDataLoaded = true

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    override fun onDataNotAvailable() {
        dataLoading.set(false)
    }

    // Called when clicking on fab.
    fun saveTask() {
        var task = Task(title.get(), description.get())
        if (task.isEmpty()) {
            mSnackbarText.setValue(R.string.empty_task_message)
            return
        }
        if (isNewTask() || mTaskId == null) {
            createTask(task)
        } else {
            task = Task(title.get(), description.get(), mTaskId!!, mTaskCompleted)
            updateTask(task)
        }
    }

    fun getSnackbarMessage(): SnackbarMessage {
        return mSnackbarText
    }

    fun getTaskUpdatedEvent(): SingleLiveEvent<Void>  {
        return mTaskUpdated
    }

    private fun isNewTask(): Boolean {
        return mIsNewTask
    }

    private fun createTask(newTask: Task) {
        mTasksRepository.saveTask(newTask)
        mTaskUpdated.call()
    }

    private fun updateTask(task: Task) {
        if (isNewTask()) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        mTasksRepository.saveTask(task)
        mTaskUpdated.call()
    }
}
