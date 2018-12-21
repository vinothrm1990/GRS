package com.app.grs.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.jsoup.Jsoup;

public class PlayStoreUpdate extends AsyncTask<String, Void, String> {

    String newVersion = "";
    String currentVersion = "";
    VersionListener versionListener;
    boolean isVersionAvailabel;
    boolean isAvailableInPlayStore;
    Context mContext;
    String mStringCheckUpdate = "";

    public PlayStoreUpdate(Context mContext, VersionListener versionListener) {
        this.versionListener = versionListener;
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            isAvailableInPlayStore = true;
            if (isNetworkAvailable(mContext)) {
                mStringCheckUpdate = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContext.getPackageName())
                        .timeout(10000)
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return mStringCheckUpdate;
            }

        } catch (Exception e) {
            isAvailableInPlayStore = false;
            return mStringCheckUpdate;
        } catch (Throwable e) {
            isAvailableInPlayStore = false;
            return mStringCheckUpdate;
        }
        return mStringCheckUpdate;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);

        if (isAvailableInPlayStore == true) {
            newVersion = string;
            Log.e("new Version", newVersion);
            checkCurrentVersion();
            if (currentVersion.equalsIgnoreCase(newVersion)) {
                isVersionAvailabel = false;
                Toast.makeText(mContext, "GRS is working under Updated Version", Toast.LENGTH_LONG).show();
            } else {
                isVersionAvailabel = true;
            }
            versionListener.onGetResponse(isVersionAvailabel);
        }
    }

    private void checkCurrentVersion() {

        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        currentVersion = packageInfo.versionName;
        Log.i("currentVersion", currentVersion);
    }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}

