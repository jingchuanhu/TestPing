package com.sohu.jch.testping;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

/**
 * Created by jch on 2017/4/28.
 */

public class MainPresentImp implements MainPresent {

    private Handler handler = null;
    private MainView view = null;
    private static final String TAG = "MainPresentImp";
    private boolean pingFlag = false;

    public MainPresentImp() {
        this.handler = new Handler(Looper.getMainLooper());
    }


    @Override
    public void addViewListener(MainView view) {
        this.view = view;
    }

    @Override
    public void removeViewListener() {
        this.view = null;
    }

    @Override
    public void startPing() {

        doPing();
    }

    @Override
    public void stopPing() {
        pingFlag = false;
    }

    private void doPing() {
        new Thread(() -> {
            ping();
        }).start();
    }


    private void ping() {
        pingFlag = true;
        String[] addres = {"www.baidu.com"};

        if (addres.length < 1) {
            Log.d(TAG, "syntax Error !");
        } else {
            Log.d(TAG, "start Ping");

            for (int i = 0; i < addres.length; i++) {

                String line = null;


                try {
                    boolean status = InetAddress.getByName(addres[i]).isReachable(3000);//超时应该在3钞以上

                    String pingStr = "ping -s 100 -c 4 -W 1 " + addres[i];     //packetsize 1000type count 4  timout 2s
                    Log.d(TAG, " ping : " + pingStr);
                    java.lang.Process pro = Runtime.getRuntime().exec(pingStr);
                    InputStream is = pro.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    while ((line = br.readLine()) != null) {

                        Log.d(TAG, "ping : " + line);
                        postResult(line);

                        if (line.startsWith("rtt")){
                            postAvg(getAvgTime(line));
                        }

//                        if (!pingFlag) {
//                            Runtime.getRuntime().exec("exit(0)");
//                        }
                    }
                    is.close();
                    isr.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ping error: " + e.getMessage());
                    postResult(e.getMessage());
                } finally {
                    pingFlag = false;
                }

                postResult("ping finished");

            }
        }


    }

    private void postResult(String result) {
        handler.post(() -> {
            view.pingResult(result);
        });
    }

    private void postAvg(float avg){
        handler.post(()->{
            view.pingAvg(avg);
        });
    }

    /**
     * parse the ping string and get the average time.
     * @param pingStr  example: rtt min/avg/max/mdev = 7.641/20.824/55.036/19.872 ms
     * @return  -1 :the ping string is error.
     */
    private float getAvgTime(String pingStr){

        float avg = -1f;
        if (pingStr == null){

            return avg;
        }

        String[] strTypes = pingStr.split("=");
        if (strTypes.length == 2){

            String[] keysStr = strTypes[0].trim().split("/");
            String[] valuses = strTypes[1].trim().split("/");
            for (int i = 0; i< keysStr.length; i++){

                if (keysStr[i].equals("avg")){
                    String avgStr = valuses[i];
                    avg = Float.valueOf(avgStr);
                }
            }

        }

        return avg;

    }

}
