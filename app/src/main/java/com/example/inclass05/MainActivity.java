package com.example.inclass05;

/*
* Group 29
* Mayuri Jain, Narendra Pahuja
*
* */
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button gobt;
    TextView tv_option;
    ImageView im_view;
    ImageView im_next;
    ImageView im_prev;
    ProgressBar pg_bar;
    ArrayList<String> finalUrls = new ArrayList<>();
    int Current_Index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gobt = findViewById(R.id.bt_go);
        tv_option = findViewById(R.id.tv_option);
        im_view = findViewById(R.id.im_view);
        pg_bar = findViewById(R.id.pg_bar);
        im_next = findViewById(R.id.im_next);
        im_prev = findViewById(R.id.im_prev);

        pg_bar.setVisibility(View.INVISIBLE);
        im_next.setVisibility(View.INVISIBLE);
        im_prev.setVisibility(View.INVISIBLE);

        gobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()){
                    finalUrls.clear();
                    new getAsyncKeywords().execute("http://dev.theappsdr.com/apis/photos/keywords.php");
                }else{
                    Toast.makeText(MainActivity.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        im_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newindex=getindex("right", Current_Index,finalUrls.size());
                new getAsyncImagesfromUrl().execute(finalUrls.get(newindex));
            }
        });

        im_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newindex=getindex("left", Current_Index,finalUrls.size());
                new getAsyncImagesfromUrl().execute(finalUrls.get(newindex));
            }
        });


    }

    private int getindex(String direction,int currentIndex,int size)
    {
        if(direction=="left" && currentIndex !=0)
            return currentIndex-1;
        else if( direction=="left" && currentIndex==0)
            return size-1;

        else if( direction=="right" && currentIndex!=size-1)
            return currentIndex+1;
        else
            return 0;

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni != null && ni.isConnected()){
            return true;
        }
        return false;
    }

    public class getAsyncKeywords extends AsyncTask<String, Void, LinkedList<String>>{

        @Override
        protected LinkedList<String> doInBackground(String... params) {
            LinkedList<String> list = new LinkedList<>();
            HttpURLConnection  conn = null;
            String result = null;
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    result = IOUtils.toString(conn.getInputStream(), "UTF-8");
                    Log.d("demo", result);
                    for (String x : result.split(";")) {
                        list.add(x);
                        Log.d("demo", x);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(conn != null){
                    conn.disconnect();
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(LinkedList<String> mlist) {

            final LinkedList<String> list = mlist;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose item")
                    .setItems(list.toArray(new CharSequence[list.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("demo","clicking on option");
                            tv_option.setText(list.get(i));
                            new getAsyncImagesUrl(list.get(i)).execute("http://dev.theappsdr.com/apis/photos/index.php");
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public class getAsyncImagesUrl extends AsyncTask<String, Void, LinkedList<String>>{
        String keyword = null;

        @Override
        protected void onPreExecute() {
            pg_bar.setVisibility(View.VISIBLE);
        }

        public getAsyncImagesUrl(String mkeyword){
            this.keyword = mkeyword;
        }
        @Override
        protected LinkedList<String> doInBackground(String... params) {
            LinkedList<String> list = new LinkedList<>();
            HttpURLConnection  conn = null;
            String result = null;
            Log.d("demo", keyword);
            try {
                URL url = new URL(params[0]+"?keyword="+keyword);
                conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    result = IOUtils.toString(conn.getInputStream(), "UTF-8");
                    Log.d("demo1", result);
                    if(!result.isEmpty()) {
                        for (String x : result.split("\n")) {
                            list.add(x);
                            //finalUrls.add(x);
                            Log.d("demo", x);
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(conn != null){
                    conn.disconnect();
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(final LinkedList<String> list) {
            if(list != null){
                for(String x: list){
                    Log.d("demo2", x);
                }
                finalUrls.addAll(list);
                if(list.size() == 0){
                    im_next.setVisibility(View.INVISIBLE);
                    im_prev.setVisibility(View.INVISIBLE);
                    im_view.setImageResource(R.drawable.download);
                    Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
                    pg_bar.setVisibility(View.INVISIBLE);
                }else if(list.size() == 1){
                    im_next.setVisibility(View.INVISIBLE);
                    im_prev.setVisibility(View.INVISIBLE);
                    new getAsyncImagesfromUrl().execute(list.get(0));
                } else {
                    im_next.setVisibility(View.VISIBLE);
                    im_prev.setVisibility(View.VISIBLE);
                    new getAsyncImagesfromUrl().execute(list.get(0));
                }

            }
        }
    }

    public class getAsyncImagesfromUrl extends AsyncTask<String, Void, Bitmap>{

        Bitmap bitmap;

        @Override
        protected Bitmap doInBackground(String... params) {
            HttpURLConnection  conn = null;
            bitmap = null;
            try {
                URL url = new URL(params[0]);
                Current_Index = finalUrls.indexOf(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(conn != null){
                    conn.disconnect();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pg_bar.setVisibility(View.INVISIBLE);
            if (bitmap != null) {
                im_view.setImageBitmap(bitmap);
            }

        }
    }
}
