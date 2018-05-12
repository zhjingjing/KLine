/*
 * Copyright 2012 GitHub Inc. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.kchart.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.DST_IN;

/**
 * @author ANXAINJIE
 */
public class ImageUtils {

    private static final String TAG="ImageUtils";

    /**
     * Get a bitmap from the image path
     * @param imagePath
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath) {
        return getBitmap(imagePath, 1);
    }

    /**
     * Get a bitmap from the image path
     * @param imagePath
     * @param sampleSize
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath, int sampleSize) {
        final Options options=new Options();
        options.inDither=false;
        options.inSampleSize=sampleSize;
        RandomAccessFile file=null;
        try {
            file=new RandomAccessFile(imagePath, "r");
            return BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
        } catch(IOException e) {
            Log.d(TAG, e.getMessage(), e);
            return null;
        } finally {
            if(file != null)
                try {
                    file.close();
                } catch(IOException e) {
                    Log.d(TAG, e.getMessage(), e);
                }
        }
    }

    /**
     * Get a bitmap from the image
     * @param image
     * @param sampleSize
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final byte[] image, int sampleSize) {
        final Options options=new Options();
        options.inDither=false;
        options.inSampleSize=sampleSize;
        return BitmapFactory.decodeByteArray(image, 0, image.length, options);
    }

    /**
     * Get scale for image of size and max height/width
     * @param size
     * @param width
     * @param height
     * @return scale
     */
    public static int getScale(Point size, int width, int height) {
        if(size.x > width || size.y > height)
            return Math.max(Math.round((float)size.y / (float)height), Math.round((float)size.x / (float)width));
        else
            return 1;
    }

    /**
     * Get size of image
     * @param imagePath
     * @return size
     */
    public static Point getSize(final String imagePath) {
        final Options options=new Options();
        options.inJustDecodeBounds=true;
        RandomAccessFile file=null;
        try {
            file=new RandomAccessFile(imagePath, "r");
            BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
            return new Point(options.outWidth, options.outHeight);
        } catch(IOException e) {
            Log.d(TAG, e.getMessage(), e);
            return null;
        } finally {
            if(file != null)
                try {
                    file.close();
                } catch(IOException e) {
                    Log.d(TAG, e.getMessage(), e);
                }
        }
    }

    /**
     * Get size of image
     * @param image
     * @return size
     */
    public static Point getSize(final byte[] image) {
        final Options options=new Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(image, 0, image.length, options);
        return new Point(options.outWidth, options.outHeight);
    }

    /**
     * Get bitmap with maximum height or width
     * @param imagePath
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final String imagePath, int width, int height) {
        Point size=getSize(imagePath);
        return getBitmap(imagePath, getScale(size, width, height));
    }

    /**
     * Get bitmap with maximum height or width
     * @param image
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final byte[] image, int width, int height) {
        Point size=getSize(image);
        return getBitmap(image, getScale(size, width, height));
    }

    /**
     * Get bitmap with maximum height or width
     * @param image
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final File image, int width, int height) {
        return getBitmap(image.getAbsolutePath(), width, height);
    }

    /**
     * Get a bitmap from the image file
     * @param image
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final File image) {
        return getBitmap(image.getAbsolutePath());
    }

    /**
     * Load a {@link Bitmap} from the given path and set it on the given {@link ImageView}
     * @param imagePath
     * @param view
     */
    public static void setImage(final String imagePath, final ImageView view) {
        setImage(new File(imagePath), view);
    }

    /**
     * Load a {@link Bitmap} from the given {@link File} and set it on the given {@link ImageView}
     * @param image
     * @param view
     */
    public static void setImage(final File image, final ImageView view) {
        Bitmap bitmap=getBitmap(image);
        if(bitmap != null)
            view.setImageBitmap(bitmap);
    }

    /**
     * Round the corners of a {@link Bitmap}
     * @param source
     * @param radius
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(final Bitmap source, final float radius) {
        int width=source.getWidth();
        int height=source.getHeight();
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(WHITE);
        Bitmap clipped= Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas=new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(DST_IN));
        Bitmap rounded= Bitmap.createBitmap(width, height, ARGB_8888);
        canvas=new Canvas(rounded);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawBitmap(clipped, 0, 0, paint);
        source.recycle();
        clipped.recycle();
        return rounded;
    }

    // 生成圆角图片
    public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        try {
            Bitmap output= Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas=new Canvas(output);
            final Paint paint=new Paint();
            final Rect rect=new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF=new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            final Rect src=new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch(Exception e) {
            return bitmap;
        }
    }

    public static Bitmap getBitmapByUrl(String url) {
        byte[] data;
        Bitmap bm=null;
        try {
            data=getBytes(new URL(url).openStream());
            bm= BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    public static byte[] getBytes(java.io.InputStream inputStream) throws IOException {
        ByteArrayOutputStream outstream=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024]; // 用数据装
        int len=-1;
        while((len=inputStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        // 关闭流一定要记得。
        return outstream.toByteArray();
    }

    /**
     * @param bitmap 原图
     * @param edgeLength 希望得到的正方形部分的边长
     * @return 缩放截取正中部分后的位图。
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if(null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result=bitmap;
        int widthOrg=bitmap.getWidth();
        int heightOrg=bitmap.getHeight();

        if(widthOrg > edgeLength && heightOrg > edgeLength) {
            // 压缩到一个最小长度是edgeLength的bitmap
            int longerEdge=(int)(edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth=widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight=widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try {
                scaledBitmap= Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch(Exception e) {
                return null;
            }
            //
            // // 从图中截取正中间的正方形部分。
            int xTopLeft=(scaledWidth - edgeLength) / 2;
            int yTopLeft=(scaledHeight - edgeLength) / 2;
            // int xTopLeft=(scaledWidth - edgeLength)*2;
            // int yTopLeft=(scaledHeight - edgeLength)*2;
            int cha=10;// 矩形的上下各少多少

            try {
                result= Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft + cha, edgeLength, edgeLength - cha);
                result=big(result, 0.0f, 0.0f);
                scaledBitmap.recycle();
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return result;
    }

    public static Bitmap big(Bitmap b, float x, float y) {
        int w=b.getWidth();
        int h=b.getHeight();
        float scale=1.5f;
        // float sx=(float)x / w;// 要强制转换，不转换我的在这总是死掉。
        // float sy=(float)y / h;
        Matrix matrix=new Matrix();
        matrix.postScale(1.0f, scale); // 长和宽放大缩小的比例
        Bitmap resizeBmp= Bitmap.createBitmap(b, 0, 0, w, h, matrix, true);
        return resizeBmp;
    }

    // 根据资源文件获取默认图片
    public static Bitmap getBitmapFromResources(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

}
