package com.app.grs.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.activity.MyCartActivity;
import com.app.grs.adapter.ProductSliderAdapter;
import com.app.grs.adapter.ReviewAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.libizo.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ir.apend.slider.model.Slide;
import ir.apend.slider.ui.Slider;
import me.relex.circleindicator.CircleIndicator;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeaturedDetailsFragment extends Fragment {

    public Button btncart, btnbuy, btnrate;
    LinearLayout empty_heart, filled_heart;
    private ImageView imageView;
    private TextView tvfeaname, tvfeaprice, tvfeadesc, tvfeacprice, tvDealer;
    String proid = "", proname = "",proqty="",did="", dmobile="", proimage = "", prodesc = "",proprice = "", procprice="", prosize="", procolor="";
    TextView textItemCount, tvnoreview, tvSize, tvColor, tvStockOut, tvStockIn;
    int numItemCount;
    AlertDialog alertDialog;
    private MaterialRatingBar ratingBar;
    String custid = "";
    public RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    String wish_flag, cart_flag, rate;
    private ArrayList<HashMap<String,String>> reviewList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    SimpleDateFormat sdf;
    Date now;
    private CircleIndicator circleIndicator;
    private static ViewPager viewPager;
    private static int numofPage = 0;
    private ProductSliderAdapter productSliderAdapter;
    LinearLayout noImage, yesImage, buttonLayout;
    Bundle bundle;

    public FeaturedDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_featured_details, container, false);

        bundle = this.getArguments();
        if (bundle != null){
            proname = getArguments().getString("proname");
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(proname);
        }else {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Featured Product");
        }

        Constants.pref = getActivity().getSharedPreferences("GRS",Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        custid = Constants.pref.getString("mobileno", "");

        if (bundle != null){
            proid = getArguments().getString("proid");
            proimage = getArguments().getString("prosilde");
            prodesc = getArguments().getString("prodesc");
            proprice = getArguments().getString("proprice");
            procprice = getArguments().getString("procprice");
            prosize = getArguments().getString("prosize");
            procolor = getArguments().getString("procolor");
            proqty = getArguments().getString("proqty");
            did = getArguments().getString("did");
            dmobile = getArguments().getString("dmobile");

        }else {
            proid = "";
            proimage = "";
            prodesc = "";
            proprice = "";
            procprice = "";
            prosize = "";
            procolor = "";
            proqty = "";
        }


        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        new fetchCartCount(getActivity(), custid, timestamp).execute();
        new getFlag(getActivity(), proid,  custid).execute();

        btncart = view.findViewById(R.id.featured_btn_addtocart);
        btnbuy = view.findViewById(R.id.featured_btn_buynow);
        imageView = view.findViewById(R.id.featuredimage_iv);
        tvfeaname = view.findViewById(R.id.featuredname_tv);
        tvfeaprice = view.findViewById(R.id.featuredprice_tv);
        tvfeacprice = view.findViewById(R.id.featuredcprice_tv);
        tvfeadesc = view.findViewById(R.id.featureddesc_tv);
        btnrate = view.findViewById(R.id.featured_ratenow_btn);
        tvnoreview = view.findViewById(R.id.featured_tv_no_review);
        empty_heart = view.findViewById(R.id.fea_unchecked_fav_layout);
        filled_heart = view.findViewById(R.id.fea_checked_fav_layout);
        ratingBar = view.findViewById(R.id.feature_product_rate);
        noImage = view.findViewById(R.id.noimage_layout);
        yesImage = view.findViewById(R.id.proimage_layout);
        tvSize = view.findViewById(R.id.featuredsize_tv);
        tvColor = view.findViewById(R.id.featuredcolor_tv);
        tvStockIn = view.findViewById(R.id.fea_tv_stockin);
        tvStockOut = view.findViewById(R.id.fea_tv_stockout);
        buttonLayout = view.findViewById(R.id.button_layout);
        tvDealer = view.findViewById(R.id.featureddealer_tv);

        if (prosize.isEmpty()){
            tvSize.setVisibility(View.GONE);
        }else {
            tvSize.setVisibility(View.VISIBLE);
        }

        if (!proqty.equals("0") && !proqty.isEmpty()){
            tvStockIn.setVisibility(View.VISIBLE);
            tvStockOut.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);
        }else {
            tvStockIn.setVisibility(View.GONE);
            tvStockOut.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
        }

        tvfeaname.setText(proname);
        tvfeaname.setSelected(true);
        tvDealer.setText(did);
        tvfeaprice.setText("₹" + proprice);
        tvfeacprice.setText("₹" + procprice);
        tvfeacprice.setPaintFlags(tvfeacprice.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        tvfeadesc.setText(prodesc);
        tvSize.setText(prosize);
        tvColor.setText(procolor);
        tvSize.setSelected(true);
        tvColor.setSelected(true);

        if (prosize.isEmpty()){
            tvSize.setVisibility(View.GONE);
        }

        viewPager = view.findViewById(R.id.featured_pager);
        circleIndicator = view.findViewById(R.id.featured_indicator);

        if (!proimage.isEmpty()){
            yesImage.setVisibility(View.VISIBLE);
            noImage.setVisibility(View.GONE);
            String [] list = proimage.split(",");
            List<String> sepList = Arrays.asList(list);
            ArrayList<String> proList = new ArrayList<String>(sepList);
            numofPage = proList.size();
            productSliderAdapter= new ProductSliderAdapter(getActivity(),proList);
            viewPager.setAdapter(productSliderAdapter);
            viewPager.setOffscreenPageLimit(numofPage);
            circleIndicator.setViewPager(viewPager);

        }else {
            yesImage.setVisibility(View.GONE);
            noImage.setVisibility(View.VISIBLE);
        }


        new fetchReview(getActivity(), proid).execute();

        recyclerView = view.findViewById(R.id.rv_featured_details);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);


        btnbuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                now = new Date();
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                String timestamp = sdf.format(now);

                int flag = 1;
                Constants.cart="1";
                new addtoCart(getActivity(), proid, custid, flag, timestamp, did, dmobile).execute();

                //new fetchCartCount(getActivity(), custid, timestamp).execute();
                startActivity(new Intent(getActivity(), MyCartActivity.class));

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

                now = new Date();
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                String timestamp = sdf.format(now);

                if (Constants.cart.equals("0")){

                    int flag = 1;
                    new addtoCart(getActivity(), proid, custid, flag, timestamp, did, dmobile).execute();
                    Constants.cart="1";
                    btncart.setText("REMOVE FROM CART");
                    new fetchCartCount(getActivity(), custid, timestamp).execute();

                }else if (Constants.cart.equals("1")){

                    int flag = 0;
                    new addtoCart(getActivity(), proid, custid, flag, timestamp, did, dmobile).execute();
                    Constants.cart="0";
                    btncart.setText("ADD TO CART");
                    new fetchCartCount(getActivity(), custid, timestamp).execute();

                }
            }
        });
        empty_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 1;
                empty_heart.setVisibility(View.GONE);
                filled_heart.setVisibility(View.VISIBLE);
                new storeWishlist(getActivity(), custid, proid, flag).execute();
            }
        });
        filled_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 0;
                empty_heart.setVisibility(View.VISIBLE);
                filled_heart.setVisibility(View.GONE);
                new storeWishlist(getActivity(), custid, proid, flag).execute();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        String cusid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(getActivity(), cusid, timestamp).execute();
        new getFlag(getActivity(), proid,  custid).execute();
        GRS.registerReceiver(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        // For Internet disconnect checking
        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        String cusid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(getActivity(), cusid, timestamp).execute();
        GRS.unregisterReceiver(getActivity());
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

    private void rateDialog() {

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final String timestamp = sdf.format(now);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
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
                    new postRating(getActivity(), proid, custid, rate, review, timestamp).execute();
                    new fetchReview(getActivity(), proid).execute();
                    new getFlag(getActivity(), proid,  custid).execute();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.action_cart);
        View cart = MenuItemCompat.getActionView(menuItem);
        textItemCount = cart.findViewById(R.id.cart_badge);
        setBadge();

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), MyCartActivity.class);
                startActivity(i);
            }

        });
    }

    private void setBadge() {

        if (textItemCount != null) {
            if (Constants.numItemCount == 0) {
                if (textItemCount.getVisibility() != View.GONE) {
                    textItemCount.setVisibility(View.GONE);
                }
            } else {
                textItemCount.setText(String.valueOf(Math.min(Constants.numItemCount, 99)));
                if (textItemCount.getVisibility() != View.VISIBLE) {
                    textItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class addtoCart extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.ADD_CART;
        String proid, cusid, date;
        int flag;
        ProgressDialog progress;

        public addtoCart(Context context, String proid, String cusid, int flag, String date, String id, String mobile) {
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
                jonj = new JSONObject(jsonData);
                if (jonj.getString("status").equalsIgnoreCase(
                        "Inserted")) {

                    new fetchCartCount(context, cusid, date).execute();
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }else if (jonj.getString("status").equalsIgnoreCase(
                        "Already"))
                {
                    btncart.setText("REMOVE FROM CART");
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
                else {

                    new fetchCartCount(context, cusid, date).execute();
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class postRating extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.RATING;
        String proid, cusid, rate, review;
        String timestamp;
        ProgressDialog progress;

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
                    .add("timestamp", timestamp)
                    .add("did", did)
                    .add("dmobile", dmobile)
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

    private class fetchCartCount extends AsyncTask<String, Integer, String> {

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

                        Constants.numItemCount = Constants.pref.getInt("count", 0);
                        setBadge();

                    } else if (jonj.getString("status").equalsIgnoreCase(
                            "failed")) {

                        int count = 0;
                        Constants.editor.putInt("count", count);
                        Constants.editor.apply();
                        Constants.editor.commit();

                        Constants.numItemCount = Constants.pref.getInt("count", 0);
                        setBadge();
                        //Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchReview extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_REVIEW;
        String proid;
        ProgressDialog progress;
        HashMap<String,String> map;
        String name, time, review, rate;

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

                        reviewAdapter = new ReviewAdapter(getActivity(), reviewList);
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
                            Constants.cart="1";
                            btncart.setText("REMOVE FROM CART");
                        } else {
                            Constants.cart="0";
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

}
