/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailViewModel
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel

/**
 * A creator is used to inject the product ID into the ViewModel
 * <p>
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
class ViewModelFactory private constructor(application: Application, repository: TasksRepository)
    : ViewModelProvider.NewInstanceFactory() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null

        @JvmStatic fun getInstance(application: Application): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = ViewModelFactory(application,
                                Injection.provideTasksRepository(application.applicationContext))
                    }
                }
            }
            return INSTANCE!!
        }
    }


    private val mApplication = application

    private val mTasksRepository = repository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) ->
                StatisticsViewModel(mApplication, mTasksRepository) as T
            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) ->
                TaskDetailViewModel(mApplication, mTasksRepository) as T
            modelClass.isAssignableFrom(AddEditTaskViewModel::class.java) ->
                AddEditTaskViewModel(mApplication, mTasksRepository) as T
            modelClass.isAssignableFrom(TasksViewModel::class.java) ->
                TasksViewModel(mApplication, mTasksRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
