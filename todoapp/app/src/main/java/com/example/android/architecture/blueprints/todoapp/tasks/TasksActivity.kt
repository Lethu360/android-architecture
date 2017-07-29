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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentActivity
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import com.example.android.architecture.blueprints.todoapp.LifecycleAppCompatActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils


class TasksActivity : LifecycleAppCompatActivity(), TaskItemNavigator, TasksNavigator {

    private lateinit var mDrawerLayout: DrawerLayout

    private lateinit var mViewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        setupToolbar()

        setupNavigationDrawer()

        setupViewFragment()

        mViewModel = obtainViewModel(this)

        // Subscribe to "open task" event
        mViewModel.getOpenTaskEvent().observe(this, Observer<String> {
            if (it != null) {
                openTaskDetails(it)
            }
        })

        // Subscribe to "new task" event
        mViewModel.getNewTaskEvent().observe(this, Observer<Void> {
            addNewTask()
        })
    }

    companion object {
        @JvmStatic fun obtainViewModel(activity: FragmentActivity): TasksViewModel {
            // Use a Factory to inject dependencies into the ViewModel
            val factory = ViewModelFactory.getInstance(activity.getApplication())

            val viewModel = ViewModelProviders.of(activity, factory).get(TasksViewModel::class.java)

            return viewModel
        }
    }

    private fun setupViewFragment() {
        var tasksFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (tasksFragment == null) {
            // Create the fragment
            tasksFragment = TasksFragment.newInstance()
            ActivityUtils.replaceFragmentInActivity(
                    getSupportFragmentManager(), tasksFragment, R.id.contentFrame)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar as ActionBar
        ab.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView = findViewById(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView as NavigationView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener(
                NavigationView.OnNavigationItemSelectedListener {
                    when (it.getItemId()) {
                        R.id.list_navigation_menu_item -> {
                            // Do nothing, we're already on that screen
                        }
                        R.id.statistics_navigation_menu_item -> {
                            val intent = Intent(TasksActivity@ this, StatisticsActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }
                    }
                    // Close the navigation drawer when an item is selected.
                    it.setChecked(true)
                    mDrawerLayout.closeDrawers()
                    return@OnNavigationItemSelectedListener true
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mViewModel.handleActivityResult(requestCode, resultCode)
    }

    override fun openTaskDetails(taskId: String) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE)

    }

    override fun addNewTask() {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE)
    }
}
