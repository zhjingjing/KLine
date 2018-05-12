package com.kchart.aty;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kchart.R;
import com.kchart.chart.ChartBase;
import com.kchart.chart.FsChart;
import com.kchart.chart.FsChart2;
import com.kchart.chart.KxChart;
import com.kchart.chart.OnCursorChangedListener;
import com.kchart.chart.OnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class KActivity extends Activity implements View.OnClickListener, OnCursorChangedListener, OnScrollListener, RadioGroup.OnCheckedChangeListener {


    private ChartBase mFsChart;

    private ChartBase mKxChart1;

    private ChartBase mKxChart2;

    private ChartBase mKxChart3;

    private static final SimpleDateFormat mDateFormat=new SimpleDateFormat("yyyy-MM-dd");

    private static final String TAG="KActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_kchart);
        init();
    }

    private void init() {
        mFsChart=(FsChart2)findViewById(R.id.fs_View);
        mFsChart.setDebug(true);
        mFsChart.setOnLandscapeClickListener(this);
        mFsChart.setOnCursorChangedListener(this);
        mKxChart1=(KxChart)findViewById(R.id.kx_View1);
        mKxChart1.setDebug(true);
        mKxChart1.setOnLandscapeClickListener(this);
        mKxChart1.setOnCursorChangedListener(this);
        mKxChart1.setOnScrollListener(this);
        mKxChart2=(KxChart)findViewById(R.id.kx_View2);
        mKxChart2.setDebug(true);
        mKxChart2.setOnLandscapeClickListener(this);
        mKxChart2.setOnCursorChangedListener(this);
        mKxChart2.setOnScrollListener(this);
        mKxChart3=(KxChart)findViewById(R.id.kx_View3);
        mKxChart3.setDebug(true);
        mKxChart3.setOnLandscapeClickListener(this);
        mKxChart3.setOnCursorChangedListener(this);
        mKxChart3.setOnScrollListener(this);
        RadioGroup group=(RadioGroup)findViewById(R.id.radioGroupLand);
        group.setOnCheckedChangeListener(this);
        RadioButton timeBtn=(RadioButton)findViewById(R.id.radio_time);
        RadioButton dayBtn=(RadioButton)findViewById(R.id.radio_day);
        RadioButton weekBtn=(RadioButton)findViewById(R.id.radio_week);
        RadioButton monthBtn=(RadioButton)findViewById(R.id.radio_month);

        timeBtn.setChecked(true);
        sendRequest(0);
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * 光标移动回调事件
     * @param chart
     * @param cursorPos 光标移动到该位置 -1代表光标不显示
     * @param data 光标移动的位置对应的数据
     */
    @Override
    public void onCursorChanged(ChartBase chart, int cursorPos, JSONObject data) {

        if(ChartBase.LINE_TYPE_FS.equals(chart.getLinetype())) {
            // 用于在分时图外面显示光标所在位置的数据
            if(cursorPos == -1) {
            } else if(data != null) {
                try {
                    String time=data.getString("time");
                    String price=data.getString("price");
                    String volume=data.getString("volume");
                    String avg_price=data.getString("avg_price");
                    Log.d(TAG, "time:" + time + " price:" + price + " volume:" + volume + " avg_price:" + avg_price);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if(ChartBase.LINE_TYPE_DAY.equals(chart.getLinetype())) {
            // 用于在分时图外面显示光标所在位置的数据
            if(cursorPos == -1) {
                Log.d(TAG, "hide cursor");
            } else if(data != null) {
                try {
                    String date=data.getString("date");
                    String open=data.getString("open");
                    String high=data.getString("high");
                    String low=data.getString("low");
                    String close=data.getString("close");
                    String volume=data.getString("volume");
                    Log.d(TAG, "date:" + date + " open:" + open + " high:" + high + " low:" + low + " close:" + close + " volume:"
                            + volume);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * K线滚动
     * @param chart
     * @param scrolledPos Pos滚动了多少， 大于0，说明右移了，小于0说明向左移了，绝对值代表滚动的相对位置
     * @param firstDisPos 滚动后一屏显示的第一个Pos 可用于判断前面还剩余多少数据了，据此判断是否要请求更多的历史数据
     * @param lastDisPos 滚动后一屏显示的最后一个Pos
     */
    @Override
    public void onScroll(ChartBase chart, int scrolledPos, int firstDisPos, int lastDisPos) {
        if(ChartBase.LINE_TYPE_DAY.equals(chart.getLinetype())) {
            int scrolledPosAbs=Math.abs(scrolledPos);
            if(scrolledPos < 0) {
                // 向左移动了scrolledPosAbs个位置
                if(firstDisPos < 5) {
                    // 缓冲区左边只剩下5个数据可显示了，要不要再请求新数据放进去
                    // 新的数据过来后，直接调用 setData即可
                    try {
                        // 取出第一条数据的日期
                        String date1=chart.getData(0).getString("date");
                        Date d1=parseDate(date1);
                        Calendar c=Calendar.getInstance();
                        // 第一条数据的前一天作为endDate
                        c.set(Calendar.DATE, -1);
                        Date endDate=c.getTime();
                        // 再前10天作为startDate
                        c.set(Calendar.DATE, -10);
                        Date startDate=c.getTime();
                        // TODO 请求历史数据
                        sendRequest(1);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private Date parseDate(String d) {
        try {
            mDateFormat.parse(d);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }



    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        int lineType=0;
        switch(checkedId) {
            case R.id.radio_time:
                lineType = 0;
                mFsChart.setVisibility(View.VISIBLE);
                mKxChart1.setVisibility(View.GONE);
                mKxChart2.setVisibility(View.GONE);
                mKxChart3.setVisibility(View.GONE);
                break;
            case R.id.radio_day:
                lineType = 1;
                mKxChart1.setVisibility(View.VISIBLE);
                mKxChart2.setVisibility(View.GONE);
                mKxChart3.setVisibility(View.GONE);
                mFsChart.setVisibility(View.GONE);
                break;
            case R.id.radio_week:
                lineType = 2;
                mKxChart2.setVisibility(View.VISIBLE);
                mKxChart1.setVisibility(View.GONE);
                mKxChart3.setVisibility(View.GONE);
                mFsChart.setVisibility(View.GONE);
                break;
            case R.id.radio_month:
                lineType = 3;
                mKxChart3.setVisibility(View.VISIBLE);
                mKxChart2.setVisibility(View.GONE);
                mKxChart1.setVisibility(View.GONE);
                mFsChart.setVisibility(View.GONE);
                break;
            default:
                return;
        }
        sendRequest(lineType);
    }

    private JSONObject base;

    private JSONArray fsData;

    private JSONArray kxData1=new JSONArray();

    private JSONArray kxData2;

    private JSONArray kxData3;

    /**
     * 根据类型发送请求
     * @param type
     */
    private void sendRequest(int type) {
        base =new JSONObject();
//    k线图需求字段
//        m_currPrice=getFloat(base, "now_pri");
//        m_priceLast=getFloat(base, "yestod_end_pri");
//        m_currPrice=getFloat(base, "now_pri");
//        m_currL=getFloat(base, "trade_volume");
//  分时线 需求字段
//        m_currPrice=getFloat(base, "current_price");
//        m_currL=getFloat(base, "trade_volume");
//        m_priceLast=getFloat(base, "yestod_end_price");
//        m_maxPrice=getFloat(base, "today_max");
//        m_minPrice=getFloat(base, "today_min");

        try {
            base.put("yestod_end_pri","2000");
            base.put("now_pri","1210");
            base.put("current_price","1000");
            base.put("trade_volume",20l);
            base.put("yestod_end_price","12000");
            base.put("today_max","30000");
            base.put("today_min","1800");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (type==0){
            fsData=new JSONArray();
            for (int i=0;i<=126;i++){
                JSONObject obj=new JSONObject();
                try {
                    obj.put("time",new Date());
                    obj.put("price",(float)Math.random()+1000);
                    obj.put("avg_price",(float)Math.random()*1000);
                    obj.put("volume",50l);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                fsData.put(obj);
            }
        }else if (type==1){
//            float open=getDetailsFloat(m_nCursorIndex, "open");
//            float high=getDetailsFloat(m_nCursorIndex, "high");
//            float low=getDetailsFloat(m_nCursorIndex, "low");
//            float close=getDetailsFloat(m_nCursorIndex, "close");
//            float ma5=getDetailsFloat(m_nCursorIndex, "ma5");
//            float ma10=getDetailsFloat(m_nCursorIndex, "ma10");
//            float ma20=getDetailsFloat(m_nCursorIndex, "ma20");

            for (int i=0;i<=20;i++){
                JSONObject obj=new JSONObject();
                try {
                    obj.put("open",(float)Math.random()*10000);
                    obj.put("high",(float)Math.random()*15000);
                    obj.put("low",(float)Math.random()*1000);
                    obj.put("close",(float)Math.random()*10000);
                    obj.put("ma5",(float)Math.random()*10000);
                    obj.put("ma10",(float)Math.random()*10000);
                    obj.put("ma20",(float)Math.random()*10000);
                    obj.put("volume",(float)Math.random()*50);
                    obj.put("date",new Date());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                kxData1.put(obj);
            }
        }else if (type==2){
            kxData2=new JSONArray();

            for (int i=0;i<=6;i++){
                JSONObject obj=new JSONObject();
                try {
                    obj.put("open",(float)Math.random()*10000);
                    obj.put("high",(float)Math.random()*15000);
                    obj.put("low",(float)Math.random()*1000);
                    obj.put("close",(float)Math.random()*10000);
                    obj.put("ma5",(float)Math.random()*10000);
                    obj.put("ma10",(float)Math.random()*10000);
                    obj.put("ma20",(float)Math.random()*10000);
                    obj.put("volume",(float)Math.random()*50);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                kxData2.put(obj);
            }
        }else if (type==3){
            kxData3=new JSONArray();
            for (int i=0;i<=30;i++){
                JSONObject obj=new JSONObject();
                try {
                    obj.put("open",(float)Math.random()*10000);
                    obj.put("high",(float)Math.random()*15000);
                    obj.put("low",(float)Math.random()*1000);
                    obj.put("close",(float)Math.random()*10000);
                    obj.put("ma5",(float)Math.random()*10000);
                    obj.put("ma10",(float)Math.random()*10000);
                    obj.put("ma20",(float)Math.random()*10000);
                    obj.put("volume",(float)Math.random()*50);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                kxData3.put(obj);
            }
        }

            if(type==0) {
                mFsChart.setData("fs", base, fsData);
                mFsChart.invalidate();
            } else if(type==1) {
                mKxChart1.setData("day", base, kxData1);
                mKxChart1.invalidate();
            } else if(type==2) {
                mKxChart2.setData("week", base, kxData2);
                mKxChart2.invalidate();
            } else if(type==3) {
                mKxChart3.setData("month", base, kxData3);
                mKxChart3.invalidate();
            }
        }
}
