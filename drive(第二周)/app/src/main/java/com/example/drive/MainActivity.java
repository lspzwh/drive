package com.example.drive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String BACK_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION";


    MapView mapview= null;
    AMap aMap;
    MyLocationStyle myLocationStyle;
    UiSettings mUiSettings;
    private ImageButton btn_traffic = null;
    private ImageButton btn_satellite = null;
    private ImageButton btn_normal = null;
    private int normarl_night_flag = 0;
    private int traffic_flag = 0;
    private int statellite_flag = 0;


    protected void createMap(Bundle savedInstanceState){
        mapview=findViewById(R.id.map_view);
        btn_normal = findViewById(R.id.btn_map);
        btn_traffic = findViewById(R.id.btn_traffic);
        btn_satellite = findViewById(R.id.btn_satellite);
        //
        mapview.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapview.getMap();
        }
        //定位蓝点
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;
        //
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setScaleControlsEnabled(true);
        //夜间\正常切换
        btn_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (normarl_night_flag == 0){
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                    btn_normal.setBackgroundResource(R.drawable.m);
                }else{
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                    btn_normal.setBackgroundResource(R.drawable.moon);
                }
                normarl_night_flag = (normarl_night_flag + 1)%2;
            }
        });
        //交通
        btn_traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (traffic_flag == 0){
                    aMap.setTrafficEnabled(true);
                    btn_traffic.setBackgroundResource(R.drawable.t);
                }else{
                    aMap.setTrafficEnabled(false);
                    btn_traffic.setBackgroundResource(R.drawable.traffic);
                }
                traffic_flag = (traffic_flag+1)%2;
            }

        });
        //显示卫星地图
        btn_satellite.setOnClickListener(v -> {
            if (statellite_flag == 0){
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                btn_satellite.setBackgroundResource(R.drawable.s);
            }else{
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                btn_satellite.setBackgroundResource(R.drawable.satelite);
            }
            statellite_flag = (statellite_flag + 1)%2;
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }
        createMap(savedInstanceState);
    }
    protected void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }
    protected String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            BACK_LOCATION_PERMISSION
    };
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;
    private void initPermission() {
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限
            }
        }
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }
     //回调
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面
            }
        }
    }
    AlertDialog mPermissionDialog;
    String mPackName = "com.example.drive";

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，是否确定授予权限")
                    .setPositiveButton("确定", (dialog, which) -> {
                        cancelPermissionDialog();

                        Uri packageURI = Uri.parse("package:" + mPackName);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        //关闭页面或者做其他操作
                        cancelPermissionDialog();
                    })
                    .create();
        }
        mPermissionDialog.show();
    }
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

}
