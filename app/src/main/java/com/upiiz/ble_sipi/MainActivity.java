package com.upiiz.ble_sipi;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {
    private LineChart lineChart;
    private float x = 1;
    private List<String> xValues;
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private static final String DEVICE_NAME = "Daniel";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private TextView tvData;
    private Button btnConnect;

    private final int REQUEST_ENABLE_BT = 1;

    private float sumaOrderV = 0;
    private float sumaMAV = 0;
    private float sumaWL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lineChart = findViewById(R.id.chart);
        Description description = new Description();
        description.setText("Valores");
        description.setPosition(150f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        xValues = Arrays.asList("Voltajes");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(1);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(5f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        yAxis.setDrawLabels(true);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(true);

        List<Entry> entries = new ArrayList<>();

        LineDataSet dataSet = new LineDataSet(entries,"Maths");
        dataSet.setColor(Color.BLUE);
        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.invalidate();


        tvData = findViewById(R.id.tvData);
        btnConnect = findViewById(R.id.btnConnect);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        btnConnect.setOnClickListener(v -> startScan());
    }
    private void startScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_ENABLE_BT);
            return;
        }

        Toast.makeText(this, "Buscando dispositivo BLE...", Toast.LENGTH_SHORT).show();

        bleScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (device.getName() != null && device.getName().equals(DEVICE_NAME)) {
                    Toast.makeText(MainActivity.this, "Encontrado: " + device.getName(), Toast.LENGTH_SHORT).show();
                    bleScanner.stopScan(this);
                    connectToDevice(device);
                }
            }
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                    runOnUiThread(() -> tvData.setText("Conectado a " + DEVICE_NAME));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    gatt.discoverServices();
                } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(() -> tvData.setText("Desconectado"));
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                    if (characteristic != null) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (characteristic.getUuid().equals(UUID.fromString(CHARACTERISTIC_UUID))) {
                    String value = characteristic.getStringValue(0);
                    runOnUiThread(() -> tvData.setText("Dato recibido: " + value));
                    String numberOnly = value.replaceAll("[^0-9\\.]", "");
                    float yValue = 0f;

                    try {
                        yValue = Float.parseFloat(numberOnly);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    lineChart.getData().addEntry(new Entry(x, yValue), 0);

                    LineDataSet dataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);

                    if (dataSet.getEntryCount() > 100) {
                        dataSet.removeFirst();
                        for (Entry e : dataSet.getEntriesForXValue(0)) {
                            dataSet.removeEntry(e);
                        }
                    }

                    lineChart.getData().notifyDataChanged();
                    lineChart.notifyDataSetChanged();

                    lineChart.invalidate();

                    x += 1;
                    // Leer periódicamente
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    gatt.readCharacteristic(characteristic);
                }
            }
        },BluetoothDevice.TRANSPORT_LE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_BT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            Toast.makeText(this, "Permisos BLE denegados", Toast.LENGTH_SHORT).show();
        }
    }

    public float MAV(List<Float> muestras){
        float suma = 0;
        for(int i=0;i<muestras.size();i++){
            suma += muestras.get(i);
        }
        return suma / muestras.size();
    }
    public float MAVRT(float nueva){
        sumaMAV += nueva;
        return sumaMAV / x;
    }
    public float orderV(List<Float> muestras){
        float suma = 0;
        for(int i=0;i<muestras.size();i++){
            suma += (float) Math.pow(muestras.get(i),2);
        }
        return (float) Math.sqrt(suma / muestras.size());
    }
    public float orderVRT(float nueva){
        sumaOrderV += (float) Math.pow(nueva,2);
        return (float) Math.sqrt(sumaOrderV / x);
    }
    public float WL(List<Float> muestras){
        float suma = 0;
        for(int i=0;i<muestras.size()-1;i++){
            suma += Math.abs(muestras.get(i+1) - muestras.get(i));
        }
        return suma;
    }
    public float WLRT(float anterior, float nueva){
        sumaWL += Math.abs(nueva - anterior);
        return sumaWL;
    }
}