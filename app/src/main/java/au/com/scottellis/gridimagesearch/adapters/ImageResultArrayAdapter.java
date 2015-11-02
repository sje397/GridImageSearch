package au.com.scottellis.gridimagesearch.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import au.com.scottellis.gridimagesearch.R;
import au.com.scottellis.gridimagesearch.models.ImageResult;

/**
 * Created by sellis on 11/1/15.
 */
public class ImageResultArrayAdapter extends ArrayAdapter<ImageResult> {
    public ImageResultArrayAdapter(Context context, ArrayList<ImageResult> results) {
        super(context, R.layout.result_item, results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageResult result = getItem(position);

        final Holder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.result_item, parent, false);
            holder = new Holder();
            holder.ivResult = (ImageView) convertView.findViewById(R.id.ivResult);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Picasso.with(getContext()).load(result.getThumbUrl()).into(holder.ivResult);
        holder.tvTitle.setText(Html.fromHtml(result.getTitle()));

        return convertView;
    }

    private static class Holder {
        ImageView ivResult;
        TextView tvTitle;
    }
}
