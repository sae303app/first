package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
        setHasOptionsMenu(true);
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
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);

        EditText editChatMessage = view.findViewById(R.id.editChatMessage);
        Button buttonSendMessage = view.findViewById(R.id.buttonSendMessage);

        buttonSendMessage.setOnClickListener(v -> {
            String message = editChatMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null && activity.userPseudo != null) {
                    // Le format de MSG est bon, on n'y touche pas
                    String command = "MSG;" + activity.userPseudo + ";" + chatId + ";" + message;
                    activity.sendUdpMessage(command);
                    editChatMessage.setText("");
                }
            }
        });

        // On demande l'historique dès qu'on ouvre le chat
        requestHistory();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh_chat) {
            requestHistory(); // Le bouton "Actualiser" utilise la même logique
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestHistory() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && activity.userPseudo != null) {
            // C'est ici qu'on envoie la BONNE commande
            String command = "GET_HISTORY;" + activity.userPseudo + ";" + chatId;
            activity.sendUdpMessage(command);
        }
    }

    public void displayMessages(List<ChatMessage> messages) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.clearMessages();
                for (ChatMessage message : messages) {
                    chatAdapter.addMessage(message);
                }
                if (chatAdapter.getItemCount() > 0) {
                    chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        }
    }

    public void addMessageToChat(ChatMessage message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(message);
                if (chatAdapter.getItemCount() > 0) {
                    chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        }
    }
}
