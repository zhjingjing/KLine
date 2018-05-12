package com.kchart.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kchart.BaseApplication;

/**
 * @project_name：EveryOneStockGod_2.0
 * @author：ANXAINJIE
 * @update_time：2015-3-13 下午2:47:09
 * @version 没有任何触发时间的分时图 主要用于股票详情页面的分时图
 */

public final class FsChart extends ChartBase {

    private static final String TAG="FsChart";

    private String time[];

    public FsChart(Context context) {
        super(context);
    }

    public FsChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onCreate() {
        supportGesture=true;
        super.onCreate();
    }

    public final boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        boolean invalidate=false;
        switch(event.getAction()) {
            case 0: // '\0'
            case 2: // '\002'
                invalidate=setTouchCursorLine(true, event);
                break;

            case 1: // '\001'
                invalidate=setTouchCursorLine(false, event);
                break;
        }
        if(invalidate)
            invalidate();
        return true;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(base != null && chartData != null) {
            drawHQChart(); // 画分时线、均线、成交量竖线
            // drawMidText1();
        } else {
            drawGrid();
        }
    }

    protected void drawMidText1() {
        if(isLandscape()) {
            return;
        } else {
            float X=0;
            float Y=midBlankRect.bottom - ((midBlankRect.height() - textHeigth) * 2.0F) / 3F;
            paintText3.setColor(Color.parseColor("#ffffff"));
            // String intelligentNum=ChartUtil.getIntelligentNum(m_maxCJLBL, 1, 2);
            ChartUtil.setTextSize(paintText3, 10, BaseApplication.getInstance());
            canvas.drawText("11.22", X, Y, paintText3);
            Y=textHeigth;
            canvas.drawText("11.23", X, Y, paintText3);

            return;
        }
    }

    protected int getPosByX(float x) {
        double perX=(chartRect.width() * 1000F) / 242F;
        int index=(int)((double)((x - indicatorRect.left) * 1000F) / perX);
        return index;
    }

    protected void initDataParam() {
        super.initDataParam();
        time=(new String[]{"9:30", "11:30/13:00", "15:00"});
        m_currPrice=getFloat(base, "current_price");
        m_currL=getFloat(base, "trade_volume");
        m_priceLast=getFloat(base, "yestod_end_price");
        m_maxPrice=getFloat(base, "today_max");
        m_minPrice=getFloat(base, "today_min");


        m_maxCJLBL=0.0F;
        float temp=0.0F;
        for(int i=0; i < mDataLength; i++) {
            temp=getDetailsFloat(i, "volume");
            if(m_maxCJLBL < temp)
                m_maxCJLBL=temp;
        }

        float maxPrice= Math.max(Math.abs(m_maxPrice - m_priceLast), Math.abs(m_minPrice - m_priceLast));
        float xzs=m_priceLast / 500F;
        m_maxPrice=m_priceLast + m_priceLast / 500F;
        m_minPrice=m_priceLast - m_priceLast / 500F;
        m_maxPrice=m_priceLast + xzs + maxPrice;
        m_minPrice=m_priceLast - xzs - maxPrice;
        mPriceArr[0]=m_maxPrice;
        mPriceArr[4]=m_minPrice;
        mPriceArr[2]=m_priceLast;
        mPriceArr[1]=(mPriceArr[0] + mPriceArr[2]) / 2.0F;
        mPriceArr[3]=(mPriceArr[2] + mPriceArr[4]) / 2.0F;
        double zdf=((m_maxPrice - m_priceLast) * 100F) / (2.0F * m_priceLast);


        mZdfArr[0]=(new StringBuilder(String.valueOf(ChartUtil.getIntelligentNum(zdf * 2D, 2)))).append("%").toString();
        mZdfArr[1]=(new StringBuilder(String.valueOf(ChartUtil.getIntelligentNum(zdf, 2)))).append("%").toString();
        // LogUtil.i(TAG, "mZdfArr[0]:"+mZdfArr[0]);
        // LogUtil.i(TAG, "mZdfArr[1]:"+mZdfArr[1]);
        mZdfArr[2]="0.00%";
        mZdfArr[3]=(new StringBuilder("-")).append(mZdfArr[1]).toString();
        mZdfArr[4]=(new StringBuilder("-")).append(mZdfArr[0]).toString();
    }

    protected void initDrawFrame() {
        super.initDrawFrame();
        mY[0]=chartRect.top + textHeigth * 0.7F;
        mY[4]=chartRect.bottom - textHeigth * 0.1F;
        mY[2]=(mY[0] + mY[4]) / 2.0F;
        mY[1]=(mY[0] + mY[2]) / 2.0F;
        mY[3]=(mY[2] + mY[4]) / 2.0F;
    }

    private void drawGrid() {
        float p_y=chartRect.centerY();
        float p_x=chartRect.centerX();
        canvas.drawRect(chartRect, talbePaint);
        canvas.drawLine(chartRect.left, p_y, chartRect.right, p_y, talbePaint);
        canvas.drawLine(p_x, chartRect.top, p_x, chartRect.bottom, talbePaint);
        float p_y2=(chartRect.top + p_y) / 2.0F;
        canvas.drawLine(chartRect.left, p_y2, chartRect.right, p_y2, dashedPaint);
        float p_y3=(chartRect.bottom + p_y) / 2.0F;
        canvas.drawLine(chartRect.left, p_y3, chartRect.right, p_y3, dashedPaint);
        float p_x1=(p_x + chartRect.left) / 2.0F;
        canvas.drawLine(p_x1, chartRect.top, p_x1, chartRect.bottom, dashedPaint);
        float p_x2=(p_x + chartRect.right) / 2.0F;
        canvas.drawLine(p_x2, chartRect.top, p_x2, chartRect.bottom, dashedPaint);
        canvas.drawRect(indicatorRect, talbePaint);
        Paint paint3=new Paint();
        paint3.setColor(-256);
        float j_p_y=indicatorRect.centerY();
        float j_p_x2=indicatorRect.centerX();
        canvas.drawLine(indicatorRect.left, j_p_y, indicatorRect.right, j_p_y, talbePaint);
        canvas.drawLine(j_p_x2, indicatorRect.top, j_p_x2, indicatorRect.bottom, talbePaint);
        float j_p_y3=(j_p_x2 + indicatorRect.left) / 2.0F;
        canvas.drawLine(j_p_y3, indicatorRect.top, j_p_y3, indicatorRect.bottom, talbePaint);
        float j_p_y4=(j_p_x2 + indicatorRect.right) / 2.0F;
        canvas.drawLine(j_p_y4, indicatorRect.top, j_p_y4, indicatorRect.bottom, talbePaint);
    }

    private void drawPrice() {
        float margin=ChartUtil.dip2px(mContext, 1.0F);
        float marginLeft1=paintText1.measureText(mZdfArr[2]) + margin;
        float marginLeft2=paintText1.measureText((new StringBuilder("-")).append(mZdfArr[2]).toString()) + margin;
        for(int i=0; i < mPriceArr.length; i++) {
            if(i < 2)
                paintText1.setColor(Color.parseColor("#df5d57"));
            else if(i == 2)
                paintText1.setColor(Color.parseColor("#494949"));
            else if(i > 2)
                paintText1.setColor(Color.parseColor("#42ad8b"));
            String price=ChartUtil.getIntelligentNum(mPriceArr[i], precision);
            canvas.drawText(price, chartRect.left, mY[i], paintText1);
            canvas.drawText(mZdfArr[i], chartRect.right - (i <= 2 ? marginLeft1 : marginLeft2), mY[i], paintText1);
        }

        float timeWidth1=paintText1.measureText(time[1]) + margin;
        float timeWidth2=paintText1.measureText(time[2]);
        float timeBottom=textHeigth + indicatorRect.bottom;
        paintText1.setColor(Color.parseColor("#31302f"));
        canvas.drawText(time[0], indicatorRect.left + margin, timeBottom, paintText1);
        canvas.drawText(time[1], indicatorRect.centerX() - timeWidth1 / 2.0F, timeBottom, paintText1);
        canvas.drawText(time[2], indicatorRect.right - timeWidth2, timeBottom, paintText1);
    }

    private void drawHQChart() {
        if(chartData != null) {
            chartPaint.setColor(ColorScheme._HQ_FONT_COLOR); // 阴影颜色
            drawChartLine(chartPaint, indicatorRect, "volume", m_maxCJLBL, 0.0F, 242, 1, true);
            chartPaint.setColor(Color.parseColor("#ffffff")); // 分时线
            drawChartLine(chartPaint, chartRect, "price", m_maxPrice, m_minPrice, 242, 3);
            volumePaint.setColor(Color.parseColor("#ffffff")); // 0轴均线
            drawChartLine(volumePaint, chartRect, "avg_price", m_maxPrice, m_minPrice, 242, 5);
        }
    }

    protected void drawFrameData() {
        if(chartData != null)
            try {
                String keys[]={"时:", "价:", "均"};
                String vals[]=new String[keys.length];
                int colors[]=new int[keys.length];
                vals[0]=needPaintCursor ? getDetailsString(m_nCursorIndex, "time") : getDetailsString(mDataLength - 1, "time");
                colors[0]= Color.parseColor("#31302f");
                float ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "price") : m_currPrice;
                vals[1]=ChartUtil.getIntelligentNum(ftTemp, 2);
                colors[1]=ChartUtil.getColor(m_priceLast, ftTemp);
                ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "avg_price") : m_currPrice;
                vals[2]=ChartUtil.getIntelligentNum(ftTemp, 2);
                colors[2]=colors[1];
                ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "volume") : m_currL;
                String cjslStr=ChartUtil.getIntelligentNum(ftTemp, 2);
                float margin=ChartUtil.dip2px(getContext(), 8F);
                float x=0.0F - margin / 2.0F;
                float x2=paintText2.measureText(keys[0]);
                float y=chartRect.top - ChartUtil.dip2px(getContext(), 5F);
                float delta=chartRect.left + 2.0F;
                if(delta < 2.0F)
                    delta=2.0F;
                paintText2.setColor(Color.parseColor("#31302f"));
                canvas.drawText((new StringBuilder("量:")).append(cjslStr).toString(), delta,
                    chartRect.bottom + paintText2.getTextSize(), paintText2);
                float width=0.0F;
                for(int i=0; i < keys.length; i++) {
                    x+=width;
                    x+=margin;
                    paintText2.setColor(Color.parseColor("#31302f"));
                    canvas.drawText(keys[i], x, y, paintText2);
                    paintText2.setColor(colors[i]);
                    x+=x2;
                    canvas.drawText(vals[i], x, y, paintText2);
                    width=paintText2.measureText(vals[i]);
                }

            } catch(Exception exception) {
            }
    }

    private float drawCursorLine() {
        float newY=0.0F;
        if(needPaintCursor) {
            float nItemWidth=chartRect.width() / 242F;
            nItemWidth/=2.0F;
            float perX=(chartRect.width() * 1000F) / 242F;
            float newX=chartRect.left + (perX * (float)m_nCursorIndex) / 1000F + nItemWidth;
            drawCursorLine(newX);
            float perY=1.0F;
            if(m_maxPrice - m_minPrice != 0.0F)
                perY=(chartRect.height() * 100000F) / (m_maxPrice - m_minPrice);
            newY=chartRect.bottom - ((getDetailsFloat(m_nCursorIndex, "price") - m_minPrice) * perY) / 100000F;
            canvas.drawLine(chartRect.left, newY, chartRect.right, newY, cursorPaint);
            int color=ChartUtil.getColor(m_priceLast, getDetailsFloat(m_nCursorIndex, "price"));
            trafficLightPaint.setColor(color);
            drawTrafficLight(newX, newY, 5F, trafficLightPaint);
            drawCursorInfo(newX, newY);
            drawDetailInfo(newX, newY);
        }
        return newY;
    }

    private void drawCursorInfo(float x, float y) {
        float top=0.0F;
        float bottom=0.0F;
        float left=0.0F;
        float right=0.0F;
        String vals[]=new String[3];
        vals[0]=needPaintCursor ? getFsTime(m_nCursorIndex) : getFsTime(mDataLength - 1);
        float ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "price") : m_currPrice;
        vals[1]=ChartUtil.getIntelligentNum(ftTemp, 2);
        ftTemp-=m_priceLast;
        double zdf=ftTemp;
        zdf=(ftTemp * 100F) / m_priceLast;
        vals[2]=(new StringBuilder(String.valueOf(ChartUtil.getIntelligentNum(zdf, precision)))).append("%").toString();
        float margin=ChartUtil.dip2px(getContext(), 2.0F);
        float widthTime=paintText1.measureText(vals[0]);
        float widthPrice=paintText1.measureText(vals[1]);
        float widthZDF=paintText1.measureText(vals[2]);
        float TextHeigth=textHeigth + margin * 2.0F;
        top=y - TextHeigth / 2.0F;
        bottom=y + TextHeigth / 2.0F;
        left=x - widthTime / 2.0F;
        right=x + widthTime / 2.0F;
        if(left - margin < chartRect.left) {
            left=chartRect.left + margin;
            right=left + widthTime;
        }
        if(right + margin > chartRect.right) {
            right=chartRect.right - margin;
            left=right - widthTime;
        }
        RectF leftR=new RectF(chartRect.left, top, chartRect.left + widthPrice + margin * 2.0F, bottom);
        RectF rightR=new RectF(chartRect.right - margin * 2.0F - widthZDF, top, chartRect.right, bottom);
        RectF bttomR=new RectF(left - margin, indicatorRect.bottom, right + margin, (indicatorRect.bottom + TextHeigth) - margin);
        paintText1.setColor(ColorScheme._FS_FRAME_BG_COLOR);
        canvas.drawRect(leftR, paintText1);
        canvas.drawRect(rightR, paintText1);
        canvas.drawRect(bttomR, paintText1);
        paintText1.setColor(ColorScheme._FS_FRAME_TEXT_COLOR);
        canvas.drawText(vals[0], bttomR.left + margin, bttomR.bottom - margin, paintText1);
        canvas.drawText(vals[1], leftR.left + margin, leftR.bottom - margin * 2.0F, paintText1);
        canvas.drawText(vals[2], rightR.left + margin, rightR.bottom - margin * 2.0F, paintText1);
    }

    private void drawDetailInfo(float x, float y) {
        String vals[]=new String[5];
        vals[0]=needPaintCursor ? getFsTime(m_nCursorIndex) : getFsTime(mDataLength - 1);
        float ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "price") : m_currPrice;
        vals[1]=ChartUtil.getIntelligentNum(ftTemp, precision);
        // 去掉修复红绿只有一个bug
        int color=ChartUtil.getColor(m_priceLast, ftTemp);
        ftTemp-=m_priceLast;
        double zdf=ftTemp;
        zdf=(ftTemp * 100F) / m_priceLast;

        vals[2]=(new StringBuilder(String.valueOf(ChartUtil.getIntelligentNum(zdf, precision)))).append("%").toString();

        ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "avg_price") : m_currPrice;
        vals[3]=ChartUtil.getIntelligentNum(ftTemp, precision);
        ftTemp=needPaintCursor ? getDetailsFloat(m_nCursorIndex, "volume") : m_currPrice;
        vals[4]=formatCjl(ftTemp);
        float maxRightWidth=0.0F;
        float rightWidth[]=new float[5];
        for(int i=0; i < vals.length; i++)
            if(vals[i] != null) {
                rightWidth[i]=paintText2.measureText(vals[i]);
                if(maxRightWidth < rightWidth[i])
                    maxRightWidth=rightWidth[i];
            }

        String titleArr[]={"时间:", "现价:", "涨幅:", "均价:", "现量:"};
        int colors[]={Color.parseColor("#31302f"), color, color, color, Color.parseColor("#31302f")};
        drawFrameInfo(5, x, titleArr, vals, colors);
        int j=3;
        if(j == 3)
            return;
        float titleWidth=paintText2.measureText(titleArr[0]);
        float marginBox=ChartUtil.dip2px(mContext, 10F);
        float marginHor=ChartUtil.dip2px(mContext, 2.0F);
        float marginVer=ChartUtil.dip2px(mContext, 2.0F);
        float boxWidth=titleWidth + maxRightWidth + 3F * marginHor;
        float boxHeight=textHeigth * (float)titleArr.length + 8F * marginVer;
        RectF box=new RectF();
        if(x > chartRect.centerX())
            box.set(chartRect.left + marginBox, chartRect.top + marginBox, chartRect.left + marginBox + boxWidth, chartRect.top
                + marginBox + boxHeight);
        else
            box.set(chartRect.right - marginBox - boxWidth, chartRect.top + marginBox, chartRect.right - marginBox, chartRect.top
                + marginBox + boxHeight);
        float Y[]=new float[5];
        Y[4]=box.bottom - marginVer * 3F;
        Y[3]=Y[4] - marginVer - textHeigth;
        Y[2]=Y[3] - marginVer - textHeigth;
        Y[1]=Y[2] - marginVer - textHeigth;
        Y[0]=Y[1] - marginVer - textHeigth;
        float radius=ChartUtil.dip2px(mContext, 3F);
        drawRoundBox(box, radius, pop_paint);
        paintText2.setColor(ColorScheme._TITLE_FG_COLOR);
        for(int i=0; i < vals.length; i++) {
            canvas.drawText(titleArr[i], box.left + marginHor, Y[i], paintText2);
            canvas.drawText(vals[i], box.right - marginHor - rightWidth[i], Y[i], paintText2);
        }

    }

    private String getFsTime(int index) {
        int cur=index;
        String tm="--:--";
        if(cur >= 0 && cur < mDataLength) {
            org.json.JSONObject obj=getRowItem(chartData, cur);
            tm=getString(obj, "time");
            tm=formatTime(tm);
        }
        return tm;
    }

    protected int getDetailColor(String tag, int row) {
        if("volume".equals(tag) && row >= 0 && row < mDataLength) {
            float curPrice=getDetailsFloat(row, "price");
            float lastPrice=0.0F;
            if(row == 0)
                lastPrice=m_priceLast;
            else
                lastPrice=getDetailsFloat(row - 1, "price");
            return ChartUtil.getColorTrans(lastPrice, curPrice);
        } else {
            return super.getDetailColor(tag, row);
        }
    }
}
