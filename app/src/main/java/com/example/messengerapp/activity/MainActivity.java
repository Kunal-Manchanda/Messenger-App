package com.example.messengerapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.messengerapp.R;
import com.example.messengerapp.adapter.NewChatAdapter;
import com.example.messengerapp.model.Chat;
import com.example.messengerapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView txtUsername;

    FirebaseUser firebaseUser;
    DatabaseReference ref;
    FloatingActionButton fab;

    private RecyclerView recentChatRecycler;
    private NewChatAdapter adapter;
    private List<String> userlist;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, NewChatActivity.class);
                startActivity(intent);
            }
        });


        profileImage=findViewById(R.id.profileImage);
        txtUsername=findViewById(R.id.txtUsername);
        recentChatRecycler=findViewById(R.id.recentChatRecycler);
        recentChatRecycler.setHasFixedSize(true);
        recentChatRecycler.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        ref= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                txtUsername.setText(user.getUsername());
                 if (user.getImageUrl().equals("default")) {
                        profileImage.setImageResource(R.mipmap.ic_launcher);
                 } else {
                        Picasso.get().load(user.getImageUrl()).into(profileImage);
                 }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userlist=new ArrayList<>();
        ref=FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();

                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Chat chat=ds.getValue(Chat.class);

                    if(chat.getSender().equals(firebaseUser.getUid())){
                        if(!userlist.contains(chat.getReceiver())){
                        userlist.add(chat.getReceiver());
                    }
                    }
                    if(chat.getReceiver().equals(firebaseUser.getUid())){
                        if(!userlist.contains(chat.getSender())){
                        userlist.add(chat.getSender());
                    }
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
        }
        return false;
    }

    private void readChats(){
        users=new ArrayList<>();

        ref=FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();

                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    User user=ds.getValue(User.class);

                    for(String id:userlist){
                        if(user.getId().equals(id)){
                            if(users.size()!=0){
//                                Iterator<User> ite=users.iterator();
                                for(User user1: users){
//                                while(ite.hasNext()){
//                                    User user1=ite.next();
                                    if(!user.getId().equals(user1.getId())){
                                        users.add(user);
                                        break;
                                    }
                                }
                            }else{
                                users.add(user);
                            }
                        }
                    }
                }
                adapter=new NewChatAdapter(MainActivity.this,users);
                recentChatRecycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
