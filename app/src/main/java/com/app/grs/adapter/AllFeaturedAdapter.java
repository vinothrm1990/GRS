package com.app.grs.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.grs.R;
import com.app.grs.activity.SubFeaturedAllActivity;
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class AllFeaturedAdapter extends RecyclerView.Adapter<AllFeaturedAdapter.MyViewHolder>{

    private Context mContext;
    private ArrayList<HashMap<String,String>> allFeaturedList;

    public AllFeaturedAdapter(Context mContext, ArrayList<HashMap<String, String>> allFeaturedList) {
        this.mContext = mContext;
        this.allFeaturedList = allFeaturedList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_featured_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AllFeaturedAdapter.MyViewHolder holder, int position) {

        final HashMap<String, String> itemmap = allFeaturedList.get(position);

        holder.productName.setText(itemmap.get("product"));
        holder.productPrice.setText("₹" + itemmap.get("price"));
        holder.pcprice.setText("₹" + itemmap.get("cross_price"));
        holder.pcprice.setPaintFlags(holder.pcprice.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);

        Glide.with(mContext)
                .load(Constants.IMAGE_URL + itemmap.get("image"))
                .apply(RequestOptions.centerInsideTransform())
                .into(holder.productImage);


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, SubFeaturedAllActivity.class);
                intent.putExtra("data", itemmap);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allFeaturedList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView productName, productPrice, pcprice;
        public ImageView productImage;
        public CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.all_featuredname_tv);
            productPrice = itemView.findViewById(R.id.all_featuredprice_tv);
            pcprice = itemView.findViewById(R.id.all_featured_cprice_tv);
            productImage = itemView.findViewById(R.id.all_featuredimage_iv);
            cardView = itemView.findViewById(R.id.cv_all_featured);
        }
    }
}
