package com.rmtd.melo.welcomedemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melo on 2017/2/20.
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    Banner banner;
    List<String> images;
    TextView tv_skip;
    int count = 10;

    private int DOWN_FINISH = 0x335;
    private int SKIP_REDUCE = 0x332;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x335:
                    setBanner();//设置banner
                    mHandler.sendEmptyMessage(SKIP_REDUCE);
                    break;
                case 0x332:

                    count--;
                    tv_skip.setText("剩余" + count + "" + "S");
                    if (count > 0) {
                        mHandler.sendEmptyMessageDelayed(SKIP_REDUCE, 1000);
                    } else {
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        banner = (Banner) findViewById(R.id.banner);
        tv_skip = (TextView) findViewById(R.id.textview_skip);
        images = new ArrayList<>();

        OkhttpHelper.doGet(UrlConfig.WelComeUrl, new OkhttpHelper.MyCallback() {
            @Override
            public void onFail() {
                Log.i(TAG, "onError");
            }

            @Override
            public void onSuccess(String result) {
                WelcomeBean welcomeBean = JsonUtil.json2Bean(result, WelcomeBean.class);
                for (int i = 0; i < welcomeBean.getData().size(); i++) {
                    String coverUrl = welcomeBean.getData().get(i).getCover();
                    images.add(coverUrl);
                    Log.i("tag", "1:" + coverUrl);
                }
                Message message = new Message();
                message.what = DOWN_FINISH;
                mHandler.sendMessage(message);

                Log.i("tag", "images:" + images.size());


            }
        });


        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (position == (images.size() - 1)) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }
            }
        });

    }

    private void setBanner() {
//        //设置banner样式
//        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        Log.i("tag", "images11111:" + images.size());
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
//        //设置标题集合（当banner样式有显示title时）
//        banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        banner.isAutoPlay(false);
        //设置轮播时间
        banner.setDelayTime(1500);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }
}
