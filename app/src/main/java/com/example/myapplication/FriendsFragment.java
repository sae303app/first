package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FriendsFragment extends Fragment {

    private EditText editFriendName;
    private Button buttonAddFriend, buttonRefreshRequests, buttonRefreshFriends;
    private LinearLayout requestsContainer, friendsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        editFriendName = view.findViewById(R.id.editFriendName);
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend);
        buttonRefreshRequests = view.findViewById(R.id.buttonRefreshRequests);
        buttonRefreshFriends = view.findViewById(R.id.buttonRefreshFriends);
        requestsContainer = view.findViewById(R.id.requestsContainer);
        friendsContainer = view.findViewById(R.id.friendsContainer);

        buttonAddFriend.setOnClickListener(v -> {
            String friendName = editFriendName.getText().toString().trim();
            if (!friendName.isEmpty()) {
                sendUdpCommand("REQUEST_FRIEND;" + getPseudo() + ";" + friendName, "Demande d'ami envoyée.");
                editFriendName.setText("");
            }
        });

        buttonRefreshRequests.setOnClickListener(v -> {
            sendUdpCommand("GET_REQUESTS;" + getPseudo(), "Actualisation des demandes...");
        });

        buttonRefreshFriends.setOnClickListener(v -> {
            sendUdpCommand("GET_FRIENDS;" + getPseudo(), "Actualisation de la liste d'amis...");
        });

        return view;
    }

    public void displayFriendRequests(String[] requesters) {
        if (getContext() == null) return;
        requestsContainer.removeAllViews();
        if (requesters.length == 0 || (requesters.length == 1 && requesters[0].isEmpty())) {
            addInfoTextView(requestsContainer, "Aucune nouvelle demande.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String requesterName : requesters) {
            View requestView = inflater.inflate(R.layout.item_friend_request, requestsContainer, false);
            ((TextView) requestView.findViewById(R.id.textRequesterName)).setText(requesterName);

            requestView.findViewById(R.id.buttonAccept).setOnClickListener(v -> {
                sendUdpCommand("REPLY_FRIEND;" + getPseudo() + ";" + requesterName + ";ACCEPT", "Demande acceptée.");
                requestView.setVisibility(View.GONE);
            });
            requestView.findViewById(R.id.buttonRefuse).setOnClickListener(v -> {
                sendUdpCommand("REPLY_FRIEND;" + getPseudo() + ";" + requesterName + ";REFUSE", "Demande refusée.");
                requestView.setVisibility(View.GONE);
            });
            requestsContainer.addView(requestView);
        }
    }

    public void displayFriendsList(String[] friends) {
        if (getContext() == null) return;
        friendsContainer.removeAllViews();
        if (friends.length == 0 || (friends.length == 1 && friends[0].isEmpty())) {
            addInfoTextView(friendsContainer, "Vous n'avez aucun ami pour le moment.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String friendName : friends) {
            View friendView = inflater.inflate(R.layout.item_friend, friendsContainer, false);
            ((TextView) friendView.findViewById(R.id.textFriendName)).setText(friendName);
            friendView.findViewById(R.id.buttonDeleteFriend).setOnClickListener(v -> {
                sendUdpCommand("DEL_AMI;" + getPseudo() + ";" + friendName, "Ami supprimé.");
                friendView.setVisibility(View.GONE);
            });
            // Add a click listener to the friend item to start a chat
            friendView.setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(friendName);
            });
            friendsContainer.addView(friendView);
        }
    }

    private void sendUdpCommand(String command, String toastMessage) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.sendUdpMessage(command);
            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private String getPseudo() {
        MainActivity activity = (MainActivity) getActivity();
        return (activity != null) ? activity.userPseudo : null;
    }
    
    private void addInfoTextView(LinearLayout container, String text) {
        TextView infoView = new TextView(getContext());
        infoView.setText(text);
        container.addView(infoView);
    }
}
