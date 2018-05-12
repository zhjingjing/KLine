package com.kchart.chart;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


import com.kchart.BaseApplication;
import com.kchart.R;
import com.kchart.utils.ImageUtils;
import com.kchart.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ChartBase extends View {

    protected class MygestureListener implements android.view.GestureDetector.OnGestureListener {

        private float disX;

        public boolean onDown(MotionEvent e) {
            if(DEBUG)
                Log.d("Chart", "OnGestureListener onDown");
            isShowPress=false;
            onShowEvent(e, false);
            disX=0.0F;
            return false;
        }

        public void onShowPress(MotionEvent e) {
            if(DEBUG)
                Log.d("Chart", "OnGestureListener onShowPress");
            isShowPress=true;
            onShowEvent(e, true);
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if(DEBUG)
                Log.d("Chart", "OnGestureListener onSingleTapUp");
            onSingleClick(e);
            return false;
        }

        public void onLongPress(MotionEvent e) {
            if(DEBUG)
                Log.d("Chart", "OnGestureListener onLongPress");
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(DEBUG)
                Log.d("Chart", (new StringBuilder("OnGestureListener onScroll ")).append(distanceX).toString());
            if(isShowPress)
                return false;
            if(onHorizontalScroll(distanceX + disX))
                disX=0.0F;
            else
                disX+=distanceX;
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(DEBUG)
                Log.d("Chart", "OnGestureListener onFling");
            return false;
        }

        protected MygestureListener() {
        }
    }

    protected static final String LOG_TAG="Chart";

    protected Context mContext;

    protected boolean DEBUG;

    protected GestureDetector mGestureDetector;

    protected boolean isShowPress;

    protected boolean supportGesture;

    protected boolean supportZoom;

    protected static final int NONE=0;

    protected static final int DRAG=1;

    protected static final int ZOOM=2;

    protected float MIN_ZOOM_RANGE;

    protected int mode;

    protected RectF chartRect;

    protected RectF indicatorRect;

    protected RectF midBlankRect;

    protected float midTextWidth;

    // protected final String MID_TEXT = "横屏查看更多";
    protected final String MID_TEXT="";

    protected float mY[];

    protected float mPriceArr[];

    protected String mZdfArr[];

    float oldDist;

    public static final String LINE_TYPE_FS="fs";

    public static final String LINE_TYPE_DAY="day";

    public static final String LINE_TYPE_WEEK="week";

    public static final String LINE_TYPE_MONTH="month";

    protected String LINE_TYPE;

    protected Canvas canvas;

    protected Paint cursorPaint;

    protected Paint trafficLightPaint;

    protected Paint talbePaint;

    protected Paint dashedPaint;

    protected Paint chartPaint;

    protected Paint volumePaint;

    protected Paint paintText1;

    protected Paint paintText2;

    protected Paint paintText3;

    protected Paint pop_paint;

    protected float textHeigth;

    protected int mDataLength;

    protected boolean needPaintCursor;

    protected int m_nCursorIndex;

    protected int fontSize;

    protected float m_priceLast;

    protected float m_currPrice;

    protected float m_currL;

    protected int precision;

    protected float zrsp;

    protected float m_maxPrice;

    protected float m_minPrice;

    protected float m_maxCJLBL;

    protected int m_stockType;

    protected JSONObject base;

    protected JSONArray chartData;

    protected static final String TAG_BASE_CODE="code";

    protected static final String TAG_BASE_NAME="name";

    protected static final String TAG_BASE_DATE="date";

    protected static final String TAG_BASE_CURPRICE="now_pri";

    protected static final String TAG_BASE_ZRSP="yestod_end_pri";

    protected static final String TAG_BASE_T_MAX="today_max";

    protected static final String TAG_BASE_T_MIN="today_min";

    protected static final String TAG_BASE_VOL="trade_volume";

    private static final SimpleDateFormat mDateFormat1=new SimpleDateFormat("yy/MM/dd");

    private static final SimpleDateFormat mDateFormat2=new SimpleDateFormat("yyyy/MM/dd");

    private static final SimpleDateFormat mDateFormat3=new SimpleDateFormat("yyyy/MM");

    private android.view.View.OnClickListener mMidTextClickListener;

    private OnCursorChangedListener mCursorPosChangedListener;

    protected OnScrollListener mScrollListener;

    public ChartBase(Context context) {
        super(context);
        DEBUG=false;
        isShowPress=false;
        supportGesture=true;
        supportZoom=false;
        mode=0;
        mY=new float[5];
        mPriceArr=new float[5];
        mZdfArr=new String[5];
        LINE_TYPE="day";
        needPaintCursor=false;
        m_nCursorIndex=0;
        fontSize=12;
        precision=2;
        m_stockType=0;
        mMidTextClickListener=null;
        onCreate();
        checkChildClass();
    }

    public ChartBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        DEBUG=false;
        isShowPress=false;
        supportGesture=true;
        supportZoom=false;
        mode=0;
        mY=new float[5];
        mPriceArr=new float[5];
        mZdfArr=new String[5];
        LINE_TYPE="day";
        needPaintCursor=false;
        m_nCursorIndex=0;
        fontSize=12;
        precision=2;
        m_stockType=0;
        mMidTextClickListener=null;
        onCreate();
        checkChildClass();
    }

    public void setData(String lineType, JSONObject base, JSONArray data) {
        LINE_TYPE=lineType;
        this.base=base;
        chartData=data;
        initDataParam();
    }

    public void setDebug(boolean dEBUG) {
        DEBUG=dEBUG;
    }

    public void setOnLandscapeClickListener(android.view.View.OnClickListener listener) {
        mMidTextClickListener=listener;
    }

    public void setOnCursorChangedListener(OnCursorChangedListener listener) {
        mCursorPosChangedListener=listener;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mScrollListener=listener;
    }

    public String getLinetype() {
        return LINE_TYPE;
    }

    public RectF getMidArea() {
        return midBlankRect;
    }

    public JSONObject getData(int row) {
        return getRowItem(chartData, row);
    }

    protected void onCreate() {
        mContext=getContext();
        initChartPaint(mContext);
        textHeigth=ChartUtil.getFontHeight(paintText1);
        if(supportGesture)
            mGestureDetector=new GestureDetector(mContext, new MygestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    protected void onDraw(Canvas canvas) {
        this.canvas=canvas;
        initDrawFrame();
    }

    protected void initDataParam() {
        mDataLength=chartData != null ? chartData.length() : 0;
    }

    protected void initDrawFrame() {
        int height=getHeight();
        int width=getWidth();
        float margin=ChartUtil.dip2px(getContext(), 2.0F);
        float verticalDevi=((float)height - textHeigth) * 0.7F;
        float x1=margin;
        float x2=(float)width - margin;
        float y1=margin;
        float y2=verticalDevi - textHeigth / 2.0F - margin * 2.0F;
        float y3=verticalDevi + textHeigth / 2.0F + margin * 2.0F;
        float y4=(float)height - textHeigth - margin;
        chartRect=new RectF(x1, y1, x2, y2);
        indicatorRect=new RectF(x1, y3, x2, y4);
        midBlankRect=new RectF(x1, y2, x2, y3);
         midTextWidth = paintText1.measureText("横屏查看更多");
        midTextWidth=paintText1.measureText("");
        mY[0]=chartRect.top + textHeigth * 0.7F;
        mY[4]=chartRect.bottom - textHeigth * 0.1F;
        mY[2]=(mY[0] + mY[4]) / 2.0F;
        mY[1]=(mY[0] + mY[2]) / 2.0F;
        mY[3]=(mY[2] + mY[4]) / 2.0F;
    }

    private void checkChildClass() {
        if(this instanceof FsChart || this instanceof FsChart2) {
            if(DEBUG)
                Log.d("Chart", getClass().getSimpleName());
        } else if(this instanceof KxChart) {
            if(DEBUG)
                Log.d("Chart", getClass().getSimpleName());
        } else {
            throw new RuntimeException("Un support Class");
        }
    }

    private void initChartPaint(Context context) {
        paintText1=new Paint();
        paintText2=new Paint();
        paintText3=new Paint();
        ChartUtil.setTextSize(paintText1, fontSize, context);
        ChartUtil.setTextSize(paintText2, fontSize, context);
        ChartUtil.setTextSize(paintText3, 14, context);
        cursorPaint=new Paint();
        dashedPaint=new Paint();
        talbePaint=new Paint();
        chartPaint=new Paint();
        volumePaint=new Paint();
        trafficLightPaint=new Paint();
        trafficLightPaint=new Paint();
        trafficLightPaint.setStyle(android.graphics.Paint.Style.FILL);
        trafficLightPaint.setAntiAlias(true);
        pop_paint=new Paint();
        pop_paint.setStyle(android.graphics.Paint.Style.FILL_AND_STROKE);
        pop_paint.setStrokeWidth(ChartUtil.getPxViaDip(context, 1.0F));
        chartPaint.setStrokeWidth(ChartUtil.getPxViaDip(context, 1.0F));
        chartPaint.setAntiAlias(true);
        chartPaint.setStyle(android.graphics.Paint.Style.STROKE);
        volumePaint.setStrokeWidth(ChartUtil.getPxViaDip(context, 0.5F));
        volumePaint.setAntiAlias(true);
        paintText1.setFlags(1);
        paintText2.setFlags(1);
        // 表格颜色
        talbePaint.setColor(Color.parseColor("#e0e0e0"));
        talbePaint.setStyle(android.graphics.Paint.Style.STROKE);
        talbePaint.setAntiAlias(true);
        talbePaint.setStrokeWidth(ChartUtil.getPxViaDip(context, 1.0F));
        dashedPaint.setStyle(android.graphics.Paint.Style.STROKE);
        // 表格虚线
        dashedPaint.setColor(Color.parseColor("#e0e0e0"));
        dashedPaint.setAntiAlias(true);
        dashedPaint.setStrokeWidth(ChartUtil.getPxViaDip(context, 0.5F));
        float temp=ChartUtil.getPxViaDip(context, 4F);
        android.graphics.PathEffect e=new DashPathEffect(new float[]{temp, temp, temp, temp}, ChartUtil.dip2px(context, 1.0F));
        dashedPaint.setPathEffect(e);
        cursorPaint.setStyle(android.graphics.Paint.Style.STROKE);
        chartPaint.setAntiAlias(true);
        cursorPaint.setStrokeWidth(ChartUtil.getPxViaDip(context, 0.8F));
        cursorPaint.setColor(ColorScheme._CURSOR_LINE_COLOR);
    }

    protected final void drawChartLine(Paint p, RectF r, String nPos, float maxValueOfTop, float minValueOfBottom,
                                       int countOfWidth, int lineType) {
        drawChartLine(p, r, nPos, maxValueOfTop, minValueOfBottom, countOfWidth, lineType, false);
    }

    /**
     * 在RectF中使用tag的值画一条曲线 需要指定tag值的最大值最小值，指定数据的数量
     * @param p
     * @param r
     * @param tag
     * @param maxValueOfTop
     * @param minValueOfBottom
     * @param countOfWidth
     * @param lineType
     * @param divColor
     */
    protected final void drawChartLine(Paint p, RectF r, String tag, float maxValueOfTop, float minValueOfBottom, int countOfWidth,
                                       int lineType, boolean divColor) {
        int nDataSize=getDetailsSize();
        if(nDataSize == 0 || countOfWidth < 1)
            return;
        if(maxValueOfTop - minValueOfBottom <= 0.0F)
            return;
//        double perX=((double)r.width() * 1.0D) / (double)countOfWidth;
        double perX=800;
        double perY=((double)r.height() * 1.0D) / (double)(maxValueOfTop - minValueOfBottom);
        float LEFT=(float)((double)r.left + perX / 3D);
        float newX=0.0F;
        float newY=0.0F;
        if(lineType == 1) {
            chartPaint.setStrokeWidth(ChartUtil.getPxViaDip(BaseApplication.getInstance(), 1.0F));
            float bottomY=(float)((double)r.bottom - (double)(0.0F - minValueOfBottom) * perY);
            for(int i=0; i < nDataSize && i < countOfWidth; i++) {
                float tmp=getDetailsFloat(i, tag);
                newX=(float)((double)LEFT + perX * (double)i);
                newY=(float)((double)r.bottom - (double)(tmp - minValueOfBottom) * perY);
                if(r.contains(newX, newY)) {
                    if(divColor) {
                        p.setAlpha(50);
                        // p.setColor(getDetailColor(tag, i));
                    }
                    canvas.drawLine(newX, bottomY, newX, newY, p);
                }
            }
        } else {
            float tmp=getDetailsFloat(0, tag);
            float oldX=LEFT;
            float oldY=(float)((double)r.bottom - (double)(tmp - minValueOfBottom) * perY);
            Path mPath=new Path();
            if(lineType == 5) {
                float cX=0.0F; // 设置线宽
                // float cY=118.200005F;
                float cY=Utils.Dp2Px(BaseApplication.getInstance(), 39.5);
                p.setStrokeWidth(3f);
                // 画中间线
                for(int i=1; i < countOfWidth && i < countOfWidth; i++) {
                    tmp=getDetailsFloat(i, tag);
                    newX=(float)((double)LEFT + perX * (double)i);
                    newY=(float)((double)r.bottom - (double)(tmp - minValueOfBottom) * perY);
                    cX+=5.5f;
                    /**
                     * 画线 ： 参数介绍 1.起始端点的X坐标。 2.起始端点的Y坐标。 3.终止端点的X坐标。 4.终止端点的Y坐标。 5.绘制直线所使用的画笔。
                     */
                    canvas.drawLine(cX, cY, cX+=10, cY, p);
                    oldX=newX;
                    oldY=newY;
                }
            } else {
                p.setStyle(android.graphics.Paint.Style.STROKE);
                // 设置线宽
                if(lineType == 3)
                    p.setStrokeWidth(5f);
                mPath.moveTo(oldX, oldY);
                for(int i=1; i < nDataSize && i < countOfWidth; i++) {
                    tmp=getDetailsFloat(i, tag);
                    newX=(float)((double)LEFT + perX * (double)i);
                    newY=(float)((double)r.bottom - (double)(tmp - minValueOfBottom) * perY);
                    float cX=(newX + oldX) / 2.0F;
                    float cY=(newY + oldY) / 2.0F;
                    mPath.quadTo(oldX, oldY, cX, cY);
                    if(!r.contains(newX, newY) || !r.contains(oldX, oldY))
                        Log.e("Chart", (new StringBuilder("drawChartLine ")).append(i).append(" value:").append(tmp).toString());
                    oldX=newX;
                    oldY=newY;
                }
                mPath.lineTo(newX, newY);
                canvas.drawPath(mPath, p);
                if(lineType == 3) {
                    // 画点
                    Bitmap bitmap=
                        ImageUtils.getBitmapFromResources(BaseApplication.getInstance(),
                               R.mipmap.zhangzhang_light_oval);
                    float y=newY - Utils.Dp2Px(BaseApplication.getInstance(), 6);
                    float x=newX - Utils.Dp2Px(BaseApplication.getInstance(), 4);
                    float right=r.right;
                    if(right - Utils.Dp2Px(BaseApplication.getInstance(), 6) > x)// 表示点全在区域内
                        drawBitmap(canvas, x, y, bitmap);
                }
            }
            if(lineType == 2) {
                Paint areaPaint=new Paint(p);
                areaPaint.setStyle(android.graphics.Paint.Style.FILL);
                areaPaint.setColor(Color.parseColor("#CBDAF2")); // 画分时里面阴影颜色
                mPath.lineTo(newX, r.bottom);
                mPath.lineTo(r.left, r.bottom);
                canvas.drawPath(mPath, areaPaint);
            }
        }
    }

    private void drawBitmap(Canvas canvas, float cx, float xy, Bitmap bitmap) {
        Paint circlePaint=new Paint();
        canvas.drawBitmap(bitmap, cx, xy, circlePaint);
    }

    protected final void drawChartLineInt(String nPos, Paint p, RectF r, double maxValueOfTop, double minValueOfBottom,
                                          int countOfWidth, int lineType) {
        if(getDetailsSize() == 0 || countOfWidth < 1)
            return;
        if(maxValueOfTop <= minValueOfBottom)
            return;
        double perX=((double)r.width() * 1.0D) / (double)countOfWidth;
        double perY=(double)r.height() / (maxValueOfTop - minValueOfBottom);
        float oldX=r.left;
        float oldY=(int)((double)r.bottom - ((double)getDetailsFloat(0, nPos) - minValueOfBottom) * perY);
        int nDataSize=getDetailsSize();
        for(int i=0; i < nDataSize && i < countOfWidth; i++) {
            float newX=(int)((double)r.left + perX * (double)i);
            float newY=(int)((double)r.bottom - ((double)getDetailsFloat(i, nPos) - minValueOfBottom) * perY);
            if(r.contains(newX, newY)) {
                switch(lineType) {
                    default:
                        break;

                    case 0: // '\0'
                        if(r.contains(oldX, oldY))
                            canvas.drawLine(oldX, oldY, newX, newY, p);
                        break;

                    case 1: // '\001'
                        int bottomY=(int)((double)r.bottom + minValueOfBottom * perY);
                        canvas.drawLine(newX, bottomY, newX, newY, p);
                        break;
                }
                oldX=newX;
                oldY=newY;
            }
        }

    }

    protected void drawCursorLine(float newX) {
        canvas.drawLine(newX, chartRect.top, newX, chartRect.bottom, cursorPaint);
        canvas.drawLine(newX, indicatorRect.top, newX, indicatorRect.bottom, cursorPaint);
    }

    protected void drawMidText() {
        if(isLandscape()) {
            return;
        } else {
            float margin=ChartUtil.dip2px(getContext(), 2.0F);
            float X=midBlankRect.left + margin;
            float Y=midBlankRect.bottom - ((midBlankRect.height() - textHeigth) * 2.0F) / 3F;
            paintText3.setColor(ColorScheme._HQ_LANDSCAPE_HIT_TEXT_COLOR);
            // canvas.drawText("横屏查看更多", X, Y, paintText3);
            canvas.drawText("", X, Y, paintText3);
            return;
        }
    }

    protected void drawTrafficLight(float f, float f1, float f2, Paint paint1) {
    }

    protected void drawFrameInfo(int LENGTH, float x, String titles[], String values[], int colors[]) {
        if(LENGTH < 1)
            return;
        float tmpLeftWidth=0.0F;
        float maxLeftWidth=0.0F;
        float maxRightWidth=0.0F;
        float rightWidth[]=new float[LENGTH];
        for(int i=0; i < LENGTH; i++) {
            if(!ChartUtil.isEmpty(titles, i)) {
                tmpLeftWidth=paintText2.measureText(titles[i]);
                if(maxLeftWidth < tmpLeftWidth)
                    maxLeftWidth=tmpLeftWidth;
            }
            if(!ChartUtil.isEmpty(values, i)) {
                rightWidth[i]=paintText2.measureText(values[i]);
                if(maxRightWidth < rightWidth[i])
                    maxRightWidth=rightWidth[i];
            }
        }

        // float marginBox = ChartUtil.dip2px(mContext, 10F);
        float marginBox=ChartUtil.dip2px(mContext, 15F);
        float marginHor=ChartUtil.dip2px(mContext, 2.0F);
        float marginVer=ChartUtil.dip2px(mContext, 2.0F);
        float boxWidth=maxLeftWidth + maxRightWidth + 3F * marginHor;
        float boxHeight=textHeigth * (float)titles.length + 8F * marginVer;
        RectF box=new RectF();
        if(x > chartRect.centerX())
            box.set(chartRect.left + marginBox, chartRect.top + marginBox, chartRect.left + marginBox + boxWidth, chartRect.top
                + marginBox + boxHeight);
        else
            box.set(chartRect.right - marginBox - boxWidth, chartRect.top + marginBox, chartRect.right - marginBox, chartRect.top
                + marginBox + boxHeight);
        float Y[]=new float[LENGTH];
        Y[LENGTH - 1]=box.bottom - marginVer * 3F;
        for(int i=LENGTH - 2; i >= 0 && i < LENGTH; i--)
            Y[i]=Y[i + 1] - marginHor - textHeigth;

        float radius=ChartUtil.dip2px(mContext, 3F);
        drawRoundBox(box, radius, pop_paint);
        for(int i=0; i < values.length; i++) {
            paintText2.setColor(ColorScheme._TITLE_FG_COLOR);
            canvas.drawText(titles[i], box.left + marginHor, Y[i], paintText2);
            paintText2.setColor(colors[i]);
            // if (i==values.length-1) {
            // values[values.length-1]=values[values.length-1]+"手";
            // }

            canvas.drawText(values[i], box.right - marginHor - rightWidth[i], Y[i], paintText2);
        }

    }

    /**
     * 画圆角框
     * @param box
     * @param radius
     * @param paint
     */
    protected void drawRoundBox(RectF box, float radius, Paint paint) {
        pop_paint.setStyle(android.graphics.Paint.Style.STROKE);
        // pop_paint.setColor(ColorScheme.FRAME_BOX_STROKE);

        pop_paint.setColor(Color.parseColor("#eaf5f9")); // pop弹窗窗体颜色
        canvas.drawRoundRect(box, radius, radius, pop_paint);

        pop_paint.setStyle(android.graphics.Paint.Style.FILL);
        // pop_paint.setColor(ColorScheme.FRAME_BOX_FILL);
        pop_paint.setColor(Color.parseColor("#eaf5f9")); // pop弹窗窗体颜色
        canvas.drawRoundRect(box, radius, radius, pop_paint);
    }

    protected String getDetailsString(int row, String tag) {
        JSONObject obj=getRowItem(chartData, row);
        if(obj != null)
            return getString(obj, tag);
        else
            return "";
    }

    protected float getDetailsFloat(int row, String tag) {
        JSONObject obj=getRowItem(chartData, row);
        if(obj != null)
            return getFloat(obj, tag);
        else
            return 0.0F;
    }

    protected int getDetailsSize() {
        return mDataLength;
    }

    protected int getDetailColor(String tag, int row) {
        return ColorScheme._HQ_EQ_COLOR;
    }

    protected float getFloat(JSONObject obj, String tag) {
        return ChartUtil.getFloat(obj, tag);
    }

    protected int getInt(JSONObject obj, String tag) {
        return ChartUtil.getInt(obj, tag);
    }

    protected double getDouble(JSONObject obj, String tag) {
        return ChartUtil.getDouble(obj, tag);
    }

    protected String getString(JSONObject obj, String tag) {
        return ChartUtil.getString(obj, tag);
    }

    protected JSONObject getRowItem(JSONArray arr, int row) {
        return ChartUtil.getRowItem(arr, row);
    }

    protected String formatTime(String time) {
        if(isEmpty(time))
            return "";
        if(time.length() == 4)
            return (new StringBuffer(time)).insert(2, ":").toString();
        else
            return time;
    }

    protected String formatDate(Date date1, int type) {
        // if (date1 == null)
        // return "";
        // if (type == 1)
        // return mDateFormat1.format(date1);
        // else
        // return mDateFormat2.format(date1);

        if(date1 == null) {
            return "";
        } else if(type == 1) {
            return mDateFormat1.format(date1);
        } else if(type == 2) {
            return mDateFormat2.format(date1);
        } else {
            return mDateFormat3.format(date1);
        }
    }

    protected Date parseDate(String datestr) {
        return ChartUtil.parseDate(datestr);
    }

    protected String formatCjl(float cjl) {
        return ChartUtil.getIntelligentNum(cjl, 1, 2);
    }

    protected boolean isLandscape() {
        Configuration newConfig=getResources().getConfiguration();
        return newConfig.orientation == 2;
    }

    protected boolean isEmpty(String str) {
        return ChartUtil.isEmpty(str);
    }

    protected boolean moveCurosrTo(int index) {
        int oldCursorIndex=m_nCursorIndex;
        if(mDataLength == 0)
            m_nCursorIndex=0;
        else if(index < 0)
            m_nCursorIndex=0;
        else if(index >= mDataLength)
            m_nCursorIndex=mDataLength - 1;
        else
            m_nCursorIndex=index;
        boolean changed=oldCursorIndex != m_nCursorIndex;
        if(changed && mCursorPosChangedListener != null) {
            JSONObject data=getRowItem(chartData, m_nCursorIndex);
            if(data != null)
                mCursorPosChangedListener.onCursorChanged(this, m_nCursorIndex, data);
        }
        return changed;
    }

    protected boolean setShowCursor(boolean show) {
        if(show != needPaintCursor) {
            if(mCursorPosChangedListener != null)
                mCursorPosChangedListener.onCursorChanged(this, -1, null);
            needPaintCursor=show;
            return true;
        } else {
            return false;
        }
    }

    // 修复分时 k线崩溃问题
    protected final boolean setTouchCursorLine(boolean show, MotionEvent event) {
        boolean changedShow=false;
        boolean changedCursorPos=false;
        if(chartRect != null && chartRect.contains(event.getX(), event.getY()) || indicatorRect != null
            && indicatorRect.contains(event.getX(), event.getY())) {
            if(show) {
                if(mDataLength > 0) {
                    changedShow=setShowCursor(show);
                    int index=getPosByX(event.getX());
                    if(index < 0)
                        changedCursorPos=moveCurosrTo(0);
                    else if(index >= mDataLength)
                        changedCursorPos=moveCurosrTo(mDataLength - 1);
                    else
                        changedCursorPos=moveCurosrTo(index);
                }
            } else {
                changedShow=setShowCursor(show);
            }
        } else if(!show)
            changedShow=setShowCursor(show);
        return changedShow || changedCursorPos;
    }

    protected boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    protected boolean onHorizontalScroll(float distanceX) {
        return false;
    }

    protected boolean onShowEvent(MotionEvent e, boolean show) {
        return false;
    }

    protected boolean onSingleClick(MotionEvent e) {
        if(mMidTextClickListener != null && midBlankRect != null && midBlankRect.contains(e.getX(), e.getY())
            && e.getX() < midTextWidth + midBlankRect.left)
            mMidTextClickListener.onClick(this);
        return false;
    }

    protected abstract int getPosByX(float f);

}
