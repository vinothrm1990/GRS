package com.app.grs.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.adapter.ProductSliderAdapter;
import com.app.grs.adapter.ReviewAdapter;
import com.app.grs.fragment.FeaturedDetailsFragment;
import com.app.grs.fragment.SubProductFragment;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.bumptech.glide.Glide;
import com.libizo.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubFeaturedAllActivity extends AppCompatActivity {

    Date now;
    public RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private ArrayList<HashMap<String,String>> reviewList;
    LinearLayout empty_heart, filled_heart;
    String wish_flag, cart_flag;
    private CircleIndicator circleIndicator;
    private static ViewPager viewPager;
    String slider;
    private static int numofPage = 0;
    private ProductSliderAdapter productSliderAdapter;
    private MaterialRatingBar ratingBar;
    HashMap<String, String> data;
    RecyclerView.LayoutManager mLayoutManager;
    private TextView tvfeaname, tvfeaprice, tvfeadesc, tvfearate, tvtotalrating, tvnoreview;
    private Button btnrate, btncart, btnbuy;
    String proname = "",proid = "",rate;
    String custid = "";
    TextView textItemCount;
    int numItemCount;
    AlertDialog alertDialog;
    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data =  (HashMap<String, String>) getIntent().getExtras().get("data");
        proname = data.get("product");
        getSupportActionBar().setTitle(proname);
        setContentView(R.layout.activity_sub_featured_all);

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String timestamp = sdf.format(now);

        custid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(this, custid, timestamp).execute();
        numItemCount = Constants.pref.getInt("count", 0);
        setBadge();

        proid = data.get("id");
        new getFlag(this, proid,  custid).execute();

        tvfeaname = findViewById(R.id.subfea_name_tv);
        tvfeaprice = findViewById(R.id.subfea_price_tv);
        tvfeadesc = findViewById(R.id.subfea_desc_tv);
        btnrate = findViewById(R.id.btn_subfea_ratenow);
        btnbuy = findViewById(R.id.btn_subfea_buynow);
        btncart = findViewById(R.id.btn_subfea_addtocart);
        tvnoreview = findViewById(R.id.subfea_tv_no_review);
        empty_heart = findViewById(R.id.fea_det_unchecked_fav_layout);
        filled_heart = findViewById(R.id.fea_det_checked_fav_layout);
        ratingBar = findViewById(R.id.fea_product_rate);

        String proprice = data.get("price");
        String prodesc = data.get("pro_desc");

        tvfeaname.setText(proname);
        tvfeaprice.setText("₹\t" + proprice);
        tvfeadesc.setText(prodesc);

        viewPager = findViewById(R.id.subfea_pager);
        circleIndicator = findViewById(R.id.subfea_indicator);

        slider = data.get("image1");

        List<String> sliderlist= Arrays.asList(slider.split(","));
        numofPage = sliderlist.size();
        productSliderAdapter= new ProductSliderAdapter(SubFeaturedAllActivity.this,sliderlist);
        viewPager.setAdapter(productSliderAdapter);
        viewPager.setOffscreenPageLimit(3);
        circleIndicator.setViewPager(viewPager);

        reviewList = new ArrayList<HashMap<String, String>>();

        new fetchReview(this, proid).execute();

        recyclerView = findViewById(R.id.rv_subfea_rate);
        mLayoutManager = new LinearLayoutManager(SubFeaturedAllActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);

        btnbuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 1;
                new addtoCart(SubFeaturedAllActivity.this, proid, custid, flag, timestamp).execute();
                Constants.cart="1";
                new fetchCartCount(SubFeaturedAllActivity.this, custid, timestamp).execute();

                startActivity(new Intent(SubFeaturedAllActivity.this, MyCartActivity.class));
            }
        });
        btnrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rateDialog();
            }
        });
        btncart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Constants.cart.equals("0")){

                    int flag = 1;
                    new addtoCart(SubFeaturedAllActivity.this, proid, custid, flag, timestamp).execute();
                    Constants.cart="1";
                    btncart.setText("REMOVE FROM CART");
                    new fetchCartCount(SubFeaturedAllActivity.this, custid, timestamp).execute();

                }else if (Constants.cart.equals("1")){

                    int flag = 0;
                    new addtoCart(SubFeaturedAllActivity.this, proid, custid, flag, timestamp).execute();
                    Constants.cart="0";
                    btncart.setText("ADD TO CART");
                    new fetchCartCount(SubFeaturedAllActivity.this, custid, timestamp).execute();

                }
            }
        });
        empty_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 1;
                empty_heart.setVisibility(View.GONE);
                filled_heart.setVisibility(View.VISIBLE);
                new storeWishlist(SubFeaturedAllActivity.this, custid, proid, flag).execute();
            }
        });
        filled_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 0;
                empty_heart.setVisibility(View.VISIBLE);
                filled_heart.setVisibility(View.GONE);
                new storeWishlist(SubFeaturedAllActivity.this, custid, proid, flag).execute();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(SubFeaturedAllActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(SubFeaturedAllActivity.this);
    }

    private class storeWishlist extends AsyncTask<String, Integer, String> {

        private Context context;
        public String cusid, proid;
        int flagstatus;
        private String url = Constants.BASE_URL + Constants.ADD_WISHLIST;
        ProgressDialog progress;

        public storeWishlist(Context context, String cusid, String proid, int flag) {
            this.context = context;
            this.cusid = cusid;
            this.proid = proid;
            this.flagstatus = flag;
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
            RequestBody body = new FormBody.Builder()
                    .add(Constants.CUSTOMER_ID, cusid)
                    .add(Constants.PRODUCT_ID, proid)
                    .add(Constants.cartflag, String.valueOf(flagstatus))
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
                if (jonj.getString("status").equalsIgnoreCase(
                        "Inserted")) {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                } else if (jonj.getString("status").equalsIgnoreCase(
                        "Already")) {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class getFlag extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_FLAG;
        String proid, cusid;
        ProgressDialog progress;

        public getFlag(Context context, String proid, String cusid) {
            this.context = context;
            this.proid = proid;
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
                    .add("product_id", proid)
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
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {
                        String data = jonj.getString("data");
                        wish_flag = jonj.getString("wish_flag");
                        cart_flag = jonj.getString("cart_flag");
                        rate = jonj.getString("rating");
                        JSONArray array = new JSONArray(data);
                        JSONObject jcat = array.getJSONObject(0);

                        if (cart_flag.equalsIgnoreCase("1")) {
                            btncart.setText("REMOVE FROM CART");
                        } else {
                            btncart.setText("ADD TO CART");
                        }
                        if (wish_flag.equalsIgnoreCase("1")) {
                            empty_heart.setVisibility(View.GONE);
                            filled_heart.setVisibility(View.VISIBLE);
                        } else {
                            empty_heart.setVisibility(View.VISIBLE);
                            filled_heart.setVisibility(View.GONE);
                        }
                        if (rate.equalsIgnoreCase("null")) {
                            ratingBar.setRating(0);
                        } else {
                            ratingBar.setRating(Integer.parseInt(rate));
                        }
                    } else {
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void rateDialog() {

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final String timestamp = sdf.format(now);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SubFeaturedAllActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.rate_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Rate & Review :");

        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        Button btnlater = dialogView.findViewById(R.id.btn_later_ratedialog);
        Button btnsubmit = dialogView.findViewById(R.id.btn_submit_ratedialog);
        final CustomEditText etreview = dialogView.findViewById(R.id.rate_et_review);

        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);

        if (ratingBar != null && etreview != null){

            btnsubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Float star = ratingBar.getRating();
                    String rate = String.valueOf(Math.round(star));
                    String review = etreview.getText().toString().trim();
                    new postRating(SubFeaturedAllActivity.this, proid, custid, rate, review, timestamp).execute();
                    new fetchReview(SubFeaturedAllActivity.this, proid).execute();
                    new fetchFeaProducts(SubFeaturedAllActivity.this, proid, custid).execute();

                }
            });
        }else {
            btnsubmit.setEnabled(false);
        }

        btnlater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private class postRating extends AsyncTask<String, Integer, String> {

        Context context;
        String url = Constants.BASE_URL + Constants.RATING;
        String proid, cusid, rate, review;
        ProgressDialog progress;
        String timestamp;

        public postRating(Context context, String proid, String cusid, String rate, String review, String timestamp) {
            this.context = context;
            this.proid = proid;
            this.cusid = cusid;
            this.rate = rate;
            this.review = review;
            this.timestamp = timestamp;
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
            RequestBody body = new FormBody.Builder()
                    .add("customer_id", cusid)
                    .add("product_id", proid)
                    .add("rate", rate)
                    .add("review", review)
                    .add("timestamp", String.valueOf(timestamp))
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
                if (jonj.getString("status").equalsIgnoreCase(
                        "success")) {

                    alertDialog.dismiss();
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }else if (jonj.getString("status").equalsIgnoreCase(
                        "Already")){

                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchCartCount extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.CART_COUNT;
        String cusid, date;
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

                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        int count = Integer.parseInt(jonj.getString("count"));
                        Constants.editor.putInt("count", count);
                        Constants.editor.apply();
                        Constants.editor.commit();

                        numItemCount = Constants.pref.getInt("count", 0);
                        setBadge();

                    } else if (jonj.getString("status").equalsIgnoreCase(
                            "failed")){
                        int count = 0;
                        Constants.editor.putInt("count", count);
                        Constants.editor.apply();
                        Constants.editor.commit();

                        Constants.numItemCount = Constants.pref.getInt("count", 0);
                        setBadge();
                    }

                }else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
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

                Intent i = new Intent(SubFeaturedAllActivity.this, MyCartActivity.class);
                startActivity(i);
            }

        });
        return true;
    }

    private class fetchReview extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_REVIEW;
        String proid;
        ProgressDialog progress;
        HashMap<String,String> map;
        String name, review, rate, time;

        public fetchReview(Context context, String proid) {
            this.context = context;
            this.proid = proid;
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
                    .add("product_id", proid)
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
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        String data = jonj.getString("data");
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            map = new HashMap<String, String>();

                            name = jcat.getString("cus_name");
                            review = jcat.getString("review");
                            rate = jcat.getString("rating");
                            time = jcat.getString("date");

                            map.put("cus_name", name);
                            map.put("review", review);
                            map.put("rating", rate);
                            map.put("date", time);

                            reviewList.add(map);
                        }
                        reviewAdapter = new ReviewAdapter(SubFeaturedAllActivity.this, reviewList);
                        recyclerView.setAdapter(reviewAdapter);

                        btnrate.setEnabled(false);
                        btnrate.setText("RATED");

                        recyclerView.setVisibility(View.VISIBLE);
                        tvnoreview.setVisibility(View.GONE);

                    } else {

                        recyclerView.setVisibility(View.GONE);
                        tvnoreview.setVisibility(View.VISIBLE);
                    }
                }else {
                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class addtoCart extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.ADD_CART;
        String proid, cusid, date;
        int flag;
        ProgressDialog progress;

        public addtoCart(Context context, String proid, String cusid, int flag, String date) {
            this.context = context;
            this.proid = proid;
            this.cusid = cusid;
            this.flag = flag;
            this.date = date;
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
            RequestBody body = new FormBody.Builder()
                    .add(Constants.CUSTOMER_ID, cusid)
                    .add(Constants.PRODUCT_ID, proid)
                    .add(Constants.cartflag, String.valueOf(flag))
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
                    if (jonj.getString("status").equalsIgnoreCase(
                            "Inserted")) {

                        new fetchCartCount(context, cusid, date).execute();
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                    } else if (jonj.getString("status").equalsIgnoreCase(
                            "Already")) {
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        new fetchCartCount(context, cusid, date).execute();
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchFeaProducts extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_FEATURED_DETAILS;
        String proid, cusid;
        ProgressDialog progress;

        public fetchFeaProducts(Context context, String proid, String cusid) {
            this.context = context;
            this.proid = proid;
            this.cusid = cusid;
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
            RequestBody body = new FormBody.Builder()
                    .add("product_id", proid)
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
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        String data = jonj.getString("data");
                        wish_flag = jonj.getString("wish_flag");
                        cart_flag = jonj.getString("cart_flag");
                        rate = jonj.getString("rating");
                        JSONArray array = new JSONArray(data);
                        JSONObject jcat = array.getJSONObject(0);

                        if (cart_flag.equalsIgnoreCase("1")) {
                            btncart.setText("REMOVE FROM CART");
                        } else {
                            btncart.setText("ADD TO CART");
                        }
                        if (wish_flag.equalsIgnoreCase("1")) {
                            empty_heart.setVisibility(View.GONE);
                            filled_heart.setVisibility(View.VISIBLE);
                        } else {
                            empty_heart.setVisibility(View.VISIBLE);
                            filled_heart.setVisibility(View.GONE);
                        }if (rate.equalsIgnoreCase("null")){
                            ratingBar.setRating(0);
                        }else {
                            ratingBar.setRating(Integer.parseInt(rate));
                        }

                    } else {

                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

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

}
