package com.app.grs.activity;

import android.app.AlertDialog;
import com.app.grs.R;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.grs.adapter.SearchAdapter;
import com.app.grs.fragment.CategoryFragment;
import com.app.grs.fragment.HomeFragment;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.app.grs.helper.GetSet;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    TextView textItemCount, name;
    int numItemCount;
    private AlertDialog alertDialog;
    private BroadcastReceiver broadcastReceiver;
    private SearchView searchView = null;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, String>> searchList;
    RecyclerView.LayoutManager mLayoutManager;
    FrameLayout frameLayout;
    ImageView imageView;
    String cusid = "";
    String url="", userpicpath = "", username = "";
    CircleImageView circleImageView;
    private static boolean isSearchView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GRS");

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        cusid = Constants.pref.getString("mobileno", "");
        new fetchCartCount(HomeActivity.this, cusid).execute();
        new fetchProfileImage(HomeActivity.this).execute();
        numItemCount = Constants.pref.getInt("count", 0);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new HomeFragment());
        fragmentTransaction.addToBackStack(null).commit();
        frameLayout = findViewById(R.id.content_frame);
        recyclerView = findViewById(R.id.rv_search);
        imageView = findViewById(R.id.noitem);
        searchList = new ArrayList<HashMap<String, String>>();
        searchList.clear();
        mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        frameLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        new fetchProfile(HomeActivity.this, cusid).execute();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        circleImageView = headerLayout.findViewById(R.id.nav_iv);
        name= headerLayout.findViewById(R.id.nav_tv_name);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isSearchView){
            isSearchView = false;
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {

            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
            alertDialog.setTitle("Exit GRS");
            alertDialog.setMessage("Are you sure you want to Exit?");
            /*alertDialog.setIcon(R.drawable.exit);*/
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(@NonNull DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }
        else {
            super.onBackPressed();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        MenuItem menuItem1 = menu.findItem(R.id.action_cart);
        View cart = MenuItemCompat.getActionView(menuItem1);

        textItemCount = cart.findViewById(R.id.cart_badge);
        setBadge();

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(HomeActivity.this, MyCartActivity.class);
                startActivity(i);
            }

        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            isSearchView = true;
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (searchView != null) {
                searchView.clearFocus();
                searchList.clear();
            }
            new getSearch(HomeActivity.this, query).execute();
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
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home){

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new HomeFragment());
            fragmentTransaction.commit();
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_category){

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new CategoryFragment());
            fragmentTransaction.addToBackStack(null).commit();
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();
        }
        else if (id == R.id.action_order) {

            startActivity(new Intent(this, MyOrderActivity.class));
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_cart){


            startActivity(new Intent(this, MyCartActivity.class));
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_wishlist){

            startActivity(new Intent(this, MyWishListActivity.class));
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_account){

            startActivity(new Intent(this, MyAccountActivity.class));
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_contact){

            startActivity(new Intent(this, ContactActivity.class));
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();

        }else if (id == R.id.action_logout){

            logoutDialog();
            frameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchList.clear();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void logoutDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.logout_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Logout :");
        Button btnyes = dialogView.findViewById(R.id.btn_yes_logout);
        Button btnno = dialogView.findViewById(R.id.btn_no_logout);

        alertDialog = dialogBuilder.create();

        btnyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetSet.reset();
                finish();
                Intent p = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(p);
            }
        });
        btnno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
            }
        });

        alertDialog.show();

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

    @Override
    protected void onResume() {
        super.onResume();
        searchList.clear();
        GRS.registerReceiver(HomeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        GRS.unregisterReceiver(HomeActivity.this);
        super.onPause();
    }

    private class getSearch extends AsyncTask<String, Integer, String>{

        Context context;
        String url = Constants.BASE_URL + Constants.GET_SEARCH;
        ProgressDialog progress;
        HashMap<String,String> map;
        String searchQuery, id, name, rate, image;

        public getSearch(Context context, String searchQuery) {
            this.context = context;
            this.searchQuery = searchQuery;
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
                    .add("product_name", searchQuery)
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

                    String data = jonj.getString("data");
                    JSONArray array = new JSONArray(data);
                    for(int i=0;i<array.length();i++) {
                        JSONObject jcat = array.getJSONObject(i);
                        map = new HashMap<>();

                        id = jcat.getString("id");
                        name = jcat.getString("product");
                     /*   rate = jcat.getString("rating");*/
                        image = jcat.getString("image");

                        map.put("product", name);
                       /* map.put("rating", rate);*/
                        map.put("image", image);
                        map.put("id", id);

                        searchList.add(map);
                    }

                    searchAdapter = new SearchAdapter(HomeActivity.this, searchList);
                    recyclerView.setAdapter(searchAdapter);

                    frameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);

                }else if (jonj.getString("status").equalsIgnoreCase(
                        "empty")){
                    frameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
              }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchProfileImage extends AsyncTask<String,Integer,String>{

        private Context context;
        private String url = Constants.BASE_URL + Constants.GET_PROFILE;
        ProgressDialog progress;
        String picname, picpath;

        public fetchProfileImage(Context context) {
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
            RequestBody body = new FormBody.Builder()
                    .add(Constants.mobileno, cusid)
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
                    String data = jonj.getString("message");
                    JSONArray array = new JSONArray(data);
                    JSONObject object = array.getJSONObject(0);

                    GetSet.setIsLogged(true);
                    GetSet.setUserpic(object.getString("userpic"));
                    GetSet.setUserpicurl(object.getString("userpicurl"));

                    Constants.editor.putString("userpic", GetSet.getUserpic());
                    Constants.editor.putString("userpicurl", GetSet.getUserpicurl());
                    Constants.editor.commit();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class fetchProfile extends AsyncTask<String, Integer, String>{

        private Context context;
        private String url = Constants.BASE_URL + Constants.GET_PROFILE;
        ProgressDialog progress;
        String cusid;

        public fetchProfile(Context context, String cusid) {
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
                    .add(Constants.mobileno, cusid)
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
                    String data = jonj.getString("message");
                    JSONArray array = new JSONArray(data);
                    JSONObject object = array.getJSONObject(0);

                    username = object.getString("name");
                    userpicpath = object.getString("userimageurl");

                    if(userpicpath != null){
                        url = userpicpath;
                        Glide.with(HomeActivity.this)
                                .load(url)

                                .into(circleImageView);
                    }else {
                        Glide
                                .with(HomeActivity.this)
                                .load(R.drawable.grslogo)
                                .into(circleImageView);
                    }
                    name.setText(username);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
