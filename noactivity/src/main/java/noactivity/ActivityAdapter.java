package noactivity;


import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;

/**
 * Lets components follow an activity without needing to know its lifecycle.
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

    public void install(@NonNull A activity) {
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

    public void eject(@NonNull A activity) {
        stack.remove(activity);
        if (activity == head) {
            head = stack.peekFirst();

            if (head == null && activity.isFinishing()) {
                onFinishedListener.execute(activity);
            }
        }
    }

    public void enqueue(@NonNull ActivityCommand<A> command) {
        if (head == null) {
            commandQueue.add(command);
        } else {
            command.execute(head);
        }
    }
}

