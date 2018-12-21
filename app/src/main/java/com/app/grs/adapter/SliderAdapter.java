package com.app.grs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.grs.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


public class SliderAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> bannerList;

    public SliderAdapter(Context context, ArrayList<String> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((View) object);

    }
    @Override
    public int getCount() {
        return bannerList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = LayoutInflater.from(context).inflate(R.layout.pager_layout, container, false);

        ImageView imageView = view.findViewById(R.id.iv_slider);
        Glide.with(context)
                .load(bannerList.get(position))
                .apply(RequestOptions.centerInsideTransform())
                .into(imageView);

        container.addView(view);

        return  view;

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


}
