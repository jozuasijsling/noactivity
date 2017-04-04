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

/**
 * @see <a href="https://github.com/JakeWharton/butterknife/blob/master/butterknife-compiler/src/main/java/butterknife/compiler/QualifiedId.java">
 * QualifiedId.java | butterknife-compiler (GitHub)</a>
 */
final class QualifiedId {
    final String packageName;
    final int id;

    QualifiedId(String packageName, int id) {
        this.packageName = packageName;
        this.id = id;
    }

    @Override
    public String toString() {
        return "QualifiedId{packageName='" + packageName + "', id=" + id + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QualifiedId)) return false;
        QualifiedId other = (QualifiedId) o;
        return id == other.id
                && packageName.equals(other.packageName);
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + id;
        return result;
    }
}
