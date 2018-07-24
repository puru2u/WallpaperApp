package com.georgcantor.wallpaperapp.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.georgcantor.wallpaperapp.R;
import com.georgcantor.wallpaperapp.model.Pic;
import com.georgcantor.wallpaperapp.network.ApiClient;
import com.georgcantor.wallpaperapp.network.ApiService;
import com.georgcantor.wallpaperapp.network.NetworkUtilities;
import com.georgcantor.wallpaperapp.network.interceptors.OfflineResponseCacheInterceptor;
import com.georgcantor.wallpaperapp.network.interceptors.ResponseCacheInterceptor;
import com.georgcantor.wallpaperapp.ui.adapter.WallpAdapter;
import com.georgcantor.wallpaperapp.ui.util.EndlessRecyclerViewScrollListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchActivity extends AppCompatActivity {

    public static final String FETCH_TYPE = "fetch_type";
    public WallpAdapter catAdapter;
    public RecyclerView recyclerView;
    public NetworkUtilities networkUtilities;
    private String type;
    public int columnNo;
    private Pic picResult = new Pic();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkUtilities = new NetworkUtilities(this);
        type = getIntent().getStringExtra(FETCH_TYPE);
        setContentView(R.layout.activity_fetch);
        Toolbar toolbar = findViewById(R.id.toolbar_fetch);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(type);
        loadNextDataFromApi(1);
        recyclerView = findViewById(R.id.fetchRecView);
        recyclerView.setHasFixedSize(true);

        checkScreenSize();
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columnNo, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        EndlessRecyclerViewScrollListener scrollListener_cat =
                new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        loadNextDataFromApi(page);
                    }
                };
        recyclerView.addOnScrollListener(scrollListener_cat);
        catAdapter = new WallpAdapter(this);
        recyclerView.setAdapter(catAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void loadNextDataFromApi(int index) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addNetworkInterceptor(new ResponseCacheInterceptor());
        httpClient.addInterceptor(new OfflineResponseCacheInterceptor());
        httpClient.cache(new Cache(new File(FetchActivity.this
                .getCacheDir(), "ResponsesCache"), 10 * 1024 * 1024));
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        ApiService client = ApiClient.getClient(httpClient).create(ApiService.class);
        Call<Pic> call;
        call = client.getSearchResults(type, index);
        call.enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(Call<Pic> call, Response<Pic> response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.d(getResources().getString(R.string.No_Success),
                                response.errorBody().string());
                    } else {
                        picResult = response.body();
                        if (picResult != null) {
                            catAdapter.setPicList(picResult);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                Toast.makeText(FetchActivity.this, getResources()
                        .getString(R.string.wrong_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkScreenSize() {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                columnNo = 4;
                break;
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
                columnNo = 3;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                columnNo = 3;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                columnNo = 2;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                columnNo = 2;
                break;
            default:
                columnNo = 2;
        }
    }
}