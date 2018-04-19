package com.example.android.myvolleyapp;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.myvolleyapp.data.UserContract.UserEntry;
import com.example.android.myvolleyapp.databinding.ActivityDetailBinding;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.android.myvolleyapp.MainActivity.LOG_TAG;

/**
 * A class that shows details of the particular GitHub user that is clicked
 */
public class DetailActivity extends AppCompatActivity {
    // Contains a link to user image url
    private String mImageUrl = "";
    // Contains the username
    private String userName = "";
    // Contains a link to user url
    private String mUserUrl = "";
    // Contains user's info
    private String mUserInfo = "";
    // Bind data to views
    ActivityDetailBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Get the imageLoader
        ImageLoader imageLoader = MySingleton.getInstance(this).getImageLoader();
        // Check if this activity was opened from the MainActivity
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            String[] profile = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            userName = profile[0];
            mUserUrl = profile[1];
            mImageUrl = profile[2];
            mUserInfo = profile[3];
        }
        // Check if this activity was opened from the SearchActivity
        else {
            Uri uri = intent.getData();
            String selection = UserEntry._ID + "=?";
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            String[] selectionArgs = {idStr};
            Cursor cursor = getContentResolver().query(uri, null, selection,
                    selectionArgs, null);
            assert cursor != null;
            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(UserEntry.COLUMN_USER_NAME);
                int imageUrlIndex = cursor.getColumnIndex(UserEntry.COLUMN_IMAGE_URL);
                int userUrlIndex = cursor.getColumnIndex(UserEntry.COLUMN_USER_URL);
                int userInfoIndex = cursor.getColumnIndex(UserEntry.COLUMN_USER_INFO);

                userName = cursor.getString(nameIndex);
                mImageUrl = cursor.getString(imageUrlIndex).trim();
                mUserUrl = cursor.getString(userUrlIndex).trim();
                mUserInfo = cursor.getString(userInfoIndex).trim();
            }
            cursor.close();
        }

        // Perform operation for landscape
        if (mBinding.repo != null) {
            if (checkNetworkConnectivity()) {
                // Load up the required variables and methods
                mBinding.username.setText(userName);
                mBinding.userImage.profileImage.setImageUrl(mImageUrl, imageLoader);
                processInfo();
            } else {
                mBinding.errorText.setText(R.string.no_internet_connection);
                mBinding.errorText.setVisibility(View.VISIBLE);
            }
        }
        // Perform operation for portrait
        if (mBinding.repo == null) {
            if(checkNetworkConnectivity()){
                portraitOperation(imageLoader);
            } else {
                showErrorView();
            }
        }
    }

    /**
     * This method loads views when in portrait mode
     * @param imageLoader returns the image loader for the user profile image
     */
    private void portraitOperation(ImageLoader imageLoader) {
        // Set the username text
        mBinding.username.setText(userName);
        // Set the userDetails text
        mBinding.userDetails.userUrl.setText(R.string.user_detail);
        mBinding.userImage.profileImage.setImageUrl(mImageUrl, imageLoader);
        // Make sure that profile image view is visible, while error view is invisible
        mBinding.userImage.profileImage.setVisibility(View.VISIBLE);
        mBinding.userImage.errorView.setVisibility(View.INVISIBLE);
    }

    /**
     * This method performs the json request and changes the array list loaded to it
     */
    private void processInfo() {
        JsonObjectRequest objectRequest = new JsonObjectRequest(mUserInfo, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mBinding.bio.setText(R.string.bio);
                            mBinding.bioText.setText(response.getString("bio"));
                            mBinding.repo.setText(getResources().
                                    getString(R.string.repo,response.getString("public_repos")));
                            mBinding.followers.setText(getResources().
                                    getString(R.string.followers,response.getString("followers")));
                            mBinding.following.setText(getResources().
                                    getString(R.string.following,response.getString("following")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG,"Unhandled JSON exception");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mBinding.errorText.setText(R.string.no_internet_connection);
                mBinding.errorText.setVisibility(View.VISIBLE);
                mBinding.scrollView.setVisibility(View.INVISIBLE);
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(objectRequest);
    }

    /**
     * This method is called when the User Details button is clicked. It will open
     * the site specified by the mUserUrl
     */
    public void onClickOpenWebAddress(View v) {
        Uri webPage = Uri.parse(mUserUrl);
        // Create an intent with action of ACTION_VIEW
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        // We perform a check on this implicit intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Uses the ShareCompat Intent Builder to create an intent we share
     * the intent is just regular text
     * @return the intent built
     */
    private Intent createShareIntent() {
        String shareText = "Check out this awesome developer " +
                "@<" + userName + ">, <" + mUserUrl + ">.";
        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(shareText)
                .getIntent();
    }

    /**
     * Creates an overflow menu
     * @param menu overflow menu created
     * @return a boolean true or false, if operation is successful or not.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareIntent());
        return true;
    }

    /**
     * This method sets the up button, so that when clicked,
     * it returns one back to the MainActivity.
     * @param item is the MenuItem
     * @return calls the same method on its super class
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the last activity
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A method that checks if there is network available for network operations
     * @return true or false depending on network state.
     */
    private Boolean checkNetworkConnectivity() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, return true, else return false
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Shows the error view
     */
    private void showErrorView() {
        // Hide the other views
        mBinding.userImage.profileImage.setVisibility(View.INVISIBLE);
        // Show the errorView
        mBinding.userImage.errorView.setVisibility(View.VISIBLE);
    }
}
