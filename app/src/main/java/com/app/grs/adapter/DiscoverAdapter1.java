package com.app.grs.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.fragment.FeaturedDetailsFragment;
import com.app.grs.fragment.SubCategoryFragment;
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscoverAdapter1 extends RecyclerView.Adapter<DiscoverAdapter1.MyViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String,String>> discoverList1;

    public DiscoverAdapter1(Context mContext, ArrayList<HashMap<String, String>> discoverList1) {
        this.mContext = mContext;
        this.discoverList1 = discoverList1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.discover_layout1, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final HashMap<String,String> itemmap = discoverList1.get(position);

        Glide.with(mContext).load(itemmap.get("cat_img_url") + itemmap.get("cat_img_name")).thumbnail(0.1f).into(holder.image);

        holder.name.setText(itemmap.get("cat"));

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("catname", itemmap.get("cat"));

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment myFragment = new SubCategoryFragment();
                myFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, myFragment).addToBackStack(null).commit();

                //Toast.makeText(mContext, "You have selected :\t" + itemmap.get("cat"), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return discoverList1.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        public TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.discover_iv1);
            name = itemView.findViewById(R.id.discover_tv1);
        }
    }
}
