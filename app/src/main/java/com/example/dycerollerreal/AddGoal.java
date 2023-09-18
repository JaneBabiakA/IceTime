package com.example.dycerollerreal;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AddGoal extends Fragment {
    editStatsListener myListener;
    String name;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (editStatsListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        name = arguments.getString("Name");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);
        Button goal = view.findViewById(R.id.goal);
        goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myListener != null){
                    Log.e("hola", name + "scored a goal");
                    myListener.editGoals(name);
                }
                getFragmentManager().beginTransaction().remove(AddGoal.this).commit();
            }
        });
        Button assist = view.findViewById(R.id.assist);
        assist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myListener != null){
                    Log.e("hola", name + "got an assist");
                    myListener.editAssists(name);
                }
                getFragmentManager().beginTransaction().remove(AddGoal.this).commit();
            }
        });
        Button back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().remove(AddGoal.this).commit();
            }
        });
        return view;
    }
    public interface editStatsListener {
        void editGoals(String name);
        void editAssists(String name);
    }
}