package com.camilomontoya.fresco;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class MainActivity extends AppCompatActivity {

    private Button btnPaired;
    private ListView deviceList;

    private BluetoothAdapter myBlue = null;
    private Set pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPaired = (Button) findViewById(R.id.button);
        deviceList = (ListView) findViewById(R.id.listView);

        myBlue = BluetoothAdapter.getDefaultAdapter();

        if(myBlue == null){
            aviso("Bluetooth no esta activado");
            finish();
        } else {
            if(myBlue.isEnabled()){
                Intent turnBtn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBtn,1);
            }
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList(){
        pairedDevices = myBlue.getBondedDevices();
        ArrayList list = new ArrayList();

        if(pairedDevices.size()>0){
            for (Object o: pairedDevices) {
                BluetoothDevice bt = (BluetoothDevice) o;
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            aviso("No hay dispositivos disponibles");
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);
            Intent i = new Intent(MainActivity.this,FanControl.class);
            i.putExtra("EXTRA_ADDRESS",address);
            startActivity(i);
        }
    };

    private void aviso(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

}
