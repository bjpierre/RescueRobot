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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    final String ADDRESS = "192.168.1.1";
    final int PORT = 288;
    ImageButton fowardButton, backButton, leftButton, rightButton, songButton, scanButton, restartButton;
    TextView receivedText;
    EditText MTargetGoal, RTargetGoal;
    Connection connection;
    RadarChart radarChart;
    StringBuilder stringBuilder;
    boolean scanComplete = false;
    ArrayList<RadarEntry> irDataVals = null;
    ArrayList<RadarEntry> pingDataVals = null;
    String labels[];

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildLists();
        stringBuilder = new StringBuilder();
        labels = new String[181];
        initializeLabels(labels);
        Log.v("Graph", "Filled empty vals");

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
        receivedText = findViewById(R.id.RecievedText);


        //Note this class implements View.OnTouchListener
        fowardButton.setOnTouchListener(this);
        backButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
        songButton.setOnTouchListener(this);
        scanButton.setOnTouchListener(this);
        restartButton.setOnTouchListener(this);


        //Call connection constructor and boot it
        connection = new Connection(ADDRESS, PORT);
        Log.v("Connection", "Connecting");
        connection.boot();
        Log.v("Connection", "Connected");
    }

    /**
     * Method designed to handle an input
     *
     * @param input
     */
    protected void handleInput(char input) {
        Log.v("input","Got input");
        if(input=='p'){
            buildLists();
            return;
        }
        if (input == 13) {
            stringBuilder.append(input);
            scanComplete = true;
        } else {
            stringBuilder.append(input);
        }
    }

    public void parseString(String input) {
        boolean irDataDone = false;
        StringBuilder numberToAdd = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.') {
                numberToAdd.append(input.charAt(i));
            } else if (!irDataDone) {
                irDataVals.add(0,new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
                numberToAdd.delete(0, numberToAdd.length());
                irDataDone = true;
            } else if (input.charAt(i) == (char) 13) {
                pingDataVals.add(0,new RadarEntry((Float.valueOf(numberToAdd.toString()) > 50 ? 50 : Float.valueOf(numberToAdd.toString()))));
                numberToAdd.delete(0, numberToAdd.length());
            }

        }

        if (pingDataVals.size() > 179) startGraph();
    }

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
                        if (scanComplete) {
                            System.out.println(stringBuilder.toString());
                            parseString(stringBuilder.toString());
                            stringBuilder.delete(0, stringBuilder.length());
                            scanComplete = false;
                        }
                    }
                }
            });
            //Starts the two threads
            connection.start();
            while (inputStream == null) ;
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
        Collections.rotate(irDataVals,-23);
        Collections.rotate(pingDataVals,-23);

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
        for(int i=135; i<180; i++)
            aL.set(i,aL.get(i-135));
        for(int i=0; i<45;i++)
            aL.set(i, aL.get(i+45));
        for(int i=45; i<135;i++)
            aL.set(i,temp);
        return aL;
    }

    private void initializeLabels(String[] labels) {
        for (int i = 0; i < 181; i++) {
            labels[i] = Integer.toString(i);
        }
    }

    private void buildLists(){
        irDataVals = new ArrayList<>();
        pingDataVals = new ArrayList<>();
        while (irDataVals.size() < 90) {
            irDataVals.add(new RadarEntry(0));
            pingDataVals.add(new RadarEntry(0));
        }
    }

}
