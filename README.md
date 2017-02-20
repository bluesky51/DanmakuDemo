####弹幕第三方库(DanmakuFlameMaster)的使用说明
#####配置集成说明:
######《1》导入库
    ndkbitmap-armv5
    ndkbitmap-armv7a
    ndkbitmap-x86
    DanmakuFlameMaster
######《2》在Module下的build.gradle添加依赖:
    compile project(':DanmakuFlameMaster')
    compile project(':ndkbitmap-armv5')
    compile project(':ndkbitmap-armv7a')
    compile project(':ndkbitmap-x86')
#####依赖库使用说明:
######《1》布局文件中添加显示视频和弹幕的View控件:
显示视频控件：

     <VideoView
             android:id="@+id/videoview"
             android:layout_width="fill_parent"
             android:layout_height="fill_parent" />

显示弹幕的控件:

     <master.flame.danmaku.ui.widget.DanmakuView
             android:id="@+id/sv_danmaku"
             android:layout_width="match_parent"
             android:layout_height="match_parent" />
######《2》java代码中配置弹幕显示需要的信息和视频全屏显示的代码:
 1.VideoView全屏显示视频:

    //注意，VideoView最外层的容器是RelativeLayout
    RelativeLayout.LayoutParams layoutParams=
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    videoView.setLayoutParams(layoutParams);
 2.VideoView播放视频基本配置:

      //设置播放路径
      videoView.setVideoPath(video_url);
      //设置播放时的控制器
      videoView.setMediaController(new MediaController(this));
      //开始播放
      videoView.start();
 3.配置弹幕信息:

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
 4.从网络下载的到网络弹幕数据并进行解析(Json结构):
    下载请看getDanmukuByNetwork()方法
    解析主要看createParser()方法
    以上两个方法主要在MainActivity中，真正内容解析在AcFunDanmukuParser.java类中

 得到的数据列表中其中一个对象为：

    {
        "c": "13.937,16777215,1,25,3129163,1487080769,670b00a2-2062-46c6-9ae8-2b0a91728cfb",
        "m": "留个位置"
    }

c是弹幕配置信息，m是弹幕文字，其中c最复杂依次解释为:

      第一个参数为弹幕出现时间  13.937;
      第二个参数为颜色 16777215;
      第三个参数为类型 1 类型(1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕);
      第四个参数为字号 25;
      第五个参数为     3129163;
      第六个参数为时间戳  1487080769;
      第7个参数为      670b00a2-2062-46c6-9ae8-2b0a91728cfb；
5.添加文字弹幕:

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
6.添加图片和文字的弹幕: 查看MainActivity中addDanmaKuShowTextAndImage()方法即可；

7.弹幕DanmakuViewb伴随着Activity的生命周期从生存到死亡

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
8.显示和隐藏弹幕：
   显示弹幕：  danmakuView.show();
   隐藏弹幕：  danmakuView.hide();


