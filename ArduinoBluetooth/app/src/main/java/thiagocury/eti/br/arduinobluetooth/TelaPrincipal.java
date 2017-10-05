package thiagocury.eti.br.arduinobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Handler;

public class TelaPrincipal extends AppCompatActivity {

    //Widgets
    private TextView tvDispositivoConectado;
    private EditText etTexto;
    private Button btnTexto;

    private Spinner spVogalMai;
    private Spinner spVogalMin;
    private Button btnVogalMai;
    private Button btnVogalMin;

    private Spinner spReles;
    private CheckBox chReles;
    private TextView tvComandoFinalRele;
    private Button btnEnviarComandoRele;

    private String novaSenha = "";

    protected BluetoothDevice device = null;
    private boolean connectSuccess = false;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    private Handler bluetoothIn;
    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        setTitle(getResources().getString(R.string.app_title));

        //Ref.
        tvDispositivoConectado = (TextView) findViewById(R.id.tv_dispositivo_conectado);

        etTexto = (EditText) findViewById(R.id.et_texto);
        btnTexto = (Button) findViewById(R.id.btn_texto);

        spVogalMai = (Spinner) findViewById(R.id.sp_vogal_mai);
        spVogalMin = (Spinner) findViewById(R.id.sp_vogal_min);
        btnVogalMai = (Button) findViewById(R.id.btn_enviar_vogal_mai);
        btnVogalMin = (Button) findViewById(R.id.btn_enviar_vogal_min);

        spReles = (Spinner) findViewById(R.id.sp_reles);
        chReles = (CheckBox) findViewById(R.id.ch_reles);
        tvComandoFinalRele = (TextView) findViewById(R.id.tv_comando_final_rele);
        btnEnviarComandoRele = (Button) findViewById(R.id.btn_tp_enviar_comando_rele);

        //Bluetooth + senha
        device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        novaSenha = getIntent().getStringExtra("senha").toString();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //ENVIAR TEXTO
        btnTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectSuccess){
                    String texto = etTexto.getText().toString();
                    if(texto.isEmpty()){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_erro_texto_que_sera_enviado), Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            mConnectedThread.enviarString(texto);
                            etTexto.setText(null);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_sucesso_comando_enviado), Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_aviso_nao_esta_conectado), Toast.LENGTH_LONG).show();
                }
            }
        });

        //ENVIAR VOGAIS MAIUSCULAS
        btnVogalMai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectSuccess){
                    String texto = spVogalMai.getSelectedItem().toString();
                    try {
                        mConnectedThread.enviarString(texto);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_sucesso_comando_enviado), Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                    }
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_aviso_nao_esta_conectado), Toast.LENGTH_LONG).show();
                }
            }
        });

        //ENVIAR VOGAIS MINUSCULAS
        btnVogalMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectSuccess){
                    String texto = spVogalMin.getSelectedItem().toString();
                    try {
                        mConnectedThread.enviarString(texto);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_sucesso_comando_enviado), Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                    }
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_aviso_nao_esta_conectado), Toast.LENGTH_LONG).show();
                }
            }
        });

        //ENVIAR COMANDO PARA RELÉS
        spReles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                montarComandoRele();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        chReles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    chReles.setText(getResources().getString(R.string.ch_tp_on_marcado));
                    montarComandoRele();
                }else{
                    chReles.setText(getResources().getString(R.string.ch_tp_off_desmarcado));
                    montarComandoRele();
                }
            }
        });

        btnEnviarComandoRele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mConnectedThread.enviarString(montarComandoRele());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_sucesso_comando_enviado), Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                }
            }
        });

        if(bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_erro_dispositivo_sem_bluetooth), Toast.LENGTH_LONG).show();
        }else {

            try {
                if (btSocket == null || !isBtConnected) {
                    conectar();
                }
            } catch (Exception e) {
                connectSuccess = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        desconectar();
        connectSuccess = false;
        tvDispositivoConectado.setText(getResources().getString(R.string.msg_aviso_nao_esta_conectado));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        conectar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){

            case R.id.menu_sobre:
                Intent it = new Intent(TelaPrincipal.this, TelaSobre.class);
                startActivity(it);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public String montarComandoRele(){
        String rele = spReles.getSelectedItem().toString();
        String comando = rele +""+ chReles.getText().toString();
        tvComandoFinalRele.setText(comando);
        return comando;
    }

    public void conectar(){

        BluetoothDevice dev = bluetoothAdapter.getRemoteDevice(device.getAddress());

        try{

            //Log.d("TAG","vai setar nova senha: "+novaSenha);
            Method m = BluetoothDevice.class.getMethod("setPin", byte[].class);
            byte[] pin = (byte[]) BluetoothDevice.class.getMethod("convertPinToBytes", String.class).invoke(BluetoothDevice.class, novaSenha);
            boolean isSetPin = (Boolean) m.invoke(dev, pin);
            //Log.d("TAG","setou nova senha: "+novaSenha);

            /* Method m = dev.getClass().getMethod("setPin", byte[].class);
              byte[] bytes = new byte[]{0,0,0,0};
              m.invoke(dev, bytes); */

            btSocket = dev.createInsecureRfcommSocketToServiceRecord(MEU_UUID);
            btSocket.connect();

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_sucesso_conexao_bem_sucedida), Toast.LENGTH_LONG).show();

            tvDispositivoConectado.setText("Você está conectado em: "+device.getName()+"\nMAC Address: "+device.getAddress());

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();
            connectSuccess = true;

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_erro_fazer_conexao), Toast.LENGTH_LONG).show();
            tvDispositivoConectado.setText(getResources().getString(R.string.msg_aviso_nao_esta_conectado));
        }
    }

    public void desconectar(){
        try{
            if(btSocket != null) {
                btSocket.close();
                btSocket = null;
            }
        }catch(IOException e){
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void enviarInt(int input) {
            try {
                mmOutStream.write(input);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_erro_enviar_dados), Toast.LENGTH_LONG).show();
                finish();
            }
        }

        public void enviarString(String s) throws IOException {
            try {
                mmOutStream.write(s.getBytes());
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_erro_enviar_dados), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}