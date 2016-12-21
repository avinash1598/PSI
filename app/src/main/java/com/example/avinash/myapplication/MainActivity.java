package com.example.avinash.myapplication;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout layoutToAdd; static View viewbt,viewhome,viewgf;int check=0;
    static int checkbt=0,checkhome=0,checkgf=0;
    static btconn btc;int tempstrt=0;
    static ImageButton pd,disconnect;
    ImageView discimg;
    TextView disctext;
    static String msgrec=null;
    EditText url,fuelkey,rpmkey,speedkey;
    public static BufferedReader in = null;
    String message;

    private boolean isConnected = false;
    public static BluetoothAdapter myBluetooth = null;
    public Set<BluetoothDevice> pairedDevices;
    public ListView devlv;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false, ConnectSuccess = true;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public TextView t;
    String addr;
    private PrintWriter out;
    static OutputStream tmpOut = null;
     public ConnectedThread mConnectedThread;
    public Handler bluetoothIn,texthandler;
    final int handlerState = 0;
    public static BluetoothSocket bsocket;
    public static String btstatus="disconnected";
    public InputStream mmInStream=null;
    public OutputStream mmOutStream=null;

    public static String URL = null, FUEL_ID = null, RPM_ID = null, SPEED_ID = null ;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "data";
    int test=0;
    RelativeLayout btlayout;
    public static View btview, paad;
    public static ProgressBar progressBar2,progressBar,progressBar3;
    String fuel,rpm,speed;
    public static TextView btdatarec,sendingstatus,fuelview,rpmview,speedview,tv;
    int start=0;
    public static Drawable drawable,drawable2;
    public static ImageView imv,iv;
    int mainerror=0;
    static String name;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        layoutToAdd=(DrawerLayout)findViewById(R.id.drawer_layout);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_APPEND);

        try {Log.d("sp","aaya");
            URL = sharedpreferences.getString("urk", null);
            FUEL_ID = sharedpreferences.getString("fuel", null);
            RPM_ID = sharedpreferences.getString("rpm", null);
            SPEED_ID = sharedpreferences.getString("speed", null);
        } catch (Exception e) {
            Toast.makeText(this, "CURRENTLY NO SAVED VALUE", Toast.LENGTH_SHORT).show();
        }

        LayoutInflater inflater = LayoutInflater
                .from(getApplicationContext());
        viewhome = inflater.inflate(R.layout.main, null);
        layoutToAdd.addView(viewhome,1);
        progressBar3=(ProgressBar)findViewById(R.id.progressBar3);
        progressBar3.setVisibility(View.GONE);
        tv=(TextView)findViewById(R.id.statusview);
        iv=(ImageView)findViewById(R.id.imgbtstatus);
        //tv.setTextColor(Color.TRANSPARENT);
        btdatarec=(TextView)findViewById(R.id.btdata);
        sendingstatus=(TextView)findViewById(R.id.sendingstatus);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        paad=(View)findViewById(R.id.paad);
        imv=(ImageView)findViewById(R.id.paad);
        fuelview=(TextView)findViewById(R.id.fuelview);
        rpmview=(TextView)findViewById(R.id.rpmview);
        speedview=(TextView)findViewById(R.id.speedview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable=getDrawable(R.mipmap.ic_launcher_alertdialog);
            drawable2=getDrawable(R.mipmap.ic_launcher_right);
        }
        checkhome=1;checkbt=0;checkgf=0;
         setup();   //-------------------------> 1
         registerbroadcast();  //-------------------> 2

        texthandler= new Handler(){
            @Override
            public void handleMessage(Message msg){
                try{sendingstatus.setText("Sending data to: "+MainActivity.URL);
                progressBar.setVisibility(View.VISIBLE);}catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        bluetoothIn = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //byte[] writeBuf = (byte[]) msg.obj;
                String data = (String) msg.obj;


                if(message.length()>7&&message.contains("-")) {

                    fuel = message.split("-")[0];
                    Log.d("fuel", fuel);
                    rpm = message.split("-")[1];
                    Log.d("rpm", rpm);
                    speed = message.split("-")[2];
                    Log.d("speed", speed);

                    try {
                        btdatarec.setText(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    btconn bt = new btconn();
                    bt.send(fuel, rpm, speed);
                }
            }
        };

    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                 //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(MainActivity.this,"Device Connected",Toast.LENGTH_SHORT).show();
                btstatus="connected";

                if(checkbt==1) {
                    progressBar2.setVisibility(View.GONE);
                    btview.setAlpha(1);
                }
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(500);

                if(checkbt==1){discimg.setAnimation(fadeIn);
                    discimg.setImageResource(R.mipmap.ic_launchersuccess);
                disctext.setTextColor(Color.parseColor("#26862e"));
                disctext.setText("Connected");}
                if(checkhome==1){iv.setAnimation(fadeIn);
                    progressBar3.setVisibility(View.GONE);
                    iv.setImageResource(R.mipmap.ic_launchersuccess);
                    tv.setTextColor(Color.parseColor("#26862e"));
                    tv.setText("Connected: "+name);
                }

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                 //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                 //Device is about to disconnect
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                 //Device has disconnected
                 try {
                     btstatus = "disconnected";
                     btSocket=null;
                     ConnectSuccess = true;
                     isBtConnected = false;
                     Toast.makeText(MainActivity.this, "Device Disconnected", Toast.LENGTH_SHORT).show();
                     Animation fadeIn = new AlphaAnimation(0, 1);
                     fadeIn.setInterpolator(new DecelerateInterpolator());
                     fadeIn.setDuration(500);
                     if(checkbt==1) {
                         btview.setAlpha(1);
                         progressBar2.setVisibility(View.GONE);
                         discimg.setAnimation(fadeIn);
                         discimg.setImageResource(R.mipmap.ic_disconn);
                         disctext.setTextColor(Color.RED);
                         disctext.setText("Disconnected");
                     }
                     if(checkhome==1){
                         //btview.setAlpha(1);
                         progressBar3.setVisibility(View.GONE);
                         iv.setAnimation(fadeIn);
                         iv.setImageResource(R.mipmap.ic_disconn);
                         tv.setTextColor(Color.RED);
                         tv.setText("Disconnected");
                     }
                 }catch (Exception e){
                     e.printStackTrace();
}
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Button b=(Button)menu.findItem(R.id.disconnectbt).getActionView();
        Button button=(Button)menu.findItem(R.id.connectbt).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.disconnectbt){
            if(btstatus.matches("disconnected")){
                Toast.makeText(MainActivity.this,"Bluetooth disconnected",Toast.LENGTH_SHORT).show();
            }
            else {progressBar3.setVisibility(View.VISIBLE);disconnect();}
        }
        if(id==R.id.connectbt){
            Log.d("yo", "yess");
           if(btstatus.matches("connected")){
               Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_SHORT).show();
           }
            else {

               setup();
               pairedDevices = myBluetooth.getBondedDevices();
               ArrayList list = new ArrayList();
               if (pairedDevices.size() > 0) {
                   for (BluetoothDevice bt : pairedDevices) {
                       list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address

                   }
               }
               final CharSequence[] dialogtext = new CharSequence[list.size()];
               int i = 0;
               while (i < list.size()) {
                   dialogtext[i] = list.get(i).toString();
                   i++;
               }
               //Log.d("size2",""+dialogtext.length);
               AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
               builder.setTitle("PAIRED DEVICES ");
               builder.setItems(dialogtext, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int item) {
                       Log.d("pos", "" + item);
                       String info = dialogtext[item].toString();
                       name=info.split("\n")[0];
                       String add = info.substring(info.length() - 17);

                       if(checkhome==1){progressBar3.setVisibility(View.VISIBLE);}

                       if (btstatus.matches("disconnected")) {
                           BtConnect con = new BtConnect();
                           con.execute(add);
                       } else
                           Toast.makeText(MainActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();
                   }
               });
               AlertDialog alert = builder.create();
               alert.show();

           }
        }

        return super.onOptionsItemSelected(item);
    }

//--------------------------------------------------------menu-------------------------------------------------------------------

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bt) {
            start=1;
            checkbt=1;
            if(checkhome==1){
                layoutToAdd.removeViewAt(1);
            }
            if(checkgf==1){
                layoutToAdd.removeViewAt(1);
            }
            checkgf=0;checkhome=0;

            LayoutInflater inflater = LayoutInflater
                    .from(getApplicationContext());
            viewbt = inflater.inflate(R.layout.bluetooth, null);
            layoutToAdd.addView(viewbt,1);check=1;
            pd=(ImageButton)findViewById(R.id.paireddev);
            discimg=(ImageView)findViewById(R.id.discid);
            disctext=(TextView)findViewById(R.id.disctextid);
            disconnect=(ImageButton)findViewById(R.id.imageButton2);

            btlayout=(RelativeLayout)findViewById(R.id.btlayout);
            btview=(View)findViewById(R.id.btlayout);
            Drawable btdraw=btview.getBackground();
             progressBar2=(ProgressBar)findViewById(R.id.progressBar3);
            View view=(View)findViewById(R.id.progressBar3);
            view.setAlpha(1);
             progressBar2.setVisibility(View.GONE);
            if(btstatus.matches("connected")){
                disctext.setText("Connected");
                disctext.setTextColor(Color.parseColor("#26862e"));
                discimg.setImageResource(R.mipmap.ic_launchersuccess);
            }
            Button b=(Button)findViewById(R.id.intentbutton);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent();
                    i.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(i);
                }
            });
                //btview.setAlpha((float) 0.3);
            disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{btview.setAlpha((float) 0.5);
                        progressBar2.setVisibility(View.VISIBLE);}catch(Exception e){
                        e.printStackTrace();
                    }

                    if (btstatus.matches("disconnected")) {
                        try{progressBar2.setVisibility(View.GONE);
                            btview.setAlpha(1);}catch(Exception e){
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();
                    } else {
                        if (mmInStream != null) {
                            try {
                                mmInStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mmInStream = null;
                        }

                        if (mmOutStream != null) {
                            try {
                                mmOutStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mmOutStream = null;
                        }
                        if (bsocket != null) {
                            try {
                                bsocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bsocket = null;
                        }
                        // btview.setAlpha(1);
                    }
                }

            });

            pd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("yo", "yess");

                    setup();
                    pairedDevices = myBluetooth.getBondedDevices();
                    ArrayList list = new ArrayList();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice bt : pairedDevices) {
                            list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address

                        }
                    }
                    final CharSequence[] dialogtext = new CharSequence[list.size()];
                    int i = 0;
                    while (i < list.size()) {
                        dialogtext[i] = list.get(i).toString();
                        i++;
                    }
                    //Log.d("size2",""+dialogtext.length);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("PAIRED DEVICES ");
                    builder.setItems(dialogtext, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Log.d("pos", "" + item);
                            String info = dialogtext[item].toString();
                            name = info.split("\n")[0];
                            String add = info.substring(info.length() - 17);

                            btview.setAlpha((float) 0.5);
                            progressBar2.setVisibility(View.VISIBLE);

                            if (btstatus.matches("disconnected")) {
                                BtConnect con = new BtConnect();
                                con.execute(add);
                            } else
                                Toast.makeText(MainActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                   // btview.setAlpha(1);
                }
            });
        }

        if (id == R.id.nav_home){

            checkhome=1;
            if(checkbt==1){
                layoutToAdd.removeViewAt(1);
            }
            if(checkgf==1){
                layoutToAdd.removeViewAt(1);
            }
            checkbt=0;checkgf=0;
            LayoutInflater inflater = LayoutInflater
                    .from(getApplicationContext());
            if(start>0){viewhome = inflater.inflate(R.layout.main, null);
                layoutToAdd.addView(viewhome, 1);}
            start=1;
            progressBar3=(ProgressBar)findViewById(R.id.progressBar3);
            progressBar3.setVisibility(View.GONE);
            tv=(TextView)findViewById(R.id.statusview);
            iv=(ImageView)findViewById(R.id.imgbtstatus);
            btdatarec=(TextView)findViewById(R.id.btdata);
            sendingstatus=(TextView)findViewById(R.id.sendingstatus);
            progressBar=(ProgressBar)findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            paad=(View)findViewById(R.id.paad);
            imv=(ImageView)findViewById(R.id.paad);
            fuelview=(TextView)findViewById(R.id.fuelview);
            rpmview=(TextView)findViewById(R.id.rpmview);
            speedview=(TextView)findViewById(R.id.speedview);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable=getDrawable(R.mipmap.ic_launcher_alertdialog);
                drawable2=getDrawable(R.mipmap.ic_launcher_right);
            }



            if(btstatus.matches("connected")){
                iv.setImageResource(R.mipmap.ic_launchersuccess);
                tv.setTextColor(Color.parseColor("#26862e"));
                tv.setText("Connected");
            }
            progressBar3.setVisibility(View.GONE);
            // tv=(TextView)findViewById(R.id.statusview);
            // iv=(ImageView)findViewById(R.id.imgbtstatus);
            //progressBar3=(ProgressBar)findViewById(R.id.progressBar3);
            if(fuel!=null&&rpm!=null&&speed!=null){
                fuelview.setText(fuel);
                speedview.setText(speed);
                rpmview.setText(rpm);
            }
            if(btstatus.matches("connected")){
            paad.setAlpha(0);
            imv.setImageResource(R.mipmap.ic_launcher_right);
        }}

        if(id==R.id.nav_goglfrm){
            start=1;
            checkgf=1;
            if(checkbt==1){Log.d("inside","gf");
            layoutToAdd.removeViewAt(1);
            }
            if(checkhome==1){
                layoutToAdd.removeViewAt(1);
            }
            checkbt=0;checkhome=0;

            LayoutInflater inflater = LayoutInflater
                    .from(getApplicationContext());
            viewgf = inflater.inflate(R.layout.googleforminfo, null);

            layoutToAdd.addView(viewgf,1);
            ImageButton subinfo=(ImageButton)findViewById(R.id.submitt);
            url=(EditText)findViewById(R.id.url);
            fuelkey=(EditText)findViewById(R.id.fuel);
            rpmkey=(EditText)findViewById(R.id.rpm);
            speedkey=(EditText)findViewById(R.id.speed);

            if(URL!=null){
                url.setText(URL);
                fuelkey.setText(FUEL_ID);
                rpmkey.setText(RPM_ID);
                speedkey.setText(SPEED_ID);
            }

            subinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                          //-------------------------------> submitt
                    if (url.length() == 0 || fuelkey.length() == 0 || rpmkey.length() == 0 || speedkey.length() == 0) {
                        Toast.makeText(MainActivity.this, "Enter the Credentials", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Save Changes")
                                .setMessage("You are about to change the Google Form data !")
                                .setIcon(R.mipmap.ic_launcher_alertdialog)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        URL = url.getText().toString();
                                        FUEL_ID = fuelkey.getText().toString();
                                        RPM_ID = rpmkey.getText().toString();
                                        SPEED_ID = speedkey.getText().toString();

                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.clear();
                                        editor.putString("urk", URL);
                                        editor.putString("fuel", FUEL_ID);
                                        editor.putString("rpm", RPM_ID);
                                        editor.putString("speed", SPEED_ID);
                                        editor.apply();
                                        Toast.makeText(MainActivity.this,"Changes Saved",Toast.LENGTH_SHORT).show();
                                    }
                                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                    }
                }
            });
        }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setup() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
    }

    public void registerbroadcast(){
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);
    }

    public class BtConnect extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                if (btSocket == null || !isBtConnected) {
                    //myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(params[0]);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    myBluetooth.cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                //Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                btstatus="disconnected";
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);
            if(checkbt==1){progressBar2.setVisibility(View.GONE); btview.setAlpha(1);}
            else if(checkhome==1)progressBar3.setVisibility(View.GONE);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
            } else {
                isBtConnected = true;
                //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                btstatus = "connected";
                try {
                    mConnectedThread = new ConnectedThread(btSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mConnectedThread.start();
            }
        }
    }

    private class ConnectedThread extends Thread {

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) throws IOException {
            InputStream tmpIn = null;

            int test=0,start=0;
            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                bsocket = socket;

            } catch (IOException e) {
            }
            mainerror=0;

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            in = new BufferedReader(new InputStreamReader(mmInStream));
        }
        public void run() {
            byte[] buffer2= new byte[0];

            /*if(tempstrt==0){tempstrt=1;
            try {
                buffer2 = IOUtils.toByteArray(mmInStream);
                Log.d("buffer length",""+buffer2.length);
            } catch (IOException e) {
                e.printStackTrace();
            }}*/
            byte[] buffer=new byte[6000000];
            int begin = 0;
            int bytes = 0;


            try {
                bytes = mmInStream.read(buffer,0,buffer.length);
                Log.d("bytesoutside",""+bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            int prevbyte=0,netbyte=0;
            while (true&&mmInStream!=null&&mmOutStream!=null) {
                try {
                    message=in.readLine();
                    Log.d("message",message);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, message).sendToTarget();
                } catch (IOException e) {
                    break;
                }

            }
        }
    }

    public void disconnect(){

         {

                if (mmInStream != null) {
                    try {
                        mmInStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mmInStream = null;
                }

                if (mmOutStream != null) {
                    try {
                        mmOutStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mmOutStream = null;
                }
                if (bsocket != null) {
                    try {
                        bsocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bsocket = null;
                }
        }
    }
}

