package  ru.ifmo.ctddev.vanyan.imageseeker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ifmo.ctddev.vanyan.imageseeker.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;



public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, GreenAdapter.ListItemClickListener {
    private static final int SEARCH_LOADER = 23;
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String SEARCH_KEY = "search_key";
    private GreenAdapter mAdapter;
    private RecyclerView mNumbersList;
    private SearchView  searchView;
    private String searchString;

    @Override
    public void onListItemClick(String link) {
        Context context = MainActivity.this;
        Class destinationActivity = ChildActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportLoaderManager().initLoader(SEARCH_LOADER, null, this);
        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_KEY);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null) {
            searchString = searchView.getQuery().toString();
            outState.putString(SEARCH_KEY, searchString);
        }
    }

    void buildRecycler(List<String> small_pics, List<String> big_pics, List<String> description) {
        mNumbersList = findViewById(R.id.rv_item);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersList.setLayoutManager(layoutManager);
        mNumbersList.setHasFixedSize(true);
        mAdapter = new GreenAdapter(small_pics, big_pics, description, this);
        mNumbersList.setAdapter(mAdapter);
    }


    private void makeSearchQuery(String query) {
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, query);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader = loaderManager.getLoader(SEARCH_LOADER);
        if (searchLoader == null) {
            loaderManager.initLoader(SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(SEARCH_LOADER, queryBundle, this);

        }
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override

    public Loader<String> onCreateLoader(int i, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) { //my mama said this won't leak

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                forceLoad();
            }

            @Nullable
            @Override
            public String loadInBackground() {
                String searchString = args.getString(SEARCH_QUERY_URL_EXTRA);
                if (searchString == null || TextUtils.isEmpty(searchString)) {
                    return null;
                }
                return NetworkUtils.getResponseFromHttpUrl(searchString);
            }
        };
    }


    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String JSONString) {
        try {
            JSONObject allJSON = new JSONObject(JSONString);
            JSONArray items = allJSON.getJSONArray("results");
            List<String>  small_pic = new ArrayList<>();
            List<String>  big_pic = new ArrayList<>();
            List<String>  description = new ArrayList<>();
            for (int i = 0; i < items.length(); i++ ) {
                JSONObject cur = items.getJSONObject(i);
                description.add(cur.getString("description"));
                JSONObject urls = cur.optJSONObject("urls");

                small_pic.add(urls.getString("thumb"));
                big_pic.add(urls.getString("full"));

            }
            buildRecycler(small_pic, big_pic, description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}


    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        if (searchString != null) {
            myActionMenuItem.expandActionView();
            searchView.setQuery(searchString, true);
            Log.d("SEARCH INPUT FOR NOW IS", searchString);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                makeSearchQuery(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

}
