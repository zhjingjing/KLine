package com.kchart.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kchart.BaseApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ANXIANJIE
 */
public class Utils {

    /**
     * 该函数用于返回在注册界面上显示带有超链接的文本
     * @param str
     * @return
     */
    public static SpannableString getSpannableTextForRegist(String str, int start, int end) {
        SpannableString msp=null;
        msp=new SpannableString(str);
        // 设置下划线
        msp.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // msp.setSpan(new UnderlineSpan(), 88, 105,
        // Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 超级链接（需要添加setMovementMethod方法附加响应）
        // msp.setSpan(new URLSpan(HuobanUtils.protocolUrl), 11, 15,
        // Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // msp.setSpan(new URLSpan(HuobanUtils.huobanUrl), 88, 105,
        // Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return msp;
    }

    public static void showToast(Context context, String str, int time) {
        if(!TextUtils.isEmpty(str))
            Toast.makeText(context, str, time).show();
    }

    /**
     * 判断是否有网络连接
     * @param context
     * @return
     */
    public synchronized static boolean isNetworkConnected(Context context) {
        if(context != null) {
            ConnectivityManager mConnectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo=mConnectivityManager.getActiveNetworkInfo();
            if(mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }
        return false;
    }

    // 现在由于种种原因只判断手机号的长度不判断是否是手机号
    public static boolean isMobileNO(String mobiles) {
        if(!TextUtils.isEmpty(mobiles) && mobiles.length() == 11)
            return true;
        return false;
    }

    // public static boolean isMobileNO(String mobiles) {
    // if(TextUtils.isEmpty(mobiles))
    // return false;
    // Pattern p=Pattern.compile("^((13[0-9])|(17[0-9])|(14[5-7])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
    // Matcher m=p.matcher(mobiles);
    // return m.matches();
    // }
    public static boolean isMobileNO2(String mobiles) {
        if(TextUtils.isEmpty(mobiles))
            return false;
        // 海外用户需要12的号码段
        Pattern p= Pattern.compile("^((13[0-9])|(12[0-9])|(17[0-9])|(14[5-7])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m=p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isMatcher(String str, String reg) {
        if(TextUtils.isEmpty(str))
            return false;
        Pattern p= Pattern.compile(reg);
        Matcher m=p.matcher(str);
        return m.matches();
    }

    // 随机生成六位数,作为验证码
    public static String getRandomVerificationCode() {
        Random random=new Random();
        int x=random.nextInt(899999);
        int y=x + 100000;
        return String.valueOf(y);
    }


    public static String getDate(Date date) {
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /**
     * 获取软件版本号
     * @return
     */
    public static Integer getVersionCode() {
        if(versionCode == null || versionCode == 0) {
            try {
                // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
                versionCode= BaseApplication.getInstance().getPackageManager().getPackageInfo("com.zhangzhang", 0).versionCode;
            } catch(NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return versionCode;
    }

    public static String getVersionName() {
        if(TextUtils.isEmpty(versionName)) {
            try {
                versionName=BaseApplication.getInstance().getPackageManager().getPackageInfo("com.zhangzhang", 0).versionName;
            } catch(NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return versionName;
    }

    public static long getTime(String dateTime) {
        Date date=null;
        try {
            date=new SimpleDateFormat("yyyy-MM-dd").parse(dateTime);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() / 1000;
    }

    // 通常是1:3的转换 1px==3dp
    public static int Dp2Px(Context context, float dp) {
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public static int Dp2Px(Context context, double dp) {
        final double scale=context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public static int Px2Dp(Context context, float px) {
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(px / scale + 0.5f);
    }

    /**
     * md5加密的工具方法
     * @param text
     * @return
     */
    public static String md5Encode(String text) {
        if(TextUtils.isEmpty(text))
            return "";
        try {
            MessageDigest mess= MessageDigest.getInstance("md5");
            byte[] md5=mess.digest(text.getBytes());
            StringBuilder sb=new StringBuilder();
            for(byte b: md5) {
                int i=b & 0xff;
                // 将每一个字节都转换成16进制数
                String string= Integer.toHexString(i);
                if(string.length() == 1)
                    sb.append("0");// 16进制数都是两位数，如果不足两位，就在这个字符串前面添加一个0
                sb.append(string);
            }
            return sb.toString();
        } catch(NoSuchAlgorithmException e) {
            // 此异常不可能出现
            e.printStackTrace();
        }
        return "";
    }

    private static Integer versionCode;

    private static String versionName;

    private static TelephonyManager telManager;


    /**
     * 将输入流转为byte数组
     * @param in
     * @return
     * @throws Exception
     */
    private static byte[] readInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int len=-1;
        while((len=in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        baos.close();
        in.close();
        return baos.toByteArray();
    }

    // 根据tradable判断是否可以自动刷新
    public static boolean isAutomatic(String tradable) {
        if(TextUtils.isEmpty(tradable) || tradable.equals("1")) {
            return true;
        }
        return false;
    }

    /**
     * 根据字符串来给定的开始和结尾来截取字符串
     * @Description: TODO
     * @author ANXIANJIE
     * @date 2015-1-21
     */
    public static String repStr(String string, int start, int end) {
        String str=string;
        if(!TextUtils.isEmpty(str) && str.length() > end) {
            String subStr=str.substring(0, start);
            String subStr2=str.substring(end, str.length());
            String subStr3="";
            for(int i=0; i < end - start; i++) {
                subStr3+="*";
            }
            if(!TextUtils.isEmpty(subStr3))
                str=subStr + subStr3 + subStr2;
            else
                str=subStr + "****" + subStr2;
        }
        return str;
    }


    /**
     * 根据imagepath获取bitmap
     * @param imagePath
     * @return
     */

    public static Bitmap getBitmap(String imagePath) {
        if(imagePath == null || !(imagePath.length() > 5)) {
            return null;
        }
        int lastIndexOf=imagePath.lastIndexOf("/");// 记录最后一个/的位置
        int lastIndexOf2=imagePath.lastIndexOf(".");// 记录最后一个点的位置
        if(lastIndexOf + 1 < lastIndexOf2) {
            String subpath=imagePath.substring(lastIndexOf + 1, lastIndexOf2);
            if(!TextUtils.isEmpty(subpath)) {
                File cache_file=new File(new File(Environment.getExternalStorageDirectory(), subpath), "cachebitmap");
                cache_file=new File(cache_file, getMD5(imagePath));
                if(cache_file.exists()) {
                    return BitmapFactory.decodeFile(getBitmapCache(imagePath, subpath));
                } else {
                    try {
                        URL url=new URL(imagePath);
                        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                        conn.setConnectTimeout(5000);
                        if(conn.getResponseCode() == 200) {
                            InputStream inStream=conn.getInputStream();
                            File file=new File(new File(Environment.getExternalStorageDirectory(), subpath), "cachebitmap");
                            if(!file.exists())
                                file.mkdirs();
                            file=new File(file, getMD5(imagePath));
                            FileOutputStream out=new FileOutputStream(file);
                            byte buff[]=new byte[1024];
                            int len=0;
                            while((len=inStream.read(buff)) != -1) {
                                out.write(buff, 0, len);
                            }
                            out.close();
                            inStream.close();
                            return BitmapFactory.decodeFile(getBitmapCache(imagePath, subpath));
                        }
                    } catch(Exception e) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取缓存
     * @param url
     * @return
     */

    public static String getBitmapCache(String url, String dirpath) {

        File file=new File(new File(Environment.getExternalStorageDirectory(), dirpath), "cachebitmap");

        file=new File(file, getMD5(url));

        if(file.exists()) {

            return file.getAbsolutePath();

        }

        return null;

    }

    // 加密为MD5

    public static String getMD5(String content) {

        try {

            MessageDigest digest= MessageDigest.getInstance("MD5");

            digest.update(content.getBytes());

            return getHashString(digest);

        } catch(Exception e) {

        }

        return null;

    }

    private static String getHashString(MessageDigest digest) {

        StringBuilder builder=new StringBuilder();

        for(byte b: digest.digest()) {

            builder.append(Integer.toHexString((b >> 4) & 0xf));

            builder.append(Integer.toHexString(b & 0xf));

        }

        return builder.toString().toLowerCase();

    }


    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter=listView.getAdapter();
        if(listAdapter == null) {
            return;
        }
        int totalHeight=0;
        for(int i=0; i < listAdapter.getCount(); i++) {
            View listItem=listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight+=listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height=totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    // 获取屏幕的宽度
    public static int getScreenWidth(Context context) {
        try {
            WindowManager manager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display=manager.getDefaultDisplay();
            return display.getWidth();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取屏幕的高度
    public static int getScreenHeight(Context context) {
        try {
            WindowManager manager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display=manager.getDefaultDisplay();
            return display.getHeight();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =======
    /**
     * 检查当前WIFI是否连接，两层意思——是否连接，连接是不是WIFI
     * @param context
     * @return true表示当前网络处于连接状态，且是WIFI，否则返回false
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if(info != null && info.isConnected() && ConnectivityManager.TYPE_WIFI == info.getType()) {
            return true;
        }
        return false;
    }

    /**
     * 检查当前GPRS是否连接，两层意思——是否连接，连接是不是GPRS
     * @param context
     * @return true表示当前网络处于连接状态，且是GPRS，否则返回false
     */
    public static boolean isGprsConnected(Context context) {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if(info != null && info.isConnected() && ConnectivityManager.TYPE_MOBILE == info.getType()) {
            return true;
        }
        return false;
    }

    /**
     * 检查当前是否连接
     * @param context
     * @return true表示当前网络处于连接状态，否则返回false
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if(info != null && info.isConnected()) {
            return true;
        }
        return false;
    }


}
