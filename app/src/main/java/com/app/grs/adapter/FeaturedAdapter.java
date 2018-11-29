package com.app.grs.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.app.grs.R;
import com.app.grs.activity.FeaturedAllActivity;
import com.app.grs.fragment.FeaturedDetailsFragment;
import com.app.grs.fragment.SubProductFragment;
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.HashMap;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String,String>> featuredList;

    public FeaturedAdapter(Context mContext, ArrayList<HashMap<String, String>> featuredList) {

        this.mContext = mContext;
        this.featuredList = featuredList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.featured_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final HashMap<String,String> itemmap = featuredList.get(position);

        Glide.with(mContext).load(Constants.IMAGE_URL + itemmap.get("image")).thumbnail(0.1f).into(holder.feaImage);

        holder.feaName.setText(itemmap.get("product"));
        holder.feaName.setSelected(true);
        holder.feaprice.setText("₹\t" +itemmap.get("price"));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("proid", itemmap.get("id"));
                bundle.putString("proname", itemmap.get("product"));
                bundle.putString("proprice", itemmap.get("price"));
                bundle.putString("prodesc", itemmap.get("pro_desc"));
                bundle.putString("proimage", itemmap.get("image"));
                bundle.putString("prosilde", itemmap.get("image1"));

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new FeaturedDetailsFragment();
                myFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, myFragment).addToBackStack(null).commit();

                //Toast.makeText(mContext, "You have selected :\t" + itemmap.get("product"), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return featuredList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public ImageView feaImage;
        public TextView  feaName, feaprice;
        public CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);

            feaImage = itemView.findViewById(R.id.featuredimage_iv);
            feaName = itemView.findViewById(R.id.featuredname_tv);
            feaprice = itemView.findViewById(R.id.featuredprice_tv);
            cardView = itemView.findViewById(R.id.cv_featured);


        }
    }
}
