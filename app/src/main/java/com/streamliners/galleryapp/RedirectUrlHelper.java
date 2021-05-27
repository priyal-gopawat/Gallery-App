package com.streamliners.galleryapp;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class RedirectUrlHelper extends AsyncTask<String , Void, String> {

    String redirectUrl;
    OnUrlFetched onUrlFetched;

    /**
     * parametrized constructor for RedirectUrlHelper
     * @param onUrlFetched
     */
    public RedirectUrlHelper(OnUrlFetched onUrlFetched) {
        this.onUrlFetched = onUrlFetched;
    }

    /**
     * get redirected url
     * @param strings
     * @return
     */
    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        URLConnection con = null;
        try {
            con = new URL( url ).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            con.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = null;
        try {
            is = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
      redirectUrl = con.getURL().toString();
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return redirectUrl;
    }

    @Override
    protected void onPostExecute(String s) {
        onUrlFetched.getUrl(redirectUrl);
    }

    interface OnUrlFetched{
        void getUrl(String URL);
    }

}
