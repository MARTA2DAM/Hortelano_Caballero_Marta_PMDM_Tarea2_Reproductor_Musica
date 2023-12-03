package com.example.mireproductor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    private MediaPlayer mp;
    private Boolean iniciado=false;
    private ImageButton btn_play, btn_explorador, btn_repetir, btn_detener, btn_lista, btn_siguiente_pista, btn_anterior_pista, btn_avanzar, btn_retroceder;
    private TextView nombre_cancion;
    private SeekBar barra_reproduccion, barra_volumen;
    private int permisos;
    private int posicion_actual=0;
    private final int requestCode = 19;
    private AudioManager audioManager;
    private ArrayList<Uri> lista_reproduccion = new ArrayList<>();
    private ArrayList<String> lista_reproduccion_nombres = new ArrayList<>();
    private ActivityResultLauncher<Intent> lista_launcher;
    private ActivityResultLauncher<String> explorador_laucher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //solicitamos permisos
        solicitarpermisos();
        //instanciamos un MediaPlayer
        mp = new MediaPlayer();
        //vinculamos los objetos a la interfaz y configuramos sus repectivos listeners
        btn_explorador = findViewById(R.id.btn_explorador);
        btn_explorador.setOnClickListener(this::abril_laucher_explorador);
        btn_play = findViewById(R.id.btn_play_plause);
        btn_play.setOnClickListener(view -> {
            if(mp!=null){
                if(mp.isPlaying()) {
                    pause();
                }
                else {
                    play();
                }
            }else Toast.makeText(this, "No hay pistas para reproducir", Toast.LENGTH_SHORT).show();
        });

        btn_repetir = findViewById(R.id.btn_repetir);
        btn_repetir.setOnClickListener(view -> {
            repetir();
        });
        btn_detener = findViewById(R.id.btn_detener);
        btn_detener.setOnClickListener(view -> {
            detener();
        });
        btn_siguiente_pista = findViewById(R.id.btn_siguiente);
        btn_siguiente_pista.setOnClickListener(view -> {
            siguiente_pista();
        });
        btn_anterior_pista = findViewById(R.id.btn_anterior);
        btn_anterior_pista.setOnClickListener(view -> {
            anterior_pista();
        });
        btn_avanzar = findViewById(R.id.btn_avanzar);
        btn_avanzar.setOnClickListener(view -> {
            avanzar_tiempo();
        });
        btn_retroceder = findViewById(R.id.btn_retroceder);
        btn_retroceder.setOnClickListener(view -> {
            retroceder_tiempo();
        });
        btn_lista = findViewById(R.id.btn_lista_reproducción);
        nombre_cancion= findViewById(R.id.nombre_pista);
        barra_reproduccion = findViewById(R.id.barra_tiempo);
        barra_reproduccion.setOnSeekBarChangeListener(this);
        barra_reproduccion.setEnabled(false);
        //Configuramos la barra del volumen
        barra_volumen = findViewById(R.id.barra_volumen);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maximo_volumen = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volumen_actual = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        barra_volumen.setMax(maximo_volumen);
        barra_volumen.setProgress(volumen_actual);
        barra_volumen.setOnSeekBarChangeListener(this);
        actualizar_barra_sonido();
        getPackageResourcePath();
        configurar_launcher_explorador();
        configurar_laucher_lista();
        btn_lista.setOnClickListener(this::abrir_launcher_lista);

        mp.setOnCompletionListener(mediaPlayer -> siguiente_pista());
        //añadimos las canciones preguardadas
        Uri cancion_preguardada_1 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.buttercup);
        Uri cancion_preguardada_2 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.jungle);
        lista_reproduccion.add(cancion_preguardada_1);
        lista_reproduccion_nombres.add("Buttercup song");
        lista_reproduccion.add(cancion_preguardada_2);
        lista_reproduccion_nombres.add("Jungle - Tash sultana");
    }
    private void play(){
        if(!barra_reproduccion.isEnabled()){
            barra_reproduccion.setEnabled(true);
        }
        if (!lista_reproduccion.isEmpty()) {
            try {
                if(!iniciado){
                    mp.setDataSource(this, lista_reproduccion.get(posicion_actual));
                    mp.prepare();
                }
                mp.start();
                iniciado=true;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            btn_play.setBackgroundResource(R.drawable.pausa);
        } else{
            Toast.makeText(this, "Lista de reproducción vacia. No hay pistas para reproducir", Toast.LENGTH_SHORT).show();
            return;
        }
        nombre_cancion.setText(lista_reproduccion_nombres.get(posicion_actual));
        barra_reproduccion.setMax(mp.getDuration());
        actualizar_barra();

    }

    private void anterior_pista(){

        if (posicion_actual!=0) {
            posicion_actual --;
        } else {
            posicion_actual = lista_reproduccion.size()-1;
        }
        mp.reset();
        iniciado=false;
        play();
    }

    private void siguiente_pista(){
        if (lista_reproduccion.size() > (posicion_actual+1)) {
            posicion_actual ++;
        } else {
            posicion_actual=0;
        }
        mp.reset();
        iniciado=false;
        play();
    }
    private void avanzar_tiempo(){
        if(mp!=null){
            int avanzar10s=mp.getCurrentPosition()+10000;
            if(avanzar10s<mp.getDuration()){
                mp.seekTo(avanzar10s);
            }else{
                mp.seekTo(mp.getDuration()-1000);
            }

        }
    }
    private void retroceder_tiempo(){
        if(mp!=null){
            int retroceder10s=mp.getCurrentPosition()-10000;
            if(retroceder10s>0){
                mp.seekTo(retroceder10s);
            }else{
                mp.seekTo(0);
            }

        }
    }
    private void detener() {
        mp.pause();
        mp.seekTo(0);
        barra_reproduccion.setProgress(0);
        btn_play.setBackgroundResource(R.drawable.reproducir);

    }


    private void pause(){
        if(mp!=null){
            mp.pause();
            btn_play.setBackgroundResource(R.drawable.reproducir);

        }
    }
    private void repetir(){
        if(mp!=null){
            if(!mp.isLooping()){
                mp.setLooping(true);
                btn_repetir.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple)));
            }else{
                mp.setLooping(false);
                btn_repetir.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

    }
    private void actualizar_barra() {
        if (mp.isPlaying() && barra_reproduccion.isEnabled()) {
            barra_reproduccion.setProgress(mp.getCurrentPosition());
        }
        barra_reproduccion.postDelayed(() -> actualizar_barra(), 1000);
    }
    private void actualizar_barra_sonido() {
        barra_volumen.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        barra_reproduccion.postDelayed(() -> actualizar_barra_sonido(), 300);
    }
    public String get_nombre_pista(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        String nombre_archivo = documentFile.getName();
        return nombre_archivo;
    }

    private void abril_laucher_explorador(View v){
        explorador_laucher.launch("audio/*");
    }

    private void abrir_launcher_lista(View v){
        if(!lista_reproduccion.isEmpty()) {
            Intent pantalla2 = new Intent(this, ListaReproduccion.class);
            pantalla2.putStringArrayListExtra("lista", lista_reproduccion_nombres);
            lista_launcher.launch(pantalla2);

        }else{
            Toast.makeText(this, "Lista de reproduccion vacia", Toast.LENGTH_SHORT).show();
        }

    }

    private void configurar_laucher_lista(){

        lista_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        posicion_actual = result.getData().getIntExtra("posicion", 0);
                        mp.reset();
                        iniciado=false;
                        play();
                    }
                    if(result.getResultCode() == Activity.RESULT_FIRST_USER){
                        Toast.makeText(this, "el result cod = "+result.getResultCode(), Toast.LENGTH_SHORT).show();
                        int eliminar_pista=result.getData().getIntExtra("posicion", 0);
                        lista_reproduccion.remove(eliminar_pista);
                        lista_reproduccion_nombres.remove(eliminar_pista);
                    }
                });
    }
    private void configurar_launcher_explorador(){

        explorador_laucher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {

                        if(!lista_reproduccion.contains(result)) {
                            lista_reproduccion.add(result);
                            lista_reproduccion_nombres.add(get_nombre_pista(result));
                            Toast.makeText(this, get_nombre_pista(result) + " ha sido añadido a la lista de reproducción", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Esta pista ya ha sido añadida a la lista de reproducción", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void solicitarpermisos() {
        //chequeamos permisos de lectura de la memoria externa
        permisos = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        //comprabamos si tenemos o no permisos
        if(permisos == getPackageManager().PERMISSION_GRANTED){

        }else{
            //si no tenemos permisos los solicitamos
            Toast.makeText(this, "Solicitamos permisos", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b&&barra_reproduccion.equals(seekBar)) {
            mp.seekTo(i);
        }
        if (barra_volumen.equals(seekBar)){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
