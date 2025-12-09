package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatFragment extends Fragment {

    private static final String ARG_CHAT_ID = "chat_id";
    private String chatId;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;

    public static ChatFragment newInstance(String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatId = getArguments().getString(ARG_CHAT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = view.findViewById(R.id.chatToolbar);
        toolbar.setTitle(chatId);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);

        EditText editChatMessage = view.findViewById(R.id.editChatMessage);
        Button buttonSendMessage = view.findViewById(R.id.buttonSendMessage);
        Button buttonRefreshChat = view.findViewById(R.id.buttonRefreshChat);

        buttonSendMessage.setOnClickListener(v -> {
            String message = editChatMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null && activity.userPseudo != null) {
                    String command = "MSG;" + activity.userPseudo + ";" + chatId + ";" + message;
                    activity.sendUdpMessage(command);
                    editChatMessage.setText("");
                    // Le message ne s'affiche plus ici pour éviter les doublons.
                    // Il s'affichera à la réception de la confirmation du serveur.
                }
            }
        });

        // Le bouton Actualiser demande l'historique complet
        buttonRefreshChat.setOnClickListener(v -> {
             MainActivity activity = (MainActivity) getActivity();
            if (activity != null && activity.userPseudo != null) {
                String command = "GET_HISTORY;" + activity.userPseudo + ";" + chatId;
                activity.sendUdpMessage(command);
            }
        });

        return view;
    }

    // Méthode pour afficher tout l'historique
    public void displayMessages(List<ChatMessage> messages) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.clearMessages(); // On vide l'écran
                for (ChatMessage message : messages) {
                    chatAdapter.addMessage(message); // On ajoute chaque message de l'historique
                }
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            });
        }
    }

    // Méthode pour ajouter un seul nouveau message (confirmation d'envoi ou réception)
    public void addMessageToChat(ChatMessage message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(message);
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            });
        }
    }
}
