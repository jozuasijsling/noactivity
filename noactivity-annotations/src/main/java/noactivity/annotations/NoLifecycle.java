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
