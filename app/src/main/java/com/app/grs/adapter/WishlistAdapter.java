package com.app.grs.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.activity.HomeActivity;
import com.app.grs.activity.SingleWishlistActivity;
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

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

import static android.content.Context.MODE_PRIVATE;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String,String>> wishlistList;

    public WishlistAdapter(Context mContext, ArrayList<HashMap<String, String>> wishlistList) {
        this.mContext = mContext;
        this.wishlistList = wishlistList;
    }

    @NonNull
    @Override
    public WishlistAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wishlist_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistAdapter.MyViewHolder holder, final int position) {

        final HashMap<String,String> itemmap = wishlistList.get(position);

        holder.productName.setText(itemmap.get("product"));
        holder.productPrice.setText( "₹" +itemmap.get("price"));
        holder.pcprice.setText( "₹" +itemmap.get("cross_price"));
        holder.pcprice.setPaintFlags(holder.pcprice.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);


        Glide.with(mContext)
                .load(Constants.IMAGE_URL + itemmap.get("image"))
                .apply(RequestOptions.centerInsideTransform())
                .into(holder.productImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SingleWishlistActivity.class);
                intent.putExtra("data", itemmap);
                mContext.startActivity(intent);
            }
        });

        /*holder.wishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SingleWishlistActivity.class);
                intent.putExtra("data", itemmap);
                mContext.startActivity(intent);
            }
        });*/

        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(mContext);
                alertDialog.setTitle("Alert!");
                alertDialog.setMessage("Are you sure you want to remove from your Wishlist?");
                alertDialog.setIcon(R.drawable.ic_delete_black_24dp);
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Constants.pref = mContext.getSharedPreferences("GRS",MODE_PRIVATE);

                        String cusid = Constants.pref.getString("mobileno", "");
                        String proid = itemmap.get("id");

                        wishlistList.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();

                        int flag = 0;
                        new deleteWishlist(mContext, cusid, proid, flag).execute();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView productName, productPrice, pcprice;
        public ImageView productImage;
        public CardView cardView;
        public LinearLayout deleteLayout, wishLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.wish_productname_tv);
            productPrice = itemView.findViewById(R.id.wish_productprice_tv);
            pcprice = itemView.findViewById(R.id.wish_productcprice_tv);
            productImage = itemView.findViewById(R.id.wish_productimage_iv);
            cardView = itemView.findViewById(R.id.cv_wishlist);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            wishLayout = itemView.findViewById(R.id.wish_layout);
        }
    }

    private class deleteWishlist extends AsyncTask<String, Integer, String> {

        private Context context;
        public String cusid, proid;
        int flag;
        private String url = Constants.BASE_URL + Constants.ADD_WISHLIST;
        ProgressDialog progress;

        public deleteWishlist(Context context, String cusid, String proid, int flag) {
            this.context = context;
            this.cusid = cusid;
            this.proid = proid;
            this.flag = flag;
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
                        "Deleted")) {

                    Toast.makeText(context, jonj.getString("message"), Toast.LENGTH_SHORT).show();

                }else
                {
                    Toast.makeText(context,jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
