package ru.ifmo.ctddev.vanyan.imageseeker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static java.lang.System.exit;


public class ChildActivity extends Activity {
    private ImageView mImage;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_child);
        mImage = findViewById(R.id.fullscreen_image);
        mProgressBar = findViewById(R.id.progressbar);
        Intent parent = getIntent();
        if (parent.hasExtra(Intent.EXTRA_TEXT)) {
            String url = parent.getStringExtra(Intent.EXTRA_TEXT);
            Picasso.get().setIndicatorsEnabled(true);
            Picasso.get().load(url).fit().centerCrop().error(android.R.drawable.presence_busy).into(mImage, new Callback() {
                @Override
                public void onSuccess() {
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.wtf("IT'S A SMACHNOEPADENIE", "#" + "Activity started in a strange way");
            throw new IllegalStateException("this should never happen");
        }
    }
}
