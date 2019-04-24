package android.example.rescuerobot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    final String ADDRESS = "192.168.1.1";
    //final String ADDRESS = "10.0.2.2";
    final int PORT = 288;
    ImageButton fowardButton, backButton, leftButton, rightButton, songButton, scanButton, restartButton, toggleButton;
    TextView receivedText;
    EditText MTargetGoal, RTargetGoal;
    Connection connection;
    RadarChart radarChart;
    ScatterChart scatterChart;
    StringBuilder stringBuilder;
    boolean scanComplete = false;
    ArrayList<RadarEntry> irDataVals = null;
    ArrayList<RadarEntry> pingDataVals = null;
    ArrayList<Entry> walls = new ArrayList<>();
    ArrayList<Entry> objects = new ArrayList<>();
    ArrayList<Entry> holes = new ArrayList<>();
    String labels[];
    Boolean visibilty = true;
    private boolean recObj;
    private boolean scanObj;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("Debug",""+1);
        buildLists();
        stringBuilder = new StringBuilder();
        labels = new String[181];
        initializeLabels(labels);
        Log.v("Graph", "Filled empty vals");
        recObj = false;
        scanObj = false;
        toggleButton = findViewById(R.id.ToggleButton);
        fowardButton = findViewById(R.id.fButton);
        backButton = findViewById(R.id.bButton);
        leftButton = findViewById(R.id.lButton);
        rightButton = findViewById(R.id.rButton);
        songButton = findViewById(R.id.mButton);
        scanButton = findViewById(R.id.sButton);
        restartButton = findViewById(R.id.restartButton);
        MTargetGoal = findViewById(R.id.MTargetGoal);
        RTargetGoal = findViewById(R.id.RTargetGoal);

        radarChart = findViewById(R.id.radarChart);
        radarChart.setVisibility(View.INVISIBLE);
        receivedText = findViewById(R.id.RecievedText);


        //Note this class implements View.OnTouchListener
        fowardButton.setOnTouchListener(this);
        backButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
        songButton.setOnTouchListener(this);
        scanButton.setOnTouchListener(this);
        restartButton.setOnTouchListener(this);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleViews();
            }
        });


        //Call connection constructor and boot it
        connection = new Connection(ADDRESS, PORT);
        Log.v("Connection", "Connecting");
        connection.boot();
        Log.v("Connection", "Connected");


        //Chart shiz
        scatterChart = findViewById(R.id.scatterGraph);
        scatterChart.getDescription().setEnabled(false);

        scatterChart.setDrawGridBackground(false);
        scatterChart.setTouchEnabled(true);
        scatterChart.setMaxHighlightDistance(50f);

        // enable scaling and dragging
        scatterChart.setDragEnabled(true);
        scatterChart.setScaleEnabled(true);

        scatterChart.setMaxVisibleValueCount(200);
        scatterChart.setPinchZoom(true);

        Legend l = scatterChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
//        l.setTypeface(tfLight);
        l.setXOffset(5f);

        YAxis yl = scatterChart.getAxisLeft();
//        yl.setTypeface(tfLight);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        scatterChart.getAxisRight().setEnabled(false);

        XAxis xl = scatterChart.getXAxis();
//        xl.setTypeface(tfLight);
        xl.setDrawGridLines(false);


    }

    /**
     * Method designed to handle an input
     *
     * @param input
     */
    public void handleInput(char input) {
        Log.v("input", "Got:" + input);
        if (recObj) {
            if (input == ',') {
                parseObject(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                recObj = false;
            } else
                stringBuilder.append(input);
        } else if (scanObj) {
            if (input == ',') {
                parseScan(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                if (irDataVals.size() > 180) {
                    scanObj = false;
                    startGraph();
                    System.out.println("Scan Completed");
                }
            } else
                stringBuilder.append(input);

        } else if (input == 'o') { // start listening for an object
            recObj = true;
            return;
        } else if (input == 'p') { // start listening for a list of stuff
            buildLists(); // clear lists
            scanObj = true;
            return;
        }
    }
//    public void handleInput(char input) {
//        Log.v("input", "Got input = " + input);
//        if(recObj){
//            if(input == ','){
//                parseObject(stringBuilder.toString());
//                stringBuilder.delete(0,stringBuilder.length());
//                recObj=false;
//            }
//            else stringBuilder.append(input);
//        }else if(!scanComplete){
//            if(input==(char)13){
//                parseScan(stringBuilder.toString());
//                stringBuilder.delete(0,stringBuilder.length());
//                if(irDataVals.size()>179) scanComplete=true;
//            }
//        } else if(input == 'o'){ //start listening for an object
//            recObj=true;
//            return;
//        }else if(input == 'p'){ //start listening for a list of stuff
//            buildLists(); //clear lists
//            scanComplete=false;
//            return;
//        }
//
////        if(input=='o'){
////            recObj = true;
////            stringBuilder.append(input);
////        }
////        if (input == ',' && recObj) {
////            stringBuilder.append(input);
////            parseString(stringBuilder.toString());
////            recObj = false;
////        } else if(recObj) {
////            stringBuilder.append(input);
////        }
////        if (input == 'p') {
////            buildLists();
////            scanComplete = false;
////            return;
////        }
////        if (input == 13 && !scanComplete) {
////            stringBuilder.append(input);
////            scanComplete = true;
////        } else if(!scanComplete) {
////            stringBuilder.append(input);
////        }
//    }

    private void parseScan(String toString) {
        Scanner sc = new Scanner(toString);
        sc.useDelimiter(" ");
        String tempa = sc.next();
        String tempb = sc.next();
        float irData = Float.parseFloat(tempa);
        float pingData = Float.parseFloat(tempb);
        irDataVals.add(0, new RadarEntry(irData > 50 ? 50 : irData));
        pingDataVals.add(0, new RadarEntry(pingData > 50 ? 50 : pingData));
        Log.v("Graph","Radar = "  + tempa +"," + tempb + "at: " + irDataVals.size());

        sc.close();
    }

    private void parseObject(String toString) {
        Scanner sc = new Scanner(toString);
        sc.useDelimiter(" ");
        char type = sc.next().charAt(0);
        int x = Integer.parseInt(sc.next());
        int y = Integer.parseInt(sc.next());
        sc.close();
        scatterAddValues(x,y,(Character.isAlphabetic(type) ? 1 : 2));
        System.out.println("Added " + type + " at : " + x + "," + y);

    }

//    public void parseString(String input) {
//        Log.v("Ben", input.charAt(0)+"");
//        if(input.charAt(0) == 'o'){
//            Scanner sc = new Scanner(input);
//            sc.useDelimiter(" ");
//            char type = sc.next().charAt(1);
//            int x = Integer.parseInt(sc.next());
//            int y = Integer.parseInt(sc.next());
//            sc.close();
//            scatterAddValues(x,y,(Character.isAlphabetic(type) ? 1 : 2));
//
//        }else {
//            boolean irDataDone = false;
//            StringBuilder numberToAdd = new StringBuilder();
//            for (int i = 0; i < input.length(); i++) {
//                if (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.') {
//                    numberToAdd.append(input.charAt(i));
//                } else if (!irDataDone) {
//                    irDataVals.add(0, new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
//                    numberToAdd.delete(0, numberToAdd.length());
//                    irDataDone = true;
//                } else if (input.charAt(i) == (char) 13) {
//                    pingDataVals.add(0, new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
//                    numberToAdd.delete(0, numberToAdd.length());
//                }
//
//            }
//
//            if (pingDataVals.size() > 179) startGraph();
//        }
//    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //If we need to restart
        if (v.getId() == R.id.restartButton) {
            connection.boot();
            return true;
        }

        //If not connected do nothing
        if (!connection.isConnected()) {
            Log.v("Buttons", "Button clicked without connection");
            return true;
        }

        //Get move targets
        String MGoal = MTargetGoal.getText().toString();
        String RGoal = RTargetGoal.getText().toString();
        switch (v.getId()) {
            case R.id.fButton:
               /*
               if button pressed not released
               note this is not needed but provides flexablity for pressing and releasing buttons later
                */
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".f" + MGoal + ',');
                }
                break;
            case R.id.bButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".b" + MGoal + ',');
                }
                break;
            case R.id.lButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".l" + RGoal + ',');
                }
                break;
            case R.id.rButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".r" + RGoal + ',');
                }
                break;
            case R.id.sButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".s,");
                }
                break;
            case R.id.mButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".m,");
                }
                break;

        }
        return true;
    }

    /**
     * Handles a connection
     */
    public class Connection {
        volatile Socket socket = null;
        final String address;
        final int port;
        OutputStream outputStream;
        OutputStreamWriter writer;
        InputStream inputStream;
        BufferedReader reader;

        /**
         * Assigns values to address and port
         */
        Connection(final String address, int port) {
            this.address = address;
            this.port = port;
        }

        /**
         * Creates a socket, assigns all streams
         * Creates a listening thread for input
         */
        void boot() {
            //This is for my gui pls ignore
            final ImageView good, bad;
            good = findViewById(R.id.ConnectedInd);
            bad = findViewById(R.id.ErrConnect);
            good.setVisibility(View.GONE);
            bad.setVisibility(View.GONE);
            restartButton.setVisibility(View.INVISIBLE);
            findViewById(R.id.connectedMeasure).setVisibility(View.VISIBLE);

            Thread connection = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Connect socket and wait until its really connected
                        socket = new Socket(address, port);
                        if (socket == null) {
                            do {
                            } while (socket == null);
                        }
                        //Grab streams after socket made
                        outputStream = socket.getOutputStream();
                        writer = new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII);
                        inputStream = socket.getInputStream();
                        while (inputStream == null) ;
                        Log.v("Connection", "Streams set up");

                        //More gui stuff
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.connectedMeasure).setVisibility(View.INVISIBLE);
                                good.setVisibility(View.VISIBLE);
                                Log.v("Connection", "Connected, set gui");
                            }
                        });

                    } catch (IOException e) {
                        //Handles an exception and does more gui stuff
                        final String emessage = e.getLocalizedMessage();
                        Looper.prepare();
                        Log.v("Connection", emessage);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receivedText.setText(emessage);
                                findViewById(R.id.connectedMeasure).setVisibility(View.INVISIBLE);
                                bad.setVisibility(View.VISIBLE);
                                restartButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });

            //This is the listener for input, it handles input from the brobot by passing them to
            //@handleInput()
            Thread listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            while (socket == null) ;
                            while (inputStream == null) ;
                            handleInput((char) inputStream.read());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        if (scanComplete) {
//                            System.out.println(stringBuilder.toString());
//                            parseString(stringBuilder.toString());
//                            stringBuilder.delete(0, stringBuilder.length());
//                            scanComplete = false;
//                        }
                    }
                }
            });
            //Starts the two threads
            Log.v("Debug", "Statting thread");

            connection.start();
            while (inputStream == null) ;
            Log.v("Debug", "Out her again");

            listener.start();
            Log.v("Connection", "Starting input thread");

        }


        /**
         * . = command
         * , = end command
         * .f = forward
         * .b = backwards
         * .l = turn left
         * .r = turn right
         * .s = scan
         * .m = play song
         *
         * @param stringin string to send, see header for command code
         */
        void sendString(String stringin) {
            Log.v("Connection", "Sending " + stringin);
            final String string = stringin;
            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
                        DataOutputStream dos = new DataOutputStream(connection.socket.getOutputStream());
                        dos.write(13);
                        dos.flush();
                        for (int i = 0; i < bytes.length; i++) {
                            dos.write(bytes[i]);
                            dos.flush();
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        Log.v("Connection", e.getLocalizedMessage());
                    }
                }
            });
            thr.start();
        }

        /**
         * Checks if the connection is valid
         *
         * @return
         */
        boolean isConnected() {
            return socket != null && socket.isConnected();
        }

    }

    public void startGraph() {
        Collections.rotate(irDataVals, -45);
        Collections.rotate(pingDataVals, -45);

        final RadarDataSet dataSet1 = new RadarDataSet(irDataVals, "IR Sensor");
        final RadarDataSet dataSet2 = new RadarDataSet(pingDataVals, "Ping Sensor");

        dataSet1.setColor(Color.BLUE);
        dataSet2.setColor(Color.RED);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                radarChart.setBackgroundColor(Color.WHITE);
                radarChart.getDescription().setEnabled(false);
                radarChart.setWebLineWidth(0.1f);

                radarChart.setWebColor(Color.GRAY);
                radarChart.setWebLineWidth(0.1f);
                radarChart.setWebColorInner(Color.GRAY);
                radarChart.setWebAlpha(100);

                RadarData data = new RadarData();
                data.addDataSet(dataSet1);
                data.addDataSet(dataSet2);

                XAxis xAxis = radarChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setTextSize(0.1f);
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
        });
    }

    public static <RadarEntry> ArrayList<RadarEntry> manipulate(ArrayList<RadarEntry> aL) {
        RadarEntry temp = aL.get(150);
        for (int i = 135; i < 180; i++)
            aL.set(i, aL.get(i - 135));
        for (int i = 0; i < 45; i++)
            aL.set(i, aL.get(i + 45));
        for (int i = 45; i < 135; i++)
            aL.set(i, temp);
        return aL;
    }

    private void initializeLabels(String[] labels) {
        for (int i = 0; i < 181; i++) {
            labels[i] = Integer.toString(i);
        }
    }

    private void buildLists() {
        irDataVals = new ArrayList<>();
        pingDataVals = new ArrayList<>();
        while (irDataVals.size() < 90) {
            irDataVals.add(new RadarEntry(0));
            pingDataVals.add(new RadarEntry(0));
        }
    }





    private void scatterAddValues(int xval, int yval, int type){
        if(type == 0){
            walls.add(new Entry(xval,yval));
        } else if(type ==1){
            objects.add(new Entry(xval,yval));
        } else if (type ==2){
            holes.add(new Entry(xval,yval));
        }

        ScatterDataSet set1 = new ScatterDataSet(walls, "DS 1");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        ScatterDataSet set2 = new ScatterDataSet(objects, "DS 2");
        set2.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set2.setScatterShapeHoleColor(ColorTemplate.COLORFUL_COLORS[3]);
        set2.setScatterShapeHoleRadius(3f);
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        ScatterDataSet set3 = new ScatterDataSet(holes, "DS 3");
        set3.setShapeRenderer(new CustomScatterShapeRenderer());
        set3.setColor(ColorTemplate.COLORFUL_COLORS[2]);

        set1.setScatterShapeSize(8f);
        set2.setScatterShapeSize(8f);
        set3.setScatterShapeSize(8f);

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets
        dataSets.add(set2);
        dataSets.add(set3);

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);
//        data.setValueTypeface(tfLight);

        scatterChart.setData(data);
        scatterChart.invalidate();
    }

    public void toggleViews(){
        if(visibilty){
            visibilty = !visibilty;
            radarChart.setVisibility(View.VISIBLE)
            ;
            scatterChart.setVisibility(View.GONE);
        }else{
            visibilty = !visibilty;
            radarChart.setVisibility(View.GONE)
            ;
            scatterChart.setVisibility(View.VISIBLE);
        }
    }
}
