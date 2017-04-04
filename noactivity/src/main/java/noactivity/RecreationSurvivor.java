package noactivity;


import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

import noactivity.utils.Factory;

public class RecreationSurvivor<T> {

    private final Factory<T> factory;
    private int claims = 0;
    private T instance;

    private RecreationSurvivor(Factory<T> factory) {
        this.factory = factory;
    }

    public T obtain(Activity claimant) {
        claims++;
        if (instance == null) {
            instance = factory.create();
        }
        return instance;
    }

    public void release(Activity claimant) {
        claims--;
        if (claims == 0) {
            if (claimant.isFinishing()) {
                instance = null;
            }
        }
    }

    // TODO Storing by class disallows multiple independently stacked screens
    // Open A, Open A.details, Open B, Open B.details (same activity as A.details) would not work

    public static <A extends Activity, T> RecreationSurvivor<T> create(A activity, Factory<T> factory) {
        Class activityClass = activity.getClass();

        RecreationSurvivor survivor = survivorCache.get(activityClass);
        if (survivor == null) {
            survivor = new RecreationSurvivor<>(factory);
            survivorCache.put(activityClass, survivor);
        }

        //noinspection unchecked
        return survivor;
    }

    private static Map<Class, RecreationSurvivor> survivorCache = new HashMap<>();
}
