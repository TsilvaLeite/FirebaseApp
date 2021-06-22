package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.models.Upload;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class StorageActivity extends AppCompatActivity {
    //Regerencia um FirebaseStorage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button btnUpload, btnGaleria;
    private ImageView imageView;
    private Uri imageUri=null;
    private EditText editiNome;

    //referencia para um nó Realtime
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("uploads");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        editiNome = findViewById(R.id.storage_edit_nome);
        imageView = findViewById(R.id.storage_image_cel);
        btnGaleria = findViewById(R.id.storage_btn_galeria);
        btnUpload = findViewById(R.id.storage_btn_upload);

        //evento de clivk para upload
        btnUpload.setOnClickListener(v -> {
            if(editiNome.getText().toString().isEmpty()){
                Toast.makeText(this,"Digite um nome para a imagem",Toast.LENGTH_SHORT).show();
                return;
            }
            if(imageUri!=null){
                uploadImageUri();
            }else {
                uploadImagemByte();
            }

        });

        btnGaleria.setOnClickListener(v -> {
            Intent intent = new Intent();
            //intent implicita -> pegar um arquivo do celular
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // inicia uma Activity, e espera o retorno(foto)
            startActivityForResult(intent,112);
        });
    }

    private void uploadImageUri(){
        String tipo = getFileExtension(imageUri);

        Date d = new Date();
        String nome = editiNome.getText().toString();
        //1.0 -> referencia do arquivo firbase
        //1.1 -> criando uma referencia da imagem no Storage
        StorageReference imageRef = storage.getReference().child("imagens/"+nome+"-"+d.getTime()+"."+tipo);

        imageRef.putFile(imageUri)
        .addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this,"Uploado feito com sucesso",Toast.LENGTH_SHORT).show();
            /* Inserir dados da imagem no RealtimeDatabase */

            //1 -> Pegar a URL da imagem
            taskSnapshot.getStorage().getDownloadUrl()
            .addOnSuccessListener(uri -> {
                //1.3 -> Criando referência (database) do upload
                DatabaseReference refUpload = database.push();
                String id = refUpload.getKey();

                Upload upload = new Upload(id, nome, uri.toString());
                //salvando upload no db
                refUpload.setValue(upload);
            });
        })
        .addOnFailureListener(e -> {
            e.printStackTrace();
        })
        ;
    }

    //retorna o tipo(.png, .jpg) da imagem
    private String getFileExtension(Uri imageUri) {
        ContentResolver cr = getContentResolver();
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(cr.getType(imageUri));
    }

    //Resultado do startActivityResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("RESULT","requestCode" + requestCode + ", resultCode" + resultCode);
        if(requestCode == 112 && resultCode == Activity.RESULT_OK){
            //caso o usuário selecionou uma imagem da "galeria"
           imageUri = data.getData(); // -> Endereço da imagem selecionada
            imageView.setImageURI(imageUri);
        }
    }

    public byte[] convertImage2Byte(ImageView imageView){

        //Converter ImageView para -> byte[]
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable() ).getBitmap();
        //objeto baos ->  armazenar a imagem convertida
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();

        return baos.toByteArray();

    }
    //Fazer um upload de uma imagem convertida para bytes
    public void uploadImagemByte(){
        byte[] data = convertImage2Byte(imageView);

        //criar uma referência pra imagem no Storage
        StorageReference imagemRef = storage.getReference().child("imagens/01.jpeg");

        //Realiza o upload da imagem
        imagemRef.putBytes(data)
        .addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this,"Upload feito com sucesso!",Toast.LENGTH_SHORT).show();
            Log.i("UPLOAD","SECESSO");
        })
        .addOnFailureListener(e -> {
            e.printStackTrace();
        })
        ;

        //storage.getReference().putBytes();

    }
}
