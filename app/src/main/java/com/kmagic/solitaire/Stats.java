/*
  Copyright 2008 - 2010 Google Inc.
  Copyright 2016 Obsidian-Studios, Inc.
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/ 
package com.kmagic.solitaire;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class Stats {

  public Stats(final Solitaire solitaire, final SolitaireView view) {

    solitaire.setContentView(R.layout.stats);
    View statsView = (View) solitaire.findViewById(R.id.stats_view);
    statsView.setFocusable(true);
    statsView.setFocusableInTouchMode(true);

    Rules rules = view.getRules();
    final SharedPreferences settings = solitaire.getSettings();
    final String gameAttemptString = rules.GetGameTypeString() + "Attempts";
    final String gameWinString = rules.GetGameTypeString() + "Wins";
    final String gameTimeString = rules.GetGameTypeString() + "Time";
    final String gameScoreString = rules.GetGameTypeString() + "Score";
    int attempts = settings.getInt(gameAttemptString, 0);
    int wins = settings.getInt(gameWinString, 0);
    int bestTime = settings.getInt(gameTimeString, -1);
    int highScore = settings.getInt(gameScoreString, -52);
    float ratio = 0;
    if (attempts > 0) {
      ratio = (float)wins / (float)attempts * 100.0f;
    }

    TextView tv = (TextView)solitaire.findViewById(R.id.text_title);
    String text = rules.GetPrettyGameTypeString() + " \n\n";
    tv.setText(text);
    tv = (TextView)solitaire.findViewById(R.id.text_wins);
    text = solitaire.getResources().getText(R.string.stats_wins)+": "+ wins + " "
           + solitaire.getResources().getText(R.string.stats_attempts)+": " + attempts;
    tv.setText(text);
    tv = (TextView)solitaire.findViewById(R.id.text_percentage);
    text = solitaire.getResources().getText(R.string.stats_win_percent)+": " + ratio;
    tv.setText(text);
    if (bestTime != -1) {
      int seconds = (bestTime / 1000) % 60;
      int minutes = bestTime / 60000;
      tv = (TextView)solitaire.findViewById(R.id.text_best_time);
      text = solitaire.getResources().getText(R.string.stats_fastest_time)+": "
             + String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);
      tv.setText(text);
    }
    if (rules.HasScore()) {
      tv = (TextView)solitaire.findViewById(R.id.text_high_score);
      text = solitaire.getResources().getText(R.string.stats_high_score)+": " + highScore;
      tv.setText(text);
    }


    final Button accept = (Button) solitaire.findViewById(R.id.button_accept);
    accept.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        solitaire.cancelOptions();
      }
    });
    final Button clear = (Button) solitaire.findViewById(R.id.button_clear);
    clear.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(gameAttemptString, 0);
        editor.putInt(gameWinString, 0);
        editor.putInt(gameTimeString, -1);
        editor.apply();
        view.clearGameStarted();
        solitaire.cancelOptions();
      }
    });
    statsView.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
          case KeyEvent.KEYCODE_HOME:
            solitaire.cancelOptions();
            return true;
        }
        return false;
      }
    });
    statsView.requestFocus();
  }
}

