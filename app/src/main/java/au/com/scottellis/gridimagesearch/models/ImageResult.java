package au.com.scottellis.gridimagesearch.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sellis on 11/1/15.
 */
public class ImageResult implements Serializable {
    private String title;
    private String fullUrl;
    private String thumbUrl;

    public ImageResult(JSONObject json) throws JSONException {
        title = json.getString("title");
        fullUrl = json.getString("url");
        thumbUrl = json.getString("tbUrl");
    }

    public String getTitle() {
        return title;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public static ArrayList<ImageResult> fromJSONArray(JSONArray array) throws JSONException {
        ArrayList<ImageResult> ret = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            ret.add(new ImageResult(array.getJSONObject(i)));
        }
        return ret;
    }
}
