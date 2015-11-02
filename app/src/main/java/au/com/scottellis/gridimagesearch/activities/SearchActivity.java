package au.com.scottellis.gridimagesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.scottellis.gridimagesearch.R;
import au.com.scottellis.gridimagesearch.adapters.ImageResultArrayAdapter;
import au.com.scottellis.gridimagesearch.models.ImageResult;
import au.com.scottellis.gridimagesearch.utils.EndlessScrollListener;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0";
    private static final int IMAGES_PER_PAGE = 8;

    private GridView gvResults;
    private ImageResultArrayAdapter adapter;

    private ArrayList<ImageResult> images = new ArrayList<>();
    private String currentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        gvResults = (GridView) findViewById(R.id.gvResults);
        adapter = new ImageResultArrayAdapter(this, images);
        gvResults.setAdapter(adapter);

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageResult result = images.get(position);
                Intent i = new Intent(getApplicationContext(), ViewImageActivity.class);
                i.putExtra("imageResult", result);
                startActivity(i);
            }
        });

        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.d("DEBUG", "onLoadMore, page = " + page + ", totalItemsCount = " + totalItemsCount);
                int offset = (page - 1) * IMAGES_PER_PAGE;
                if (offset < images.size()) return false;

                fetchResults(currentQuery, offset);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                images.clear();
                adapter.notifyDataSetChanged();
                fetchResults(query, 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fetchResults(String query, final int start) {
        Log.d("DEBUG", "fetchResults(\"" + query + "\"," + start + ")");

        if(isNetworkAvailable()) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(2000);
            String apiUrl = buildUrl(start, query);
            client.get(apiUrl, new JsonHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if(response.isNull("responseData")) {
                            String msg = response.getString("responseDetails");
                            if(msg.equalsIgnoreCase("out of range start")) {
                                // no more images
                            }
                        } else {
                            JSONArray array = response.getJSONObject("responseData").getJSONArray("results");
                            while (images.size() > start) {
                                images.remove(images.size() - 1);
                            }
                            images.addAll(ImageResult.fromJSONArray(array));
                            adapter.notifyDataSetChanged();
                        }
                    } catch (final JSONException ex) {
                        //Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Error: " + response.toString(), Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                }
            });
        } else {
            Toast.makeText(this, R.string.promt_no_network, Toast.LENGTH_LONG).show();
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private String buildUrl(int start, String query) {
        String url = IMAGE_SEARCH_API_URL
                + "&rsz=" + IMAGES_PER_PAGE
                + "&start=" + start;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean safe = preferences.getBoolean("safe_switch", true);
        String site = preferences.getString("site_text", "");
        String size = preferences.getString("size_list", "large");
        String type = preferences.getString("type_list", "all");
        String colorFilter = preferences.getString("color_list", "none");

        url += "&safe=" + (safe ? "active" : "off");
        if(!site.isEmpty()) {
            url+="&as_sitesearch=" + site;
        }
        if(!size.isEmpty()) {
            url+="&imgsz=" + size;
        }
        if(!type.isEmpty() && !type.equals("all")) {
            url+="&imgtype=" + type;
        }
        if(!colorFilter.isEmpty() && !colorFilter.equals("none")) {
            url+="&imgcolor=" + colorFilter;
        }

        url += "&q=" + query;

        Log.d("DEBUG", "Returning url: " + url);
        return url;
    }
}
