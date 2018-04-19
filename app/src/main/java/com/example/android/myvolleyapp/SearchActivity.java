package com.example.android.myvolleyapp;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.android.myvolleyapp.data.UserContract.UserEntry;
import java.util.ArrayList;
import java.util.List;
import static com.example.android.myvolleyapp.MainActivity.LOG_TAG;


public class SearchActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener {

    /** Adapter for the ListView */
    SearchCursorAdapter mCursorAdapter;

    // ListView
    ListView usersListView;

    // SearchView
    private SearchView searchView;

    // ErrorView
    TextView errorDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize the list view and errorView
        usersListView = (ListView) findViewById(R.id.list);
        errorDisplay = (TextView) findViewById(R.id.user_not_found);

        mCursorAdapter = new SearchCursorAdapter(this,null);

        usersListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);

                // Extract the username, imageUrl, userUrl and userInfo
                Uri currentDevUri = ContentUris.withAppendedId(UserEntry.CONTENT_URI, id);


                // Set the URI on the data field of the intent
                intent.setData(currentDevUri);

                // Launch the {@link DetailActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });
        handleIntent(getIntent());
    }

    // This method ensures that when a new intent is recognized, an action is carried out
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    // This method extracts the search query from the intent
    private void handleIntent(Intent intent){
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            getLoaderManager().initLoader(0,null,this);
        }
    }


    // Sets up an action when the search button is created and clicked.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchView.clearFocus();
        return true;
    }

    // Sets up actions when back button is pressed on the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }
        // When the home button is pressed, take the user back to the last activity
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    private ArrayList<String> doMySearch(String mQuery, Cursor cursor) {

        ArrayList<String> showCursor = new ArrayList<>();
        if (cursor == null) {
            Log.e(LOG_TAG, "Cursor is null");
        } else {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.
                            getString(cursor.getColumnIndex(UserEntry.COLUMN_USER_NAME)).trim();
                    if (name.toLowerCase().contains(mQuery)) {
                        // Add to array not to be displayed
                        showCursor.add(name);
                    }
                } while (cursor.moveToNext());
            }

        }
        return showCursor;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getIntent();
        Cursor cursor = getContentResolver().query(UserEntry.CONTENT_URI,null,null,null,null);
        String query = intent.getStringExtra(SearchManager.QUERY).trim().toLowerCase();
        ArrayList<String> showCursor = doMySearch(query,cursor);
        if (showCursor.size() < 1) {
            showErrorView();
            return null;
        } else {
            String[] selectionArgs = showCursor.toArray(new String[showCursor.size()]);
            String selection = UserEntry.COLUMN_USER_NAME + " IN ("
                    + makePlaceholders(selectionArgs.length) + ")";
            return new CursorLoader(this,UserEntry.CONTENT_URI,null,selection,selectionArgs,null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader != null) {
            mCursorAdapter.swapCursor(data);
            showListView();
        } else {showErrorView();}
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Cursor cursor = getContentResolver().query(UserEntry.CONTENT_URI,null,null,null,null);
        List<String> newList = doMySearch(query.toLowerCase().trim(),cursor);
        if (newList.size() < 1) {
            showErrorView();
        } else {
            String[] selectionArgs = newList.toArray(new String[newList.size()]);
            String selection = UserEntry.COLUMN_USER_NAME + " IN ("
                    + makePlaceholders(selectionArgs.length) + ")";
            Cursor resultCursor = getContentResolver().
                    query(UserEntry.CONTENT_URI,null,selection,selectionArgs,null);
            mCursorAdapter.swapCursor(resultCursor);
            showListView();
        }
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void showListView(){
        errorDisplay.setVisibility(View.INVISIBLE);
        usersListView.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        usersListView.setVisibility(View.INVISIBLE);
        errorDisplay.setText(R.string.user_not_found);
        errorDisplay.setVisibility(View.VISIBLE);
    }
}
