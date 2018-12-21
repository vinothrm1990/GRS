package com.app.grs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.grs.R;
import com.app.grs.helper.Constants;
import com.app.grs.helper.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


public class ProductSliderAdapter  extends PagerAdapter {

    private Context context;
    private ArrayList<String> productList;

    public ProductSliderAdapter(Context context, ArrayList<String> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
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

        View view = LayoutInflater.from(context).inflate(R.layout.pager_layout, container, false);

        ImageView imageView = view.findViewById(R.id.iv_slider);
      /*  Glide.with(context)
                .load(Constants.IMAGE_URL+productList.get(position))
                .apply(RequestOptions.centerInsideTransform())
                .into(imageView);*/


        int loader = R.drawable.no_preview;

        String image_url = Constants.IMAGE_URL+productList.get(position);

        ImageLoader imgLoader = new ImageLoader(context);

        imgLoader.DisplayImage(image_url, loader, imageView);

        container.addView(view);

        return  view;

    }
}
