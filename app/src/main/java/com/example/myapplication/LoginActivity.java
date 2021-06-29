package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private Button btnCadastrar;
    private Button btnlogin;
    private EditText editEmail, editSenha;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        editEmail = findViewById(R.id.login_edit_email);
        editSenha = findViewById(R.id.login_edit_sennha);
        btnlogin = findViewById(R.id.login_btn_logar);
        btnlogin.setOnClickListener(v -> {
            logar();
        });


        btnCadastrar = findViewById(R.id.login_btn_cadastrar);
        btnCadastrar.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                startActivity(intent);
        });


        //Caso o usuario logado
        if(auth.getCurrentUser() != null){
            String email = auth.getCurrentUser().getEmail();
            Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
            //passar email p/ MainActivity
            intent.putExtra("email",email);
            startActivity(intent);

        }
    }

    public void logar(){
        String email = editEmail.getText().toString();
        String senha = editSenha.getText().toString();
        if(email.isEmpty() || senha.isEmpty()){
            Toast.makeText(this, "Preencha os campos",Toast.LENGTH_LONG).show();
            return;
        }

        // t -> é uma tarefa para logar
       auth.signInWithEmailAndPassword(email, senha)

        //listinner com sucesso
        .addOnSuccessListener(authResult -> {
            Toast.makeText(this,"Bem vindo usuário",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        })

        //listener de falha
        .addOnFailureListener(e -> {
           try{
               //Disparando exceção
               throw e;
           }catch (FirebaseAuthInvalidUserException userException){
               //Exceção para E-mail invalido
               Toast.makeText(this,"E-mail invalido!",Toast.LENGTH_SHORT).show();
           }catch (FirebaseAuthInvalidCredentialsException credException){
               //Exceção para Senha incorreta
               Toast.makeText(this,"Senha incorreta",Toast.LENGTH_SHORT).show();
           }catch (Exception ex){
               //Exceção para erro genérico
               Toast.makeText(this,"Erro inesperado",Toast.LENGTH_SHORT).show();
           }


        });
    }
}
