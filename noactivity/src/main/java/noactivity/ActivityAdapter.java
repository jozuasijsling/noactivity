package noactivity;


import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;

/**
 * Lets components follow an activity without needing to know its lifecycle.
 * <h2>How does it work?</h2>
 * Similar to how fragments work, we let activity attach and detach itself. On attach, if
 * we were not initialized already, we dispatch the onInitialized event. On detach, if
 * the activity is {@linkplain Activity#isFinishing() finishing} we dispatch the onFinished event.
 * This is the general strategy to reduce lifecycle events to what we're truly interested in:
 * <ul>
 * <li>screen starts <i>(because of an intent)</i></li>
 * <li>screen ends <i>(because Activity.finish() was called)</i></li>
 * </ul>
 * Anything happening in between, activity pausing, stopping, background or recreating is either
 * ignored or hidden by this adapter.
 * <h2>How to set up</h2>
 * <pre><code>
 * {@literal @}Override
 * public void onCreate() {
 *     adapter.install(this);
 * }
 *
 * {@literal @}Override
 * public void onDestroy() {
 *     adapter.eject(this);
 * }
 * </code></pre>
 * <h2>Usage</h2>
 * <pre><code>
 *
 * adapter.forward(activity -> activity.startActivityForResult(...));
 *
 * </code></pre>
 */
@MainThread
public class ActivityAdapter<A extends Activity> {

    @Nullable private A head;

    @NonNull private final ArrayDeque<A> stack = new ArrayDeque<>(4);
    @NonNull private final ArrayDeque<ActivityCommand<A>> commandQueue = new ArrayDeque<>(4);

    @NonNull private final ActivityCommand<A> onInitializedListener;
    @NonNull private final ActivityCommand<A> onFinishedListener;
    private boolean taken = false;

    public ActivityAdapter(@NonNull ActivityCommand<A> onInitialized, @NonNull ActivityCommand<A> onFinished) {
        onInitializedListener = onInitialized;
        onFinishedListener = onFinished;
    }

    /**
     * Attaches activity to this adapter. Call this in {@link Activity#onCreate}.
     */
    public void install(@NonNull A activity) {
        if (stack.contains(activity)) {
            throw new IllegalArgumentException("Activity " + activity + " was already installed");
        }

        stack.addLast(activity);
        head = activity;

        if (!taken) {
            taken = true;
            onInitializedListener.execute(activity);
        } else {
            while (!commandQueue.isEmpty()) {
                commandQueue.remove().execute(activity);
            }
        }
    }

    /**
     * Detaches activity from this adapter. Call this in {@link Activity#onDestroy()}.
     */
    public void eject(@NonNull A activity) {
        stack.remove(activity);
        if (activity == head) {
            head = stack.peekFirst();

            if (head == null && activity.isFinishing()) {
                taken = false;
                onFinishedListener.execute(activity);
            }
        }
    }

    /**
     * Forwards a command to the attached activity. If this method is called when no activity
     * is attached (e.g. during a recreate) then the command is queued and will be executed in
     * FIFO order when the recreated activity comes back.
     */
    public void forward(@NonNull ActivityCommand<A> command) {
        if (head == null) {
            commandQueue.add(command);
        } else {
            command.execute(head);
        }
    }
}

