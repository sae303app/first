package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout loginRegisterLayout;
    private ConstraintLayout mainAppLayout;
    private EditText editPseudo;
    private Button buttonRegister, buttonConnect, buttonLogout;
    private TextView titleTextView;
    private BottomNavigationView bottomNavigation;

    private ClientUDP client;
    private Handler uiHandler;
    public String userPseudo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginRegisterLayout = findViewById(R.id.loginRegisterLayout);
        mainAppLayout = findViewById(R.id.mainAppLayout);
        editPseudo = findViewById(R.id.editPseudo);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonLogout = findViewById(R.id.buttonLogout);
        titleTextView = findViewById(R.id.titleTextView);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        uiHandler = new Handler(Looper.getMainLooper());
        client = new ClientUDP(this::handleServerResponse);
        new Thread(client).start();

        setupListeners();
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> {
            String pseudo = editPseudo.getText().toString().trim();
            if (!pseudo.isEmpty()) {
                sendUdpMessage("CREATION;" + pseudo);
            }
        });

        buttonConnect.setOnClickListener(v -> {
            userPseudo = editPseudo.getText().toString().trim();
            if (!userPseudo.isEmpty()) {
                sendUdpMessage("CONNEXION;" + userPseudo);
            }
        });

        buttonLogout.setOnClickListener(v -> logout());

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_discussions) {
                selectedFragment = new DiscussionsFragment();
                title = "Discussions";
            } else if (itemId == R.id.navigation_groups) {
                selectedFragment = new GroupsFragment();
                title = "Groupes";
            } else if (itemId == R.id.navigation_friends) {
                selectedFragment = new FriendsFragment();
                title = "Amis";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                titleTextView.setText(title);
            }
            return true;
        });
    }

    public void sendUdpMessage(String message) {
        try {
            client.envoi(message);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur réseau: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void openChat(String chatId) {
        Fragment chatFragment = ChatFragment.newInstance(chatId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, chatFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void handleServerResponse(String response) {
        Log.d("UDP_RESPONSE", "Server says: " + response);
        String[] parts = response.split(";", -1);
        if (parts.length < 1) return;

        String command = parts[0];
        String status = (parts.length > 1) ? parts[1] : "";

        uiHandler.post(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if ("OK".equalsIgnoreCase(status)) {
                switch (command) {
                    case "CREATION":
                        Toast.makeText(this, "Compte créé ! Veuillez vous connecter.", Toast.LENGTH_LONG).show();
                        break;
                    case "CONNEXION":
                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        showMainUI();
                        break;
                    case "DECONNEXION":
                        Toast.makeText(this, "Vous avez été déconnecté.", Toast.LENGTH_SHORT).show();
                        showLoginUI();
                        break;
                    case "GET_FRIENDS":
                        if (currentFragment instanceof FriendsFragment) {
                            String[] friends = (parts.length > 2) ? parts[2].split(",") : new String[0];
                            ((FriendsFragment) currentFragment).displayFriendsList(friends);
                        } else if (currentFragment instanceof DiscussionsFragment) {
                            String[] friends = (parts.length > 2) ? parts[2].split(",") : new String[0];
                            ((DiscussionsFragment) currentFragment).displayPrivateDiscussions(friends);
                        }
                        break;
                    case "GET_GROUPS":
                        if (currentFragment instanceof GroupsFragment) {
                            String[] groups = (parts.length > 2) ? parts[2].split(",") : new String[0];
                            ((GroupsFragment) currentFragment).displayGroupsList(groups);
                        } else if (currentFragment instanceof DiscussionsFragment) {
                            String[] groups = (parts.length > 2) ? parts[2].split(",") : new String[0];
                            ((DiscussionsFragment) currentFragment).displayGroupDiscussions(groups);
                        }
                        break;
                    case "GET_REQUESTS":
                        if (currentFragment instanceof FriendsFragment) {
                            if (parts.length > 2) {
                                String[] requesters = parts[2].split(",");
                                ((FriendsFragment) currentFragment).displayFriendRequests(requesters);
                            } else {
                                ((FriendsFragment) currentFragment).displayFriendRequests(new String[0]);
                            }
                        }
                        break;
                    case "CREATE_GROUP":
                        if (currentFragment instanceof GroupsFragment) {
                            String groupName = (parts.length > 2) ? parts[2] : "Nouveau Groupe";
                            ((GroupsFragment) currentFragment).addGroupToList(groupName);
                        }
                        break;
                    case "MSG":
                        if (currentFragment instanceof ChatFragment) {
                            String sender = parts[2];
                            String message = parts[4];
                            boolean isSentByUser = userPseudo.equals(sender);
                            ((ChatFragment) currentFragment).addMessageToChat(new ChatMessage(sender, message, isSentByUser));
                        }
                        break;
                    case "GET_HISTORY":
                        if (currentFragment instanceof ChatFragment) {
                            List<ChatMessage> messages = new ArrayList<>();
                            if (parts.length > 2 && !parts[2].isEmpty()) {
                                String[] allMessages = parts[2].split("\\|");
                                for (String msg : allMessages) {
                                    String[] msgParts = msg.split(":", 2);
                                    if (msgParts.length == 2) {
                                        boolean isSentByUser = userPseudo.equals(msgParts[0]);
                                        messages.add(new ChatMessage(msgParts[0], msgParts[1], isSentByUser));
                                    }
                                }
                            }
                            ((ChatFragment) currentFragment).displayMessages(messages);
                        }
                        break;
                }
            } else if ("KO".equalsIgnoreCase(status)) {
                String reason = (parts.length > 2) ? parts[2] : "Raison inconnue";
                Toast.makeText(this, "Échec " + command + ": " + reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMainUI() {
        loginRegisterLayout.setVisibility(View.GONE);
        mainAppLayout.setVisibility(View.VISIBLE);
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_discussions);
        }
    }

    private void showLoginUI() {
        mainAppLayout.setVisibility(View.GONE);
        loginRegisterLayout.setVisibility(View.VISIBLE);
        userPseudo = null;
    }

    private void logout() {
        if (userPseudo != null) {
            sendUdpMessage("DECONNEXION;" + userPseudo);
        }
        showLoginUI();
    }

    @Override
    protected void onDestroy() {
        logout();
        super.onDestroy();
        if (client != null) {
            client.close();
        }
    }

    private static class ClientUDP implements Runnable {
        private static final int SERVER_PORT = 6090;
        private DatagramSocket socket;
        private InetAddress address;
        private volatile boolean running = true;
        private final ServerResponseCallback callback;

        public interface ServerResponseCallback {
            void onResponse(String response);
        }

        public ClientUDP(ServerResponseCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                address = InetAddress.getByName("10.0.2.2");
                socket = new DatagramSocket();
                while (running) {
                    try {
                        byte[] buf = new byte[2048];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String receivedMsg = new String(packet.getData(), 0, packet.getLength());
                        if (callback != null) callback.onResponse(receivedMsg);
                    } catch (IOException e) {
                        if (running) Log.e("UDP", "Receive error", e);
                    }
                }
            } catch (Exception e) {
                Log.e("UDP", "Client run error", e);
            }
        }

        public void envoi(String msg) throws IOException {
            if (socket == null || socket.isClosed()) return;
            byte[] message = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length, address, SERVER_PORT);
            new Thread(() -> {
                try { socket.send(packet); } catch (IOException e) { e.printStackTrace(); }
            }).start();
        }

        public void close() {
            running = false;
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }
}
