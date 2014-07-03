package co.sveinung.watchtest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class PlayerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Shouldn't be opened on phone", Toast.LENGTH_LONG).show();
        finish();
    }
}
