package com.example.colin.labfinal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Created by Colin on 11/18/2015.
 */
public class StockService extends Service {

    String url;
    private ArrayList<String> urlPreviousData = new ArrayList<>();
    private static ArrayList<String> urlStrings = new ArrayList<>(); //an array of symbols
    private boolean isStarted = false;
    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode;

    /**
     * interface for clients that bind
     */
    IBinder mBinder;

    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind;

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {

    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("url");

        if (!isStarted) {
            startTimer(2);
            isStarted = true;
        }
        addURL(url);
        return START_REDELIVER_INTENT;
    }
    public void startTimer( final long time) {
        Log.d("timerUrlcheck", url);
        new CountDownTimer((time*5000), time*5000) {
            public void onTick(long millis) {
                //Log.d("timer", "timer is working");

            }
            public void onFinish() {
                for (int i =0; i < urlStrings.size(); i++) {
                    AsyncWeb task = new AsyncWeb();
                    task.execute(urlStrings.get(i).toString(), Integer.toString(i));

                    Log.d("timer", "Onfinish is working");
                }
                startTimer(time);
            }
        }.start();

    }
    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {

    }
    public class Wrapper {
        public int i;
        public String html;
    }
    public class AsyncWeb extends AsyncTask<String, Void, Wrapper> {

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected Wrapper doInBackground(String... params) {

            //String html = "";
            Wrapper html = new Wrapper();
            try {
                Log.d("url", params[0].toString());
                URL address = new URL(params[0]);
                //URL address = new URL("http://www.google.com/");
                BufferedReader reader = new BufferedReader(new InputStreamReader(address.openStream()));
                String buffer = "";
                while ((buffer = reader.readLine()) != null) {
                    html.html += buffer;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            //WebView web = (WebView)params[1];
            html.i = Integer.parseInt(params[1]);
            return html;
        }

        @Override
        protected void onPostExecute(Wrapper result) {
            Log.d("URL", result.html);
            Log.d("index", Integer.toString(result.i));
            if (urlPreviousData.isEmpty() || urlPreviousData.size() <= result.i) {
                urlPreviousData.add(result.i, result.html);
                Log.d("ASYNC_POST", "Added result into ArrayLst");
            }
            else if (!result.html.equals(urlPreviousData.get(result.i).toString())) {
                Log.d("ASYNC_POST", "Found new version");
                urlPreviousData.set(result.i, result.html);
// get the supported ids for GMT-08:00 (Pacific Standard Time)
                String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
                // if no ids were returned, something is wrong. get out.
                if (ids.length == 0)
                    System.exit(0);

                // begin output
                System.out.println("Current Time");

                // create a Pacific Standard Time time zone
                SimpleTimeZone pdt = new SimpleTimeZone(-5 * 60 * 60 * 1000, ids[0]);

                // set up rules for daylight savings time
                pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
                pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

                // create a GregorianCalendar with the Pacific Daylight time zone
                // and the current date and time
                Calendar calendar = new GregorianCalendar(pdt);
                Date trialTime = new Date();
                calendar.setTime(trialTime);
                int icon = R.mipmap.ic_launcher;
                long when = System.currentTimeMillis();
                NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);



                Log.d("DateTime", urlStrings.get(result.i) + " has been changed at " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));

                intent.putExtra("changes", urlStrings.get(result.i) + " has been changed at " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                //intent.addFlags() |= Notification.FLAG_AUTO_CANCEL;
                //intent.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationMessage, pendingNotificationIntent);

                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent  pending=PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                Notification notification;

                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Changes have been found")
                        .setContentText(
                                urlStrings.get(result.i)).setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pending).setWhen(when).setAutoCancel(true)
                        .build();

                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND;
                nm.notify(0, notification);

            }
            else {
                Log.d("ASYNC_POST", "Did not find new version");
            }


        }
    }
    public static void addURL(String url) {
        urlStrings.add(url);
    }
}
