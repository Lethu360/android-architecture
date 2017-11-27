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

package com.example.android.architecture.blueprints.todoapp.util

import android.support.test.espresso.IdlingResource

/**
 * Contains a static reference to {@link IdlingResource}, only available in the 'mock' build type.
 *
 * Converted to kotlin by whylee259@gmail.com
 */
object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    private val mCountingIdlingResource = SimpleCountingIdlingResource(RESOURCE)

    @JvmStatic fun increment() {
        mCountingIdlingResource.increment()
    }

    @JvmStatic fun decrement() {
        mCountingIdlingResource.decrement()
    }

    @JvmStatic fun getIdlingResource(): IdlingResource {
        return mCountingIdlingResource
    }
}
