package co.sveinung.watchtest;

import android.support.wearable.activity.InsetActivity;

/**
 * Created by sveinung on 01.07.14.
 */
public class PlayerActivity extends InsetActivity {
    @Override
    public void onReadyForContent() {
        setContentView(R.layout.activity_player);
    }
}
