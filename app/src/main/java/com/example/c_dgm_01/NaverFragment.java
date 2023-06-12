package com.example.c_dgm_01;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class NaverFragment extends Fragment  implements OnMapReadyCallback
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private MapView mapView;
    private TextView txtn; //대피소 이름
    private TextView txta; //대피소 주소
    private TextView txtp; //대피소 수용인원
    private ImageButton btn; //이미지 버튼
    private LinearLayout linearLayout ; //레이아웃 초기에 안보이게 설정
    double currentLatitude=0; //경도 저장
    double currentLongitude=0; //위도  저장
    private ArrayList<Marker> markers = new ArrayList<>();
    public int c = 0;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_naver, container, false);
        requestLocationPermission();
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        linearLayout = v.findViewById(R.id.map_info_layout2);
        txtn =v.findViewById(R.id.map_info_name);
        txta =v.findViewById(R.id.map_info_addr);
        txtp = v.findViewById(R.id.map_info_people);
        btn = v.findViewById(R.id.map_info_button);
        return v;
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        setMarkers();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }



    private void setMarkers() {
        c=0; //대피소 수
        new Handler().postDelayed(new Runnable() //딜레이 2초 -> 바로실행시 내 위치를 못 가져옴
        {

            @Override
            public void run()
            {
                Location lastLocation = locationSource.getLastLocation();
                if (lastLocation != null) {
                    currentLatitude=lastLocation.getLatitude();
                    currentLongitude=lastLocation.getLongitude();
                    String a= String.valueOf(currentLatitude);
                    InputStream inputStream = getResources().openRawResource(R.raw.inf); // 리소스파일 안에 있는 inf txt파일을 읽음
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {  //한줄씩 가져와서 널이면 종료
                            String arr[] = line.split(","); // , 를 기준으로 분류해서 배열생성
                            // System.out.println(arr[4]); // 좌표 테스트 코드
                            if (distance(currentLatitude, currentLongitude, Double.parseDouble(arr[5]), Double.parseDouble(arr[4]), "kilometer") < 1) { //좌표사이 거리가 1km미만일 경우 마커에 추가
                                markers.add(new Marker());
                                LatLng destinationLatLng = new LatLng(Double.parseDouble(arr[5]), Double.parseDouble(arr[4])); //경도 위도 좌표형태로 변환
                                markers.get(c).setPosition(destinationLatLng); //마커 추가
                                markers.get(c).setMap(naverMap); //마커 맵에 표시
                                markers.get(c++).setOnClickListener(new Overlay.OnClickListener() { //마커 버튼이벤트
                                    @Override
                                    public boolean onClick(@NonNull Overlay overlay) {
                                        txtn.setText(arr[2]);
                                        txta.setText(arr[0]+" "+arr[1]);
                                        txtp.setText(arr[3]);
                                        linearLayout.setVisibility(View.VISIBLE);
                                        btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openKakaomap(currentLatitude, currentLongitude, Double.parseDouble(arr[5]), Double.parseDouble(arr[4]));
                                            }
                                        });

                                        return false;

                                    }
                                });

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }, 2000);


    }
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if (unit == "meter") {
            dist = dist * 1609.344;
        }

        return (dist);
    }

    //좌표 사이 거리구하기
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    //카카오맵 연동
    public void openKakaomap(double lat1, double lon1, double lat2, double lon2){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("").setMessage("길찾기를 시작하겠습니까?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() { //예 버튼
            public void onClick(DialogInterface dialog, int id) {
                String url = "kakaomap://route?sp="+String.valueOf(lat1)+","+String.valueOf(lon1)+"&ep="+String.valueOf(lat2)+","+String.valueOf(lon2)+"&by=FOOT"; //카카오맵 URL 만들기
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url)); //인터넷 연결

                startActivity(intent);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() { //아니요 버튼
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show(); //알람 표시시

    }



}