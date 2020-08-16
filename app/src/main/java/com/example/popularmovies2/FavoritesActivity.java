package com.example.popularmovies2;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.example.popularmovies2.data.FavoritesContract;

public class FavoritesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FavoritesActivity.class.getSimpleName();
    private static final int FAVORITE_LOADER_ID = 0;
    private FavoritesCursorAdapter mAdapter;
    RecyclerView mRecyclerView;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_favorites);

        int mNoOfColumns = MainActivity.calculateNoOfColumns(getApplicationContext());

        GridLayoutManager layoutManager = new GridLayoutManager(this, mNoOfColumns);

        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new FavoritesCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mFavData = null;

            @Override
            protected void onStartLoading() {
                if (mFavData != null){
                    deliverResult(mFavData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try{
                    return getContentResolver().query(FavoritesContract.FavoritesAdd.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                } catch (Exception e){
                    Log.e(TAG, "failed to load fav data asyncly");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mFavData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    //https://stackoverflow.com/questions/28236390/recyclerview-store-restore-state-between-activities
    @Override
    protected void onPause() {
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }
}