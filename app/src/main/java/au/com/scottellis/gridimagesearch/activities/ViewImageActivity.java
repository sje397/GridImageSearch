package au.com.scottellis.gridimagesearch.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import au.com.scottellis.gridimagesearch.R;
import au.com.scottellis.gridimagesearch.models.ImageResult;

public class ViewImageActivity extends AppCompatActivity {
    private ImageView ivImage;
    private TextView tvImageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ivImage = (ImageView) findViewById(R.id.ivImage);
        tvImageTitle = (TextView) findViewById(R.id.tvImageTitle);

        Intent launchIntent = getIntent();
        ImageResult result = (ImageResult) launchIntent.getSerializableExtra("imageResult");

        Picasso.with(this)
                .load(result.getFullUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(ivImage);
        tvImageTitle.setText(Html.fromHtml(result.getTitle()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
