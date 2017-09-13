package com.example.android.myvolleyapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

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
    // View to display profile image
    private NetworkImageView mImageView;
    // View to display error view
    private ImageView mErrorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Set up the Action bar for the home button
        ActionBar actionBar = this.getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ImageLoader imageLoader = MySingleton.getInstance(this).getImageLoader();
        // Displays the username of the user
        TextView mUserName = (TextView) findViewById(R.id.username);
        // Displays the profile picture of the user
        mImageView = (NetworkImageView) findViewById(R.id.profile_image);
        // Display the error image view
        mErrorView = (ImageView) findViewById(R.id.error_view);
        // Get the intent passed to this activity
        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            String[] profile = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            userName = profile[0];
            mUserUrl = profile[1];
            mImageUrl = profile[2];
        }
        // Set the view with its value.
        mUserName.setText(userName);
        // Set the image with its picture.
        if(checkNetworkConnectivity()) {
            mImageView.setImageUrl(mImageUrl, imageLoader);
        }
        else {
           showErrorView();
        }
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
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
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

    private void showErrorView() {
        // Hide the NetworkImageView
        mImageView.setVisibility(View.INVISIBLE);
        // Show the errorView
        mErrorView.setVisibility(View.VISIBLE);
    }
}
