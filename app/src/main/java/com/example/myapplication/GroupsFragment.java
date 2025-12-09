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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class GroupsFragment extends Fragment {

    private EditText editGroupName;
    private Button buttonCreateGroup, buttonRefreshGroups;
    private LinearLayout groupsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        editGroupName = view.findViewById(R.id.editGroupName);
        buttonCreateGroup = view.findViewById(R.id.buttonCreateGroup);
        buttonRefreshGroups = view.findViewById(R.id.buttonRefreshGroups);
        groupsContainer = view.findViewById(R.id.groupsContainer);

        buttonCreateGroup.setOnClickListener(v -> {
            String groupName = editGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                sendUdpCommand("CREATE_GROUP;" + getPseudo() + ";" + groupName, "Demande de création de groupe envoyée.");
                editGroupName.setText("");
            }
        });

        buttonRefreshGroups.setOnClickListener(v -> {
            sendUdpCommand("GET_GROUPS;" + getPseudo(), "Actualisation des groupes...");
        });

        return view;
    }

    public void displayGroupsList(String[] groupNames) {
        if (getContext() == null) return;
        groupsContainer.removeAllViews();
        if (groupNames.length == 0 || (groupNames.length == 1 && groupNames[0].isEmpty())) {
            addInfoTextView(groupsContainer, "Vous n'êtes dans aucun groupe.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String groupName : groupNames) {
            View groupView = inflater.inflate(R.layout.item_group, groupsContainer, false);
            ((TextView) groupView.findViewById(R.id.textGroupName)).setText(groupName);
            groupView.findViewById(R.id.buttonAddMember).setOnClickListener(v -> showAddMemberDialog(groupName));
            groupView.setOnClickListener(v -> {
                ((MainActivity) getActivity()).openChat(groupName);
            });
            groupsContainer.addView(groupView);
        }
    }

    private void showAddMemberDialog(String groupName) {
        if (getContext() == null) return;
        final EditText input = new EditText(getContext());
        input.setHint("Pseudo de l'ami à ajouter");

        new AlertDialog.Builder(getContext())
                .setTitle("Ajouter à " + groupName)
                .setView(input)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String friendName = input.getText().toString().trim();
                    if (!friendName.isEmpty()) {
                        sendUdpCommand("ADD_TO_GROUP;" + groupName + ";" + friendName, "Demande d'ajout envoyée.");
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
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
