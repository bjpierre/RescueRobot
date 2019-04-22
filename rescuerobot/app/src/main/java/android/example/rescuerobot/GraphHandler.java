package android.example.rescuerobot;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;

public class GraphHandler {
    boolean scanComplete = false;
    ArrayList<RadarEntry> irDataVals = null;
    ArrayList<RadarEntry> pingDataVals = null;
    RadarChart radarChart;
    String[] labels;
    StringBuilder stringBuilder;


    public GraphHandler(RadarChart in) {
        radarChart = in;
        irDataVals = new ArrayList<>();
        pingDataVals = new ArrayList<>();
        stringBuilder = new StringBuilder();
        Log.v("Graph", "Made handler");
        run();
    }

    public static <T> ArrayList<T> rotate(ArrayList<T> aL, int shift) {
        if (aL.size() == 0) return aL;

        T element;
        for (int i = 0; i < shift; i++) {
            // remove last element, add it to front of the ArrayList
            element = aL.remove(aL.size() - 1);
            aL.add(0, element);
        }

        return aL;
    }

    public void run() {
        Log.v("Graph","Running Graph");

        while (irDataVals.size() < 90) {
            irDataVals.add(new RadarEntry(0));
            pingDataVals.add(new RadarEntry(0));
        }

        Log.v("Graph","Graph filled blank");

        while (irDataVals.size() < 180) ;

        Log.v("Graph","Graph filled real live data");
        rotate(irDataVals, 44);
        rotate(pingDataVals, 44);
        Collections.reverse(irDataVals);
        Collections.reverse(pingDataVals);
        labels = new String[181];
        initializeLabels(labels);


        RadarDataSet dataSet1 = new RadarDataSet(irDataVals, "IR Sensor");
        RadarDataSet dataSet2 = new RadarDataSet(pingDataVals, "Ping Sensor");

        dataSet1.setColor(Color.GRAY);
        dataSet2.setColor(Color.RED);

        radarChart.setBackgroundColor(Color.rgb(60, 65, 82));
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(0.1f);

        radarChart.setWebColor(Color.GRAY);
        radarChart.setWebLineWidth(0.1f);
        radarChart.setWebColorInner(Color.WHITE);
        radarChart.setWebAlpha(100);

        RadarData data = new RadarData();
        data.addDataSet(dataSet1);
        data.addDataSet(dataSet2);
        Log.v("Graph","Data added to view");

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(9f);
        xAxis.setXOffset(0);
        xAxis.setYOffset(0);
        xAxis.setTextColor(Color.WHITE);

        //for reducing the size of chart

//        YAxis yAxis = radarChart.getYAxis();
//        yAxis.setLabelCount(5);
//        yAxis.setTextSize(9f);
//        yAxis.setAxisMaximum(12);
//        yAxis.setAxisMinimum(1f);
//        yAxis.setDrawLabels(false);

        //for hiding legend , axis description
        radarChart.getLegend().setEnabled(false);
        radarChart.setSkipWebLineCount(0);
        radarChart.setData(data);
        radarChart.invalidate();

    }

    private void initializeLabels(String[] labels) {
        for (int i = 0; i < 181; i++) {
            labels[i] = Integer.toString(i);
        }
    }

    public void parseString(String input) {
        boolean irDataDone = false;
        StringBuilder numberToAdd = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.') {
                numberToAdd.append(input.charAt(i));
            } else if (!irDataDone) {
                irDataVals.add(new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
                numberToAdd.delete(0, numberToAdd.length());
                Log.v("Length", "IRVal :" + irDataVals.size());
                irDataDone = true;
            } else if (input.charAt(i) == (char) 13) {
                pingDataVals.add(new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
                numberToAdd.delete(0, numberToAdd.length());
                Log.v("Length", "PVal :" + pingDataVals.size());
            }

        }
    }

    public void passInput(char input) {
        Log.v("Graph", "Got input from main :" + input);
        if (input == 13) {
            stringBuilder.append(input);
            scanComplete = true;
        } else {
            stringBuilder.append(input);
        }

        if(scanComplete)
        {
            System.out.println(stringBuilder.toString());
            parseString(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
            scanComplete = false;
        }

    }
}
