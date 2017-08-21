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

package com.example.android.architecture.blueprints.todoapp.statistics

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * Exposes the data to be used in the statistics screen.
 * <p>
 * This ViewModel uses both {@link ObservableField}s ({@link ObservableBoolean}s in this case) and
 * {@link Bindable} getters. The values in {@link ObservableField}s are used directly in the layout,
 * whereas the {@link Bindable} getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
class StatisticsViewModel constructor(context: Application, tasksRepository: TasksRepository)
    : AndroidViewModel(context) {

    @JvmField val dataLoading = ObservableBoolean(false)

    @JvmField val error = ObservableBoolean(false)

    @JvmField val numberOfActiveTasks = ObservableField<String>()

    @JvmField val numberOfCompletedTasks = ObservableField<String>()

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    @JvmField val empty = ObservableBoolean()

    private var mNumberOfActiveTasks = 0

    private var mNumberOfCompletedTasks = 0

    @SuppressLint("StaticFieldLeak")
    private val mContext = context

    private val mTasksRepository = tasksRepository

    fun start() {
        loadStatistics()
    }

    fun loadStatistics() {
        dataLoading.set(true)

        mTasksRepository.getTasks(object: TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>?) {
                error.set(false)
                tasks?.run { computeStats(this) }
            }

            override fun onDataNotAvailable() {
                error.set(true)
                mNumberOfActiveTasks = 0
                mNumberOfCompletedTasks = 0
                updateDataBindingObservables()
            }
        })
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>) {
        var completed = 0
        var active = 0

        for (task in tasks) {
            if (task.isCompleted) {
                completed += 1
            } else {
                active += 1
            }
        }
        mNumberOfActiveTasks = active
        mNumberOfCompletedTasks = completed

        updateDataBindingObservables()
    }

    private fun updateDataBindingObservables() {
        numberOfCompletedTasks.set(
                mContext.getString(R.string.statistics_completed_tasks, mNumberOfCompletedTasks))
        numberOfActiveTasks.set(
                mContext.getString(R.string.statistics_active_tasks, mNumberOfActiveTasks))
        empty.set(mNumberOfActiveTasks + mNumberOfCompletedTasks == 0)
        dataLoading.set(false)

    }
}
