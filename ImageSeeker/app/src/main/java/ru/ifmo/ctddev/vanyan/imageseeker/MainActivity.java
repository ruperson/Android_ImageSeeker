package  ru.ifmo.ctddev.vanyan.imageseeker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import ru.ifmo.ctddev.vanyan.imageseeker.utilities.DbHelper;
import ru.ifmo.ctddev.vanyan.imageseeker.utilities.DeserializeDataJson;
import ru.ifmo.ctddev.vanyan.imageseeker.utilities.UnsplashApi;

import ru.ifmo.ctddev.vanyan.imageseeker.utilities.Contract.Entry;


import java.util.ArrayList;
import java.util.List;

import com.squareup.moshi.Moshi;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements  GreenAdapter.ListItemClickListener {
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String SEARCH_KEY = "search_key";
    private GreenAdapter mAdapter;
    private RecyclerView mNumbersList;
    private SearchView  searchView;
    private String searchString;

    private final Handler handler = new Handler(Looper.getMainLooper());
    OkHttpClient client;
    Moshi moshi;
    Call<DeserializeDataJson> userCall;
    UnsplashApi api;
    private UnsplashApi createApi() {
        return new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(UnsplashApi.class);
    }

    private DbHelper mDbHelper;

    @Override
    public void onListItemClick(String small_pic, String big_pic, String descr, Boolean fromSearch) {
        Context context = MainActivity.this;
        Class destinationActivity = ChildActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(Intent.EXTRA_TEXT, big_pic);
        startActivity(intent);

        if (fromSearch) {
            insertPhoto(small_pic, big_pic, descr);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (client == null) {
            client = new OkHttpClient.Builder().build();
            moshi = new Moshi.Builder().build();
            api = createApi();
        }
        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_KEY);
        }


        mDbHelper = new DbHelper(this);

        displayDatabaseInfo();
    }
    private void displayDatabaseInfo() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Cursor cursor = db.rawQuery("SELECT * FROM " + Entry.TABLE_NAME, null);
        String[] projection = {
                Entry._ID,
                Entry.COLUMN_SMALL_PHOTO,
                Entry.COLUMN_BIG_PHOTO,
                Entry.COLUMN_DESCRIPTION};

        try (Cursor cursor = db.query(Entry.TABLE_NAME, projection,null,null,null,null,null)) {
            Toast.makeText(this, "You have " + cursor.getCount() + " photos in the database", Toast.LENGTH_SHORT).show();

            int nameColumnIndex = cursor.getColumnIndex(Entry.COLUMN_SMALL_PHOTO);
            int breedColumnIndex = cursor.getColumnIndex(Entry.COLUMN_BIG_PHOTO);
            int genderColumnIndex = cursor.getColumnIndex(Entry.COLUMN_DESCRIPTION);

            List<String> small_pic = new ArrayList<>();
            List<String> big_pic = new ArrayList<>();
            List<String> description = new ArrayList<>();

            while (cursor.moveToNext()) {
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                String currentGender = cursor.getString(genderColumnIndex);

                small_pic.add(currentName);
                big_pic.add(currentBreed);
                description.add(currentGender);
            }
            if (small_pic.size() > 0) {
                buildRecycler(small_pic, big_pic, description, false);
            }
        }
    }

    private void insertPhoto(String small_photo, String big_photo, String description) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Entry.COLUMN_SMALL_PHOTO, small_photo);
        values.put(Entry.COLUMN_BIG_PHOTO, big_photo);
        values.put(Entry.COLUMN_DESCRIPTION, description);

        long newRowId = db.insert(Entry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            Toast.makeText(this, "Error with saving photo", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Photo added to the database with the number: " + newRowId, Toast.LENGTH_SHORT).show();
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

    void buildRecycler(List<String> small_pics, List<String> big_pics, List<String> description, Boolean fromSearch) {
        mNumbersList = findViewById(R.id.rv_item);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersList.setLayoutManager(layoutManager);
        mNumbersList.setHasFixedSize(true);
        mAdapter = new GreenAdapter(small_pics, big_pics, description, fromSearch, this);
        mNumbersList.setAdapter(mAdapter);
    }


    private void makeSearchQuery(String query) {
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, query);



        if (userCall != null)
            userCall.cancel();
        userCall = api.getContributors(query);
        //As role model: https://github.com/Android-ITMO-2018/GHAPI/commit/e3c539f150ea7255da80a5d4e567271dcd68bb6e
        userCall.enqueue(new Callback<DeserializeDataJson>() {
            @Override
            @SneakyThrows
            public void onResponse(@NonNull Call<DeserializeDataJson> call, @NonNull Response<DeserializeDataJson> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    final ResponseBody errorBody = response.errorBody();
                    final String str = errorBody == null ? null : errorBody.string();
                    onFailure(call, new Throwable(str));
                    return;
                }
                val respond = response.body();
                handler.post(() -> {
                    List<String> small_pic = new ArrayList<>();
                    List<String> big_pic = new ArrayList<>();
                    List<String> description = new ArrayList<>();

                    for (DeserializeDataJson.Photo p : respond.results) {
                        small_pic.add(p.urls.thumb);
                        big_pic.add(p.urls.full);
                        description.add(p.description);
                    }
                    buildRecycler(small_pic, big_pic, description, true);

                });
            }

            @Override
            public void onFailure(@NonNull Call<DeserializeDataJson> call, @NonNull Throwable t) {
                handler.post(() -> {
                    Toast.makeText(
                            MainActivity.this,
                            t.getLocalizedMessage(),
                            Toast.LENGTH_LONG)
                            .show();
                });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        if (searchString != null && !TextUtils.isEmpty(searchString)) {
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userCall != null) userCall.cancel();
        Picasso.get().cancelTag(MainActivity.class);
    }
}
