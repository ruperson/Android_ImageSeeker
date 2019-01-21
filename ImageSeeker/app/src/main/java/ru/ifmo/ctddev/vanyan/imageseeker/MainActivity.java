package  ru.ifmo.ctddev.vanyan.imageseeker;

import android.content.Context;
import android.content.Intent;
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
import ru.ifmo.ctddev.vanyan.imageseeker.utilities.DeserializeDataJson;
import ru.ifmo.ctddev.vanyan.imageseeker.utilities.UnsplashApi;

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
        if (client == null) {
            client = new OkHttpClient.Builder().build();
            moshi = new Moshi.Builder().build();
            api = createApi();
        }
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
                    buildRecycler(small_pic, big_pic, description);

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
