package co.sveinung.watchtest;

import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by sveinung on 01.07.14.
 */
public class PlayerActivity extends InsetActivity {
    private DismissOverlayView dismissOverlayView;
    private GestureDetector longTouchDetector;

    @Override
    public void onReadyForContent() {
        setContentView(R.layout.activity_player);
        dismissOverlayView = (DismissOverlayView) findViewById(R.id.player_dismiss_overlay);
        dismissOverlayView.setIntroText("Long press to dismiss!");
        dismissOverlayView.showIntroIfNecessary();
        longTouchDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                dismissOverlayView.show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return longTouchDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
