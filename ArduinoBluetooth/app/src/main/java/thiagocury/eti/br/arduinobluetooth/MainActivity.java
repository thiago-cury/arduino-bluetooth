package thiagocury.eti.br.arduinobluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //Widgets
    private RadioGroup rgSenha;
    private RadioButton rb1234;
    private RadioButton rb0000;
    private RadioButton rbSemSenha;
    private RadioButton rbOutraSenha;
    private EditText etOutraSenha;

    private String novaSenha = "1234";

    private BluetoothDevice bluetooth;
    private BluetoothClass bluetoothClass;
    private BluetoothAdapter bluetoothAdapter = null;

    private LocationManager locationManager;

    private ProgressBar progressBar;

    private ListView lvBluetoothPareados;
    private ListView lvBluetoothEncontrados;

    private ArrayList<String> bluetoothPareados;
    private ArrayList<String> bluetoothEncontrados;

    private ArrayList<BluetoothDevice> bluetoothDispositivos;

    private ArrayAdapter<String> adapterPareados;
    private ArrayAdapter<String> adapterEncontrados;

    //Permissão
    private static final int REQUEST_CODE_LOCATION = 0;
    private static final int ACTION_LOCATION = 200;

    //Permissões
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(getResources().getString(R.string.app_title));

        //Refs.
        rgSenha = (RadioGroup) findViewById(R.id.rg_ma_senha);
        rb1234 = (RadioButton) findViewById(R.id.rb_ma_1234);
        rb0000 = (RadioButton) findViewById(R.id.rb_ma_0000);
        rbSemSenha = (RadioButton) findViewById(R.id.rb_ma_sem_senha);
        rbOutraSenha = (RadioButton) findViewById(R.id.rb_ma_outra_senha);
        etOutraSenha = (EditText) findViewById(R.id.et_ma_outra_senha);

        //Senha
        rgSenha.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                etOutraSenha.setVisibility(View.INVISIBLE);

                if(radioGroup.getCheckedRadioButtonId() == rb1234.getId()) {
                    novaSenha = rb1234.getText().toString();
                }else if(radioGroup.getCheckedRadioButtonId() == rb0000.getId()){
                    novaSenha = rb0000.getText().toString();
                }else if(radioGroup.getCheckedRadioButtonId() == rbSemSenha.getId()){
                    novaSenha = "";
                }else if(radioGroup.getCheckedRadioButtonId() == rbOutraSenha.getId()){
                    novaSenha = rbOutraSenha.getText().toString();
                    etOutraSenha.setVisibility(View.VISIBLE);
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!bluetoothAdapter.isEnabled()){
            Intent solicita = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(solicita, 1);
        }

        registerReceiver(receberInfo, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receberInfo, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        lvBluetoothEncontrados = (ListView) findViewById(R.id.lv_bluetooth_encontrados);
        lvBluetoothPareados = (ListView) findViewById(R.id.lv_bluetooth_pareados);

        progressBar.setVisibility(View.INVISIBLE);

        bluetoothDispositivos = new ArrayList<BluetoothDevice>();
        bluetoothPareados = new ArrayList<String>();
        bluetoothEncontrados = new ArrayList<String>();

        adapterEncontrados = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, bluetoothEncontrados);
        adapterPareados = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, bluetoothPareados);

        lvBluetoothEncontrados.setAdapter(adapterEncontrados);
        lvBluetoothPareados.setAdapter(adapterPareados);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions,1);
        }

        /* BLUETOOTH */ /* BLUETOOTH */ /* BLUETOOTH */
        /* BLUETOOTH */ /* BLUETOOTH */ /* BLUETOOTH */
        if (!bluetoothAdapter.isEnabled()) {
            ligarBluetooth();
        }

        /* GPS */ /* GPS */ /* GPS */
        /* GPS */ /* GPS */ /* GPS */
        if(bluetoothAdapter.isEnabled()) {
            if ((ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)&&
                   ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    //informações adicionais caso ele tenha negado
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_erro_gps), Toast.LENGTH_LONG).show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions,1);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions,1);
                    }
                }//fecha else if shouldShowRequestPermissionRationale
            }
        }

        locationManager = (LocationManager) getSystemService(getBaseContext().LOCATION_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()) {
                    if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        if(verificarGPSAtivo()) {
                            iniciarBusca();
                        }
                    } else {
                      Toast.makeText(getBaseContext(),getResources().getString(R.string.msg_erro_aceitar_permissoes_gps), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(),getResources().getString(R.string.msg_erro_bluetooth_desativado), Toast.LENGTH_LONG).show();
                }
            }
        });

        lvBluetoothEncontrados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                BluetoothDevice device = bluetoothDispositivos.get(i);

                if(bluetoothEncontrados.get(i).contains(device.getAddress())){

                    if(rbOutraSenha.getId() == rgSenha.getCheckedRadioButtonId()){
                        novaSenha = etOutraSenha.getText().toString();
                    }
                    Intent it = new Intent(MainActivity.this, TelaPrincipal.class);
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                    it.putExtra("senha",novaSenha);
                    startActivity(it);
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_erro_bluetooth_encontrado_nao_coincidem), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvBluetoothPareados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                BluetoothDevice device = bluetoothDispositivos.get(i);

                if(bluetoothPareados.get(i).contains(device.getAddress())){

                    if(rbOutraSenha.getId() == rgSenha.getCheckedRadioButtonId()){
                        novaSenha = etOutraSenha.getText().toString();
                    }
                    Intent it = new Intent(MainActivity.this, TelaPrincipal.class);
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                    it.putExtra("senha",novaSenha);
                    startActivity(it);
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_erro_bluetooth_pareado_nao_coincidem), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        buscarDispositivosPareados();
        ligarBluetooth();
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
            /*case R.id.menu_ligar:
                ligarBluetooth();
                break;

            case R.id.menu_desligar:
                desligarBluetooth();
                break;*/

            case R.id.menu_sobre:
                Intent it = new Intent(MainActivity.this, TelaSobre.class);
                startActivity(it);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void iniciarBusca() {
        Toast.makeText(getBaseContext(),getResources().getString(R.string.msg_iniciando_busca_dispositivos),Toast.LENGTH_SHORT).show();
        bluetoothDispositivos.clear();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.this.registerReceiver(receberInfo, intentFilter);
        bluetoothAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void ligarBluetooth() {
        if(!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void desligarBluetooth() {
        if(bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    private boolean verificarGPSAtivo() {
        if ((ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            boolean estaAtivoGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!estaAtivoGPS) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, ACTION_LOCATION);
                return false;
            }
            return true;
        }
        return false;
    }

    private void ativarVisibilidadeDispositivo() {
        /* Ativa o dispositivo para que outros dispositivos possam econtrá-lo por 300 segundos. */
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private BroadcastReceiver receberInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Message msg = Message.obtain();
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Log.d("TAG","entrou no action found");
                if(bluetoothDispositivos.size() < 1) {
                    //Log.d("TAG","entrou no if size < 1");
                    bluetoothEncontrados.add(device.getName()+"\n"+device.getAddress());
                    bluetoothDispositivos.add(device);
                    adapterEncontrados.notifyDataSetChanged();
                } else {
                    //Log.d("TAG","entrou no else flag");
                    boolean flag = true;
                    for(int i = 0; i<bluetoothDispositivos.size();i++) {
                        if(device.getAddress().equals(bluetoothDispositivos.get(i).getAddress())) {
                            flag = false;
                        }
                    }
                    if(flag == true) {
                        //Log.d("TAG","entrou no if flag == true");
                        bluetoothEncontrados.add(device.getName()+"\n"+device.getAddress());
                        bluetoothDispositivos.add(device);
                        adapterEncontrados.notifyDataSetChanged();
                    }
                }
            }
            progressBar.setVisibility(View.INVISIBLE);

            /*             if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    lista.add(device);
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.finded)+" "+device.getName()+" : "+device.getAddress(), Toast.LENGTH_SHORT).show();
                    count++;
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                count = 0;
                Toast.makeText(getBaseContext(), getResources().getString(R.string.search_started), Toast.LENGTH_SHORT).show();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(getBaseContext(), getResources().getString(R.string.search_ended)+" "+count+" "+getResources().getString(R.string.devices_founded), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                updateLista();
            } */
        }
    };

    private void buscarDispositivosPareados() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size() > 0) {
            for(BluetoothDevice device : pairedDevice) {
                bluetoothPareados.add(device.getName()+"\n"+device.getAddress());
                bluetoothDispositivos.add(device);
            }
        }
        adapterPareados.notifyDataSetChanged();
    }
}