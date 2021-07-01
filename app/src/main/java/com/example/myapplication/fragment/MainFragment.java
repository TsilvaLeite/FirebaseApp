package com.example.myapplication.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adpter.UserAdapter;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private RecyclerView recyclerContatos;
    private UserAdapter userAdapter;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("requests");
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private User userLogged;

    private ArrayList<User> listaContatos = new ArrayList<>();

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_main, container, false);
        userLogged = new User(auth.getCurrentUser().getUid(),
                              auth.getCurrentUser().getEmail(),
                              auth.getCurrentUser().getDisplayName());
        recyclerContatos = layout.findViewById(R.id.frag_main_recycler_user);

        userAdapter = new UserAdapter(getContext(),listaContatos);
        userAdapter.setListener(new UserAdapter.ClickAdapterUser() {
            @Override
            public void adicionarContato(int position) {
                User u = listaContatos.get(position);
                //request send
                requestRef.child(userLogged.getId()).child("send").child(u.getId()).setValue(u);

                //request receive
                requestRef.child(u.getId()).child("receive").child(userLogged.getId()).setValue(userLogged);

                //tirar o usuário solicitado
                listaContatos.get(position).setReceiveRequest(true);
                userAdapter.notifyDataSetChanged();
            }
        });
        recyclerContatos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerContatos.setAdapter(userAdapter);


        return layout;
    }

    public void getUserDatabase(){
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaContatos.clear();
                //int cont = 1;
                for(DataSnapshot filho : snapshot.getChildren()){
                    User u = filho.getValue(User.class);

                    // comparar com o usuário logado
                    if(!userLogged.equals(u)){
                       /* if(cont%2==0){
                            u.setReceiveRequest(true);
                        }else{
                            u.setReceiveRequest(false);
                            cont++;
                        }*/
                        listaContatos.add(u);
                    }

                }

                // verificar quais contatos ja foram solicitados
                requestRef.child(userLogged.getId()).child("send")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot no_filho : snapshot.getChildren() ){
                                    User usuarioSolicitado = no_filho.getValue(User.class);
                                    for(int i=0; i < listaContatos.size(); i++){
                                            if(listaContatos.get(i).equals(usuarioSolicitado)){
                                                listaContatos.get(i).setReceiveRequest(true);
                                            }
                                    }
                                }

                                userAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserDatabase();
    }
}
