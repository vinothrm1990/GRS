package com.app.grs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.adapter.ProductAdapter;
import com.app.grs.adapter.WishlistAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.app.grs.helper.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyWishListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WishlistAdapter wishlistAdapter;
    private ArrayList<HashMap<String,String>> wishlistList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    TextView textItemCount;
    int numItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Wishlists");
        setContentView(R.layout.activity_my_wish_list);

        String cusid = Constants.pref.getString("mobileno", "");
        recyclerView = findViewById(R.id.rv_wishlist);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        new fetchCartCount(this, cusid).execute();
        numItemCount = Constants.pref.getInt("count", 0);
        wishlistList.clear();
        new fetchWishlist(MyWishListActivity.this, cusid).execute();

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

                Intent i = new Intent(MyWishListActivity.this, MyCartActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        setBadge();
        GRS.registerReceiver(MyWishListActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(MyWishListActivity.this);
    }

    private class fetchCartCount extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.CART_COUNT;
        String  cusid;
        ProgressDialog progress;

        public fetchCartCount(Context context, String cusid) {
            this.context = context;
            this.cusid = cusid;
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
                jonj = new JSONObject(jsonData);
                int count = Integer.parseInt(jonj.getString("count"));
                if (jonj.getString("status").equalsIgnoreCase(
                        "success")) {

                    Constants.editor.putInt("count", count);
                    Constants.editor.apply();
                    Constants.editor.commit();


                }else  Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchWishlist extends AsyncTask<String, Integer, String> {

        private Context context;
        private String cusid, proid, proname, proimage, proprice, prodesc, flag, slider;
        private String url = Constants.BASE_URL + Constants.GET_WISHLIST;
        ProgressDialog progress;
        HashMap<String,String> map;

        public fetchWishlist(Context context, String cusid) {
            this.context = context;
            this.cusid = cusid;
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
            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                jonj = new JSONObject(jsonData);
                if (jonj.getString("status").equalsIgnoreCase(
                        "success")) {

                    String data = jonj.getString("data");
                    JSONArray array = new JSONArray(data);
                    for(int i=0;i<array.length();i++) {
                        JSONObject jcat = array.getJSONObject(i);
                        map=new HashMap<String, String>();

                        proid=jcat.getString("id");
                        proimage=jcat.getString("image");
                        proname=jcat.getString("product");
                        proprice=jcat.getString("price");
                        prodesc=jcat.getString("pro_desc");
                        flag=jcat.getString("wish_flag");
                        slider=jcat.getString("image1");

                        map.put("id",proid);
                        map.put("image",proimage);
                        map.put("product",proname);
                        map.put("price",proprice);
                        map.put("pro_desc", prodesc);
                        map.put("wish_flag",flag);
                        map.put("image1",slider);
                        wishlistList.add(map);

                    }

                    wishlistAdapter = new WishlistAdapter(MyWishListActivity.this,wishlistList);
                    recyclerView.setAdapter(wishlistAdapter);

                }else
                {
                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
