package com.app.grs.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.grs.R;
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {

    private Context mContext;
    ArrayList<HashMap<String,String>> orderList;

    public OrderAdapter(Context mContext, ArrayList<HashMap<String, String>> orderList) {
        this.mContext = mContext;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final HashMap<String,String> itemmap = orderList.get(position);

        String qty = itemmap.get("qty");
        String price = itemmap.get("price");

        String total =String.valueOf(Integer.valueOf(qty) * Integer.valueOf(price));

        holder.ordername.setText(itemmap.get("pname"));
        holder.orderqty.setText(itemmap.get("qty"));
        holder.orderprice.setText("₹" + total);

        Glide.with(mContext)
                .load(Constants.IMAGE_URL + itemmap.get("pic"))
                .apply(RequestOptions.centerInsideTransform())
                .into(holder.orderimage);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView ordername, orderqty, orderprice;
        public ImageView orderimage;

        public MyViewHolder(View itemView) {
            super(itemView);

            orderimage = itemView.findViewById(R.id.order_iv);
            ordername = itemView.findViewById(R.id.order_name);
            orderqty = itemView.findViewById(R.id.order_qty);
            orderprice = itemView.findViewById(R.id.order_price);


        }
    }
}
