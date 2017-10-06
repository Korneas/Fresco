package com.camilomontoya.fresco;

import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.UUID;

public class FanControl extends AppCompatActivity {

    private Button off;
    private SeekBar intense;
    private String address = null;
    private ProgressDialog progress;
    private BluetoothAdapter myBlue = null;
    private BluetoothSocket bSocket = null;
    private boolean isBConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     *
     * Conecta los elementos de el layout con valores que se editan en java
     * para ser enviados mediante los Streams de Bluetooth
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_control);

        Bundle newi = getIntent().getExtras();
        address = newi.getString("EXTRA_ADDRESS");

        off = (Button) findViewById(R.id.disconnect);
        intense = (SeekBar) findViewById(R.id.intense);

        new ConnectBt().execute();

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOff();
            }
        });

        intense.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //Valor para setear el TextView textIntense.setText(String.valueOf(progress));
                    try {
                        enviar(String.valueOf(progress));
                    } catch (IOException e) {
                        aviso("Error de envio");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Clase que hereda de AsyncTask para conocer si realmente con el apareado
     * se tiene una conexion actual, si no existe la aplicacion se devuelve a la anterior
     * pantalla para elegir otro dispositivo
     */
    private class ConnectBt extends AsyncTask<Void, Void, Void> {

        private boolean connected = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(FanControl.this, "Conectando...", "Por favor espere");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (bSocket == null || !isBConnected) {
                    myBlue = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dpst = myBlue.getRemoteDevice(address);
                    bSocket = dpst.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bSocket.connect();
                }
            } catch (IOException e) {
                connected = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!connected) {
                aviso("Conexion fallida");
                finish();
            } else {
                aviso("Conexion completa");
                isBConnected = true;
            }
            progress.dismiss();
        }
    }

    /**
     *
     * Metodo para enviar Strings a Arduino mediante el Bluetooth
     *
     * @param t String que se envia para ser leido por el Arduino
     * @throws IOException
     */
    private void enviar(String t) throws IOException {
        if(bSocket != null) {
            bSocket.getOutputStream().write(t.toString().getBytes());
            bSocket.getOutputStream().flush();
        }
    }

    /**
     * Metodo privado para enviar el mensaje de apagar el sistema de ventilacion
     */
    private void turnOff() {
        try {
            enviar("TF");
        } catch (IOException e) {
            aviso("Error de envio");
        }

        if (bSocket != null) {
            try {
                bSocket.close();
            } catch (IOException e) {
                aviso("Conexion apagada");
            }
            finish();
        }

        if (bSocket == null){
            aviso("El dispositivo esta desconectado");
        }
    }

    /**
     * Metodo para acortar la generacion de Toast
     *
     * @param text String enviado para crear el mensaje
     */
    private void aviso(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
