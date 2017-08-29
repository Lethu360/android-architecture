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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.graphics.drawable.Drawable
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity


/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 *
 * Converted to kotlin by whylee259@gmail.com
 */
class TasksViewModel constructor(context: Application, repository: TasksRepository)
    : AndroidViewModel(context) {

    // These observable fields will update Views automatically
    val items = ObservableArrayList<Task>()

    val dataLoading = ObservableBoolean(false)

    val currentFilteringLabel = ObservableField<String>()

    val noTasksLabel = ObservableField<String>()

    val noTaskIconRes = ObservableField<Drawable>()

    val empty = ObservableBoolean(false)

    val tasksAddViewVisible = ObservableBoolean()

    val mSnackbarText = SnackbarMessage()

    var mCurrentFiltering = TasksFilterType.ALL_TASKS

    private val mTasksRepository = repository

    private val mIsDataLoadingError = ObservableBoolean(false)

    private val mOpenTaskEvent = SingleLiveEvent<String>()

    @SuppressLint("StaticFieldLeak")
    // To avoid leaks, this must be an Application Context.
    private val mContext = context.applicationContext // Force use of Application Context.

    private val mNewTaskEvent = SingleLiveEvent<Void>()

    init {
        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS)
    }

    fun start() {
        loadTasks(false)
    }

    fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate, true)
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    fun setFiltering(requestType: TasksFilterType) {
        mCurrentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            TasksFilterType.ALL_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_all))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_all))
                noTaskIconRes.set(mContext.resources.getDrawable(
                        R.drawable.ic_assignment_turned_in_24dp))
                tasksAddViewVisible.set(true)
            }
            TasksFilterType.ACTIVE_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_active))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_active))
                noTaskIconRes.set(mContext.resources.getDrawable(
                        R.drawable.ic_check_circle_24dp))
                tasksAddViewVisible.set(false)
            }
            TasksFilterType.COMPLETED_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_completed))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_completed))
                noTaskIconRes.set(mContext.resources.getDrawable(
                        R.drawable.ic_verified_user_24dp))
                tasksAddViewVisible.set(false)
            }
        }
    }

    fun clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks()
        mSnackbarText.setValue(R.string.completed_tasks_cleared)
        loadTasks(false, false)
    }

    fun completeTask(task: Task, completed: Boolean) {
        // Update the entity
        task.isCompleted = completed

        // Notify repository
        if (completed) {
            mTasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            mTasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun getSnackbarMessage(): SnackbarMessage {
        return mSnackbarText
    }

    fun getOpenTaskEvent(): SingleLiveEvent<String> {
        return mOpenTaskEvent
    }

    fun getNewTaskEvent(): SingleLiveEvent<Void> {
        return mNewTaskEvent
    }

    private fun showSnackbarMessage(message: Int?) {
        mSnackbarText.value = message
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        mNewTaskEvent.call()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            when (resultCode) {
                TaskDetailActivity.EDIT_RESULT_OK ->
                    mSnackbarText.value = R.string.successfully_saved_task_message

                AddEditTaskActivity.ADD_EDIT_RESULT_OK ->
                    mSnackbarText.value = R.string.successfully_added_task_message

                TaskDetailActivity.DELETE_RESULT_OK ->
                    mSnackbarText.value = R.string.successfully_deleted_task_message
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) {
            dataLoading.set(true)
        }
        if (forceUpdate) {

            mTasksRepository.refreshTasks()
        }

        mTasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>?) {
                val tasksToShow = ArrayList<Task>()

                // We filter the tasks based on the requestType
                tasks?.forEach {
                    when (mCurrentFiltering) {
                        TasksFilterType.ALL_TASKS -> tasksToShow.add(it)

                        TasksFilterType.ACTIVE_TASKS -> if (it.isActive()) tasksToShow.add(it)

                        TasksFilterType.COMPLETED_TASKS -> if (it.isCompleted) tasksToShow.add(it)
                    }
                }
                if (showLoadingUI) {
                    dataLoading.set(false)
                }
                mIsDataLoadingError.set(false)

                items.clear()
                items.addAll(tasksToShow)
                empty.set(items.isEmpty())
            }

            override fun onDataNotAvailable() {
                mIsDataLoadingError.set(true)
            }
        })
    }
}
