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

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.databinding.TasksFragBinding
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils

import java.util.List

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
class TasksFragment :
// Requires empty public constructor
        LifecycleFragment() {

    private lateinit var mTasksViewModel: TasksViewModel

    private lateinit var mTasksFragBinding: TasksFragBinding

    private lateinit var mListAdapter: TasksAdapter

    companion object {
        @JvmStatic fun newInstance(): TasksFragment {
            return TasksFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        mTasksViewModel.start()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false)

        mTasksViewModel = TasksActivity.obtainViewModel(activity)

        mTasksFragBinding.viewmodel = mTasksViewModel

        setHasOptionsMenu(true)

        return mTasksFragBinding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> mTasksViewModel.clearCompletedTasks()

            R.id.menu_filter -> showFilteringPopUpMenu()

            R.id.menu_refresh -> mTasksViewModel.loadTasks(true)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupSnackbar()

        setupFab()

        setupListAdapter()

        setupRefreshLayout()
    }

    private fun setupSnackbar() {
        mTasksViewModel.getSnackbarMessage().observe(this, SnackbarMessage.SnackbarObserver {
            SnackbarUtils.showSnackbar(view, getString(it))
        })
    }

    private fun showFilteringPopUpMenu() {
        val popup = PopupMenu(context, activity.findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener({ item ->
            when (item.itemId) {
                R.id.active -> mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)

                R.id.completed -> mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

                else -> mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
            }
            mTasksViewModel.loadTasks(false)
            return@setOnMenuItemClickListener true
        })

        popup.show()
    }

    private fun setupFab() {
        val fab = activity.findViewById(R.id.fab_add_task) as FloatingActionButton

        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener({
            mTasksViewModel.addNewTask()
        })
    }

    private fun setupListAdapter() {
        val listView = mTasksFragBinding.tasksList

        mListAdapter = TasksAdapter(ArrayList<Task>(0) as List<Task>, mTasksViewModel)
        listView.adapter = mListAdapter
    }

    private fun setupRefreshLayout() {
        val listView = mTasksFragBinding.tasksList
        val swipeRefreshLayout = mTasksFragBinding.refreshLayout
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(activity, R.color.colorPrimary),
                ContextCompat.getColor(activity, R.color.colorAccent),
                ContextCompat.getColor(activity, R.color.colorPrimaryDark)
        )
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView)
    }

}
