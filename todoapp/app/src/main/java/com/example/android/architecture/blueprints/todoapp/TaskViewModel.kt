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

package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.ObservableField
import android.databinding.Observable.OnPropertyChangedCallback

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository


/**
 * Abstract class for View Models that expose a single {@link Task}.
 */
abstract class TaskViewModel(context: Context, tasksRepository: TasksRepository)
    : BaseObservable(), TasksDataSource.GetTaskCallback {

    @JvmField val snackbarText = ObservableField<String>()

    @JvmField val title = ObservableField<String>()

    @JvmField val description = ObservableField<String>()

    private val mTaskObservable = ObservableField<Task>()

    private val mTasksRepository = tasksRepository

    private val mContext = context.applicationContext // Force use of Application Context.

    private var mIsDataLoading: Boolean = false

    init {
        // Exposed observables depend on the mTaskObservable observable:
        mTaskObservable.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val task = mTaskObservable.get()
                if (task != null) {
                    title.set(task.title)
                    description.set(task.description)
                } else {
                    title.set(mContext.getString(R.string.no_data))
                    description.set(mContext.getString(R.string.no_data_description))
                }
            }
        })
    }

    fun start(taskId: String?) {
        taskId?.let {
            mIsDataLoading = true
            mTasksRepository.getTask(it, this)
        }
    }

    fun setTask(task: Task) {
        mTaskObservable.set(task)
    }

    // "completed" is two-way bound, so in order to intercept the new value, use a @Bindable
    // annotation and process it in the setter.
    @Bindable
    fun getCompleted(): Boolean {
        return mTaskObservable.get().isCompleted
    }

    fun setCompleted(completed: Boolean) {
        if (mIsDataLoading) {
            return
        }
        val task = mTaskObservable.get()
        // Update the entity
        task.isCompleted = completed

        // Notify repository and user
        if (completed) {
            mTasksRepository.completeTask(task)
            snackbarText.set(mContext.getResources().getString(R.string.task_marked_complete))
        } else {
            mTasksRepository.activateTask(task)
            snackbarText.set(mContext.getResources().getString(R.string.task_marked_active))
        }
    }

    @Bindable
    fun isDataAvailable(): Boolean {
        return mTaskObservable.get() != null
    }

    @Bindable
    fun isDataLoading(): Boolean {
        return mIsDataLoading
    }

    // This could be an observable, but we save a call to Task.getTitleForList() if not needed.
    @Bindable
    fun getTitleForList(): String? {
        return mTaskObservable.get()?.getTitleForList() ?: "No data"
    }

    override fun onTaskLoaded(task: Task?) {
        mTaskObservable.set(task)
        mIsDataLoading = false
        notifyChange() // For the @Bindable properties
    }

    override fun onDataNotAvailable() {
        mTaskObservable.set(null)
        mIsDataLoading = false
    }

    fun deleteTask() {
        if (mTaskObservable.get() != null) {
            mTasksRepository.deleteTask(mTaskObservable.get().id)
        }
    }

    fun onRefresh() {
        if (mTaskObservable.get() != null) {
            start(mTaskObservable.get().id)
        }
    }

    fun getSnackbarText(): String {
        return snackbarText.get()
    }

    fun getTaskId(): String? {
        return mTaskObservable.get()?.id
    }
}
