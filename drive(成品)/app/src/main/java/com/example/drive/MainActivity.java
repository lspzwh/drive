package com.example.drive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.drive.overlay.PoiOverlay;
import com.example.drive.util.Constants;
import com.example.drive.util.GeoCoderUtil;
import com.example.drive.util.LatLngEntity;
import com.example.drive.util.ToastUtil;
import com.example.drive.util.Utils;

import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnMarkerClickListener, InfoWindowAdapter,
        OnPoiSearchListener, OnClickListener ,AMapLocationListener , AMap.OnCameraChangeListener{
    private static String BACK_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    private static final String TAG = "MainActivity";
    private static final int DEFAULT_ZOOM = 14;


    MapView mapview= null;
    AMap aMap;
    MyLocationStyle myLocationStyle;
    UiSettings mUiSettings;
    private String mKeyWords = "";// 要输入的poi搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条
    private ImageButton btn_traffic = null;
    private ImageButton btn_satellite = null;
    private ImageButton btn_normal = null;
    private int normarl_night_flag = 0;
    private int traffic_flag = 0;
    private int statellite_flag = 0;
    private PoiOverlay poiOverlay;
    private PoiResult mpoiResult; // poi返回的结果
    private int currentPage = 1;
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private TextView mKeywordsTextView;
    private Marker mPoiMarker;
    private ImageView mCleanKeyWords;
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private AMapLocation aMapLocation;

    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    public static final int RESULT_CODE_KEYWORDS = 102;


    protected void createMap(Bundle savedInstanceState){
        mapview=findViewById(R.id.map_view);
        btn_normal = findViewById(R.id.btn_map);
        btn_traffic = findViewById(R.id.btn_traffic);
        btn_satellite = findViewById(R.id.btn_satellite);
        mCleanKeyWords = (ImageView)findViewById(R.id.clean_keywords);
        mKeywordsTextView = (TextView) findViewById(R.id.main_keywords);
        //
        mapview.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapview.getMap();
        }
        //
        mCleanKeyWords.setOnClickListener(this);
        mKeywordsTextView.setOnClickListener(this);
        mKeyWords = "";
        //定位蓝点
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        //
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //初始化定位
        mLocationClient = new AMapLocationClient(this);
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //给定位客户端对象设置定位参数
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //获取一次定位结果：
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        // mLocationOption.setOnceLocationLatest(true);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
        //
        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setOnCameraChangeListener(this);

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
        Log.d(TAG,"onCreate ");
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
    //显示进度条
    private void showProgressDialog() {
        Log.d(TAG,"showProgressDialog ");
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + mKeyWords);
        progDialog.show();
    }
    //隐藏进度条
    private void dissmissProgressDialog() {
        Log.d(TAG,"dissmissProgressDialog ");
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
    //搜索
    protected void doSearchQuery(String keywords) {
        Log.d(TAG,"doSearchQuery keywords = "+keywords);
        showProgressDialog();
        currentPage = 1;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keywords, "", "");
        // 设置每页最多返回多少条
        query.setPageSize(10);
        // 设置查第一页
        query.setPageNum(currentPage);

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_keywords:
                Intent intent = new Intent(this, InputTipsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.clean_keywords:
                mKeywordsTextView.setText("");
                aMap.clear();
                mCleanKeyWords.setVisibility(View.GONE);
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d(TAG,"onLocationChanged aMapLocation = "+aMapLocation.toString());
        if (aMapLocation != null) {
            this.aMapLocation=aMapLocation;
            if (aMapLocation.getErrorCode() == 0) {
                LatLng markerPosition = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, DEFAULT_ZOOM));
            }else {
                Log.e("onLocationChanged"," Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });

        return view;
    }
    private void startAMapNavi(Marker marker) {
        if(aMapLocation==null){
            Toast.makeText(this,"hhh",Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, NaviActivity.class);
        intent.putExtra("gps", false);
        intent.putExtra("start", new NaviLatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
        intent.putExtra("end", new NaviLatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        startActivity(intent);
    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        Log.d(TAG,"showSuggestCity");
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Log.d(TAG,"showSuggestCity infomation = "+infomation);
        ToastUtil.show(this, infomation);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.d(TAG,"onCameraChangeFinish cameraPosition = "+cameraPosition.toString());
        LatLngEntity latLngEntity = new LatLngEntity(cameraPosition.target.latitude, cameraPosition.target.longitude);
        //地理反编码工具类，代码在后面
        GeoCoderUtil.getInstance(this).geoAddress(latLngEntity, new GeoCoderUtil.GeoCoderAddressListener() {
            @Override
            public void onAddressResult(String result) {
                Log.d(TAG,"onCameraChangeFinish result = "+result);
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        Log.d(TAG,"onPoiSearched");
        dissmissProgressDialog();
        if (i == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {
                // 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {
                    // 是否是同一条
                    mpoiResult = poiResult;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = mpoiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = mpoiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Log.d(TAG,"onPoiSearched no_result 1");
                        ToastUtil.show(this, R.string.no_result);
                    }
                }
            } else {
                Log.d(TAG,"onPoiSearched no_result 2");
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            Log.d(TAG,"onPoiSearched rCode = "+i);
            ToastUtil.showerror(this, i);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE_INPUTTIPS && data
                != null) {
            Log.d(TAG,"onActivityResult resultCode == RESULT_CODE_INPUTTIPS ");
            aMap.clear();
            Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
            if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                doSearchQuery(tip.getName());
            } else {
                addTipMarker(tip);
            }
            mKeywordsTextView.setText(tip.getName());
            if(!tip.getName().equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }
        } else if (resultCode == RESULT_CODE_KEYWORDS && data != null) {
            Log.d(TAG,"onActivityResult resultCode == RESULT_CODE_KEYWORDS ");
            aMap.clear();
            String keywords = data.getStringExtra(Constants.KEY_WORDS_NAME);
            if(keywords != null && !keywords.equals("")){
                doSearchQuery(keywords);
            }
            mKeywordsTextView.setText(keywords);
            if(!keywords.equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }
        }
    }
    private void addTipMarker(Tip tip) {
        Log.d(TAG,"addTipMarker ");
        if (tip == null) {
            return;
        }
        Log.d(TAG,"addTipMarker tip.getAdcode = "+tip.getAdcode()+" , tip.getAddress = "+tip.getAddress() +" , tip.getDistrict = "+tip.getDistrict()
                + " , tip.getName = "+tip.getName()+ " , tip.getPoiID = "+tip.getPoiID());
        mPoiMarker = aMap.addMarker(new MarkerOptions());
        LatLonPoint point = tip.getPoint();
        if (point != null) {
            LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
            mPoiMarker.setPosition(markerPosition);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, DEFAULT_ZOOM));
        }
        mPoiMarker.setTitle(tip.getName());
        mPoiMarker.setSnippet(tip.getAddress());
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
