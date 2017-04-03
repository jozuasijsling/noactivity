package noactivity;


import android.app.Activity;

public interface ActivityCommand<A extends Activity> {
    void execute(A a);
}
