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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.LifecycleAppCompatActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.replaceFragmentInActivity

/**
 * Displays an add or edit task screen.
 */
class AddEditTaskActivity : LifecycleAppCompatActivity(), AddEditTaskNavigator {

    companion object {
        const val REQUEST_CODE = 1

        const val ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1

        @JvmStatic fun obtainViewModel(activity: FragmentActivity): AddEditTaskViewModel {
            // Use a Factory to inject dependencies into the ViewModel
            val factory = ViewModelFactory.getInstance(activity.application)

            return ViewModelProviders.of(activity, factory).get(AddEditTaskViewModel::class.java)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onTaskSaved() {
        setResult(ADD_EDIT_RESULT_OK)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val addEditTaskFragment = obtainViewFragment()

        replaceFragmentInActivity(addEditTaskFragment, R.id.contentFrame)

        subscribeToNavigationChanges()
    }

    private fun subscribeToNavigationChanges() {
        val viewModel = obtainViewModel(this)

        // The activity observes the navigation events in the ViewModel
        viewModel.getTaskUpdatedEvent().observe(this,  Observer<Void> {
                AddEditTaskActivity@this.onTaskSaved()
        })
    }

    private fun obtainViewFragment(): AddEditTaskFragment {
        // View Fragment
        return supportFragmentManager.findFragmentById(R.id.contentFrame) as AddEditTaskFragment?
                ?:
                AddEditTaskFragment.newInstance().apply {
                    // Send the task ID to the fragment
                    val bundle = Bundle()
                    bundle.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                            intent.getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID))
                    arguments = bundle
                }
    }
}
