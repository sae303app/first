package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DiscussionsFragment extends Fragment {

    private LinearLayout privateDiscussionsContainer, groupDiscussionsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussions, container, false);

        privateDiscussionsContainer = view.findViewById(R.id.privateDiscussionsContainer);
        groupDiscussionsContainer = view.findViewById(R.id.groupDiscussionsContainer);

        Button buttonRefresh = view.findViewById(R.id.buttonRefreshDiscussions);
        buttonRefresh.setOnClickListener(v -> {
            // On demande les deux listes au serveur
            sendUdpCommand("GET_FRIENDS;" + getPseudo(), "Actualisation des amis...");
            sendUdpCommand("GET_GROUPS;" + getPseudo(), "Actualisation des groupes...");
        });

        return view;
    }

    public void displayPrivateDiscussions(String[] friendNames) {
        if (getContext() == null) return;
        privateDiscussionsContainer.removeAllViews();
        if (friendNames.length == 0 || (friendNames.length == 1 && friendNames[0].isEmpty())) {
            addInfoTextView(privateDiscussionsContainer, "Aucun ami à qui parler.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String name : friendNames) {
            View itemView = inflater.inflate(R.layout.item_discussion, privateDiscussionsContainer, false);
            ((TextView) itemView.findViewById(R.id.textDiscussionName)).setText(name);
            itemView.findViewById(R.id.buttonStartChat).setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(name);
            });
            privateDiscussionsContainer.addView(itemView);
        }
    }

    public void displayGroupDiscussions(String[] groupNames) {
        if (getContext() == null) return;
        groupDiscussionsContainer.removeAllViews();
        if (groupNames.length == 0 || (groupNames.length == 1 && groupNames[0].isEmpty())) {
            addInfoTextView(groupDiscussionsContainer, "Aucun groupe à discuter.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String name : groupNames) {
            View itemView = inflater.inflate(R.layout.item_discussion, groupDiscussionsContainer, false);
            ((TextView) itemView.findViewById(R.id.textDiscussionName)).setText(name);
            itemView.findViewById(R.id.buttonStartChat).setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(name);
            });
            groupDiscussionsContainer.addView(itemView);
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
