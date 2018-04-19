package com.example.android.myvolleyapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.myvolleyapp.data.UserContract.UserEntry;


/**
 * This is an adapter that adapts the card view to the
 * the recycler view
 */

class SearchCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link SearchCursorAdapter}.
     *
     * @param context The context
     * @param cursor       The cursor from which to get the data.
     */
    SearchCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView setName = (TextView) view.findViewById(R.id.username);
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(UserEntry.COLUMN_USER_NAME);

        // Read the pet attributes from the Cursor for the current pet
        String userName = cursor.getString(nameColumnIndex);
        setName.setText(userName);

    }
}
