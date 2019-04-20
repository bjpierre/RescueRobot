package android.example.rescuerobot;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    final String ADDRESS =  "192.168.1.1";
    final int PORT = 288;

    Button fowardButton,backButton,leftButton,rightButton,songButton,scanButton,restartButton;
    TextView receivedText;
    EditText MTargetGoal,RTargetGoal;
    Connection connection;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fowardButton = findViewById(R.id.fButton);
        backButton = findViewById(R.id.bButton);
        leftButton = findViewById(R.id.lButton);
        rightButton = findViewById(R.id.rButton);
        songButton = findViewById(R.id.mButton);
        scanButton = findViewById(R.id.sButton);
        restartButton = findViewById(R.id.restartButton);
        MTargetGoal = findViewById(R.id.MTargetGoal);
        RTargetGoal = findViewById(R.id.RTargetGoal);

        receivedText = findViewById(R.id.RecievedText);

        fowardButton.setOnTouchListener(this);
        backButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
        songButton.setOnTouchListener(this);
        scanButton.setOnTouchListener(this);
        restartButton.setOnTouchListener(this);


        connection = new Connection(ADDRESS,PORT);
        Log.v("Ben","Connecting");
        connection.boot();

    }

    protected void handleInput(String mes) {
        Log.v("Ben", "Received:" + mes);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId()==R.id.restartButton) {
            connection.boot();
            return true;
        }

        if(!connection.isConnected()){
            Log.v("Ben", "Button clicked without connection");
            return true;
        }

//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            connection.sendString(".t,");
//            return true;
//        }
        String MGoal = MTargetGoal.getText().toString();
        String RGoal = RTargetGoal.getText().toString();
        switch (v.getId()){
            case R.id.fButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".f"+ MGoal +',');
                }
                break;
            case R.id.bButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".b"+ MGoal +',');
                }
                break;
            case R.id.lButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".l" + RGoal + ',');
                }
                break;
            case R.id.rButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".r" + RGoal +',');
                }
                break;
            case R.id.sButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".s,");
                }
                break;
            case R.id.mButton:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    connection.sendString(".m,");
                }
                break;
        }
        return true;
    }

    public class Connection {
        volatile Socket socket = null;
        final String address;
        final int port;
        OutputStream outputStream;
        OutputStreamWriter writer;
        InputStream inputStream;
        BufferedReader reader;

        Connection(final String address, int port) {
            this.address = address;
            this.port = port;
        }

        void boot() {
            final ImageView good,bad;
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
                        socket = new Socket(address, port);
                        if (socket == null) {
                            do {
                            } while (socket == null);
                        }
                        outputStream = socket.getOutputStream();
                        writer = new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII);
                        InputStream inputStream = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII));
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              findViewById(R.id.connectedMeasure).setVisibility(View.INVISIBLE);
                                              good.setVisibility(View.VISIBLE);
                                          }
                                      });

                    } catch (IOException e) {
                        final String emessage = e.getLocalizedMessage();
                        Looper.prepare();
                        Log.v("Ben", emessage);
                        Toast.makeText(getApplicationContext(),"You oofed up boi", Toast.LENGTH_SHORT).show();
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


            Thread listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (socket == null) {
                        do {
                        } while (socket == null);
                    }
                    while (true) {
                        try {
                            handleInput(reader.readLine());
                        } catch (Exception e) {
                            Log.v("Ben", e.getLocalizedMessage());
                        }
                    }

                }
            });
            connection.start();
            listener.start();

        }


        /**
         * . = command
         * .f = foward
         * .b = backwards
         * .l = turn left
         * .r = turn right
         * .s = scan
         * .m = play song
         * .t = stop action
         * @param stringin string to send, see header for command code
         */
        void sendString(String stringin) {
            Log.v("Confirmation",stringin);
            final String string = stringin;
            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
//                        for (int i = 0; i < bytes.length; i++) {
//                            outputStream.write(bytes, i, 1);
//                            outputStream.flush();
//                            Thread.sleep(100);
//                        }
//                        outputStream.write(13);
//                        outputStream.write(13);
//                        outputStream.flush();
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
                        Log.v("Ben", e.getLocalizedMessage());
                    }
                }
            });
            thr.start();
        }

        boolean isConnected(){
            return socket!=null && socket.isConnected();
        }

    }
}
