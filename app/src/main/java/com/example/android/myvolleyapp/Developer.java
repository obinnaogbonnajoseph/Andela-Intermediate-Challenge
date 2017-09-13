package com.example.android.myvolleyapp;


/**
 * This creates an object that contains information related to a single developer
 * from gitHub API.
 */

class Developer {

    /** User's gitHub username */
    private String mDevName;

    /** User's profile url*/
    private String mProfileUrl;

    /** User's image url*/
    private String mImageUrl;

    /**
     * Constructs a new Developer object.
     *
     * @param devName is the User's gitHub username
     * @param profileUrl is the User's profile url
     *  @param imageUrl is the User's image url
     */
    Developer(String devName, String profileUrl, String imageUrl) {
        mDevName = devName;
        mProfileUrl = profileUrl;
        mImageUrl = imageUrl;
    }
    /**
     * Returns the gitHub username.
     */
    String getDevName() {
        return mDevName;
    }

    /**
     * Returns the User's profile url.
     */
    String getProfileUrl(){return mProfileUrl;}

    /**
     * Returns the User's image url.
     */
    String getImageUrl() {return mImageUrl;}

}
