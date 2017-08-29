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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.annotation.StringRes

import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository


/**
 * Listens to user actions from the list item in ({@link TasksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
class TaskDetailViewModel constructor(context: Application, tasksRepository: TasksRepository)
    : AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    val task = ObservableField<Task>()

    val completed = ObservableBoolean()

    private val mEditTaskCommand = SingleLiveEvent<Void>()

    private val mDeleteTaskCommand = SingleLiveEvent<Void>()

    private val mTasksRepository = tasksRepository

    private val mSnackbarText = SnackbarMessage()

    private var mIsDataLoading = false

    fun deleteTask() {
        if (task.get() != null) {
            mTasksRepository.deleteTask(task.get().id)
            mDeleteTaskCommand.call()
        }
    }

    fun editTask() {
        mEditTaskCommand.call()
    }

    fun getSnackbarMessage(): SnackbarMessage {
        return mSnackbarText
    }

    fun getEditTaskCommand(): SingleLiveEvent<Void> {
        return mEditTaskCommand
    }

    fun getDeleteTaskCommand(): SingleLiveEvent<Void> {
        return mDeleteTaskCommand
    }

    fun setCompleted(completed: Boolean) {
        if (mIsDataLoading) {
            return
        }
        val task = this.task.get()
        task.isCompleted = completed
        if (completed) {
            mTasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            mTasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        if (taskId != null) {
            mIsDataLoading = true
            mTasksRepository.getTask(taskId, this)
        }
    }

    fun setTask(task: Task?) {
        this.task.set(task)
        if (task != null) {
            completed.set(task.isCompleted)
        }
    }

    fun isDataAvailable(): Boolean {
        return task.get() != null
    }

    fun isDataLoading(): Boolean {
        return mIsDataLoading
    }

    override fun onTaskLoaded(task: Task?) {
        setTask(task)
        mIsDataLoading = false
    }

    override fun onDataNotAvailable() {
        task.set(null)
        mIsDataLoading = false
    }

    fun onRefresh() {
        if (task.get() != null) {
            start(task.get().id)
        }
    }

    fun getTaskId(): String? {
        return task.get().id
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        mSnackbarText.value = message
    }
}
