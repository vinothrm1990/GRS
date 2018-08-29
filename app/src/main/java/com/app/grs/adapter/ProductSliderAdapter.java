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
import com.app.grs.helper.Constants;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProductSliderAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageList;

    public ProductSliderAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((View) object);

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = LayoutInflater.from(context).inflate(R.layout.slider_layout, container, false);

        ImageView imageView = view.findViewById(R.id.iv_image_slider);

        Glide.with(context).load(Constants.IMAGE_URL+ imageList.get(position)).thumbnail(0.1f).into(imageView);

        container.addView(view);

        return  view;

    }
}
