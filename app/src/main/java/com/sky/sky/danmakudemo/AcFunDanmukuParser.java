package com.sky.sky.danmakudemo;

import android.graphics.Color;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

/**
 * Created by BlueSky on 17/2/17.
 */

public class AcFunDanmukuParser extends BaseDanmakuParser {
    private BaseDanmaku item = null;
    private  Danmakus danmakus=new Danmakus();
    private LinkedList<BaseDanmaku> danmakusList = null;

    @Override
    protected IDanmakus parse() {
        if (mDataSource != null) {
            JSONSource jsonSource = (JSONSource) mDataSource;
            JSONArray array = jsonSource.data();
            int array_length = array.length();
            for (int i = 0; i < array_length; i++) {
                try {
                    JSONArray jsonArray = array.getJSONArray(i);
                    int jsonArray_length = jsonArray.length();
                    danmakusList = new LinkedList<>();
                    for (int j = 0; j < jsonArray_length; j++) {
                        JSONObject obj = jsonArray.getJSONObject(j);
                        if (obj != null) {

                            String c = obj.getString("c");
                            // parse p value to danmaku
                            String[] values = c.split(",");
                            //13.937(时间(弹幕出现时间)),  16777215(颜色),  1(类型), 25(字号),  3129163, 1487080769(时间戳), 670b00a2-2062-46c6-9ae8-2b0a91728cfb
                            // 类型(1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕)
                            if (values.length > 0) {
                                long time = (long) (Float.parseFloat(values[0]) * 1000); // 出现时间
                                int type = Integer.parseInt(values[2]); // 弹幕类型
                                float textSize = Float.parseFloat(values[3]); // 字体大小
                                int color = (int) ((0x00000000ff000000 | Long.parseLong(values[1])) & 0x00000000ffffffff); // 颜色
                                // int poolType = Integer.parseInt(values[5]); // 弹幕池类型（忽略
                                item = mContext.mDanmakuFactory.createDanmaku(type, mContext);
                                if (item != null) {
                                    item.setTime(time);
                                    item.textSize = textSize * (mDispDensity - 0.6f);
                                    item.textColor = color;
                                    item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
                                }
                            }
                            //解析弹幕文字
                            String message = obj.getString("m");
                            item.text = message;
                            if (!TextUtils.isEmpty(message)) {
                                item.setTimer(mTimer);
                                item.flags = mContext.mGlobalFlagValues;
                                Object lock = danmakus.obtainSynchronizer();
                                synchronized (lock) {
                                    danmakus.addItem(item);
                                }

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return danmakus;
    }
}
