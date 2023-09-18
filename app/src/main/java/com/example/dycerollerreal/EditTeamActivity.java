package com.example.dycerollerreal;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

//TODO: make this page look prettier by having a header and textboxes for every attribute, as well as making player deletable/editable
//TODO: only make one fragment openable at a time
public class EditTeamActivity extends AppCompatActivity implements AddPlayer.infoEnteredListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team);
        File file = new File(getFilesDir() + "/player_info.json");
        if (file.isFile()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                JSONArray playerArray = new JSONArray(stringBuilder.toString());
                reloadActivity(playerArray);
            }
            catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void go_home(View v){//TODO: programmatically
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void add_player(View v){//TODO: programmatically
        AddPlayer addPlayer = new AddPlayer();
        Bundle bundle = new Bundle();
        bundle.putInt("Index", -1);
        addPlayer.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, addPlayer).commit();
    }
    @Override
    public void reloadActivity(JSONArray playerArray){
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();
        for (int i = 0; i < playerArray.length(); i++){
            try{
                int index = i;
                JSONObject player = (JSONObject) playerArray.get(i);
                player.put("Index", i); //USELESS
                TextView textView= new TextView(this);
                textView.setText(player.toString());
                tableLayout.addView(textView);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        try {
                            bundle.putInt("Index", index);
                            bundle.putString("First name", player.getString("First name"));
                            bundle.putString("Last name", player.getString("Last name"));
                            bundle.putString("Number", player.getString("Number"));
                            bundle.putString("Position", player.getString("Position"));
                            bundle.putInt("Goals", player.getInt("Goals"));
                            bundle.putInt("Assists", player.getInt("Assists"));
                            bundle.putSerializable("Time", (Serializable) player.get("Time"));
                        }
                        catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        AddPlayer addPlayer = new AddPlayer();
                        addPlayer.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, addPlayer).commit();
                    }
                });
            }
            catch (JSONException e){
                throw new RuntimeException(e);
            }

        }
    }
    @Override
    public void addToActivity(JSONObject player) {
        Log.e("hoa", player.toString());
        JSONArray playersArray = new JSONArray();
        try {
            File file = new File(getFilesDir() + "/player_info.json");
            if (file.isFile()){
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedFileReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedFileReader.readLine();
                while (line != null){
                    stringBuilder.append(line).append("\n");
                    line = bufferedFileReader.readLine();
                }
                bufferedFileReader.close();
                playersArray = new JSONArray(stringBuilder.toString());
            }
            if (player.getInt("Index") > -1 && player.getInt("Index") < playersArray.length()){
                playersArray.put(player.getInt("Index"), player);
                FileOutputStream player_info = openFileOutput("player_info.json", Context.MODE_PRIVATE);
                player_info.write(playersArray.toString().getBytes());
                player_info.close();
                reloadActivity(playersArray);
                return;
            }
            int index = playersArray.length();
            player.put("Index", index);
            playersArray.put(player);
            Log.e("INDEX", String.valueOf(index));
            FileOutputStream player_info = openFileOutput("player_info.json", Context.MODE_PRIVATE);
            player_info.write(playersArray.toString().getBytes());
            player_info.close();
            TextView textView = new TextView(this);
            textView.setText(player.toString());
            ((TableLayout) findViewById(R.id.tableLayout)).addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putInt("Index", index);
                        bundle.putString("First name", player.getString("First name"));
                        bundle.putString("Last name", player.getString("Last name"));
                        bundle.putString("Number", player.getString("Number"));
                        bundle.putString("Position", player.getString("Position"));
                        bundle.putInt("Goals", player.getInt("Goals"));
                        bundle.putInt("Assists", player.getInt("Assists"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    AddPlayer addPlayer = new AddPlayer();
                    addPlayer.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, addPlayer).commit();
                }
            });

        }
        catch (IOException | JSONException e){
            throw new RuntimeException(e);
        }
    }
}