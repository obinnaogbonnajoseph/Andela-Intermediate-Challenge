package com.example.android.myvolleyapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import java.util.ArrayList;



/**
 * This exposes a list of gitHub users data to a RecyclerView.
 */

class DevAdapter extends RecyclerView.Adapter<DevAdapter.DeveloperViewHolder> {

    private ArrayList<Developer> mDevData;

    /*
    * An on-click handler that we've defined to make it easy for an Activity to interface with
    * our RecyclerView
    */
    private final DevAdapterOnClickHandler mClickHandler;
    // Pass in a context
    private Context context;
    /**
     * The interface that receives onClick messages.
     */
    interface DevAdapterOnClickHandler {
        void onClick(String[] profile);
    }

    /**
     * Constructor for the adapter that accepts clickHandler.
     */
    DevAdapter(DevAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a list item.
     */
    class DeveloperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemDevView;
        NetworkImageView imageView;

        /*
        * Constructor for our ViewHolder. Within this constructor, we get a reference to our
        * Text and ImageViews.
        * @param itemView The View that was inflated previously.
        */
        DeveloperViewHolder(View itemView) {
            super(itemView);

            listItemDevView = (TextView) itemView.findViewById(R.id.tv_dev_list);
            imageView = (NetworkImageView) itemView.findViewById(R.id.iv_dev_list);
            itemView.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            // This gets the current adapterPosition
            int adapterPosition = getAdapterPosition();
            // The current item is gotten from the adapter
            Developer currentDeveloper = mDevData.get(adapterPosition);
            // We get the username, userUrl and imageUrl
            String username = currentDeveloper.getDevName();
            String userUrl = currentDeveloper.getProfileUrl();
            String imageUrl = currentDeveloper.getImageUrl();
            String userInfo = currentDeveloper.getmUserInfo();

            // String array to be passed as intent to the next activity.
            String[] profile = {username,userUrl,imageUrl,userInfo};
            mClickHandler.onClick(profile);
        }
    }

    /**
     * This get called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  Used when RecyclerView has more than one type of item.
     * @return 		   A new NumberViewHolder that holds the View for each list item.
     */
    @Override
    public DeveloperViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new DeveloperViewHolder(view);
    }
    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, contents of the ViewHolder is updated to display the correct
     * indices in the list for this particular position.
     *
     * @param holder 	The ViewHolder which should be updated to represent the contents of the
     * 					item at the given position in the data set.
     * @param position	The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(DeveloperViewHolder holder, int position) {
        Developer currentDeveloper = mDevData.get(position);
        ImageLoader mImageLoader = MySingleton.getInstance(context)
                .getImageLoader();
        String devName = currentDeveloper.getDevName();
        String imageUrl = currentDeveloper.getImageUrl();
        holder.listItemDevView.setText(devName);
        holder.imageView.setImageUrl(imageUrl, mImageLoader);
    }
    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available.
     */
    @Override
    public int getItemCount() {
        if (mDevData == null) return 0;
        return mDevData.size();
    }

    /**
     * This method is used to set the data on a DevAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new DevAdapter to display it.
     *
     * @param devData The new weather data to be displayed.
     */
    public void setData(ArrayList<Developer> devData) {
        mDevData = devData;
        notifyDataSetChanged();
    }
}
