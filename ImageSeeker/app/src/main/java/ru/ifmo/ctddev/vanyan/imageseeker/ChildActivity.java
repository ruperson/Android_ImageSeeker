package ru.ifmo.ctddev.vanyan.imageseeker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


public class ChildActivity extends Activity {
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_child);
        mImage = findViewById(R.id.fullscreen_image);
        Intent parent = getIntent();
        if (parent.hasExtra(Intent.EXTRA_TEXT)) {
            String url = parent.getStringExtra(Intent.EXTRA_TEXT);
            Picasso.get().setIndicatorsEnabled(true);
            Picasso.get().load(url).fit().centerCrop().error(android.R.drawable.presence_busy).into(mImage);
        } else {
            Log.d("BUYAKA", "#" + "Activity started in a strange way");
        }
    }
}
