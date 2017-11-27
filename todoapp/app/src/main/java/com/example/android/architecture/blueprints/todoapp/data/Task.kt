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

package com.example.android.architecture.blueprints.todoapp.data;

import android.util.Log
import java.util.*

/**
 * Immutable model class for a Task.
 *
 * Converted to kotlin by whylee259@gmail.com
 *
 *
 * @constructor Use this constructor to specify a completed Task if the Task already has an id (copy of
 * another Task).
 *
 * @param title       title of the task
 * @param description description of the task
 * @param id          id of the task
 * @param completed   true if the task is completed, false if it's active
 */
data class Task(
        val title: String?,
        val description: String?,
        val id: String = UUID.randomUUID().toString(),
        var isCompleted: Boolean = false) {

    /**
     * Use this constructor to create a new active Task.
     *
     * @param title       title of the task
     * @param description description of the task
     */

    constructor(title: String?, description: String?)
            : this(title, description, UUID.randomUUID().toString(), false)

    /**
     * Use this constructor to create an active Task if the Task already has an id (copy of another
     * Task).
     *
     * @param title       title of the task
     * @param description description of the task
     * @param id          id of the task
     */
    constructor(title: String?, description: String?, id: String)
            : this(title, description, id, false)

    /**
     * Use this constructor to create a new completed Task.
     *
     * @param title       title of the task
     * @param description description of the task
     * @param completed   true if the task is completed, false if it's active
     */
    constructor(title: String?, description: String?, completed: Boolean)
            : this(title, description, UUID.randomUUID().toString(), completed)

    fun getTitleForList(): String? = if (title?.isNotEmpty()!!) title else description

    fun isActive(): Boolean = !isCompleted

    fun isEmpty(): Boolean = title?.isNullOrEmpty()!! && description?.isNullOrEmpty()!!

    override fun toString(): String = "Task with title $title"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true;
        val task = other as Task
        return id == task.id &&
                title == task.title &&
                description == task.description

    }

}
