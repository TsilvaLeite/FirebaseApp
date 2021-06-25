package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adpter.ImageAdapter;
import com.example.myapplication.models.Upload;
import com.example.myapplication.util.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button btnLogout, btnStorage;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("uploads");

    private ArrayList<Upload> listaUploads = new ArrayList<>();

    private RecyclerView recyclerView;
    private  ImageAdapter imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.main_recycler);
        imageAdapter = new ImageAdapter(getApplicationContext(),listaUploads);
        imageAdapter.setListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onDeletClick(int position) {
                Upload upload = listaUploads.get(position);
                deleteUpload(upload);
            }

            @Override
            public void onUpdateClick(int position) {
                Upload upload = listaUploads.get(position);
                Intent intent = new Intent(getApplicationContext(),UpdateActivity.class);
                //Envia o uploada para a outra Activity
                intent.putExtra("uploads",upload);
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getApplicationContext())
        );
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imageAdapter);
        btnStorage = findViewById(R.id.main_btn_storage);
        btnStorage.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),StorageActivity.class);
            startActivity(intent);
        });



        btnLogout = findViewById(R.id.main_btn_logout);

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            finish();
        });

        TextView textEmail = findViewById(R.id.main_text_email);
        textEmail.setText(auth.getCurrentUser().getEmail());

        TextView textNome = findViewById(R.id.main_text_user);
        textNome.setText(auth.getCurrentUser().getDisplayName());

    }

    @Override
    protected void onStart() {
        // onStart -> faz parte do ciclo de vida da activity, depois do onCreate()
        // ***     -> É executado quando o app inicia
        // ***     -> E quando volta do background
        super.onStart();
        getData();
    }

    public void deleteUpload(Upload upload){
        LoadingDialog dialog = new LoadingDialog(this,R.layout.custom_dialog);
        dialog.startLoadingDialog();

        //deletar img no storage
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl(upload.getUrl());

        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
            database.child(upload.getId()).removeValue().addOnSuccessListener(aVoid1 -> {
                Toast.makeText(getApplicationContext(),"Item deletado!",Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void getData(){
        //Listenner para um nó uploads
        // * caso ocorra alguma alteração -> retorna TODOS os dados !!
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for( DataSnapshot no_filho : snapshot.getChildren()){
                    Upload upload = no_filho.getValue(Upload.class);
                    listaUploads.add(upload);
                    Log.i("DATABASE","id: " + upload.getId() + ", nome: " + upload.getNomeImage());

                }

                imageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
