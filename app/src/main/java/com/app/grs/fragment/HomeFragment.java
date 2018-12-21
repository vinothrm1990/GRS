package com.app.grs.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.app.grs.R;
import com.app.grs.activity.FeaturedAllActivity;
import com.app.grs.activity.HomeActivity;
import com.app.grs.activity.MyCartActivity;
import com.app.grs.adapter.DiscoverAdapter1;
import com.app.grs.adapter.DiscoverAdapter2;
import com.app.grs.adapter.DiscoverAdapter3;
import com.app.grs.adapter.FeaturedAdapter;
import com.app.grs.adapter.SliderAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.app.grs.pager.AutoScrollViewPager;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import me.relex.circleindicator.CircleIndicator;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    RecyclerView rv_featured, rv_discover1, rv_discover2, rv_discover3;
    ArrayList<String> bannerList = new ArrayList<>();
    ArrayList<HashMap<String,String>> discoverList1 = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> discoverList2= new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> discoverList3 = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> featuredList=new ArrayList<HashMap<String, String>>();
    FeaturedAdapter featuredAdapter;
    DiscoverAdapter1 discoverAdapter1;
    DiscoverAdapter2 discoverAdapter2;
    DiscoverAdapter3 discoverAdapter3;
    TextView textItemCount, featured_all;
    LinearLayoutManager linearLayoutManager1, linearLayoutManager2, linearLayoutManager3, featureLayoutManager;
    AutoScrollViewPager viewPager;
    PagerAdapter adapter;
    CircleIndicator indicator;
    SimpleDateFormat sdf;
    Date now;
    private SearchView searchView = null;
    private static boolean isSearchView = false;

    public HomeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("GRS");

        Constants.pref = getActivity().getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        viewPager = view.findViewById(R.id.pager);
        indicator = view.findViewById(R.id.indicator);

        now = new Date();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = sdf.format(now);
        String cusid = Constants.pref.getString("mobileno", "");

        new fetchCartCount(getActivity(), cusid, timestamp).execute();

        new fetchBanner(getActivity()).execute();

        linearLayoutManager1 = new GridLayoutManager(getActivity(),3);
        rv_discover1 = view.findViewById(R.id.rv_discover1);
        rv_discover1.setLayoutManager(linearLayoutManager1);
        new fetchDiscover1(getActivity()).execute();
        discoverAdapter1 = new DiscoverAdapter1(getActivity(), discoverList1);
        rv_discover1.setAdapter(discoverAdapter1);

        /*linearLayoutManager2 = new LinearLayoutManager(getActivity());
        linearLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_discover2 = view.findViewById(R.id.rv_discover2);
        rv_discover2.setLayoutManager(linearLayoutManager2);
        new fetchDiscover2(getActivity()).execute();
        discoverAdapter2 = new DiscoverAdapter2(getActivity(), discoverList2);
        rv_discover2.setAdapter(discoverAdapter2);

        linearLayoutManager3 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_discover3 = view.findViewById(R.id.rv_discover3);
        rv_discover3.setLayoutManager(linearLayoutManager3);
        new fetchDiscover3(getActivity()).execute();
        discoverAdapter3 = new DiscoverAdapter3(getActivity(), discoverList3);
        rv_discover3.setAdapter(discoverAdapter3);*/

        featureLayoutManager = new GridLayoutManager(getActivity(), 2);
        rv_featured = view.findViewById(R.id.rv_featured);
        rv_featured.setLayoutManager(featureLayoutManager);
        new fetchFeatured(getActivity()).execute();
        featuredAdapter = new FeaturedAdapter(getActivity(), featuredList);
        rv_featured.setAdapter(featuredAdapter);

        featured_all = view.findViewById(R.id.btn_featured_viewall);

        featured_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(), FeaturedAllActivity.class));
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
        GRS.registerReceiver(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(getActivity());
    }

   @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem1 = menu.findItem(R.id.action_cart);
        View cart = MenuItemCompat.getActionView(menuItem1);
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
                    Toast.makeText(context, "Something went Wrong", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchBanner extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.BANNER;
        ProgressDialog progress;
        String banid,banimgpath;

        public fetchBanner(Context context) {
            this.context = context;
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
            if (jsonData != null){

            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        String data = jonj.getString("data");
                        JSONArray array = new JSONArray(data);
                        bannerList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jcat = array.getJSONObject(i);
                            banimgpath = jcat.getString("image_url");

                           bannerList.add(banimgpath);
                        }
                        viewPager.startAutoScroll();
                        viewPager.setInterval(3000);
                        viewPager.setCycle(true);
                        viewPager.setStopScrollWhenTouch(true);

                        adapter = new SliderAdapter(getActivity(), bannerList);
                        viewPager.setAdapter(adapter);
                        indicator.setViewPager(viewPager);
                       // indicator.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out));

                    } else
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

            }
        }

    }

    private class fetchFeatured extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_FEATURED;
        ProgressDialog progress;
        HashMap<String,String> map;
        String feaid, feaname, feaimage, bid, bmobile, feaprice, feacprice, feadesc, feaslide, feasize , feacolor, feaqty;

        public fetchFeatured(Context context) {
            this.context = context;
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
            featuredList.clear();
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

                            feaid = jcat.getString("id");
                            feaname = jcat.getString("product");
                            feaimage = jcat.getString("image");
                            feaprice = jcat.getString("price");
                            feacprice = jcat.getString("cross_price");
                            feadesc = jcat.getString("pro_desc");
                            feaslide = jcat.getString("image1");
                            feasize = jcat.getString("size");
                            feacolor = jcat.getString("color");
                            feaqty = jcat.getString("qty");
                            bid = jcat.getString("b_id");
                            bmobile = jcat.getString("b_mobile");

                            map.put("id", feaid);
                            map.put("product", feaname);
                            map.put("image", feaimage);
                            map.put("price", feaprice);
                            map.put("pro_desc", feadesc);
                            map.put("image1", feaslide);
                            map.put("cross_price", feacprice);
                            map.put("size", feasize);
                            map.put("color", feacolor);
                            map.put("qty", feaqty);
                            map.put("b_id", bid);
                            map.put("b_mobile", bmobile);

                            featuredList.add(map);
                        }

                        featuredAdapter.notifyDataSetChanged();

                    } else
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class fetchDiscover1 extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_DISCOVER1;
        ProgressDialog progress;
        HashMap<String,String> map;
        String id, name, image_url, image_name;

        public fetchDiscover1(Context context) {
            this.context = context;
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
            discoverList1.clear();
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

                            id = jcat.getString("t_id");
                            name = jcat.getString("cat");
                            image_url = jcat.getString("cat_img_url");
                            image_name = jcat.getString("cat_img_name");

                            map.put("t_id", id);
                            map.put("cat", name);
                            map.put("cat_img_url", image_url);
                            map.put("cat_img_name", image_name);

                            discoverList1.add(map);
                        }
                        discoverAdapter1.notifyDataSetChanged();


                    } else
                        Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class fetchDiscover2 extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_DISCOVER2;
        ProgressDialog progress;
        HashMap<String,String> map;
        String id, name, image_url, image_name;

        public fetchDiscover2(Context context) {
            this.context = context;
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
            discoverList2.clear();
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

                            id = jcat.getString("t_id");
                            name = jcat.getString("cat");
                            image_url = jcat.getString("cat_img_url");
                            image_name = jcat.getString("cat_img_name");

                            map.put("t_id", id);
                            map.put("cat", name);
                            map.put("cat_img_url", image_url);
                            map.put("cat_img_name", image_name);

                            discoverList2.add(map);
                        }

                       discoverAdapter2.notifyDataSetChanged();

                    } else
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class fetchDiscover3 extends AsyncTask<String, Void, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_DISCOVER3;
        ProgressDialog progress;
        HashMap<String,String> map;
        String id, name, image_url, image_name;

        public fetchDiscover3(Context context) {
            this.context = context;
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
            discoverList3.clear();
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

                            id = jcat.getString("t_id");
                            name = jcat.getString("cat");
                            image_url = jcat.getString("cat_img_url");
                            image_name = jcat.getString("cat_img_name");

                            map.put("t_id", id);
                            map.put("cat", name);
                            map.put("cat_img_url", image_url);
                            map.put("cat_img_name", image_name);

                            discoverList3.add(map);
                        }

                        discoverAdapter3.notifyDataSetChanged();

                    } else
                        Toast.makeText(context, jonj.getString("data"), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
