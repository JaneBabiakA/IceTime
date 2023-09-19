package com.example.dycerollerreal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;

public class GameActivity extends AppCompatActivity implements AddGoal.editStatsListener {

    private HashMap<String, HashMap<String, Object>> gameStats = new HashMap<String, HashMap<String, Object>>();
    Boolean timerStarted = false;
    int counter;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        LinearLayout bench = findViewById(R.id.player_list);
        RelativeLayout rink = findViewById(R.id.ice_rink);
        RelativeLayout home = findViewById(R.id.home);
        Rect rinkCollider = new Rect();
        File file = new File(getFilesDir() + "/player_info.json");
        JSONArray playerArray = new JSONArray();
        if (file.isFile()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                playerArray = new JSONArray(stringBuilder.toString());

            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 0; i < playerArray.length(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.player_icons, bench, false); //TODO: issue, can't drag players before time starts
            TextView textView = view.findViewById(R.id.player_text);
            try {
                String name = ((JSONObject) playerArray.get(i)).getString("Last name");
                textView.setText(name);
                HashMap<String, Object> extraInfo = new HashMap<String, Object>();
                extraInfo.put("Goals", 0);
                extraInfo.put("Assists", 0);
                extraInfo.put("Total time", 0);
                extraInfo.put("Time entered", 0);
                gameStats.put(name, extraInfo);
                bench.addView(textView);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    rink.getGlobalVisibleRect(rinkCollider);
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                    view.startDragAndDrop(null, myShadow, view, 0);
                    view.setVisibility(View.INVISIBLE);
                    return true;
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getParent() == rink){
                        AddGoal addGoal = new AddGoal();
                        Bundle bundle = new Bundle();
                        bundle.putString("Name", ((TextView) view).getText().toString());
                        addGoal.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().add(R.id.frame_layout, addGoal).commit();
                    }
                }
            });
        }
        home.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    View dragee = (View) dragEvent.getLocalState();
                    String name = (String) ((TextView) dragee).getText();
                    HashMap<String, Object> stats = gameStats.get(name);
                    ((ViewGroup) dragee.getParent()).removeView(dragee);
                    float x = dragEvent.getX() + (dragee.getWidth() / 2); //get Raw X?
                    float y = dragEvent.getY() - (dragee.getHeight() / 2);
                    if (rinkCollider.contains((int) x, (int) y)) {
                        bench.removeView(dragee);
                        rink.addView(dragee);
                        stats.put("Time entered", counter);
                        dragee.setX(x - rink.getWidth() / 2);
                        dragee.setY(y);
                    } else {
                        rink.removeView(dragee);
                        bench.addView(dragee);
                        stats.put("Total time", ((int) stats.get("Total time")) + ((counter - (int) stats.get("Time entered"))/1000));
                        dragee.setX(0);
                        dragee.setY(0);
                    }
                    dragee.setVisibility(View.VISIBLE);
                    gameStats.put(name, stats);
                }
                return true;
            }
        });
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(getFilesDir() + "/player_info.json");
                if (file.isFile()){
                    try {
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = bufferedReader.readLine();
                        while (line != null){
                            stringBuilder.append(line);
                            line = bufferedReader.readLine();
                        }
                        bufferedReader.close();
                        JSONArray player_list = new JSONArray(stringBuilder.toString());
                        for (int i = 0; i < player_list.length(); i++){
                            JSONObject player = (JSONObject) player_list.get(i);
                            Log.e("Hola", String.valueOf(gameStats.get(player.get("Last name"))));
                            Log.e("Hola", String.valueOf(gameStats.get("B-A").get("Total time")));
                            int goals = (int) player.get("Goals") + (int) gameStats.get(player.get("Last name")).get("Goals");
                            int assists = (int) player.get("Assists") + (int) gameStats.get(player.get("Last name")).get("Assists");
                            LocalTime time = (LocalTime.parse((CharSequence) player.get("Time"))).plusSeconds((int) gameStats.get(player.get("Last name")).get("Total time"));
                            ((JSONObject) player_list.get(i)).put("Goals", goals);
                            ((JSONObject) player_list.get(i)).put("Assists", assists);
                            ((JSONObject) player_list.get(i)).put("Time", time);
                        }
                        FileOutputStream fileOutputStream = openFileOutput("player_info.json", Context.MODE_PRIVATE);
                        fileOutputStream.write(player_list.toString().getBytes());
                        fileOutputStream.close();
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
                Intent i = new Intent(GameActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GameActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
        Button timeButton = findViewById(R.id.timeButton);
        timeButton.setText("Start");
        EditText timeBox = findViewById(R.id.timer);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!timerStarted) {
                    timerStarted = true;
                    timeButton.setText("Pause");
                    timer = new CountDownTimer((long) 1.8E7, 1000) {
                        @Override
                        public void onTick(long l) {
                            counter += 1000;
                            timeBox.setText(String.format(Locale.getDefault(), "%02d:%02d", counter / 30000, (counter / 1000) % 60));
                        }
                        @Override
                        public void onFinish() {
                            //TODO: game ends
                            timerStarted = false;
                        }
                    }.start();
                } else {
                    timerStarted = false;
                    timeButton.setText("Start");
                    timer.cancel();
                }
            }
        });
    }
    @Override
    public void editGoals(String name) {
        gameStats.get(name).put("Goals", (int) gameStats.get(name).get("Goals") + 1);
    }
    @Override
    public void editAssists(String name) {
        gameStats.get(name).put("Assists", (int) gameStats.get(name).get("Assists") + 1);
    }
}