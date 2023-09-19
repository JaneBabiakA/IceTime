package com.example.dycerollerreal;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;

//TODO: Work on back button
public class AddPlayer extends Fragment {
    infoEnteredListener myListener;
    int index;
    String firstName;
    String lastName;
    String number;
    String position;
    int goals;
    int assists;
    LocalTime time;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (infoEnteredListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null){
            index = arguments.getInt("Index");
            firstName = arguments.getString("First name");
            lastName = arguments.getString("Last name");
            number = arguments.getString("Number");
            position = arguments.getString("Position");
            goals = arguments.getInt("Goals");
            assists = arguments.getInt("Assists");
            time = (LocalTime) arguments.getSerializable("Time");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_player, container, false);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.player_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(position));
        EditText firstNameText = (EditText) view.findViewById(R.id.enterFirstName);
        firstNameText.setText(firstName);
        EditText lastNameText = (EditText) view.findViewById(R.id.enterLastName);
        lastNameText.setText(lastName);
        EditText numberText = (EditText) view.findViewById(R.id.enterNumber);
        numberText.setText(number);
        Button save = (Button) view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Hola", String.valueOf(time));
                LocalTime testTime = LocalTime.of(3, 4, 2);
                Log.e("Hola", String.valueOf(testTime));
                //get all info to write to internal memory
                String firstName = firstNameText.getText().toString();
                String lastName = lastNameText.getText().toString();
                String number = numberText.getText().toString();
                String position = spinner.getSelectedItem().toString();
                if (time == null){
                    time = LocalTime.of(0, 0, 0);
                }
                try {
                    //get player info
                    JSONObject player = new JSONObject();
                    player.put("Index", index);
                    player.put("First name", firstName);
                    player.put("Last name", lastName);
                    player.put("Number", number);
                    player.put("Position", position);
                    player.put("Goals", goals);
                    player.put("Assists", assists);
                    player.put("Time", time);
                    Log.e("Hola", String.valueOf(time));
                    //callback to update activity
                    if (myListener != null){
                        myListener.addToActivity(player);
                    }
                }
                catch (JSONException e){
                    throw new RuntimeException(e);
                }
                //close fragment
                getFragmentManager().beginTransaction().remove(AddPlayer.this).commit();
            }
        });
        Button delete = (Button) view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(getActivity().getFilesDir() + "/player_info.json");
                if (file.isFile()){
                    try{
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedFileReader = new BufferedReader(fileReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = bufferedFileReader.readLine();
                        while (line != null){
                            stringBuilder.append(line).append("\n");
                            line = bufferedFileReader.readLine();
                        }
                        bufferedFileReader.close();
                        JSONArray playersArray = new JSONArray(stringBuilder.toString());
                        playersArray.remove(index);
                        FileOutputStream player_info = getActivity().openFileOutput("player_info.json", Context.MODE_PRIVATE);
                        player_info.write(playersArray.toString().getBytes());
                        player_info.close();
                        if (myListener != null){
                            myListener.reloadActivity(playersArray);
                        }
                    }
                    catch (IOException | JSONException e){
                        throw new RuntimeException(e);
                    }

                }
                //close fragment
                getFragmentManager().beginTransaction().remove(AddPlayer.this).commit();
            }
        });
        return view;
    }
    public interface infoEnteredListener {
        void addToActivity(JSONObject player);
        void reloadActivity(JSONArray playerArray);
    }
}
