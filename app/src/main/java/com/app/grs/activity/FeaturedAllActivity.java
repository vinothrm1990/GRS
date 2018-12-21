package com.app.grs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.adapter.AllFeaturedAdapter;
import com.app.grs.adapter.FeaturedAdapter;
import com.app.grs.adapter.WishlistAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeaturedAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AllFeaturedAdapter allFeaturedAdapter;
    private ArrayList<HashMap<String,String>> allFeaturedList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    TextView textItemCount;
    int numItemCount;
    SimpleDateFormat sdf;
    Date now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Featured Products");
        setContentView(R.layout.activity_featured_all);

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        String cusid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(this, cusid, timestamp).execute();

        numItemCount = Constants.pref.getInt("count", 0);

        recyclerView = findViewById(R.id.rv_all_featured);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        new fetchAllFeatured(FeaturedAllActivity.this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        MenuItem menuItem = menu.findItem(R.id.action_cart);
        View cart = MenuItemCompat.getActionView(menuItem);

        textItemCount = cart.findViewById(R.id.cart_badge);
        setBadge();

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(FeaturedAllActivity.this, MyCartActivity.class);
                startActivity(i);
            }

        });
        return true;
    }

    private void setBadge() {

        if (textItemCount != null) {
            if (numItemCount == 0) {
                if (textItemCount.getVisibility() != View.GONE) {
                    textItemCount.setVisibility(View.GONE);
                }
            } else {
                textItemCount.setText(String.valueOf(Math.min(numItemCount, 99)));
                if (textItemCount.getVisibility() != View.VISIBLE) {
                    textItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class fetchAllFeatured extends AsyncTask<String, Integer, String> {

        private Context context;
        private String url = Constants.BASE_URL + Constants.GET_ALL_FEATURED;
        ProgressDialog progress;
        HashMap<String,String> map;
        String proid, proname, proprice, prodesc, proimg, proslide, cprice, feasize , feacolor, feaqty, bid, bmobile;

        public fetchAllFeatured(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);

            try {
                response = call.execute();

                if (response.isSuccessful()) {
                    jsonData = response.body().string();
                } else {
                    jsonData = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);
            progress.dismiss();
            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        String data = jonj.getString("data");
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            map = new HashMap<String, String>();

                            proid = jcat.getString("id");
                            proimg = jcat.getString("image");
                            proname = jcat.getString("product");
                            proprice = jcat.getString("price");
                            cprice = jcat.getString("cross_price");
                            prodesc = jcat.getString("pro_desc");
                            proslide = jcat.getString("image1");
                            feasize = jcat.getString("size");
                            feacolor = jcat.getString("color");
                            feaqty = jcat.getString("qty");
                            bid = jcat.getString("b_id");
                            bmobile = jcat.getString("b_mobile");

                            map.put("id", proid);
                            map.put("image", proimg);
                            map.put("product", proname);
                            map.put("price", proprice);
                            map.put("cross_price", cprice);
                            map.put("pro_desc", prodesc);
                            map.put("image1", proslide);
                            map.put("size", feasize);
                            map.put("color", feacolor);
                            map.put("qty", feaqty);
                            map.put("b_id", bid);
                            map.put("b_mobile", bmobile);

                            allFeaturedList.add(map);

                        }

                        allFeaturedAdapter = new AllFeaturedAdapter(FeaturedAllActivity.this, allFeaturedList);
                        recyclerView.setAdapter(allFeaturedAdapter);


                    } else {
                        Toast.makeText(getApplicationContext(), jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class fetchCartCount extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.CART_COUNT;
        String  cusid, date;
        ProgressDialog progress;

        public fetchCartCount(Context context, String cusid, String date) {
            this.context = context;
            this.cusid = cusid;
            this.date = date;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("customer_id", cusid)
                    .add("date", date)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = client.newCall(request);

            try {
                response = call.execute();

                if (response.isSuccessful()) {
                    jsonData = response.body().string();
                } else {
                    jsonData = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);
            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    int count = Integer.parseInt(jonj.getString("count"));
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        Constants.editor.putInt("count", count);
                        Constants.editor.apply();
                        Constants.editor.commit();


                    } else if (jonj.getString("status").equalsIgnoreCase(
                            "failed"))
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GRS.freeMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(FeaturedAllActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(FeaturedAllActivity.this);
    }

}
