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
