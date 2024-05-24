package com.example.androiddevwifiscan;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddevwifiscan.Object.OutData;
import com.example.androiddevwifiscan.Object.WifiAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowDataActivity extends AppCompatActivity implements SensorEventListener {

    private static final String LOG_TAG = "Vtwo";
    private static final int MY_REQUEST_CODE = 123;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    //private static final String FILE_HEADER = "ScanNumber,TimeStamp,SSID,BSSID,Level,gyroX,gyroY,gyroZ,accX,accY,accZ,magneticX,magneticY,magneticZ,Ox,Oy,Oz";
    private static final String FILE_HEADER = "ScanNumber,TimeStamp,SSID,BSSID,Level,Ox,Oy,Oz";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String COMMA_DELIMITER = ",";

    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor magnetometerSensor;
    private Sensor pressureSensor;
    private Sensor gyroSensor;
    private Sensor accSensor;

    private Handler wifiScanHandler = new Handler();
    private long wifiScanInterval = 1500;
    private long remainingScans = 0;
    private List<ScanResult> listwifi;

    private float[] magneticValues = new float[3];
    private float[] gyroValues = new float[3];
    private float[] accValues = new float[3];
    private FileWriter fileWriter = null;
    private File csvFile = null;
    private File dir = null;
    private float ox;
    private float oy;
    private float oz;
    private boolean isStart = false;

    private RecyclerView rcvListWifi;
    private AppCompatButton btnContinue, btnThoat;
    private List<OutData> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_data);
        InitUI();
        myList = new ArrayList<>();
        //Khai báo và khởi tạo các thành phần
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // BroadcastReceiver
        wifiReceiver = new WifiBroadcastReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        // cảm biến
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // xử lý sự kiện ghi dữ liệu vào file
        Bundle bundle = getIntent().getBundleExtra("data");
        String nameFile = bundle.getString("name") + ".csv";
        createCSVFile(nameFile);
        readAndDisplayCSVFile();

        // hàm check quyền truy cập wifi
        checkPermissions();

        checkScanWifi();

        // khởi tạo các thành phần hiển thị lên RecylerView

        // Hàm tiếp tục quét
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkScanWifi();
            }
        });
        btnThoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    // Hàm check
    private void checkScanWifi() {
        wifiScanHandler.removeCallbacksAndMessages(null);
        isStart = true;
        startScanning();
    }

    private void InitUI() {
        btnContinue = findViewById(R.id.btn_continue);
        btnThoat = findViewById(R.id.btn_thoat);
        rcvListWifi = findViewById(R.id.rcv_list_wifi);
    }
    // 1.1 Hàm check các điều kiện trước khi thực hiện quá trình quét
    private void startScanning() {
        Bundle bundle = getIntent().getBundleExtra("data");
        int n = bundle.getInt("n");// số lần quét
        ox = bundle.getFloat("Ox");
        oy = bundle.getFloat("Oy");
        oz = bundle.getFloat("Oz");

        if (n>0) {
            remainingScans = n;
            wifiScanHandler.post(scanRunnable);

        }else {
            Toast.makeText(ShowDataActivity.this,"Số lần quét không phù hợp!",Toast.LENGTH_SHORT).show();
        }

    }
    // 1.2 Lên lịch và thực thi việc quét wifi trên luồng riêng biệt
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (remainingScans > 0) {
                wifiManager.startScan();
                wifiScanHandler.postDelayed(this, wifiScanInterval);
            } else {
                Toast.makeText(ShowDataActivity.this, "Thu thập dữ liệu hoàn tất", Toast.LENGTH_SHORT).show();
                isStart = false;// biến cờ cho biết quá trình quét đã kết thúc.
                stopScanning();
            }
        }
    };

    // 1.3 Hàm dừng quá trình quét
    private void stopScanning() {
        remainingScans = 0;
        wifiScanHandler.removeCallbacksAndMessages(null);
        Toast.makeText(this, "Kết thúc thu thập dữ liệu", Toast.LENGTH_SHORT).show();
    }

    // Hàm BoardcastReceiver
    class WifiBroadcastReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            //nếu khi nhấn nút start khởi động thì quá trình quét bắt đầu nếu có giá trị sẽ trả về true
            //ngược lại sẽ trả về false
            remainingScans--;// đây là biến biểu thị số lần quét sau khi đã quét thành công

            if (ok) {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                listwifi = wifiManager.getScanResults();
                saveToCSV(scanResults);// vừa quét vừa lưu vào file csv
                showRecylerView(scanResults);// vừa quét vừa hiển thị trên giao diện
                Log.d(LOG_TAG, "Thu thập dữ liệu hoàn tất");
            } else {
                Toast.makeText(ShowDataActivity.this,"Thu thập dữ liệu xảy ra lỗi!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues[0] = event.values[0];
                magneticValues[1] = event.values[1];
                magneticValues[2] = event.values[2];
                Log.d(LOG_TAG, "MagneticX: " + magneticValues[0] + ", MagneticY: " + magneticValues[1] + ", MagneticZ: " + magneticValues[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroValues[0] = event.values[0];
                gyroValues[1] = event.values[1];
                gyroValues[2] = event.values[2];
                Log.d(LOG_TAG, "GyroX: " + gyroValues[0] + ", GyroY: " + gyroValues[1] + ", GyroZ: " + gyroValues[2]);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accValues[0] = event.values[0];
                accValues[1] = event.values[1];
                accValues[2] = event.values[2];
                Log.d(LOG_TAG, "AccX: " + accValues[0] + ", AccY: " + accValues[1] + ", AccZ: " + accValues[2]);
                break;
        }
    }
    // 1.4 Hàm tạo file hoặc lấy file có sẵn
    private void createCSVFile(String ten_file) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (dir != null) {
                if (!dir.exists()) {
                    boolean dirCreated = dir.mkdirs();
                    if (!dirCreated) {
                        Log.d(LOG_TAG, "Không thể tạo thư mục");
                        return;
                    }
                }
                csvFile = new File(dir, ten_file);
                try {
                    boolean isNewFile = !csvFile.exists();
                    fileWriter = new FileWriter(csvFile, true);
                    if (isNewFile) {
                        fileWriter.append(FILE_HEADER);
                        fileWriter.append(NEW_LINE_SEPARATOR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi tạo hoặc ghi tệp", Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d(LOG_TAG, "Không thể có được thư mục lưu trữ");
                Toast.makeText(this, "Bộ nhớ ngoại vi không có sẵn để ghi.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bộ nhớ ngoại vi không có sẵn để ghi.", Toast.LENGTH_SHORT).show();
        }
    }

    // 1.5.0 Hàm Ghi dữ liệu vào file CSV
    private void saveToCSV(List<ScanResult> scanResults) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        if (isStart) {
            try {
                if (listwifi != null) {
                    fileWriter = new FileWriter(csvFile, true);
                    for (ScanResult result : scanResults) {
                        String[] data = {
                                String.valueOf(remainingScans + 1),
                                currentTime,
                                result.SSID,
                                result.BSSID,
                                String.valueOf(result.level),
                                ox+"",
                                oy+"",
                                oz+""
                        };
                        fileWriter.append(TextUtils.join(COMMA_DELIMITER, data));
                        fileWriter.append(NEW_LINE_SEPARATOR);
                    }
                    fileWriter.flush();
                    fileWriter.close();
                    Toast.makeText(this, "Tệp CSV đã được ghi thành công!", Toast.LENGTH_SHORT).show();
                    readAndDisplayCSVFile();
                } else {
                    Toast.makeText(this, "Không có dữ liệu Wifi!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi ghi dữ liệu WiFi vào tệp", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 1.5.1 Hàm đọc dữ liệu từ file
    private void readAndDisplayCSVFile() {
        try {
            if (csvFile.exists()) {
                FileReader fileReader = new FileReader(csvFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                bufferedReader.close();
                fileReader.close();
            } else {
                Toast.makeText(this, "Tệp không tồn tại", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi đọc tệp", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // hàm hiển thị kết quả người dùng
    private List<OutData> displayWifiResults(List<ScanResult> scanResults) {
        String ssid = "";
        String bssid = "";
        String  level = "";
        float gx = 0.0f;
        float gy = 0.0f;
        float gz = 0.0f;
        float ax = 0.0f;
        float ay = 0.0f;
        float az = 0.0f;
        float mx = 0.0f;
        float my = 0.0f;
        float mz = 0.0f;
        for (ScanResult result : scanResults) {
            ssid = result.SSID;
            bssid = result.BSSID;
            level = String.valueOf(result.level);
            // con quay hồi chuyển
            gx = gyroValues[0];
            gy = gyroValues[1];
            gz = gyroValues[2];
            // con quay gia tốc
            ax = accValues[0];
            ay = accValues[1];
            az = accValues[2];
            // ------
            mx = magneticValues[0];
            my = magneticValues[0];
            mz = magneticValues[0];

        }
        OutData outData = new OutData(ssid,bssid,level,gx,gy,gz,ax,ay,az,mx,my,mz);
        myList.add(outData);

        return myList;
    }

    // 1.5.2 Hàm check quyền wifi
    private void checkPermissions() {
        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        if (magnetometerSensor != null) {
            sensorManager.registerListener((SensorEventListener) this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(LOG_TAG, "Thiết bị hỗ trợ cảm biến từ trường");
        } else {
            Log.d(LOG_TAG, "Thiết bị không hỗ trợ cảm biến từ trường");
        }

        if (gyroSensor != null) {
            sensorManager.registerListener((SensorEventListener) this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(LOG_TAG, "Thiết bị hỗ trợ cảm biến con quay hồi chuyển");
        } else {
            Log.d(LOG_TAG, "Thiết bị không hỗ trợ cảm biến con quay hồi chuyển");
        }

        if (accSensor != null) {
            sensorManager.registerListener((SensorEventListener) this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(LOG_TAG, "Thiết bị hỗ trợ cảm biến gia tốc");
        } else {
            Log.d(LOG_TAG, "Thiết bị không hỗ trợ cảm biến gia tốc");
        }
    }

    // Hàm hiển thị lên RecylerView
    private void showRecylerView(List<ScanResult> scanResults) {
        myList = displayWifiResults( scanResults);
        WifiAdapter adpater = new WifiAdapter();
        adpater.setData(myList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvListWifi.setLayoutManager(linearLayoutManager);
        rcvListWifi.setAdapter(adpater);
    }

    // Đây là các method nằm trong vòng đời Activity
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume"); // đã quay lại
        sensorManager.registerListener((SensorEventListener) this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause(); // tạm dừng
        Log.d(LOG_TAG, "onPause");
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop"); // có thể đã tạm dừng hoặc rơi vào đa nhiệm
    }

    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener((SensorEventListener) this);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}