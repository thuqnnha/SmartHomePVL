package com.example.smarthomepvl;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private Date lastUpdateTime = new Date(0);
    private String macId = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


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

        // Bắt đầu cập nhật
        updateChartsPeriodically(macId, lastUpdateTime);


        return view;
    }
    private void updateChartsPeriodically(String macId, Date startTime) {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                executor.execute(() -> {
                    // Chạy trong background thread
                    List<ChartEntry> data = DatabaseHelper.loadChartData(macId, startTime);

                    if (data != null && !data.isEmpty()) {
                        ChartEntry latest = data.get(data.size() - 1);

                        // Cập nhật giao diện trong UI thread
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isAdded()) return;

                            voltageValue.setText(String.format(Locale.getDefault(), "%.2f V", latest.getVoltage()));
                            currentValue.setText(String.format(Locale.getDefault(), "%.2f A", latest.getCurrent()));
                            temperatureValue.setText(String.format(Locale.getDefault(), "%.2f °C", latest.getTemperature()));

                            updateLineChart(voltageChart, data, "voltage");
                            updateLineChart(currentChart, data, "current");
                            updateLineChart(temperatureChart, data, "temperature");
                        });
                    }

                    // Gọi lại chính mình sau 1 giây
                    handler.postDelayed(this, 1000);
                });
            }
        };

        handler.post(updateRunnable);
    }
    private void updateLineChart(LineChart chart, List<ChartEntry> data, String type) {
        List<Entry> entries = new ArrayList<>();

        int dataSize = data.size();
        int startIndex = Math.max(0, dataSize - 100);

        for (int i = startIndex; i < dataSize; i++) {
            float value = 0f;
            switch (type) {
                case "voltage": value = data.get(i).getVoltage(); break;
                case "current": value = data.get(i).getCurrent(); break;
                case "temperature": value = data.get(i).getTemperature(); break;
            }
            entries.add(new Entry(i- startIndex, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, type.toUpperCase());
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //Đổi màu line tùy theo loại dữ liệu
        int color = R.color.voltage_accent;
        switch (type) {
            case "voltage": color = R.color.voltage_accent; break;
            case "current": color = R.color.current_accent; break;
            case "temperature": color = R.color.temperature_accent; break;
        }

        if (getContext() == null) return;
        int resolvedColor = ContextCompat.getColor(requireContext(), color);
        dataSet.setColor(resolvedColor);

        //Tô nền dưới line (tuỳ chọn)
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(100);
        //dataSet.setFillColor(resolvedColor);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        //Cấu hình chart hiển thị
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);

        //Trục X xuống dưới
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-1f);
        xAxis.setAxisLineWidth(1f);

        //Trục Y trái
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineWidth(1f);
        yAxis.setTextColor(Color.BLACK);

        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.2f", value);
            }
        });

        //Giá trị cuối cùng trong biểu đồ
        float latestValue = entries.get(entries.size() - 1).getY();

        //LimitLine không có label, chỉ là đường ngang
        LimitLine limitLine = new LimitLine(latestValue);
        limitLine.setLineColor(resolvedColor);      // Màu theo từng loại dữ liệu
        limitLine.setLineWidth(1.2f);               // Độ dày đường
        limitLine.setLabel("");                     // Không hiện label
        // Tuỳ chọn: nét đứt cho đẹp
        limitLine.enableDashedLine(8f, 4f, 0f);      // Đường gạch gạch (tuỳ thích)

        //YAxis yAxis = chart.getAxisLeft();
        yAxis.removeAllLimitLines(); // Xoá cũ để tránh chồng
        yAxis.addLimitLine(limitLine); // Thêm mới đường ngang

        chart.setExtraBottomOffset(10f);
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updateRunnable != null) {
            handler.post(updateRunnable); // Tiếp tục cập nhật nếu bị tạm dừng trước đó
        } else {
            updateChartsPeriodically(macId, lastUpdateTime);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable); // Tạm dừng cập nhật
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable); // Dừng hẳn cập nhật
        }
        executor.shutdownNow(); // Dọn executor
    }






}