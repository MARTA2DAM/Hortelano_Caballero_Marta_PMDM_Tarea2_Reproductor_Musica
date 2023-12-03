package com.example.mireproductor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class ListaReproduccion extends AppCompatActivity {
    private ListView listview_pistas;
    int posicion=0;
    ImageButton btn_borrar;
    boolean pulsa_lista= false, borrar=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reproduccion);

        listview_pistas = findViewById(R.id.listview_pistas);
        Intent i = getIntent();

        ArrayList<String> lista_nombres = i.getStringArrayListExtra("lista");
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista_nombres);
        listview_pistas.setAdapter(adaptador);

        btn_borrar=findViewById(R.id.btn_borrar);
        btn_borrar.setOnClickListener(view -> {
            if(borrar){
                borrar=false;
                listview_pistas.setBackground(null);
            }else{
                borrar=true;
                listview_pistas.setBackground(new ColorDrawable(Color.GRAY));
                Toast.makeText(this, "Seleccione la pista que desea eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        listview_pistas.setOnItemClickListener((adapterView, view, i1, l) -> {
            pulsa_lista=true;
            posicion = i1;
            onBackPressed();
        });
    }
    @Override
    public void onBackPressed() {
        if(pulsa_lista&&!borrar) {
            Intent intent = new Intent();
            Toast.makeText(this, "Reproduciendo pista", Toast.LENGTH_SHORT).show();
            intent.putExtra("posicion", posicion);
            setResult(Activity.RESULT_OK, intent);
        }
        if(pulsa_lista&&borrar){
            Intent intent = new Intent();
            intent.putExtra("posicion", posicion);
            Toast.makeText(this, "pista eliminida", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_FIRST_USER, intent);
        }
        super.onBackPressed();
    }
}
