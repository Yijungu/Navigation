package com.example.navigation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private boolean trackingMode = true;
    TMapView tMapView = null;
    TMapGpsManager tMapGps = null;
    private String Address;
    TMapPoint Destination_Point = null;
    private Context mContext = this;
    private boolean m_bTrackingMode = false;

    private List<String> list;          // 데이터를 넣은 리스트변수
    private ListView listView;          // 검색을 보여줄 리스트변수
    private EditText editSearch;        // 검색어를 입력할 Input 창
    private Button searchbutton;
    private SearchAdapter adapter;      // 리스트뷰에 연결할 아답터
    private String des;
    private Alarm alarm = null;

    private TMapPoint testiantion;

    private TMapPoint my_location;
    private TMapPoint destination;

    private ArrayList<TMapPoint> tPoints = null;
    public boolean alarming = true;

    public Thread alarmThread;
    AtomicBoolean accident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout tmapLayout = findViewById(R.id.tmapLayout);

        tMapView = new TMapView(this);
//
        tMapView.setSKTMapApiKey("\tl7xxa5b961d8570f4cde98fa199aaa572587");
        tmapLayout.addView(tMapView);

        tMapView.setCompassMode(true);

        tMapView.setIconVisibility(true);

        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);


        tMapGps = new TMapGpsManager(MainActivity.this);
        tMapGps.setMinTime(1000);
        tMapGps.setMinDistance(5);
        tMapGps.setProvider((tMapGps.NETWORK_PROVIDER));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        //tMapGps.OpenGps();

        tMapView.setSightVisible(true);

        ImageButton searchButton = (ImageButton) findViewById(R.id.map_navigation_botton);
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SearchDestination();
            }
        });

        ImageButton gpsbutton = (ImageButton) findViewById(R.id.gps_botton);
        gpsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tMapView.setTrackingMode(true);
                tMapView.setSightVisible(true);
            }
        });

        Button testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });
    }


    TMapPoint Current_Point;
    double getCurrent_long;
    double getCurrent_lat;

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

        getCurrent_long = location.getLongitude();
        getCurrent_lat = location.getLatitude();

        Current_Point = new TMapPoint(getCurrent_lat, getCurrent_long);

        Log.d("getCurrent_lat : ", "" + getCurrent_lat);
        Log.d("getCurrent_long : ", "" + getCurrent_long);
    }

    public void SearchDestination() {
        m_bTrackingMode = true;

        LinearLayout search = (LinearLayout) findViewById(R.id.search);
        search.setVisibility(View.VISIBLE);
        Log.e("asdasd", "Asdasd");

        editSearch = (EditText) findViewById(R.id.editSearch);
        listView = (ListView) findViewById(R.id.listView);

        list = new ArrayList<String>();

        adapter = new SearchAdapter(list, mContext);

        listView.setAdapter(adapter);

        searchbutton = (Button) findViewById(R.id.search_button);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            TMapData tMapData = new TMapData();

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("장소 ", list.get(position));
                des = list.get(position);
                tMapData.findAllPOI(list.get(position), new TMapData.FindAllPOIListenerCallback() {

                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        if (poiItem != null) {
                            for (int i = 0; i < poiItem.size(); i++) {
                                Address = poiItem.get(i).getPOIAddress();
                                Destination_Point = poiItem.get(i).getPOIPoint();
                            }
                            tMapView.setTrackingMode(true);
                            tMapView.removeTMapPath();

                            my_location = tMapView.getLocationPoint();
                            destination = Destination_Point;

                            Log.e("point1 :", my_location.toString());
                            Log.e("point2 :", destination.toString());

                            TMapData tmapdata = new TMapData();

                            tmapdata.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, my_location, destination, new TMapData.FindPathDataListenerCallback() {
                                @Override
                                public void onFindPathData(TMapPolyLine polyLine) {
                                    polyLine.setLineColor(Color.BLUE);
                                    tMapView.addTMapPath(polyLine);
                                    tPoints = polyLine.getLinePoint();
                                }
                            });

                            int sleepSec = 10;

                            // 주기적인 작업을 위한
                            final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                            exec.scheduleWithFixedDelay(new Runnable(){
                                public void run(){
                                    try {
                                        alarm = new Alarm(tPoints);
                                        Log.e("test", tPoints.toString());
                                        accident = new AtomicBoolean(false);
                                        Thread alarmThread = new Thread() {
                                            public void run() {
                                                try {
                                                    if(alarming)
                                                        if(alarm != null)
                                                            accident.set(alarm.accident());
                                                } catch (IOException e) {
                                                    Log.e("11111111", e.toString());
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        Log.e("상태", alarmThread.getState().toString());
                                        if (alarmThread.getState() == Thread.State.NEW)
                                            alarmThread.start();
                                        try {
                                            alarmThread.join();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (accident.get()) {
                                            Log.e("이준구", String.valueOf(accident.get()));
                                            dialog();
                                        }
                                    } catch (Exception e) {

                                        e.printStackTrace();

                                    }
                                }
                            }, 0, sleepSec, TimeUnit.SECONDS);

                            Bitmap start = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_start);
                            Bitmap end = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_end);
                            tMapView.setTMapPathIcon(start, end);

                            tMapView.zoomToTMapPoint(my_location, destination);
                            search.setVisibility(View.GONE);
                        }
                    }

                });

            }
        });

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editSearch.getText().toString();
                TMapData tMapData = new TMapData();

                tMapData.findAllPOI(text, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        if (poiItem != null) {
                            list.clear();

                            for (int i = 0; i < poiItem.size(); i++) {
                                TMapPOIItem item = poiItem.get(i);

                                Log.e("POI Name: ", item.getPOIName().toString());
                                Log.e("Address: ", item.getPOIAddress().replace("null", ""));
                                Log.e("Point: ", item.getPOIPoint().toString());
                                testiantion = item.getPOIPoint();
                                list.add(item.getPOIName());

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            }
        });

    }


    public void dialog() {
        ConstraintLayout alarm_dialog = (ConstraintLayout) findViewById(R.id.alarm_dialog);
        alarm_dialog.setVisibility(View.VISIBLE);
        alarming = false;
        Log.e("들어갔냐", "햐");
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);

        Button alarm_button_yes = (Button) findViewById(R.id.alarm_button_yes);
        alarm_button_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TMapData tmapdata = new TMapData();
                my_location = tMapView.getLocationPoint();

                HashMap pathInfo = new HashMap();
                pathInfo.put("rStName", "충남대학교 정심화국제문화회관");
                pathInfo.put("rStlat", Double.toString(my_location.getLatitude()));
                pathInfo.put("rStlon", Double.toString(my_location.getLongitude()));
                pathInfo.put("rGoName", des);
                pathInfo.put("rGolat", Double.toString(destination.getLatitude()));
                pathInfo.put("rGolon", Double.toString(destination.getLongitude()));
                pathInfo.put("type", "arrival");
                Date currentTime = new Date();
                Log.e("이준구준구",pathInfo.toString()) ;
                tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, my_location, destination, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine polyLine) {
                        polyLine.setLineColor(Color.BLUE);
                        tMapView.addTMapPath(polyLine);
                        tPoints = polyLine.getLinePoint();
                    }
                });



//                new Thread(() -> {
//                    tmapdata.findTimeMachineCarPath(pathInfo, currentTime, null, "02");
//                    Bitmap start = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_start);
//                    Bitmap end = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_end);
//                    tMapView.setTMapPathIcon(start, end);
//                }).start();
                alarm_dialog.setVisibility(View.GONE);
                stpe.schedule(new Runnable(){
                    public void run(){
                        try {
                            alarming = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
        });

        Button alarm_button_no = (Button) findViewById(R.id.alarm_button_no);
        alarm_button_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alarm_dialog.setVisibility(View.GONE);
                stpe.schedule(new Runnable(){
                    public void run(){
                        try {
                            alarming = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 3600, TimeUnit.SECONDS);
            }
        });
    }

}




