/*
 *    Copyright 2017 Jozua Catrinus Sijsling
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package noactivity.annotations;

import android.support.annotation.LayoutRes;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * {@code NoLifecycle} generates an activity for you. It removes lifecycle complexity
 * by exposing only events that matter to you as a UI developer. It hides activity recreation,
 * keeping your state around and automatically reattaching it to the recreated activity.
 */
@Target(TYPE)
@Retention(CLASS)
public @interface NoLifecycle {

    /**
     * Layout resource to be set with {@code Activity#setContentView}.
     */
    @LayoutRes int value();
}
