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


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;

public class NoLifecycleGroupedClasses {

    private String qualifiedClassName;

    private Map<Integer, NoLifecycleAnnotatedClass> itemsMap =
            new LinkedHashMap<Integer, NoLifecycleAnnotatedClass>();

    public NoLifecycleGroupedClasses(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void add(NoLifecycleAnnotatedClass toInsert) {
        itemsMap.put(toInsert.getId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {

    }

}
