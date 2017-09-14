package com.example.android.myvolleyapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements
        DevAdapter.DevAdapterOnClickHandler{
    // Create an instance of the Adapter variable called mAdapter
    private DevAdapter mAdapter;
    // Create a RecyclerView variable called mDevelopersList
    private RecyclerView mDevelopersList;
    // Displays when there is no data from gitHub API
    private TextView mErrorMessageDisplay;
    // It indicates to the user that we are loading data
    private ProgressBar mLoadingIndicator;
    // Create an empty ArrayList that Developer objects will be added to
    ArrayList<Developer> developers = new ArrayList<>();
    // Create the needed url
    private static String mUrl =
            "https://api.github.com/search/users?q=location:lagos+language:java&per_page=500";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Using findViewById, I get reference to the RecycleView from xml.
        mDevelopersList = (RecyclerView) findViewById(R.id.rv_dev_list);
        // This TextView is used to display errors and will be hidden if no errors
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_view);
        // A LinearLayoutManager variable called layoutManager is created.
        // It is linear.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
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
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        // Check network connectivity and take an action if positive or negative.
        if (checkNetworkConnectivity()) {
            jsonVolleyRequest(mUrl);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Update errorMessageView with no connection error message
            mErrorMessageDisplay.setText(R.string.no_internet_connection);
            // Show the errorMessageView
            showErrorMessage();
        }
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
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT,profile);
        startActivity(intentToStartDetailActivity);
    }

    /*
    * This method will make the error message visible and hide the github data
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
     * A method that prepares the volley request using the Singleton class created
     * @param url is the JSON url
     */
    public void jsonVolleyRequest(String url){
        // Show progress bar
        showLoadingIndicator();
        // Create the request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

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

                                Developer developer = new Developer(username,userUrl, imageUrl);

                                // Add the new object to the list of Developers
                                developers.add(developer);
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                        // Hide the progress bar and show the results
                        showDataView();
                        mAdapter.setData(developers);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If error results log the error and display an error message
                        mErrorMessageDisplay.setText(R.string.some_error);
                        showErrorMessage();
                        error.printStackTrace();
                    }
                });

    // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * A function that refreshes the page
     */
    public void Refresh() {
        jsonVolleyRequest(mUrl);
        showDataView();
    }

    /**
     * Methods that control the options menu
     * @param menu is the created menu
     * @return true for menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            Refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

