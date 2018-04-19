package com.example.android.myvolleyapp;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.myvolleyapp.data.UserContract.UserEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity implements
        DevAdapter.DevAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener{

    // Create an instance of the Adapter variable called mAdapter
    private DevAdapter mAdapter;
    // Create a RecyclerView variable called mDevelopersList
    private RecyclerView mDevelopersList;
    // Displays when there is no data from gitHub API
    private TextView mErrorMessageDisplay;
    // It indicates to the user that we are loading data
    private ProgressBar mLoadingIndicator;
    // Swipe to refresh feature
    private SwipeRefreshLayout swipeRefresh;
    // SearchView
    SearchView searchView;
    // Previous and next buttons to load previous and next pages
    private Button nextButton, previousButton;
    // List of developers
    ArrayList<Developer> mDevelopers = new ArrayList<>();
    // Context
    private Context context = MainActivity.this;
    // String to report errors
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    // Add the pagination
    private int pageNumber = 1;
    // key to maintain page number across states
    private static final String SAVED_STATE = "saved-state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Using findViewById, I get reference to the RecycleView from xml.
        mDevelopersList = (RecyclerView) findViewById(R.id.rv_dev_list);
        // This TextView is used to display errors and will be hidden if no errors
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_view);
        // Set up the button widgets
        nextButton = (Button) findViewById(R.id.next_button);
        previousButton = (Button) findViewById(R.id.previous_button);

        // Set up swipe to refresh
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // A LinearLayoutManager variable called layoutManager is created.
        // It is linear.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        // setLayoutManager on mDevelopersList with the LinearLayoutManager
        // created above
        mDevelopersList.setLayoutManager(layoutManager);
        // setHasFixedSize(true) is used to designate that the contents of the
        // RecyclerView won't change an item's size
        mDevelopersList.setHasFixedSize(true);
        // The adapter is responsible for linking data with the views that
        // will display the gitHub data.
        mAdapter = new DevAdapter(this);
        // Set the DevAdapter created attaching it to mDevelopersList
        mDevelopersList.setAdapter(mAdapter);
         //The ProgressBar that will indicate to the user that we are loading data. It will be
         //hidden when no data is loading.
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        // Call the swipe to refresh method
        swipeAndRefresh();
        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);
        // Make sure that default values load correctly once app is installed at first
        PreferenceManager.setDefaultValues(this,R.xml.settings_main,false);

        // Check the saved state
        if (savedInstanceState != null) {
            pageNumber = savedInstanceState.getInt(SAVED_STATE);
        } else pageNumber = 1;

        // Set the buttons actions
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNumber < 4) {
                    pageNumber++;
                    mDevelopers.clear();
                    startNetworkTask(pageNumber);
                } else nextButton.setVisibility(View.INVISIBLE);
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (1 < pageNumber && pageNumber <= 4) {
                    pageNumber--;
                    mDevelopers.clear();
                    startNetworkTask(pageNumber);
                } else previousButton.setVisibility(View.INVISIBLE);
            }
        });

       startNetworkTask(pageNumber);
    }

    private void startNetworkTask(int page) {
        // Check network connectivity and take an action if positive or negative.
        if (checkNetworkConnectivity()) {
            // Show the loading indicator
            showLoadingIndicator();
            // Start the request
            jsonVolleyRequest(page);
        } else {
            // Update errorMessageView with no connection error message
            mErrorMessageDisplay.setText(R.string.no_internet_connection);
            // Show the errorMessageView
            showErrorMessage();
        }
    }

    /**
     * method that implements the swipe to refresh feature.
     */
    private void swipeAndRefresh() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
                swipeRefresh.setRefreshing(false);
            }
        });
    }


    private Boolean checkNetworkConnectivity() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, return true, else return false
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onClick(String[] profile) {
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(this, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT,profile);
        startActivity(intentToStartDetailActivity);
    }

    /*
    * This method will make the error message visible and hide the gitHub data
    * view.
    */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mDevelopersList.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        /* Also hide the progress bar */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * This method shows the progress bar while loading
     */
    private void showLoadingIndicator() {
        mDevelopersList.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Show the loaded data and hide other displays
     */
    private void showDataView() {
        /* First hide the error message and loading indicator if visible*/
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Then show the data */
        mDevelopersList.setVisibility(View.VISIBLE);
    }

    /**
     * A function that refreshes the page
     */
    public void Refresh() {
        // Clear the list
        mDevelopers.clear();
        // Restart the jsonRequest
        pageNumber = 1;
        nextButton.setVisibility(View.VISIBLE);
        previousButton.setVisibility(View.INVISIBLE);
        jsonVolleyRequest(pageNumber);
    }

    /**
     * A method that prepares the volley request using the Singleton class created
     */
    public void jsonVolleyRequest(int pageNumber) {
        // Take care of the buttons
        if (pageNumber != 4) {
            nextButton.setVisibility(View.VISIBLE);
        }
        if (pageNumber != 1) {
            previousButton.setVisibility(View.VISIBLE);
        } else previousButton.setVisibility(View.INVISIBLE);

        // First delete the database
        deleteFromDatabase();
        // Show loading indicator
        showLoadingIndicator();
        // Url
        String userQuery = handleUrl(String.valueOf(pageNumber));
        // Create first request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userQuery, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Try to parse the JSON response string. A try catch block is used
                        // to catch exceptions if any.
                        try {

                            // Extract the JSONArray associated with the key called "items"
                            // which represents a list of developers.
                            JSONArray developerArray = response.getJSONArray("items");

                            // For each developer in the array, create an object.
                            for (int i = 0; i < developerArray.length();i++) {

                                // Get a single developer at position i within the list
                                JSONObject currentDeveloper = developerArray.getJSONObject(i);

                                // Extract the necessary things needed
                                String username = currentDeveloper.getString("login");
                                String imageUrl = currentDeveloper.getString("avatar_url");
                                String userUrl = currentDeveloper.getString("html_url");
                                String userInfo = currentDeveloper.getString("url");
                                Developer developer = new Developer(username, userUrl, imageUrl,
                                        userInfo);

                                // Add the new object to the list of Developers
                                mDevelopers.add(developer);
                            }
                            addToDatabase(mDevelopers);
                            if (!mDevelopers.isEmpty()) {
                                mAdapter.setData(mDevelopers);
                                // Hide the progress bar and show the results
                                showDataView();
                            } else {
                                showNoUserExists();
                                nextButton.setVisibility(View.INVISIBLE);
                                previousButton.setVisibility(View.INVISIBLE);
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If error results log the error and display an error message
                        mErrorMessageDisplay.setText(R.string.some_error);
                        showErrorMessage();
                        error.printStackTrace();
                    }
                });
        // Access the RequestQueue(s) through your singleton class.
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void showNoUserExists() {
        /* First, hide the currently visible data */
        mDevelopersList.setVisibility(View.INVISIBLE);
        // Set the text of error Message
        mErrorMessageDisplay.setText(R.string.user_not_found);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        /* Also hide the progress bar */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void deleteFromDatabase() {
        getContentResolver().delete(UserEntry.CONTENT_URI,null,null);
    }
    
    // This method returns the values to be put into the database
    private void addToDatabase(ArrayList<Developer> developers){
        ContentValues values = new ContentValues();
        Log.i(LOG_TAG,"The total number of developers is " + String.valueOf(developers.size()));

        for (int i = 0; i < developers.size(); i++){
            String name = developers.get(i).getDevName().trim();
            String imageUrl = developers.get(i).getImageUrl();
            String userUrl = developers.get(i).getProfileUrl();
            String userInfo = developers.get(i).getmUserInfo();

            // if data does not exist, add to database
            if (!checkDataExists(name)){
                values.put(UserEntry.COLUMN_USER_NAME, name);
                values.put(UserEntry.COLUMN_IMAGE_URL, imageUrl);
                values.put(UserEntry.COLUMN_USER_URL, userUrl);
                values.put(UserEntry.COLUMN_USER_INFO, userInfo);
                getContentResolver().insert(UserEntry.CONTENT_URI, values);
            }
        }
    }

    // This method checks if a name already exists in the database or not.
    private boolean checkDataExists(String name){

        String[] projection = {UserEntry.COLUMN_USER_NAME};

        String selection = UserEntry.COLUMN_USER_NAME + " = ?";

        String[] args = {name};

        Cursor cursor = getContentResolver().query(UserEntry.CONTENT_URI,projection,
                selection, args, null);
        assert cursor != null;
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        } else {cursor.close();
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.refresh_page:
                Refresh();
                return true;
            case R.id.search_user:
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.search_user);
        searchView =
                (SearchView) MenuItemCompat.getActionView(item);
        ComponentName componentName = new ComponentName(context, SearchActivity.class);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(componentName));
        searchView.setSubmitButtonEnabled(true);
        searchView.clearFocus();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        searchView.clearFocus();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Refresh();
    }

    private String handleUrl(String pageNumber) {
        // Take care of the url
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String language = sharedPrefs.getString(
                getString(R.string.settings_language_key),
                getString(R.string.settings_language_default));

        String location = sharedPrefs.getString(
                getString(R.string.settings_location_key),
                getString(R.string.settings_location_default));

        String repos = sharedPrefs.getString(
                getString(R.string.settings_repos_key),
                getString(R.string.settings_repos_default));

        String followers = sharedPrefs.getString(
                getString(R.string.settings_followers_key),
                getString(R.string.settings_followers_default));

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https")
                .authority("api.github.com")
                .appendPath("search").appendPath("users")
                .appendQueryParameter("q", "location:"+location+"+language:"+language
                                        +"+repos:>="+repos+"+followers:>="+followers)
                .appendQueryParameter("per_page", "100")
                .appendQueryParameter("page", pageNumber);

        String userQuery = "";
        try {
            userQuery = new URL(URLDecoder.decode(uriBuilder.build().toString(), "UTF-8"))
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userQuery;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE,pageNumber);
    }
}