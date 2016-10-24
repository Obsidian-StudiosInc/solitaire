/*
  Original Work Copyright 2008-2010 Google Inc.
  Modified Work Copyright 2016 Obsidian-Studios, Inc.

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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Base activity class
 * The main activity for the Solitaire game
 */
public class Solitaire extends Activity {

  // View extracted from main.xml.
  private SolitaireView mSolitaireView;
  private SharedPreferences mSettings;

  private boolean mDoSave;
  
  // Shared preferences are where the various user settings are stored.
  public SharedPreferences getSettings() { return mSettings; }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDoSave = true;

    // Force landscape and no title for extra room
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    requestWindowFeature(Window.FEATURE_ACTION_BAR);
    getActionBar().hide();

    // If the user has never accepted the EULA show it again.
    mSettings = getSharedPreferences("SolitairePreferences", 0);
    setContentView(R.layout.main);
    mSolitaireView = (SolitaireView) findViewById(R.id.solitaire);
    mSolitaireView.setTextView((TextView) findViewById(R.id.text));

  }

  /**
   * Entry point for starting the game
   * {@inheritDoc}
   */
  @Override
  public void onStart() {
    super.onStart();
    if (mSettings.getBoolean("SolitaireSaveValid", false)) {
      SharedPreferences.Editor editor = getSettings().edit();
      editor.putBoolean("SolitaireSaveValid", false);
      editor.apply();
      // If save is corrupt, just start a new game.
      if (mSolitaireView.loadSave()) {
        helpSplashScreen();
        return;
      }
    }

    mSolitaireView.initGame(mSettings.getInt("LastType", Rules.SOLITAIRE));
    helpSplashScreen();
  }

  /**
   * show the help if this is the first time played
   */
  private void helpSplashScreen() {
    if (!mSettings.getBoolean("PlayedBefore", false)) {
      mSolitaireView.displayHelp();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_solitaire:
        mSolitaireView.initGame(Rules.SOLITAIRE);
        break;
      case R.id.menu_spider:
        mSolitaireView.initGame(Rules.SPIDER);
        break;
      case R.id.menu_freecell:
        mSolitaireView.initGame(Rules.FREECELL);
        break;
      case R.id.menu_fortythieves:
        mSolitaireView.initGame(Rules.FORTYTHIEVES);
        break;
      case R.id.menu_restart:
        mSolitaireView.restartGame();
        break;
      case R.id.menu_deal:
        mSolitaireView.deal();
        break;
      case R.id.menu_stats:
        displayStats();
        break;
      case R.id.menu_options:
        displayOptions();
        break;
      case R.id.menu_help:
        mSolitaireView.displayHelp();
        break;
      case R.id.menu_save_quit:
        mSolitaireView.saveGame();
        mDoSave = false;
        finish();
        break;
      case R.id.menu_quit:
        mDoSave = false;
        finish();
        break;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPause() {
    super.onPause();
    mSolitaireView.onPause();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    super.onStop();
    if (mDoSave) {
      mSolitaireView.saveGame();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    mSolitaireView.onResume();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  /**
   * Display options
   */
  public void displayOptions() {
    mSolitaireView.setTimePassing(false);
    new Options(this, mSolitaireView.getDrawMaster());
  }

  /**
   * Display stats
   */
  public void displayStats() {
    mSolitaireView.setTimePassing(false);
    new Stats(this, mSolitaireView);
  }

  /**
   * Cancel options action from button
   */
  public void cancelOptions() {
    setContentView(mSolitaireView);
    mSolitaireView.requestFocus();
    mSolitaireView.setTimePassing(true);
  }

  /**
   * Start new game from previous game type
   */
  public void newGame() {
    setContentView(mSolitaireView);
    mSolitaireView.initGame(mSettings.getInt("LastType", Rules.SOLITAIRE));
  }

  /**
   * This is called for option changes that require
   * a refresh, but not a new game
   */
  public void refreshOptions() {
    setContentView(mSolitaireView);
    mSolitaireView.refreshOptions();
  }
}
