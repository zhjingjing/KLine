package com.kchart.chart;

import org.json.JSONObject;

public interface OnCursorChangedListener {

    public abstract void onCursorChanged(ChartBase chartbase, int i, JSONObject jsonobject);
}
