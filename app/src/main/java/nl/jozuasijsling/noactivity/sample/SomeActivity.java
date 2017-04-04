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

package nl.jozuasijsling.noactivity.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import noactivity.ActivityAdapter;
import noactivity.ActivityCommand;
import noactivity.RecreationSurvivor;
import noactivity.utils.Factory;


public class SomeActivity extends Activity {

    private RecreationSurvivor<ActivityAdapter<SomeActivity>> survivor;
    private ActivityAdapter<SomeActivity> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        survivor = RecreationSurvivor.create(this, new Factory<ActivityAdapter<SomeActivity>>() {
            @Override
            public ActivityAdapter<SomeActivity> create() {
                return new ActivityAdapter<>(
                        new ActivityCommand<SomeActivity>() {
                            @Override
                            public void execute(SomeActivity activity) {
//                        Screen.setup();
                            }
                        },
                        new ActivityCommand<SomeActivity>() {
                            @Override
                            public void execute(SomeActivity activity) {
//                                Screen.destroy();
                            }
                        });
            }
        });
        adapter = survivor.obtain(this);
        adapter.install(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.eject(this);
        survivor.release(this);
    }
}
