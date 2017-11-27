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

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentActivity
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.replaceFragmentInActivity

/**
 * Show statistics for tasks.
 */
class StatisticsActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.statistics_act)

        setupToolbar()

        setupNavigationDrawer()

        findOrCreateViewFragment()
    }

    companion object {
        @JvmStatic fun obtainViewModel(activity: FragmentActivity): StatisticsViewModel {
            // Use a Factory to inject dependencies into the ViewModel
            val factory = ViewModelFactory.getInstance(activity.application)

            return ViewModelProviders.of(activity, factory).get(StatisticsViewModel::class.java)
        }
    }

    private fun findOrCreateViewFragment(): StatisticsFragment {
        return supportFragmentManager.findFragmentById(R.id.contentFrame) as StatisticsFragment?
                ?:
                StatisticsFragment.newInstance().apply {
                    this@StatisticsActivity.replaceFragmentInActivity(this, R.id.contentFrame)
                }
    }

    private fun setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
        (findViewById(R.id.nav_view) as NavigationView?)?.run {
            setupDrawerContent(this)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.statistics_title)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener({
            menuItem ->
            when (menuItem.itemId) {
                R.id.list_navigation_menu_item -> {
                    val intent = Intent(StatisticsActivity@ this, TasksActivity::class.java)
                    startActivity(intent)
                }
                R.id.statistics_navigation_menu_item -> {
                    // Do nothing, we're already on that screen
                }
                else -> {
                }
            }
            // Close the navigation drawer when an item is selected.
            menuItem.isChecked = true
            mDrawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        })
    }
}
