package com.example.avinash.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by AVINASH on 12/4/2016.
 */
public class btconn extends AppCompatActivity  {
    String fuellevel=null, rpmlevel=null, speedlevel=null;
    public static final MediaType FORM_DATA_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
int i=0;
    MainActivity ma=new MainActivity();
    public void send(String fuel,String rpm,String speed){
        PostDataTask postDataTask=new PostDataTask();
        if(MainActivity.URL==null||fuel==null||rpm==null||speed==null){
            Log.d("btconn", "insufficient data");
           // Toast.makeText(getApplicationContext(),"Insufficient Data to send",Toast.LENGTH_SHORT).show();
        }
        else if(i==0){
            if(MainActivity.checkhome==1){MainActivity.fuelview.setText(fuel);
            MainActivity.speedview.setText(speed);
            MainActivity.rpmview.setText(rpm);}
        postDataTask.execute(MainActivity.URL, fuel, rpm, speed);

        i=1;}
    }

    private  class PostDataTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute(){
            try {
                if(MainActivity.checkhome==1) {
                    MainActivity.sendingstatus.setText("Sending data to: " + MainActivity.URL);
                    MainActivity.progressBar.setVisibility(View.VISIBLE);
                    MainActivity.paad.setAlpha(0);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        @Override
        protected Boolean doInBackground(String... contactData) {
            Boolean result = true;
            String url = contactData[0];
             fuellevel = contactData[1];
             rpmlevel = contactData[2];
             speedlevel = contactData[3];

            url=url.replace("viewform","formResponse");
            Log.d("url",url);
            Log.d("url",MainActivity.FUEL_ID);
            Log.d("url",MainActivity.RPM_ID);
            Log.d("url",MainActivity.SPEED_ID);
            String postBody="";
//            ma.texthandler.obtainMessage();
            try {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody =  MainActivity.FUEL_ID + "=" + URLEncoder.encode(fuellevel, "UTF-8")
                + "&" + MainActivity.RPM_ID + "=" + URLEncoder.encode(rpmlevel, "UTF-8")
                + "&" + MainActivity.SPEED_ID + "=" + URLEncoder.encode(speedlevel, "UTF-8");

            } catch (UnsupportedEncodingException ex) {
                result=false;
            }

            try{
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
            }catch (IOException exception){
                result=false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result){
            //Print Success or failure message accordingly
           // Toast.makeText(getApplicationContext(),result?"Message successfully sent!":"There was some error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
        if(result==true){
            Log.d("result","Message successfully sent");
            try {if(MainActivity.checkhome==1){
                MainActivity.sendingstatus.setText("Data Sent to: " + MainActivity.URL);
                MainActivity.progressBar.setVisibility(View.GONE);
                MainActivity.imv.setImageResource(R.mipmap.ic_launcher_right);
                MainActivity.paad.setAlpha(1);}}catch (Exception e){
                e.printStackTrace();
            }
        }
            else{
            Log.d("result","There was some error in sending message. Please try again after some time.");
            try {
                if(MainActivity.checkhome==1){
                    MainActivity.sendingstatus.setText("Sending Failed at: " + MainActivity.URL);
                    MainActivity.progressBar.setVisibility(View.GONE);
                    MainActivity.imv.setImageResource(R.mipmap.ic_launcher_alertdialog);
                    MainActivity.paad.setAlpha(1);}}catch (Exception e){
                e.printStackTrace();
            }}
        }
    }
}

