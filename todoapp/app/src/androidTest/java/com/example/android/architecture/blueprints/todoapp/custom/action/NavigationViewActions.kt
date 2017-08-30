/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.example.android.architecture.blueprints.todoapp.custom.action

import android.content.res.Resources.NotFoundException
import android.support.design.widget.NavigationView
import android.support.test.espresso.PerformException
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.espresso.util.HumanReadables
import android.view.Menu
import android.view.View
import org.hamcrest.Matchers.allOf

/**
 * View actions for interacting with {@link NavigationView}
 */
object NavigationViewActions {

    /**
     * Returns a {@link ViewAction} that navigates to a menu item in {@link NavigationView} using a
     * menu item resource id.
     *
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link DrawerLayout}
     * <li>View must be of type {@link NavigationView}
     * <li>View must be visible on screen
     * <li>View must be displayed on screen
     * <ul>
     *
     * @param menuItemId the resource id of the menu item
     * @return a {@link ViewAction} that navigates on a menu item
     */
    @JvmStatic
    fun navigateTo(menuItemId: Int): ViewAction = object : ViewAction {

        override fun perform(uiController: UiController, view: View) {
            val navigationView = view as NavigationView
            val menu = navigationView.menu
            if (null == menu.findItem(menuItemId)) {
                throw PerformException.Builder()
                        .withActionDescription(this.description)
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(RuntimeException(getErrorMessage(menu, view)))
                        .build()
            }
            menu.performIdentifierAction(menuItemId, 0)
            uiController.loopMainThreadUntilIdle()
        }

        private fun getErrorMessage(menu: Menu, view: View): String {
            val NEW_LINE = System.getProperty("line.separator")
            val errorMessage = StringBuilder("Menu item was not found, available menu items:")
                    .append(NEW_LINE)
            for (position in 0 until menu.size()) {
                errorMessage.append("[MenuItem] position=$position")
                menu.getItem(position)?.run {
                    title?.run { errorMessage.append(", title=$this") }
                    view.resources?.run {
                        try {
                            errorMessage.append(", id=${getResourceName(itemId)}")
                        } catch (nfe: NotFoundException) {
                            errorMessage.append("not found")
                        }
                    }
                    errorMessage.append(NEW_LINE)
                }
            }
            return errorMessage.toString()
        }

        override fun getDescription() = "click on menu item with id"

        override fun getConstraints() = allOf(
                isAssignableFrom(NavigationView::class.java),
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDisplayingAtLeast(90)
        )
    }
}
