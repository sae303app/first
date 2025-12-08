package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerList;
    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageButton buttonSend;
    private ImageView profileAvatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views from the layout
        tabLayout = findViewById(R.id.tabLayout);
        recyclerList = findViewById(R.id.recyclerList);
        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);
        profileAvatar = findViewById(R.id.profileAvatar);


        // TODO: Set up your TabLayout, RecyclerView adapters, and button click listeners here
    }
}
