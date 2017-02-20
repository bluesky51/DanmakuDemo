package com.sky.sky.danmakudemo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;
import master.flame.danmaku.ui.widget.DanmakuView;
import okhttp3.ResponseBody;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 弹幕的网络地址:http://danmu.aixifan.com/V4/4862774/0/500
     */
    //视频地址:
    private String video_url = "http://113.142.80.101/sohu.vodnew.lxdns.com/sohu/s26h23eab6/36/112/sc1OQIuZQmyZDF8uSz263F.mp4?key=w3gkGZ3bOV0zQk0895_w8vZ4APxvd0p1&n=1&a=2023&cip=182.138.212.224&pg=0&ch=my&vid=84310632&prod=ugc&wshc_tag=0&wsiphost=ipdbm";
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private BaseDanmakuParser mParser;
    private VideoView videoView;
    private Button btn_text;
    private Button btn_textAndImg;
    private Button btn_isShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        findView();
        setListener();
        configDanmakuInfo();
        getDanmukuByNetwork();
        videoPlay(videoView);
        //设置横屏全屏播放视频
        videoPlay(videoView);

    }
    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        private Drawable mDrawable;

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
                new Thread() {

                    @Override
                    public void run() {
                        String url = "http://www.bilibili.com/favicon.ico";
                        InputStream inputStream = null;
                        Drawable drawable = mDrawable;
                        if(drawable == null) {
                            try {
                                URLConnection urlConnection = new URL(url).openConnection();
                                inputStream = urlConnection.getInputStream();
                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
                                mDrawable = drawable;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(inputStream);
                            }
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, 100, 100);
                            SpannableStringBuilder spannable = createSpannable(drawable);
                            danmaku.text = spannable;
                            if(danmakuView != null) {
                                danmakuView.invalidateDanmaku(danmaku, false);
                            }
                            return;
                        }
                    }
                }.start();
            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
        }
    };
    private void setListener() {
        btn_text.setOnClickListener(this);
        btn_textAndImg.setOnClickListener(this);
        btn_isShow.setOnClickListener(this);
    }
    //查找控件
    public void findView(){
        videoView = (VideoView) findViewById(R.id.videoview);
        danmakuView = (DanmakuView) findViewById(R.id.sv_danmaku);
        btn_text = (Button) findViewById(R.id.button2);
        btn_textAndImg = (Button) findViewById(R.id.button1);
        btn_isShow = (Button) findViewById(R.id.button);

    }
    //配置弹幕信息
    public void configDanmakuInfo(){
        /**
         * 参数1:BaseDanmakuParser:弹幕的解析类
         * 参数2:DanmakuContext:弹幕的基本配置信息设置
         */
        danmakuContext = DanmakuContext.create();
        //设置弹幕字体
        danmakuContext.setTypeface(Typeface.SANS_SERIF);
       // 图文混排使用SpannedCacheStuffer
        danmakuContext.setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter);
        //设置描边样式
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3);
        //设置是否启用合并重复弹幕
        danmakuContext.setDuplicateMergingEnabled(false);
        //置弹幕滚动速度系数,只对滚动弹幕有效
        danmakuContext.setScrollSpeedFactor(1.2f);
        //设字体大小
        danmakuContext.setScaleTextSize(1.2f);
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        danmakuContext.setMaximumLines(maxLinesPair);
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        danmakuContext.preventOverlapping(overlappingEnablePair);
    }
    //播放视频
    public void videoPlay(VideoView videoView) {
        videoView.setVideoPath(video_url);
        videoView.setMediaController(new MediaController(this));
        setVideViewFullScreen(videoView);
        videoView.start();
    }
    //设置视频全屏展示
    public void setVideViewFullScreen(VideoView videoView) {
        ////注意，VideoView最外层的容器是RelativeLayout
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        videoView.setLayoutParams(layoutParams);
    }
    //从网络获取弹幕数据
    public void getDanmukuByNetwork(){
        HttpUtils.getHttpUtils().getVideoList(new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                InputStream is = responseBody.byteStream();
                mParser = createParser(is);
                danmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                    @Override
                    public void updateTimer(DanmakuTimer timer) {
                    }

                    @Override
                    public void drawingFinished() {

                    }

                    @Override
                    public void danmakuShown(BaseDanmaku danmaku) {
                    }

                    @Override
                    public void prepared() {
                        danmakuView.start();
                    }
                });
                danmakuView.prepare(mParser, danmakuContext);
                //danmakuView.showFPS(true);
                danmakuView.enableDanmakuDrawingCache(true);
                danmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
                    @Override
                    public boolean onDanmakuClick(IDanmakus danmakus) {
                        //弹幕点击事件
                        Log.e("====","===111==="+danmakus.last().text);
                        Log.e("====","===222==="+danmakus.first().text);
                        return false;
                    }

                    @Override
                    public boolean onViewClick(IDanmakuView view) {
                        return false;
                    }
                });
            }
        });
    }
    //根据网络返回的弹幕数据流进行解析处理
    private BaseDanmakuParser createParser(InputStream inputStream) {
        if (inputStream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_ACFUN);

        try {
            loader.load(inputStream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new AcFunDanmukuParser();

        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }
    //添加文字弹幕
    private void addDanmaku(boolean islive) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory
                .createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || danmakuView == null) {
            return;
        }
        // for(int i=0;i<100;i++){
        // }
        danmaku.text = "这是一条弹幕" + System.nanoTime();
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive;
        danmaku.setTime(danmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        //danmaku.borderColor = Color.GREEN;
        danmakuView.addDanmaku(danmaku);

    }
    //添加文字带图标的弹幕
    private void addDanmaKuShowTextAndImage(boolean islive) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        drawable.setBounds(0, 0, 100, 100);
        SpannableStringBuilder spannable = createSpannable(drawable);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.setTime(danmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.underlineColor = Color.GREEN;
        danmakuView.addDanmaku(danmaku);
    }
    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (danmakuView != null) {
            // dont forget release!
            danmakuView.release();
            danmakuView = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (danmakuView != null) {
            // dont forget release!
            danmakuView.release();
            danmakuView = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                if (danmakuView.isShown()) {
                    danmakuView.hide();
                    btn_isShow.setText("显示");
                } else {
                    danmakuView.show();
                    btn_isShow.setText("隐藏");
                }
                break;
            case R.id.button2:
                addDanmaku(false);
                break;
            case R.id.button1:
                addDanmaKuShowTextAndImage(false);
                break;
        }
    }
}
