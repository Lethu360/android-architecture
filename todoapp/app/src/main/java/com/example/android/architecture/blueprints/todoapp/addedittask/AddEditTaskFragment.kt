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

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage
import com.example.android.architecture.blueprints.todoapp.databinding.AddtaskFragBinding
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : LifecycleFragment() {
    // Required empty public constructor

    companion object {
        const val ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID"

        @JvmStatic fun newInstance(): AddEditTaskFragment {
            return AddEditTaskFragment()
        }
    }

    private lateinit var mViewModel: AddEditTaskViewModel

    private var mViewDataBinding: AddtaskFragBinding? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupFab()

        setupSnackbar()

        setupActionBar()

        loadData()
    }

    private fun loadData() {
        // Add or edit an existing task?
        mViewModel.start(arguments?.getString(ARGUMENT_EDIT_TASK_ID))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.addtask_frag, container, false)
        if (mViewDataBinding == null) {
            mViewDataBinding = AddtaskFragBinding.bind(root)
        }

        mViewModel = AddEditTaskActivity.obtainViewModel(activity)

        mViewDataBinding!!.viewmodel = mViewModel

        setHasOptionsMenu(true)
        retainInstance = false

        return mViewDataBinding!!.root
    }

    private fun setupSnackbar() {
        mViewModel.getSnackbarMessage().observe(this, object : SnackbarMessage.SnackbarObserver {
            override fun onNewMessage(@StringRes snackbarMessageResourceId: Int) {
                SnackbarUtils.showSnackbar(view, getString(snackbarMessageResourceId))
            }
        })
    }

    private fun setupFab() {
        val fab = activity.findViewById(R.id.fab_edit_task_done) as FloatingActionButton?
        fab?.run {
            setImageResource(R.drawable.ic_done)
            setOnClickListener({
                mViewModel.saveTask()
            })
        }
    }

    private fun setupActionBar() {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.run {
            if (arguments != null) {
                setTitle(R.string.edit_task)
            } else {
                setTitle(R.string.add_task)
            }
        }
    }
}
