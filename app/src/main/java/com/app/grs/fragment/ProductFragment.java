package com.app.grs.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.activity.MyCartActivity;
import com.app.grs.adapter.CategoryAdapter;
import com.app.grs.adapter.ProductAdapter;
import com.app.grs.adapter.SubCategoryAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;

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

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class  ProductFragment extends Fragment {

    public RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<HashMap<String,String>> productList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    String subcatname = Constants.subcategoryname;
    TextView textItemCount;
    ImageView error, empty;
    SimpleDateFormat sdf;
    Date now;

    public ProductFragment() {
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
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(subcatname);

        Constants.pref = getActivity().getSharedPreferences("GRS",Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        productList.clear();
        String subcatname = Constants.subcategoryname;

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        String cusid = Constants.pref.getString("mobileno", "");

        new fetchCartCount(getActivity(), cusid, timestamp).execute();

        new fetchProduct(getActivity(), subcatname).execute();

        error = view.findViewById(R.id.error);
        empty = view.findViewById(R.id.empty);
        recyclerView = view.findViewById(R.id.rv_product);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        String cusid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(getActivity(), cusid, timestamp).execute();
        GRS.registerReceiver(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(getActivity());
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

    private class fetchProduct extends AsyncTask<String, Void, String> {

        Context context;
        String url = Constants.BASE_URL + Constants.PRODUCT;
        String subcatname;
        ProgressDialog progress;
        HashMap<String,String> map;
        String proid, proimg,proimgslider, proname, proprice, prodesc, bid, bmobile, rating, procprice, prosize, procolor, proqty;

        public fetchProduct(Context context, String subcatname) {
            this.context = context;
            this.subcatname = subcatname;
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
                    .add("subcatname", subcatname)
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

                        final String data = jonj.getString("data");
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            map = new HashMap<String, String>();

                            proid = jcat.getString("id");
                            proimg = jcat.getString("image");
                            proimgslider = jcat.getString("image1");
                            proname = jcat.getString("product");
                            proprice = jcat.getString("price");
                            prodesc = jcat.getString("pro_desc");
                            procprice = jcat.getString("cross_price");
                            prosize = jcat.getString("size");
                            procolor = jcat.getString("color");
                            proqty = jcat.getString("qty");
                            bid = jcat.getString("b_id");
                            bmobile = jcat.getString("b_mobile");
                           /* rating = jcat.getString("product_rating");*/

                            map.put("id", proid);
                            map.put("image", proimg);
                            map.put("image1", proimgslider);
                            map.put("product", proname);
                            map.put("price", proprice);
                            map.put("pro_desc", prodesc);
                            map.put("cross_price", procprice);
                            map.put("size", prosize);
                            map.put("color", procolor);
                            map.put("qty", proqty);
                            map.put("b_id", bid);
                            map.put("b_mobile", bmobile);
                           /* map.put("product_rating", rating);*/

                            productList.add(map);
                        }
                        productAdapter = new ProductAdapter(getActivity(), productList);
                        recyclerView.setAdapter(productAdapter);

                        recyclerView.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);

                    }else if (jonj.getString("status").equalsIgnoreCase(
                            "empty")){
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);
                    }
                    else if (jonj.getString("status").equalsIgnoreCase(
                            "failed")){

                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.GONE);

                    }
                }
                else {

                    recyclerView.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
