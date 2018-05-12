package com.kchart.chart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

final class ChartUtil {

    final static boolean DEBUG=true;

    // 分时里面底部蜡烛图颜色
    final static int getColorTrans(float sV, float value) {
        float nRet=value - sV;
        if(nRet > 0)
            return Color.parseColor("#df5d57");
        else if(nRet < 0)
            return Color.parseColor("#42ad8b");
        else
            return ColorScheme._HQ_EQ_COLOR_TRANS;
    }

    // k线里面触摸详情颜色
    final static int getColor(float sV, float value) {
        float nRet=value - sV;
        if(nRet > 0)
            return Color.parseColor("#df5d57");
        else if(nRet < 0)
            return Color.parseColor("#42ad8b");
        else
            return ColorScheme._HQ_EQ_COLOR;
    }

    final static float dip2px(Context c, float dpValue) {
        Resources r=c.getResources();
        return dpValue * r.getDisplayMetrics().density;
    }

    final static float getPxViaDip(Context c, float textSize) {
        Resources r=c.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, r.getDisplayMetrics());
    }

    final static void setTextSize(Paint paint, int textSize, Context c) {
        Resources r;
        if(c == null)
            r= Resources.getSystem();
        else
            r=c.getResources();
        int size=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, r.getDisplayMetrics());
        paint.setTextSize(size);
    }

    final static float getFontHeight(float fontSize) {
        Paint paint=new Paint();
        paint.setTextSize(fontSize);

        return getFontHeight(paint);
    }

    final static float getFontHeight(Paint paint) {
        FontMetrics fm=paint.getFontMetrics();
        return (float) Math.ceil(fm.descent - fm.ascent);
    }

    final static float spacing(MotionEvent event) {
        float x=event.getX(0) - event.getX(1);
        float y=event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
//        return FloatMath.sqrt(x * x + y * y);
    }

    final static float getMin(float... fs) {
        float tmp=0;
        for(int i=0; i < fs.length; i++) {
            if(fs[i] > 0) {
                if(tmp == 0)
                    tmp=fs[i];
                else
                    tmp= Math.min(fs[i], tmp);
            }
        }
        return tmp;
    }

    final static float getMax(float... fs) {
        float tmp=fs[0];
        for(int i=1; i < fs.length; i++) {
            tmp= Math.max(fs[i], tmp);
        }
        return tmp;
    }

    private static final SimpleDateFormat mDateParse=new SimpleDateFormat("yyMMdd");

    /**
     * @param arr
     * @param index
     * @return
     */
    final static boolean isEmpty(String[] arr, int index) {
        if(arr == null)
            return true;
        if(index >= arr.length || index < 0)
            return true;
        if(TextUtils.isEmpty(arr[index]))
            return true;
        return false;
    }

    final static String getIntelligentNum(String value) {
        return getIntelligentNum(value, 3);
    }

    final static String getIntelligentNum(String value, int maxScale) {
        if(TextUtils.isEmpty(value)) {
            return value;
        }
        double d;
        try {
            d= Double.parseDouble(value);
            return getIntelligentNum(d, maxScale);
        } catch(Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    final static String getIntelligentNum(String value, int bits, int maxScale) {
        if(TextUtils.isEmpty(value)) {
            return value;
        }
        double d;
        try {
            d= Double.parseDouble(value);
            return getIntelligentNum(d, bits, maxScale);
        } catch(Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    final static String getIntelligentNum(double d, int maxscale) {
        return String.format("%.2f", d);
    }

    final static String getIntelligentNum(double d, int bits, int maxscale) {
        double ret=d;
        String suffix="";
        int scale=getScale(ret, maxscale);
        double rate= Math.pow(10, bits - 1);
        if(Math.abs(d) > rate * 10000 * 10000) { // rate亿
            ret=d / 10000 / 10000;
            scale=getScale(ret, maxscale);
            suffix="亿手";
        } else if(Math.abs(d) > rate * 10000) {// rate万
            ret=d / 10000;
            scale=getScale(ret, maxscale);
            suffix="万手";
        } else {
            suffix="手";
        }
        BigDecimal bd=new BigDecimal(ret);
        bd=bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return bd + suffix;
    }

    final static int getScale(double d, int maxScale) {
        int prefix=(int) Math.abs(d);
        int ca=(int)(Math.abs(d - prefix) * 10000);
        int scale=maxScale;
        if(maxScale > 0 && ca % 10000 == 0) {
            scale=0;
        } else if(maxScale > 1 && ca % 1000 == 0) {
            scale=1;
        } else if(maxScale > 2 && ca % 100 == 0) {
            scale=2;
        } else if(maxScale > 3 && ca % 10 == 0) {
            scale=3;
        }
        return scale;
    }

    final static String getLocalNumber(String number) {
        String dot=",";
        int index=number.indexOf('.');
        StringBuilder sb=new StringBuilder(number);
        int lastPos=index;
        if(index < 0) {
            lastPos=number.length();
        }
        lastPos-=3;
        for(; lastPos > 0;) {
            sb.insert(lastPos, dot);
            lastPos-=3;
        }
        return sb.toString();
    }

    final static float getFloat(JSONObject obj, String tag) {
        String ss=getString(obj, tag);
        if(ss != null) {
            try {
                return Float.parseFloat(ss);
            } catch(NumberFormatException e) {
                // e.printStackTrace();
            }
        }
        return 0;
    }

    final static int getInt(JSONObject obj, String tag) {
        String ss=getString(obj, tag);
        if(ss != null) {
            try {
                return Integer.parseInt(ss);
            } catch(NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    final static double getDouble(JSONObject obj, String tag) {
        String ss=getString(obj, tag);
        try {
            if(!isEmpty(ss))
                return Double.parseDouble(ss);
        } catch(NumberFormatException e) {
        }
        return 0;
    }

    final static String getString(JSONObject obj, String tag) {
        try {
            return obj.getString(tag);
        } catch(Exception e) {
        }
        return "";
    }

    final static JSONObject getRowItem(JSONArray arr, int row) {
        try {
            return arr.getJSONObject(row);
        } catch(Exception e) {
        }
        return null;
    }

    final static Date parseDate(String datestr) {
        if(isEmpty(datestr))
            return null;
        Date date1=null;
        try {
            date1=mDateParse.parse(datestr);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    final static boolean isEmpty(String str) {
        if(str == null || "".equals(str))
            return true;
        return false;
    }

}
