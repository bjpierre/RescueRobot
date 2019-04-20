package android.example.rescuerobot;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    final String ADDRESS =  "192.168.1.1";
    final int PORT = 288;
    ImageButton fowardButton,backButton,leftButton,rightButton,songButton,scanButton,restartButton;
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

        //Note this class implements View.OnTouchListener
        fowardButton.setOnTouchListener(this);
        backButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
        songButton.setOnTouchListener(this);
        scanButton.setOnTouchListener(this);
        restartButton.setOnTouchListener(this);


        //Call connection constructor and boot it
        connection = new Connection(ADDRESS,PORT);
        Log.v("Ben","Connecting");
        connection.boot();

    }

    /**
     * Method designed to handle an input
     * @param mes
     */
    protected void handleInput(String mes) {
        Log.v("Ben", "Received:" + mes);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //If we need to restart
        if (v.getId()==R.id.restartButton) {
            connection.boot();
            return true;
        }

        //If not connected do nothing
        if(!connection.isConnected()){
            Log.v("Ben", "Button clicked without connection");
            return true;
        }

        //Get move targets
        String MGoal = MTargetGoal.getText().toString();
        String RGoal = RTargetGoal.getText().toString();
        switch (v.getId()){
            case R.id.fButton:
               /*
               if button pressed not released
               note this is not needed but provides flexablity for pressing and releasing buttons later
                */
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
                        //Connect socket and wait until its really connected
                        socket = new Socket(address, port);
                        if (socket == null) {
                            do {
                            } while (socket == null);
                        }
                        //Grab streams after socket made
                        outputStream = socket.getOutputStream();
                        writer = new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII);
                        InputStream inputStream = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII));

                        //More gui stuff
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              findViewById(R.id.connectedMeasure).setVisibility(View.INVISIBLE);
                                              good.setVisibility(View.VISIBLE);
                                          }
                                      });

                    } catch (IOException e) {
                        //Handles an exception and does more gui stuff
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

            //This is the listener for input, it handles input from the brobot by passing them to
            //@handleInput()
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
            //Starts the two threads
            connection.start();
            listener.start();

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
            Log.v("Confirmation",stringin);
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
                        Log.v("Ben", e.getLocalizedMessage());
                    }
                }
            });
            thr.start();
        }

        /**
         * Checks if the connection is valid
         * @return
         */
        boolean isConnected(){
            return socket!=null && socket.isConnected();
        }

    }
}
