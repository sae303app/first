package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // On trouve le bouton et on lui attache l'action
        Button buttonRefresh = view.findViewById(R.id.buttonRefreshDiscussions);
        buttonRefresh.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null && activity.userPseudo != null) {
                // On demande au serveur la liste des amis ET des groupes
                activity.sendUdpMessage("GET_FRIENDS;" + activity.userPseudo);
                activity.sendUdpMessage("GET_GROUPS;" + activity.userPseudo);
            }
        });

        return view;
    }

    // Méthode pour afficher les amis dans la section "Messages Privés"
    public void displayPrivateDiscussions(String[] friendNames) {
        if (getContext() == null || privateDiscussionsContainer == null) return;

        privateDiscussionsContainer.removeAllViews();
        if (friendNames == null || friendNames.length == 0 || (friendNames.length == 1 && friendNames[0].isEmpty())) {
            addInfoTextView(privateDiscussionsContainer, "Aucun ami à afficher.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String name : friendNames) {
            if (name == null || name.isEmpty()) continue;
            View itemView = inflater.inflate(R.layout.item_discussion, privateDiscussionsContainer, false);
            ((TextView) itemView.findViewById(R.id.textDiscussionName)).setText(name);
            itemView.findViewById(R.id.buttonStartChat).setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(name);
            });
            privateDiscussionsContainer.addView(itemView);
        }
    }

    // Méthode pour afficher les groupes dans la section "Groupes"
    public void displayGroupDiscussions(String[] groupNames) {
        if (getContext() == null || groupDiscussionsContainer == null) return;
        groupDiscussionsContainer.removeAllViews();
        if (groupNames == null || groupNames.length == 0 || (groupNames.length == 1 && groupNames[0].isEmpty())) {
            addInfoTextView(groupDiscussionsContainer, "Aucun groupe à afficher.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String name : groupNames) {
            if (name == null || name.isEmpty()) continue;
            View itemView = inflater.inflate(R.layout.item_discussion, groupDiscussionsContainer, false);
            ((TextView) itemView.findViewById(R.id.textDiscussionName)).setText(name);
            itemView.findViewById(R.id.buttonStartChat).setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(name);
            });
            groupDiscussionsContainer.addView(itemView);
        }
    }

    private void addInfoTextView(LinearLayout container, String text) {
        TextView infoView = new TextView(getContext());
        infoView.setText(text);
        container.addView(infoView);
    }
}
