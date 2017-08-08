/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.LifecycleAppCompatActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment.REQUEST_EDIT_TASK

/**
 * Displays task details screen.
 */
class TaskDetailActivity : LifecycleAppCompatActivity(), TaskDetailNavigator {

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"

        const val DELETE_RESULT_OK = RESULT_FIRST_USER + 2

        const val EDIT_RESULT_OK = RESULT_FIRST_USER + 3

        @JvmStatic fun obtainViewModel(activity: FragmentActivity): TaskDetailViewModel {
            // Use a Factory to inject dependencies into the ViewModel
            val factory = ViewModelFactory.getInstance(activity.application)

            return ViewModelProviders.of(activity, factory).get(TaskDetailViewModel::class.java)
        }
    }

    private lateinit var mTaskViewModel: TaskDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.taskdetail_act)

        setupToolbar()

        val taskDetailFragment = findOrCreateViewFragment()

        ActivityUtils.replaceFragmentInActivity(supportFragmentManager,
                taskDetailFragment, R.id.contentFrame)

        mTaskViewModel = obtainViewModel(this)

        subscribeToNavigationChanges(mTaskViewModel)
    }

    private fun findOrCreateViewFragment(): TaskDetailFragment {
        // Get the requested task id
        val taskId = intent!!.getStringExtra(EXTRA_TASK_ID)

        val taskDetailFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
                ?: TaskDetailFragment.newInstance(taskId)

        return taskDetailFragment as TaskDetailFragment
    }


    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun subscribeToNavigationChanges(viewModel: TaskDetailViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.editTaskCommand.observe(this, Observer<Void> {
            this@TaskDetailActivity.onStartEditTask()
        })
        viewModel.deleteTaskCommand.observe(this, Observer<Void> {
            this@TaskDetailActivity.onTaskDeleted()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onTaskDeleted() {
        setResult(DELETE_RESULT_OK)
        // If the task was deleted successfully, go back to the list.
        finish()
    }

    override fun onStartEditTask() {
//        intent?.run{
//            val taskId = getStringExtra(EXTRA_TASK_ID)
//            val intent = Intent(TaskDetailActivity@this, AddEditTaskActivity::class.java)
//            intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
//            startActivityForResult(intent, REQUEST_EDIT_TASK)
//        }
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID)
        val intent = Intent(this, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
        startActivityForResult(intent, REQUEST_EDIT_TASK)
    }

}
