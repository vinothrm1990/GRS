package com.app.grs.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.grs.R;
import com.app.grs.activity.MyCartActivity;
import com.app.grs.activity.SingleWishlistActivity;
import com.app.grs.adapter.CategoryAdapter;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView error;
    private CategoryAdapter categoryAdapter;
    private ArrayList<HashMap<String,String>> categoryList=new ArrayList<HashMap<String, String>>();
    RecyclerView.LayoutManager mLayoutManager;
    TextView textItemCount;
    int numItemCount;
    SimpleDateFormat sdf;
    Date now;
    String cusid = "";

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_category, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Categories");

        Constants.pref = getActivity().getSharedPreferences("GRS",Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        categoryList.clear();
        cusid = Constants.pref.getString("mobileno", "");

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);

        new fetchCartCount(getActivity(), cusid, timestamp).execute();

        new fetchCategory(getActivity()).execute();

        error = view.findViewById(R.id.error);
        recyclerView = view.findViewById(R.id.rv_category);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
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

    @Override
    public void onResume() {
        super.onResume();
        // For Internet checking
        cusid = Constants.pref.getString("mobileno", "");
        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        new fetchCartCount(getActivity(), cusid, timestamp).execute();
        GRS.registerReceiver(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(getActivity());
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

    private class fetchCategory extends AsyncTask<String, Void, String> {

        Context context;
        String url = Constants.BASE_URL + Constants.CATEGORY;
        ProgressDialog progress;
        HashMap<String,String> map;
        String catid,catimgpath,catimgname,catname;

        public fetchCategory(Context context) {
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

                            catid = jcat.getString("t_id");
                            catimgpath = jcat.getString("cat_img_url");
                            catimgname = jcat.getString("cat_img_name");
                            catname = jcat.getString("cat");

                            map.put("t_id", catid);
                            map.put("cat_img_url", catimgpath);
                            map.put("cat_img_name", catimgname);
                            map.put("cat", catname);
                            categoryList.add(map);
                        }

                        categoryAdapter = new CategoryAdapter(getActivity(), categoryList);
                        recyclerView.setAdapter(categoryAdapter);

                        recyclerView.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);

                    } else
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
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
