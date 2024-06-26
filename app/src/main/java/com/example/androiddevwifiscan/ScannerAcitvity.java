package com.example.androiddevwifiscan;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import java.io.File;

public class ScannerAcitvity extends AppCompatActivity {
    public EditText edtNhapN, edtNhapOx, edtNhapOy, edtNhapOz;
    private TextView txtTenFile;
    private AppCompatButton btnQuetWifi, btnBack;

    private LocationManager locationManager;
    private WifiManager wifiManager;
    private static final int MY_REQUEST_CODE = 123;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner_acitvity);

        InitUI();
        ChangeTextColor();

        String nameFile = getIntent().getStringExtra("file");

        txtTenFile.setText(nameFile + ".csv");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        checkPermissions();

        btnQuetWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = edtNhapN.getText().toString().trim();
                String ox = edtNhapOx.getText().toString().trim();
                String oy = edtNhapOy.getText().toString().trim();
                String oz = edtNhapOz.getText().toString().trim();

                if (!checkWifiStatus()) {
                    Toast.makeText(ScannerAcitvity.this,"Hãy bật wifi!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkLocationStatus()) {
                    Toast.makeText(ScannerAcitvity.this,"Hãy bật GPS!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(a)) {
                    Toast.makeText(ScannerAcitvity.this,"Hãy nhập số lần quét!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(a) <= 0 ) {
                    Toast.makeText(ScannerAcitvity.this, "Số lần quét phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(a) < 25) {

                    new AlertDialog.Builder(ScannerAcitvity.this)
                            .setTitle("Cảnh Báo")
                            .setMessage("Bạn nên nhập số lần quét lớn hơn 25!")
                            .setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onClickAlertDialogYes( a, nameFile);
                                }
                            })
                            .setNegativeButton("Quay lại", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .create().show();
                    return;
                }

                if (Integer.parseInt(a) > 80) {
                    new AlertDialog.Builder(ScannerAcitvity.this)
                            .setTitle("Cảnh báo")
                            .setMessage("Bạn nên nhập số lần nhấn nhỏ hơn hoặc bằng 80!")
                            .setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onClickAlertDialogYes(a, nameFile);
                                }
                            })
                            .setNegativeButton("Quay lại",null)
                            .create().show();
                    return;
                }
                if (TextUtils.isEmpty(ox) || TextUtils.isEmpty(oy) || TextUtils.isEmpty(oz)) {
                    Toast.makeText(ScannerAcitvity.this,"Hãy nhập tọa độ điểm !",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(a) > 1024) {
                    Toast.makeText(ScannerAcitvity.this, "Số lần quét quá lớn !",Toast.LENGTH_SHORT).show();
                    return;
                }

                onClickAlertDialogYes(a, nameFile);
            }
        });

        // sự kiện thoát
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        txtTenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile(nameFile);
            }
        });

    }

    private void InitUI() {
        edtNhapN = findViewById(R.id.edt_nhap_so_lan_quet);
        edtNhapOx = findViewById(R.id.edt_nhap_OX);
        edtNhapOy = findViewById(R.id.edt_nhap_OY);
        edtNhapOz = findViewById(R.id.edt_nhap_OZ);
        txtTenFile = findViewById(R.id.txtTenFile);
        btnQuetWifi = findViewById(R.id.btn_quet);
        btnBack = findViewById(R.id.btn_back);

    }
    private void ChangeTextColor() {
        edtNhapN.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtNhapN.setTextColor(Color.parseColor("#32357a"));
                }else {
                    edtNhapN.setTextColor(Color.parseColor("#cccccc"));
                }
            }
        });

        edtNhapOx.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtNhapOx.setTextColor(Color.parseColor("#32357a"));
                }else {
                    edtNhapOx.setTextColor(Color.parseColor("#cccccc"));
                }
            }
        });

        edtNhapOy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtNhapOy.setTextColor(Color.parseColor("#32357a"));
                }else {
                    edtNhapOy.setTextColor(Color.parseColor("#cccccc"));
                }
            }
        });

        edtNhapOz.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtNhapOz.setTextColor(Color.parseColor("#32357a"));
                }else {
                    edtNhapOz.setTextColor(Color.parseColor("#cccccc"));
                }
            }
        });
    }

    // mở file
    private void openFile(String nameFile) {
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDirectory, nameFile+".csv");

        if (file.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Xử lý nếu không có ứng dụng nào có thể xử lý tệp này
            }
        } else {
            // Xử lý nếu tệp không tồn tại trong thư mục download
            Toast.makeText(this, "File chưa được tạo", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCheckInteger(String n) {
        try {
            Integer.parseInt(n);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkLocationStatus() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    // check status wifi
    private boolean checkWifiStatus() {
        return wifiManager.isWifiEnabled();
    }

    public void setEditText() {
        edtNhapOx.setText("");
        edtNhapOy.setText("");
        edtNhapOz.setText("");
        edtNhapOx.setFocusable(true);
    }

    private void onClickAlertDialogYes(String a, String nameFile) {
        if (isCheckInteger(a)) {
            int n = Integer.parseInt(a);
            float Ox = Float.parseFloat(edtNhapOx.getText().toString().trim());
            float Oy = Float.parseFloat(edtNhapOy.getText().toString().trim());
            float Oz = Float.parseFloat(edtNhapOz.getText().toString().trim());

            Intent intent = new Intent(ScannerAcitvity.this, ShowDataActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name",nameFile);
            bundle.putInt("n",n);
            bundle.putFloat("Ox",Ox);
            bundle.putFloat("Oy",Oy);
            bundle.putFloat("Oz",Oz);

            intent.putExtra("data",bundle);
            setEditText();
            startActivity(intent);
        }else {
            Toast.makeText(ScannerAcitvity.this,"Nhập số nguyên!",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            int permission1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);


            if (permission1 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_WIFI_STATE,
                                android.Manifest.permission.ACCESS_NETWORK_STATE},
                        MY_REQUEST_CODE);
            }
        }

    }
}