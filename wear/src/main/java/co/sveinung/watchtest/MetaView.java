package co.sveinung.watchtest;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sveinung on 01.07.14.
 */
public class MetaView {
    private final TextView textView;
    private final ImageView imageView;

    public MetaView(View root) {
        textView = (TextView) root.findViewById(R.id.player_meta_text);
        imageView = (ImageView) root.findViewById(R.id.player_meta_image);
    }

    public void setMeta(final String text, final Bitmap image) {
        textView.setText(text);
        imageView.setImageBitmap(image);
    }
}
