package com.kchart.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Calendar;
import java.util.Date;

public final class KxChart extends ChartBase {

    private static final String TAG="KxChart";

    private Calendar cal;

    private boolean hasVol;

    private float kxItemWidth;

    private float kxMargin;

    private float kxWidth;

    private float harfKxWidth;

    private float kxWidthUnit;

    private float minKxWidth;

    private float maxKxWidth;

    private float boxWidth;

    private int maxDisCount;

    private int firstDisPos;

    private int lastDisPos;

    private String lastDisIndecator;

    private float ma5P;

    private float ma10P;

    private float ma20P;

    private float cjl;

    public KxChart(Context context) {
        super(context);
        cal= Calendar.getInstance();
        hasVol=true;
        kxItemWidth=0.0F;
        boxWidth=0.0F;
        maxDisCount=0;
        firstDisPos=0;
        lastDisPos=0;
        lastDisIndecator=null;
    }

    public KxChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        cal= Calendar.getInstance();
        hasVol=true;
        kxItemWidth=0.0F;
        boxWidth=0.0F;
        maxDisCount=0;
        firstDisPos=0;
        lastDisPos=0;
        lastDisIndecator=null;
    }

    protected void onCreate() {
        supportGesture=true;
        supportZoom=true;
        super.onCreate();
        MIN_ZOOM_RANGE=ChartUtil.dip2px(mContext, 5F);
        maxKxWidth=ChartUtil.dip2px(mContext, 18F);
        minKxWidth=ChartUtil.dip2px(mContext, 3F);
        kxWidthUnit=ChartUtil.dip2px(mContext, 0.3F);
        kxMargin=ChartUtil.dip2px(mContext, 1.0F);
        kxWidth=ChartUtil.dip2px(mContext, 6F);
        harfKxWidth=kxWidth / 2.0F;
        kxItemWidth=kxWidth + kxMargin;
    }

    protected void initDrawFrame() {
        super.initDrawFrame();
        initDisplayParams();
        calculateScreenData();
    }

    protected void initDataParam() {
        super.initDataParam();
        if(lastDisPos <= 0)
            lastDisPos=mDataLength;
        else if(isEmpty(lastDisIndecator))
            lastDisPos=mDataLength;
        else
            lastDisPos=findPosByIndecator(lastDisIndecator);
        lastDisIndecator=findIndecatorByPos(lastDisPos);
        if(base != null) {
            m_currPrice=getFloat(base, "now_pri");
            m_priceLast=getFloat(base, "yestod_end_pri");
            m_currPrice=getFloat(base, "now_pri");
            m_currL=getFloat(base, "trade_volume");
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(base != null && chartData != null) {
            drawGrid(); // 画K线方框和成交量的方框
            drawHQChart(); // 画K线蜡烛柱子 画成交量柱线，画均线
            drawPrice(); // 画左侧的5个价格
            drawCursorLine(); // 画光标
            drawMidText2(); // 画横屏查看更多

        } else {
            drawGrid();
        }
    }

    protected void drawMidText2() {
        if(isLandscape()) {
            return;
        } else {
            float margin=ChartUtil.dip2px(getContext(), 2.0F);
            float X=midBlankRect.left + margin;
            float Y=midBlankRect.bottom - ((midBlankRect.height() - textHeigth) * 2.0F) / 3F;
            paintText3.setColor(Color.parseColor("#494949"));

            // Log.i(TAG, "m_maxCJLBL2:"+m_maxCJLBL);
            // DecimalFormat df = new DecimalFormat("0.00");
            // if (m_maxCJLBL>9999f) {
            // canvas.drawText(df.format(m_maxCJLBL/10000)+"万手", X, Y, paintText3);
            // }else if (m_maxCJLBL>99999999f) {
            // canvas.drawText(df.format(m_maxCJLBL/100000000)+"亿手", X, Y, paintText3);
            // }else {
            // canvas.drawText(df.format(m_maxCJLBL)+"手", X, Y, paintText3);
            // }

            String intelligentNum=ChartUtil.getIntelligentNum(m_maxCJLBL, 1, 2);

            canvas.drawText(intelligentNum, X, Y, paintText3);

            return;
        }
    }

    private void initDisplayParams() {
        boxWidth=chartRect.width();
        kxItemWidth=kxWidth + kxMargin;
        maxDisCount=(int)(boxWidth / kxItemWidth);
        firstDisPos=lastDisPos - maxDisCount;
        if(firstDisPos < 0)
            firstDisPos=0;
        if(m_nCursorIndex < firstDisPos)
            m_nCursorIndex=firstDisPos;
        if(m_nCursorIndex >= lastDisPos)
            m_nCursorIndex=lastDisPos - 1;
    }

    private void calculateScreenData() {
        if(DEBUG)
            Log.d("Chart", (new StringBuilder("calculateScreenData firstDisPos:")).append(firstDisPos).append(" lastDisPos:")
                .append(lastDisPos).toString());
        m_maxPrice=0.0F;
        m_minPrice=mDataLength <= 0 ? 3.402823E+038F : getDetailsFloat(0, "low");
        m_maxCJLBL=0.0F;
        for(int i=firstDisPos; i < lastDisPos; i++) {
            float ma5=getDetailsFloat(i, "ma5");
            float ma10=getDetailsFloat(i, "ma10");
            float ma20=getDetailsFloat(i, "ma20");
            float temp=getDetailsFloat(i, "high");
            m_maxPrice=ChartUtil.getMax(new float[]{ma5, ma10, ma20, temp, m_maxPrice});
            temp=getDetailsFloat(i, "low");
            m_minPrice=ChartUtil.getMin(new float[]{ma5, ma10, ma20, temp, m_minPrice});
            temp=getDetailsFloat(i, "volume");
            m_maxCJLBL=ChartUtil.getMax(new float[]{temp, m_maxCJLBL});

        }

        float priceFd=m_maxPrice - m_minPrice;
        float minFd=m_maxPrice / 100F;
        if(priceFd < minFd)
            if(m_minPrice < minFd) {
                m_maxPrice+=minFd;
            } else {
                m_maxPrice+=minFd / 2.0F;
                m_minPrice-=minFd / 2.0F;
            }
        if(m_maxCJLBL <= 0.0F)
            Log.e("Chart", "max volume <= 0,So we can not draw cjl");

        mPriceArr[0]=m_maxPrice;
        mPriceArr[4]=m_minPrice;
        mPriceArr[2]=(m_minPrice + m_maxPrice) / 2.0F;
        mPriceArr[1]=(mPriceArr[0] + mPriceArr[2]) / 2.0F;
        mPriceArr[3]=(mPriceArr[2] + mPriceArr[4]) / 2.0F;
    }

    private int findPosByIndecator(String indecator) {
        String tmp=null;
        for(int i=mDataLength - 1; i >= 0; i--) {
            tmp=getDetailsString(i, "date");
            if(indecator.equals(tmp))
                return i + 1;
        }

        return mDataLength;
    }

    private String findIndecatorByPos(int pos) {
        if(mDataLength > 0 && pos > 1 && pos <= mDataLength)
            return getDetailsString(pos - 1, "date");
        else
            return null;
    }

    private void drawGrid() {
        canvas.drawRect(chartRect, talbePaint);
        float p_y=chartRect.centerY();
        canvas.drawLine(chartRect.left, p_y, chartRect.right, p_y, dashedPaint);
        float p_y2=(chartRect.top + p_y) / 2.0F;
        canvas.drawLine(chartRect.left, p_y2, chartRect.right, p_y2, dashedPaint);
        float p_y3=(chartRect.bottom + p_y) / 2.0F;
        canvas.drawLine(chartRect.left, p_y3, chartRect.right, p_y3, dashedPaint);
        canvas.drawRect(indicatorRect, talbePaint);
    }

    private void drawPrice() {
        for(int i=0; i < mPriceArr.length; i++) {
            paintText1.setColor(Color.parseColor("#494949"));
            String price=ChartUtil.getIntelligentNum(mPriceArr[i], precision);
            canvas.drawText(price, chartRect.left, mY[i], paintText1);
        }

        // 新增加5 10 20线的颜色指示器
        // paintText1.setColor(Color.parseColor("#3d78ff"));
        // canvas.drawText("MA5:" + ma5P, chartRect.right - ChartUtil.dip2px(mContext, 255.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#dda843"));
        // canvas.drawText("MA10:" + ma10P, chartRect.right - ChartUtil.dip2px(mContext, 170.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#ce4bce"));
        // canvas.drawText("MA20:" + ma20P, chartRect.right - ChartUtil.dip2px(mContext, 80.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        // paintText1.setColor(Color.parseColor("#3d78ff"));
        // canvas.drawText("MA5:" , chartRect.right - ChartUtil.dip2px(mContext, 255.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#dda843"));
        // canvas.drawText("MA10:" , chartRect.right - ChartUtil.dip2px(mContext, 170.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#ce4bce"));
        // canvas.drawText("MA20:" , chartRect.right - ChartUtil.dip2px(mContext, 80.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);

    }

    private void drawHQChart() {
        if(chartData == null || mDataLength < 1)
            return;
        float nHeight_kx=chartRect.height();
        float nBottom_kx=chartRect.bottom;
        float nHeight_tech=indicatorRect.height();
        float nBottom_tech=indicatorRect.bottom;
        cjl=0.0F;
        float cjlY=0.0F;
        Date lastDate=null;
        int lastDrawDatePos=-1;
        boolean drawDate=false;
        Path pathMa5=new Path();
        Path pathMa10=new Path();
        Path pathMa20=new Path();
        boolean ma5HasAddFistPoint=false;
        boolean ma10HasAddFistPoint=false;
        boolean ma20HasAddFistPoint=false;
        float priceFd=m_maxPrice - m_minPrice;
        int k=0;
        int reversK=-1;
        for(int i=firstDisPos; i < lastDisPos; i++) {
            k=i;
            reversK=i - firstDisPos;
            float newX=chartRect.left + kxMargin + kxItemWidth * (float)reversK;
            float newXCenter=newX + harfKxWidth;
            float newX2=newX + kxWidth;
            float openP=getDetailsFloat(k, "open");
            float highP=getDetailsFloat(k, "high");
            float lowP=getDetailsFloat(k, "low");
            float closeP=getDetailsFloat(k, "close");
            float lowY=nBottom_kx - ((lowP - m_minPrice) * nHeight_kx) / priceFd;
            float highY=nBottom_kx - ((highP - m_minPrice) * nHeight_kx) / priceFd;
            float openY=nBottom_kx - ((openP - m_minPrice) * nHeight_kx) / priceFd;
            float closeY=nBottom_kx - ((closeP - m_minPrice) * nHeight_kx) / priceFd;
            if(closeP < openP) {
                chartPaint.setColor(Color.parseColor("#42ad8b"));
                chartPaint.setStyle(android.graphics.Paint.Style.FILL);
                canvas.drawRect(new RectF(newX, openY, newX2, closeY), chartPaint);
                if(lowP < closeP)
                    canvas.drawLine(newXCenter, lowY, newXCenter, closeY, chartPaint);
                if(highP > openP)
                    canvas.drawLine(newXCenter, highY, newXCenter, openY, chartPaint);
            } else if(closeP > openP) {
                chartPaint.setColor(Color.parseColor("#df5d57"));
                chartPaint.setStyle(android.graphics.Paint.Style.FILL);
                canvas.drawRect(new RectF(newX, closeY, newX2, openY), chartPaint);
                if(lowP < openP)
                    canvas.drawLine(newXCenter, lowY, newXCenter, openY, chartPaint);
                if(highP > closeP)
                    canvas.drawLine(newXCenter, highY, newXCenter, closeY, chartPaint);
            } else {
                chartPaint.setColor(Color.parseColor("#494949"));
                canvas.drawLine(newX, openY, newX2, openY, chartPaint);
                if(lowP < openP)
                    canvas.drawLine(newXCenter, lowY, newXCenter, openY, chartPaint);
                if(highP > openP)
                    canvas.drawLine(newXCenter, highY, newXCenter, openY, chartPaint);
            }
            paintText2.setColor(Color.parseColor("#494949"));
            String dateStr=getDetailsString(k, "date");
            Date curDate=parseDate(dateStr);
            drawDate=drawVerTimeLine(k, curDate, lastDate, lastDrawDatePos, newXCenter);
            if(drawDate)
                lastDrawDatePos=k;
            lastDate=curDate;
            if(hasVol && m_maxCJLBL > 0.0F) {
                cjl=getDetailsFloat(k, "volume");
                cjlY=nBottom_tech - (cjl * nHeight_tech) / m_maxCJLBL;
                if(indicatorRect.contains(newX, cjlY)) {
                    if(closeP < openP)
                        chartPaint.setStyle(android.graphics.Paint.Style.FILL);
                    else
                        chartPaint.setStyle(android.graphics.Paint.Style.FILL);
                    if(closeP == openP)
                        chartPaint.setColor(Color.parseColor("#df5d57"));
                    canvas.drawRect(new RectF(newX, cjlY, newX2, nBottom_tech), chartPaint);
                }
            }
            /* float */ma5P=getDetailsFloat(k, "ma5");
            /* float */ma10P=getDetailsFloat(k, "ma10");
            /* float */ma20P=getDetailsFloat(k, "ma20");
            if(ma5P > m_minPrice) {
                float ma5Y=nBottom_kx - ((ma5P - m_minPrice) * nHeight_kx) / priceFd;
                if(!ma5HasAddFistPoint) {
                    ma5HasAddFistPoint=true;
                    pathMa5.moveTo(newXCenter, ma5Y);
                } else {
                    pathMa5.lineTo(newXCenter, ma5Y);
                }
            }
            if(ma10P > m_minPrice) {
                float ma10Y=nBottom_kx - ((ma10P - m_minPrice) * nHeight_kx) / priceFd;
                if(!ma10HasAddFistPoint) {
                    ma10HasAddFistPoint=true;
                    pathMa10.moveTo(newXCenter, ma10Y);
                } else {
                    pathMa10.lineTo(newXCenter, ma10Y);
                }
            }
            if(ma20P > m_minPrice) {
                float ma20Y=nBottom_kx - ((ma20P - m_minPrice) * nHeight_kx) / priceFd;
                if(!ma20HasAddFistPoint) {
                    ma20HasAddFistPoint=true;
                    pathMa20.moveTo(newXCenter, ma20Y);
                } else {
                    pathMa20.lineTo(newXCenter, ma20Y);
                }
            }
            if(needPaintCursor && m_nCursorIndex == k && DEBUG)
                Log.d("Chart",
                    (new StringBuilder("drawCursorLine ")).append(m_nCursorIndex).append(" open:").append(openP).append(" high:")
                        .append(highP).append(" low:").append(lowP).append(" close:").append(closeP).append(" vol:").append(cjl)
                        .toString());
        }

        chartPaint.setStyle(android.graphics.Paint.Style.STROKE);
        chartPaint.setColor(Color.parseColor("#3d78ff"));
        canvas.drawPath(pathMa5, chartPaint);
        chartPaint.setColor(Color.parseColor("#dda843"));
        canvas.drawPath(pathMa10, chartPaint);
        chartPaint.setColor(Color.parseColor("#ce4bce"));
        canvas.drawPath(pathMa20, chartPaint);
    }

    private boolean isDrawLine(Date today, Date lastDate, int disPos) {
        if(today == null)
            return false;
        cal.setTime(today);
        int month=cal.get(2);
        if("month".equals(LINE_TYPE))
            return month == 0 && disPos > 7;
        int day=cal.get(5);
        if("week".equals(LINE_TYPE))
            if(month == 0 || month == 3 || month == 6 || month == 10)
                return day < 7 && disPos > 7;
            else
                return false;
        if("day".equals(LINE_TYPE)) {
            if(lastDate != null) {
                cal.setTime(lastDate);
                int monthlast=cal.get(2);
                return disPos > 7 && (monthlast < month || month == 0 && monthlast == 11);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean drawVerTimeLine(int row, Date today, Date lastDate, int lastDrawPos, float centerX) {
        if(!isDrawLine(today, lastDate, row - lastDrawPos))
            return false;
        if(DEBUG)
            Log.d("Chart", (new StringBuilder("drawVerTimeLine ")).append(formatDate(today, 3)).toString());
        String date=formatDate(today, 3);
        float timeWidth=(int)paintText2.measureText((new StringBuilder(String.valueOf(date))).append(" ").toString());
        canvas.drawLine(centerX, chartRect.top, centerX, chartRect.bottom, dashedPaint);
        canvas.drawLine(centerX, indicatorRect.top, centerX, indicatorRect.bottom, dashedPaint);
        float x=centerX - timeWidth / 2.0F;
        canvas.drawText((new StringBuilder(String.valueOf(date))).append(" ").toString(), x, indicatorRect.bottom + textHeigth,
            paintText2);
        return true;
    }

    private void drawCursorLine() {
        if(needPaintCursor) {
            int reversK=m_nCursorIndex - firstDisPos;
            float newXCenter=chartRect.left + kxMargin + kxItemWidth * (float)reversK + harfKxWidth;
            float nheight=chartRect.height();
            float nBottom_kx=chartRect.top + nheight;
            float nPrice=m_maxPrice - m_minPrice;
            float nHeight_kx=nheight;
            float closeP=getDetailsFloat(m_nCursorIndex, "close");
            float newY=nBottom_kx - ((closeP - m_minPrice) * nHeight_kx) / nPrice;
            canvas.drawLine(chartRect.left, newY, chartRect.right, newY, cursorPaint);
            super.drawCursorLine(newXCenter);
            drawDetailInfo(newXCenter, newY);
            drawCursorInfo(newXCenter, newY);
        }
    }

    private void drawCursorInfo(float x, float y) {
        float left=0.0F;
        float right=0.0F;
        String date=formatDate(parseDate(getDetailsString(m_nCursorIndex, "date")), 2);
        float margin=ChartUtil.dip2px(getContext(), 2.0F);
        float widthTime=paintText1.measureText(date);
        float TextHeigth=textHeigth + margin * 2.0F;
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
        RectF bttomR=new RectF(left - margin, indicatorRect.bottom, right + margin, (indicatorRect.bottom + TextHeigth) - margin);
        paintText1.setColor(ColorScheme._FS_FRAME_BG_COLOR);
        canvas.drawRect(bttomR, paintText1);
        paintText1.setColor(ColorScheme._FS_FRAME_TEXT_COLOR);
        canvas.drawText(date, bttomR.left + margin, bttomR.bottom - margin, paintText1);
    }

    private void drawDetailInfo(float x, float y) {
        String titles[]={"日期:", "开盘价:", "最高价:", "最低价:", "收盘价:", "涨跌幅:"};
        String vals[]=new String[6];
        int colors[]=new int[6];
        float zdf=0.0F;
        float open=getDetailsFloat(m_nCursorIndex, "open");
        float high=getDetailsFloat(m_nCursorIndex, "high");
        float low=getDetailsFloat(m_nCursorIndex, "low");
        float close=getDetailsFloat(m_nCursorIndex, "close");
        float ma5=getDetailsFloat(m_nCursorIndex, "ma5");
        float ma10=getDetailsFloat(m_nCursorIndex, "ma10");
        float ma20=getDetailsFloat(m_nCursorIndex, "ma20");
        float zrsp;
        if(m_nCursorIndex > 0)
            zrsp=getDetailsFloat(m_nCursorIndex - 1, "close");
        else
            zrsp=open;
        if(zrsp > 0.0F)
            zdf=((close - zrsp) * 100F) / zrsp;
        colors[0]= Color.parseColor("#31302f");
        colors[1]=ChartUtil.getColor(zrsp, open);
        colors[2]=ChartUtil.getColor(zrsp, high);
        colors[3]=ChartUtil.getColor(zrsp, low);
        colors[4]=ChartUtil.getColor(zrsp, close);
        colors[5]=ChartUtil.getColor(0.0F, zdf);
        vals[0]=formatDate(parseDate(getDetailsString(m_nCursorIndex, "date")), 1);
        vals[1]=getDetailsString(m_nCursorIndex, "open");
        vals[2]=getDetailsString(m_nCursorIndex, "high");
        vals[3]=getDetailsString(m_nCursorIndex, "low");
        vals[4]=getDetailsString(m_nCursorIndex, "close");
        vals[5]=(new StringBuilder(String.valueOf(ChartUtil.getIntelligentNum(zdf, 2)))).append("%").toString();
        drawFrameInfo(6, x, titles, vals, colors);

        // 新增加5 10 20线的颜色指示器
        paintText1.setColor(Color.parseColor("#3d78ff"));
        canvas.drawText("MA5:" + ma5, chartRect.right - ChartUtil.dip2px(mContext, 255.0F), chartRect.top + textHeigth * 1.4F,
            paintText1);

        paintText1.setColor(Color.parseColor("#dda843"));
        canvas.drawText("MA10:" + ma10, chartRect.right - ChartUtil.dip2px(mContext, 170.0F), chartRect.top + textHeigth * 1.4F,
            paintText1);

        paintText1.setColor(Color.parseColor("#ce4bce"));
        canvas.drawText("MA20:" + ma20, chartRect.right - ChartUtil.dip2px(mContext, 80.0F), chartRect.top + textHeigth * 1.4F,
            paintText1);

        // paintText1.setColor(Color.parseColor("#3d78ff"));
        // canvas.drawText(ma5+"", chartRect.right - ChartUtil.dip2px(mContext, 255.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#dda843"));
        // canvas.drawText(ma10+"", chartRect.right - ChartUtil.dip2px(mContext, 170.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);
        //
        // paintText1.setColor(Color.parseColor("#ce4bce"));
        // canvas.drawText(ma20+"", chartRect.right - ChartUtil.dip2px(mContext, 40.0F), chartRect.top + textHeigth * 1.4F,
        // paintText1);

    }

    public void zoomIn() {
        zoomKxWidth(-kxWidthUnit);
    }

    public void zoomOut() {
        zoomKxWidth(kxWidthUnit);
    }

    private void zoomKxWidth(float posWidth) {
        if(posWidth > 0.0F) {
            if(kxWidth + posWidth > maxKxWidth)
                kxWidth=maxKxWidth;
            else
                kxWidth+=posWidth;
        } else if(posWidth < 0.0F)
            if(kxWidth + posWidth < minKxWidth)
                kxWidth=minKxWidth;
            else
                kxWidth+=posWidth;
        harfKxWidth=kxWidth / 2.0F;
        kxItemWidth=kxWidth + kxMargin;
    }

    public final boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        boolean invalidate=false;
        if(supportZoom)
            switch(event.getAction() & 0xff) {
                case 3: // '\003'
                case 4: // '\004'
                default:
                    break;

                case 0: // '\0'
                    mode=1;
                    if(DEBUG)
                        Log.d("Chart", "mode DRAG");
                    break;

                case 1: // '\001'
                case 6: // '\006'
                    mode=0;
                    if(DEBUG)
                        Log.d("Chart", "mode NONE");
                    break;

                case 5: // '\005'
                    oldDist=ChartUtil.spacing(event);
                    if(oldDist <= 10F)
                        break;
                    mode=2;
                    if(DEBUG)
                        Log.d("Chart", "mode ZOOM");
                    break;

                case 2: // '\002'
                    if(mode != 2)
                        break;
                    float newDist=ChartUtil.spacing(event);
                    if(DEBUG)
                        Log.d("Chart", (new StringBuilder("zoom ")).append(oldDist).append("->").append(newDist).toString());
                    if(newDist - oldDist > MIN_ZOOM_RANGE) {
                        zoomOut();
                        invalidate=true;
                    }
                    if(oldDist - newDist > MIN_ZOOM_RANGE) {
                        zoomIn();
                        invalidate=true;
                    }
                    if(DEBUG)
                        Log.d("Chart", "mode invalidate");
                    break;
            }
        if(invalidate) {
            invalidate();
            return true;
        }
        invalidate=false;

        if(isShowPress)

            switch(event.getAction()) {

                case 0: // '\0'
//                break;
                case 2: // '\002'
                    invalidate=setTouchCursorLine(true, event);
                    break;
                case 1: // '\001'
                    invalidate=setTouchCursorLine(false, event);
                    break;
            }
        else
            invalidate=setTouchCursorLine(false, event);
        if(invalidate)
            invalidate();
        return true;
    }

    protected boolean onShowEvent(MotionEvent event, boolean show) {
        boolean invalidate=false;
        if(!show)
            invalidate=setTouchCursorLine(false, event);
        else
            invalidate=setTouchCursorLine(true, event);
        if(invalidate)
            invalidate();
        return super.onShowEvent(event, show);
    }

    protected boolean onHorizontalScroll(float disX) {
        if(DEBUG)
            Log.d("Chart", (new StringBuilder("onHorizontalScroll firstDisPos:")).append(firstDisPos).append(" lastDisPos:")
                .append(lastDisPos).append(" maxDisCount:").append(maxDisCount).toString());
        if(mDataLength <= 0)
            return false;
        float absX= Math.abs(disX);
        if(absX < kxItemWidth)
            return false;
        int disPos=(int)(absX / kxItemWidth);
        if(disPos == 0)
            return false;
        boolean showCursorChanged=setShowCursor(false);
        boolean posChanged=false;
        int oldPos=lastDisPos;
        if(DEBUG)
            Log.d("Chart", (new StringBuilder("onHorizontalScroll disX:")).append(disX).append(" disPos:").append(disPos)
                .toString());
        if(disX < 0.0F) {
            int leftRemainPos=lastDisPos - maxDisCount;
            if(leftRemainPos >= disPos)
                lastDisPos-=disPos;
            else
                lastDisPos=maxDisCount;
        } else if(disX > 0.0F) {
            int rightRemainPos=mDataLength - lastDisPos;
            if(rightRemainPos >= disPos)
                lastDisPos+=disPos;
            else
                lastDisPos=mDataLength;
        }
        if(DEBUG)
            Log.d("Chart", (new StringBuilder("onHorizontalScroll update lastDisPos:")).append(lastDisPos).toString());
        posChanged=oldPos != lastDisPos;
        if(posChanged) {
            lastDisIndecator=getDetailsString(lastDisPos, "date");
            if(mScrollListener != null)
                mScrollListener.onScroll(this, lastDisPos - oldPos, lastDisPos - maxDisCount, lastDisPos);
        }
        if(showCursorChanged || posChanged)
            invalidate();
        return true;
    }

    protected int getPosByX(float x) {
        float perX=kxItemWidth * 1000F;
        int index=(int)(((x - indicatorRect.left) * 1000F) / perX);
        index=firstDisPos + index;
        return index;
    }
}
