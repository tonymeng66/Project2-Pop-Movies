package com.example.tony.popularmovie.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.example.tony.popularmovie.R;
import com.example.tony.popularmovie.data.MovieContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    private final Context mContext;
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours//
    //
    public static final int SYNC_INTERVAL = 60 * 180 * 4;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL;

    private static final String APIKEY ="";


    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        fetchMovieInfo("popularity.desc");
        fetchMovieInfo("vote_average.desc");
    }


    private void fetchMovieInfo(String sort_by){
        // Fetch data with API from TMdb
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie";


            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter("sort_by", sort_by)
                    .appendQueryParameter("api_key", APIKEY)
                    .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();
            getDiscoverInfoFromJSON(movieJsonStr,sort_by);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void fetchTrailerInfo(String movieId){ // Fetch data with API from TMdb
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String QUERY = "videos";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(QUERY)
                    .appendQueryParameter("api_key", APIKEY)
                    .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();
            getTrailerInfoFromJSON(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void fetchReviewInfo(String movieId){
        // Fetch data with API from TMdb
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String QUERY = "reviews";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(QUERY)
                    .appendQueryParameter("api_key", APIKEY)
                    .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();
            getReviewInfoFromJSON(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getDiscoverInfoFromJSON(String movieJsonStr,String sort_by) throws JSONException {

        final String ARRAY = "results";
        final String ID = "id";
        final String TITLE = "original_title";
        final String RELEASE_DATE = "release_date";
        final String MOVIE_POSTER = "poster_path";
        final String VOTE_AVERAGE= "vote_average";
        final String PLOT_SYNOPSIS = "overview";
        final String POPULARITY = "popularity";

        Vector<ContentValues> cVVector = new Vector<ContentValues>(20);

        try{
            JSONObject movieInfoJSON = new JSONObject(movieJsonStr);

            for(int i=0;i<20;i++) {
                ContentValues discoverValues = new ContentValues();

                //All tables shares the same column except for table name
                discoverValues.put(MovieContract.PopularEntry.COLUMN_MOVIE_ID,movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(ID));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_MOVIE_TITLE,movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(TITLE));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_RELEASE_DATE,movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(RELEASE_DATE));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_MOVIE_POSTER,movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(MOVIE_POSTER));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_VOTE_AVERAGE, movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(VOTE_AVERAGE));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_PLOT_SYNOPSYS, movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(PLOT_SYNOPSIS));
                discoverValues.put(MovieContract.PopularEntry.COLUMN_POPULARITY, movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(POPULARITY));

                Cursor cursor = mContext.getContentResolver().query(
                        (sort_by=="popularity.desc")? (MovieContract.PopularEntry.CONTENT_URI):(MovieContract.RatingEntry.CONTENT_URI),
                        null,
                        (sort_by=="popularity.desc")?(MovieContract.PopularEntry.TABLE_NAME+"."+ MovieContract.PopularEntry.COLUMN_MOVIE_POSTER + " = ? "):(MovieContract.RatingEntry.TABLE_NAME + "." + MovieContract.RatingEntry.COLUMN_MOVIE_POSTER + " = ? "),
                        new String[]{movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(MOVIE_POSTER)},
                        null);

                //save image when it's not already in the database.
                if(!cursor.moveToFirst())
                    cacheImageToLocal(discoverValues.getAsString(MovieContract.PopularEntry.COLUMN_MOVIE_POSTER));

                cursor.close();

                cVVector.add(discoverValues);

                fetchTrailerInfo(discoverValues.getAsString(MovieContract.PopularEntry.COLUMN_MOVIE_ID));
                fetchReviewInfo(discoverValues.getAsString(MovieContract.PopularEntry.COLUMN_MOVIE_ID));
            }
            int inserted = 0;
            int deleted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                if (sort_by == "popularity.desc") {
                    deleted = mContext.getContentResolver().delete(MovieContract.PopularEntry.CONTENT_URI, null, null);
                    inserted = mContext.getContentResolver().bulkInsert(MovieContract.PopularEntry.CONTENT_URI, cvArray);
                }
                if (sort_by == "vote_average.desc") {
                    deleted = mContext.getContentResolver().delete(MovieContract.RatingEntry.CONTENT_URI,null,null);
                    inserted = mContext.getContentResolver().bulkInsert(MovieContract.RatingEntry.CONTENT_URI, cvArray);
                }
            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + deleted + "Deleted " + inserted + " Inserted " + "sort_by = " + sort_by);

        }catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getTrailerInfoFromJSON(String movieJsonStr) throws JSONException {

        final String ARRAY = "results";
        final String MOVIE_ID = "id";
        final String KEY = "key";
        final String NAME = "name";
        final String SITE = "site";
        final String SIZE = "size";
        final String TYPE = "type";

        try{
            JSONObject movieInfoJSON = new JSONObject(movieJsonStr);

            int trailerCounts = movieInfoJSON.getJSONArray(ARRAY).length();
            if ( trailerCounts == 0 )
                return;

            for(int i=0;i<trailerCounts;i++) {
                ContentValues trailerValues = new ContentValues();

                String movieId = movieInfoJSON.getString(MOVIE_ID);

                String key = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(KEY);
                String name = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(NAME);
                String site = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(SITE);
                String size = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(SIZE);
                String type = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(TYPE);

                trailerValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_ID,movieId);
                trailerValues.put(MovieContract.VideoEntry.COLUMN_KEY,key);
                trailerValues.put(MovieContract.VideoEntry.COLUMN_NAME,name);
                trailerValues.put(MovieContract.VideoEntry.COLUMN_SITE,site);
                trailerValues.put(MovieContract.VideoEntry.COLUMN_SIZE,size);
                trailerValues.put(MovieContract.VideoEntry.COLUMN_TYPE, type);

                Uri uri = mContext.getContentResolver().insert(
                        MovieContract.VideoEntry.CONTENT_URI,
                        trailerValues
                );
            }

        }catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getReviewInfoFromJSON(String movieJsonStr) throws JSONException {

        final String ARRAY = "results";
        final String MOVIE_ID = "id";
        final String AUTHOR = "author";
        final String CONTENT = "content";
        final String TOTAL_RESULTS = "total_results";

        try{
            JSONObject movieInfoJSON = new JSONObject(movieJsonStr);

            int reviewCounts = movieInfoJSON.getJSONArray(ARRAY).length();
            if ( reviewCounts == 0 )
                return;

            for(int i=0;i<reviewCounts;i++) {
                ContentValues reviewValues = new ContentValues();

                String movieId = movieInfoJSON.getString(MOVIE_ID);
                String author = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(AUTHOR);
                String content = movieInfoJSON.getJSONArray(ARRAY).getJSONObject(i).getString(CONTENT);

                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID,movieId);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR,author);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT,content);


                Uri uri = mContext.getContentResolver().insert(
                        MovieContract.ReviewEntry.CONTENT_URI,
                        reviewValues
                );
            }

        }catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    private void cacheImageToLocal(final String posterPath){

        final String BASE_POSTER_PATH = "http://image.tmdb.org/t/p/w185/";

        //First create a new URL object
        try {
            URL url = new URL(BASE_POSTER_PATH+posterPath);

            //Next create a file
            File file = new File(mContext.getExternalCacheDir(),posterPath);

            //Next create a Bitmap object and download the image to bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());

            //Finally compress the bitmap, saving to the file previously created
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}