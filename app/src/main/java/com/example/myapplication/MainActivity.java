package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

        new Thread(() -> {
            try {
                // ✅ IP DU SERVEUR
                // ÉMULATEUR ANDROID → 10.0.2.2
                String SERVER_IP = "10.0.2.2";
                int SERVER_PORT = 5000;

                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true
                );

                // Lire le message du serveur
                String msg = in.readLine();
                System.out.println("Serveur dit : " + msg);

                // Envoyer un message test
                out.println("hello from android");

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
