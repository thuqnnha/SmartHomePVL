package com.example.smarthomepvl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Lấy username từ Intent của Activity
        String username = getActivity().getIntent().getStringExtra("username");

        // Gán vào TextView
        TextView appName = view.findViewById(R.id.appName);
        appName.setText("Hello, "+username);

        return view;
    }
}

