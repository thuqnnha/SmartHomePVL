package com.example.smarthomepvl;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChartDeviceFragment extends Fragment {
    private TextView voltageValue, currentValue, temperatureValue;
    private LineChart voltageChart, currentChart, temperatureChart;
    private ScheduledExecutorService executorService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean isUpdating = false;
    private Date lastUpdateTime = new Date(0);
    private String macId = null;



    public ChartDeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Device device = (Device) getArguments().getSerializable("device");
            if (device != null) {
                macId = device.getDiaChiMAC() + "_online";

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_device, container, false);

        voltageValue = view.findViewById(R.id.voltageValue);
        currentValue = view.findViewById(R.id.currentValue);
        temperatureValue = view.findViewById(R.id.temperatureValue);

        voltageChart = view.findViewById(R.id.voltageChart);
        currentChart = view.findViewById(R.id.currentChart);
        temperatureChart = view.findViewById(R.id.temperatureChart);

        configureChart(voltageChart);
        configureChart(currentChart);
        configureChart(temperatureChart);


        return view;
    }
    private void configureChart(LineChart chart) {
//        chart.getDescription().setEnabled(false);
//        chart.getLegend().setEnabled(false);
//        chart.getAxisRight().setEnabled(false);
//
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setDrawGridLines(false);
//
//        YAxis leftAxis = chart.getAxisLeft();
////        leftAxis.setGranularity(1f);
//        leftAxis.setGranularity(0.01f);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        // Màu nền và bo tròn viền
        //chart.setBackgroundColor(Color.parseColor("#FAFAFA")); // Màu sáng
        chart.setExtraOffsets(10, 10, 10, 10);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(0.01f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (macId != null) {
            startChartDataUpdater(macId);
        } else {
            Log.e("ChartDeviceFragment", "MAC ID is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopChartDataUpdater();
    }

    private void startChartDataUpdater(String macId) {
        isUpdating = true;
        executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleWithFixedDelay(() -> {
            try {
                if (!isUpdating) return;

                Date currentTime = new Date();
                List<ChartEntry> chartData = DatabaseHelper.loadChartData(macId, lastUpdateTime);
                lastUpdateTime = currentTime;

                if (!chartData.isEmpty()) {
                    handler.post(() -> {
                        try {
                            updateUI(chartData);
                        } catch (Exception e) {
                            Log.e("ChartUpdate", "Error updating UI", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ChartUpdate", "Error fetching data", e);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void updateUI(List<ChartEntry> chartData) {
        ChartEntry latest = chartData.get(chartData.size() - 1);

        voltageValue.setText(String.format(Locale.getDefault(), "%.2fV", latest.getVoltage()));
        currentValue.setText(String.format(Locale.getDefault(), "%.2fA", latest.getCurrent()));
        temperatureValue.setText(String.format(Locale.getDefault(), "%.2f°C", latest.getTemperature()));

        updateChart(voltageChart, chartData, "voltage");
        updateChart(currentChart, chartData, "current");
        updateChart(temperatureChart, chartData, "temperature");
    }

    private void updateChart(LineChart chart, List<ChartEntry> entries, String type) {
        LineData data = chart.getData();
        LineDataSet dataSet;

        if (data == null) {
//            dataSet = new LineDataSet(new ArrayList<>(), type);
//            dataSet.setDrawValues(false);
//            dataSet.setDrawCircles(true);
//            dataSet.setCircleRadius(3f);
//            dataSet.setLineWidth(2f);
//            //data = new LineData(dataSet);
//            //chart.setData(data);

            dataSet = new LineDataSet(new ArrayList<>(), type);
            dataSet.setDrawValues(false);
            dataSet.setDrawCircles(true);
            dataSet.setCircleRadius(3f);
            dataSet.setLineWidth(2f);
            dataSet.setDrawHighlightIndicators(true);
            dataSet.setHighLightColor(Color.RED);
            dataSet.setColor(getColorForType(type));
            dataSet.setCircleColor(getColorForType(type));
            
            chart.setDrawMarkers(true);
            dataSet.setDrawHighlightIndicators(true);
            dataSet.setHighLightColor(Color.RED);

            data = new LineData(dataSet);
            chart.setData(data);

            chart.setDrawMarkers(true);
        } else {
            dataSet = (LineDataSet) data.getDataSetByIndex(0);
        }

        // Thêm các điểm mới
        int startIndex = dataSet.getEntryCount();
        for (int i = 0; i < entries.size(); i++) {
            float value;
            switch (type) {
                case "voltage": value = entries.get(i).getVoltage(); break;
                case "current": value = entries.get(i).getCurrent(); break;
                case "temperature": value = entries.get(i).getTemperature(); break;
                default: return;
            }
            data.addEntry(new Entry(startIndex + i, value), 0);
        }

        // Giới hạn số điểm hiển thị
        if (dataSet.getEntryCount() > 50) {
            int removeCount = dataSet.getEntryCount() - 50;
            for (int i = 0; i < removeCount; i++) {
                dataSet.removeFirst();
                //dataSet.removeEntry(0);
            }
        }

        chart.notifyDataSetChanged();
        chart.invalidate();
        chart.animateX(500); // hiệu ứng trượt mượt
        chart.moveViewToX(data.getXMax());
    }

    private void stopChartDataUpdater() {
        isUpdating = false;
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
    private int getColorForType(String type) {
        switch (type) {
            case "voltage": return Color.parseColor("#4CAF50");   // Xanh lá
            case "current": return Color.parseColor("#2196F3");   // Xanh dương
            case "temperature": return Color.parseColor("#F44336"); // Đỏ
            default: return Color.BLACK;
        }
    }

}