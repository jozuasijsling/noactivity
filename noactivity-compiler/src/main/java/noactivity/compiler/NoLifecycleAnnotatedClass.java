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

package noactivity.compiler;


import android.support.annotation.LayoutRes;

import javax.lang.model.element.TypeElement;

import noactivity.annotations.NoLifecycle;

public class NoLifecycleAnnotatedClass {

    private final TypeElement annotatedClassElement;
    @LayoutRes private final int id;

    public NoLifecycleAnnotatedClass(TypeElement classElement) {
        this.annotatedClassElement = classElement;

        NoLifecycle annotation = classElement.getAnnotation(NoLifecycle.class);
        this.id = annotation.value();

        // TODO lookup R.layout.[id}
    }

    public int getId() {
        return id;
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
