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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.MotionEvent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.Math;
import java.lang.Runnable;
import java.util.Stack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

// The brains of the operation
public class SolitaireView extends View {

  private static final int MODE_NORMAL      = 1;
  private static final int MODE_MOVE_CARD   = 2;
  private static final int MODE_CARD_SELECT = 3;
  private static final int MODE_TEXT        = 4;
  private static final int MODE_ANIMATE     = 5;
  private static final int MODE_WIN         = 6;
  private static final int MODE_WIN_STOP    = 7;

  private static final String SAVE_FILENAME = "solitaire_save.bin";
  // This is incremented only when the save system changes.
  private static final String SAVE_VERSION = "solitaire_save_2";

  private CardAnchor[] mCardAnchor;
  private DrawMaster mDrawMaster;
  private Rules mRules;
  private TextView mTextView;
  private AnimateCard mAnimateCard;

  private MoveCard mMoveCard;
  private SelectCard mSelectCard;
  private int mViewMode;
  private boolean mTextViewDown;

  private PointF mLastPoint;
  private PointF mDownPoint;
  private RefreshHandler mRefreshHandler;
  private Thread mRefreshThread;
  private Stack<Move> mMoveHistory;
  private Replay mReplay;
  private Context mContext;
  private boolean mHasMoved;
  private Speed mSpeed;

  private Card[] mUndoStorage;

  private int mElapsed = 0;
  private long mStartTime;
  private boolean mTimePaused;

  private boolean mGameStarted;
  private boolean mPaused;
  private boolean mDisplayTime;

  private int mWinningScore;

  private boolean mLongKeyPress = false;

  /**
   * Create new instance
   * @param context activity/application context
   * @param attrs attributes from android manifest
   */
  public SolitaireView(final Context context,
                       final AttributeSet attrs) {
    super(context, attrs);
    setFocusable(true);
    setFocusableInTouchMode(true);

    mDrawMaster = new DrawMaster(context);
    mMoveCard = new MoveCard();
    mSelectCard = new SelectCard();
    mViewMode = MODE_NORMAL;
    mLastPoint = new PointF();
    mDownPoint = new PointF();
    mRefreshHandler = new RefreshHandler(this);
    mRefreshThread = new Thread(mRefreshHandler);
    mMoveHistory = new Stack<>();
    mUndoStorage = new Card[CardAnchor.MAX_CARDS];
    mAnimateCard = new AnimateCard(this);
    mSpeed = new Speed();
    mReplay = new Replay(this, mAnimateCard);

    mContext = context;
    mTextViewDown = false;
    mRefreshThread.start();
    mWinningScore = 0;
  }

  /**
   * Start the game
   * @param gameType type of game, default solitaire
   */
  public void initGame(final int gameType) {
    int oldScore = 0;
    String oldGameType = "None";

    // We really really want focus :)
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();

    SharedPreferences.Editor editor = getSettings().edit();
    if (mRules != null) {
      if (mRules.HasScore()) {
        if (mViewMode == MODE_WIN || mViewMode == MODE_WIN_STOP) {
          oldScore = mWinningScore;
        } else {
          oldScore = mRules.GetScore();
        }
        oldGameType = mRules.GetGameTypeString();
        if (oldScore > getSettings().getInt(mRules.GetGameTypeString() + "Score", -52)) {
          editor.putInt(mRules.GetGameTypeString() + "Score", oldScore);
        }
      }
    }
    changeViewMode(MODE_NORMAL);
    mTextView.setVisibility(View.INVISIBLE);
    mMoveHistory.clear();
    mRules = Rules.CreateRules(gameType, null, this, mMoveHistory, mAnimateCard);
    if (oldGameType.equals(mRules.GetGameTypeString())) {
      mRules.SetCarryOverScore(oldScore);
    }
    resize(gameType);
    mDrawMaster.drawCards(getSettings().getBoolean("DisplayBigCards", false));
    mCardAnchor = mRules.GetAnchorArray();
    setDisplayTime(getSettings().getBoolean("DisplayTime", true));
    editor.putInt("LastType", gameType);
    editor.apply();
    mStartTime = SystemClock.uptimeMillis();
    mElapsed = 0;
    mTimePaused = false;
    mPaused = false;
    mGameStarted = false;
  }

  /**
   * Resize game
   * @param gameType game type
   */
  private void resize(final int gameType) {
    if (mDrawMaster.getWidth() > 1) {
      Card.setSize(gameType,mDrawMaster.getWidth(), mDrawMaster.getHeight());
      mRules.Resize(mDrawMaster.getWidth(), mDrawMaster.getHeight());
      refresh();
    }
  }

  /**
   * Get settings from previous run
   * @return shared preferences with previously stored settings
   */
  public SharedPreferences getSettings() {
    return ((Solitaire)mContext).getSettings();
  }

  /**
   * Get draw master
   * @return existing instance of drawmaster
   */
  public DrawMaster getDrawMaster() { return mDrawMaster; }

  /**
   * Get game rules
   * @return existing instance of game rules
   */
  public Rules getRules() { return mRules; }

  /**
   * Clear game started
   */
  public void clearGameStarted() { mGameStarted = false; }

  /**
   * Set display time, turn time elapsed on and off
   * @param displayTime true to display time, false to not
   */
  public void setDisplayTime(final boolean displayTime) {
    mDisplayTime = displayTime;
  }

  /**
   * Set time is passing, start game play time clock, or stop/pause
   * when not playing the game, options, stats, or help
   * @param timePassing true to start counting time, false to stop
   */
  public void setTimePassing(final boolean timePassing) {
    if (timePassing && (mViewMode == MODE_WIN || mViewMode == MODE_WIN_STOP)) {
      return;
    }
    if (timePassing && mTimePaused) {
      mStartTime = SystemClock.uptimeMillis() - mElapsed;
      mTimePaused = false;
    } else if (!timePassing) {
      mTimePaused = true;
    }
  }

  /**
   * Update time, add to time when play game
   */
  public void updateTime() {
    if (!mTimePaused) {
      int elapsed = (int)(SystemClock.uptimeMillis() - mStartTime);
      if (elapsed / 1000 > mElapsed / 1000) {
        refresh();
      }
      mElapsed = elapsed;
    }
  }

  /**
   * Change the view mode between game other views
   * @param newMode view mode to change to
     */
  private void changeViewMode(final int newMode) {
    switch (mViewMode) {
      case MODE_NORMAL:
        if (newMode != MODE_NORMAL) {
          drawBoard();
        }
        break;
      case MODE_MOVE_CARD:
        mMoveCard.release();
        drawBoard();
        break;
      case MODE_CARD_SELECT:
        mSelectCard.release();
        drawBoard();
        break;
      case MODE_TEXT:
        mTextView.setVisibility(View.INVISIBLE);
        break;
      case MODE_ANIMATE:
        mRefreshHandler.setRefresh(RefreshHandler.SINGLE_REFRESH);
        break;
      case MODE_WIN:
      case MODE_WIN_STOP:
        if (newMode != MODE_WIN_STOP) {
          mTextView.setVisibility(View.INVISIBLE);
        }
        drawBoard();
        mReplay.stopPlaying();
        break;
    }
    mViewMode = newMode;
    switch (newMode) {
      case MODE_WIN:
        setTimePassing(false);
      case MODE_MOVE_CARD:
      case MODE_CARD_SELECT:
      case MODE_ANIMATE:
        mRefreshHandler.setRefresh(RefreshHandler.LOCK_REFRESH);
        break;

      case MODE_NORMAL:
      case MODE_TEXT:
      case MODE_WIN_STOP:
        mRefreshHandler.setRefresh(RefreshHandler.SINGLE_REFRESH);
        break;
    }
  }

  /**
   * Handle when the game is paused, called from main activity onPause
   */
  public void onPause() {
    mPaused = true;

    if (mRefreshThread != null) {
      mRefreshHandler.setRunning(false);
      mRules.ClearEvent();
      mRules.SetIgnoreEvents(true);
      mReplay.stopPlaying();
      try {
        mRefreshThread.join(1000);
      } catch (InterruptedException ignored) {
      }
      mRefreshThread = null;
      if (mAnimateCard.isAnimated()) {
        mAnimateCard.cancel();
      }
      if (mViewMode != MODE_WIN && mViewMode != MODE_WIN_STOP) {
        changeViewMode(MODE_NORMAL);
      }

      if (mRules != null && mRules.GetScore() > getSettings().getInt(mRules.GetGameTypeString() + "Score", -52)) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putInt(mRules.GetGameTypeString() + "Score", mRules.GetScore());
        editor.apply();
      }
    }
  }

  /**
   * Save game on quit or activity onStop
   */
  public void saveGame() {
    // This is supposed to have been called but I've seen instances where it wasn't.
    if (mRefreshThread != null) {
      onPause();
    }

    if (mRules != null && mViewMode == MODE_NORMAL) {
      try {

        FileOutputStream fout = mContext.openFileOutput(SAVE_FILENAME, 0);
        ObjectOutputStream oout = new ObjectOutputStream(fout);

        int cardCount = mRules.GetCardCount();
        int[] value = new int[cardCount];
        int[] suit = new int[cardCount];
        int[] anchorCardCount = new int[mCardAnchor.length];
        int[] anchorHiddenCount = new int[mCardAnchor.length];
        int historySize = mMoveHistory.size();
        int[] historyFrom = new int[historySize];
        int[] historyToBegin = new int[historySize];
        int[] historyToEnd = new int[historySize];
        int[] historyCount = new int[historySize];
        int[] historyFlags = new int[historySize];
        Card[] card;

        cardCount = 0;
        for (int i = 0; i < mCardAnchor.length; i++) {
          anchorCardCount[i] = mCardAnchor[i].getCount();
          anchorHiddenCount[i] = mCardAnchor[i].getHiddenCount();
          card = mCardAnchor[i].getCards();
          for (int j = 0; j < anchorCardCount[i]; j++, cardCount++) {
            value[cardCount] = card[j].getValue();
            suit[cardCount] = card[j].getSuit();
          }
        }

        for (int i = 0; i < historySize; i++) {
          Move move = mMoveHistory.pop();
          historyFrom[i] = move.getFrom();
          historyToBegin[i] = move.getToBegin();
          historyToEnd[i] = move.getToEnd();
          historyCount[i] = move.getCount();
          historyFlags[i] = move.getFlags();
        }

        oout.writeObject(SAVE_VERSION);
        oout.writeInt(mCardAnchor.length);
        oout.writeInt(cardCount);
        oout.writeInt(mRules.GetType());
        oout.writeObject(anchorCardCount);
        oout.writeObject(anchorHiddenCount);
        oout.writeObject(value);
        oout.writeObject(suit);
        oout.writeInt(mRules.GetRulesExtra());
        oout.writeInt(mRules.GetScore());
        oout.writeInt(mElapsed);
        oout.writeObject(historyFrom);
        oout.writeObject(historyToBegin);
        oout.writeObject(historyToEnd);
        oout.writeObject(historyCount);
        oout.writeObject(historyFlags);
        oout.close();

        SharedPreferences.Editor editor = getSettings().edit();
        editor.putBoolean("SolitaireSaveValid", true);
        editor.apply();

      } catch (FileNotFoundException e) {
        Log.e("SolitaireView.java", "onStop(): File not found");
      } catch (IOException e) {
        Log.e("SolitaireView.java", "onStop(): IOException");
      }
    }
  }

  /**
   * Load saved game
   * @return true if a saved game was loaded, false if not
   */
  public boolean loadSave() {
    mDrawMaster.drawCards(getSettings().getBoolean("DisplayBigCards", false));
    mTimePaused = true;

    try {
      FileInputStream fin = mContext.openFileInput(SAVE_FILENAME);
      ObjectInputStream oin = new ObjectInputStream(fin);

      String version = (String)oin.readObject();
      if (!version.equals(SAVE_VERSION)) {
        Log.e("SolitaireView.java", "Invalid save version");
        return false;
      }
      Bundle map = new Bundle();

      map.putInt("cardAnchorCount", oin.readInt());
      map.putInt("cardCount", oin.readInt());
      int type = oin.readInt();
      map.putIntArray("anchorCardCount", (int[])oin.readObject());
      map.putIntArray("anchorHiddenCount", (int[])oin.readObject());
      map.putIntArray("value", (int[])oin.readObject());
      map.putIntArray("suit", (int[])oin.readObject());
      map.putInt("rulesExtra", oin.readInt());
      map.putInt("score", oin.readInt());
      mElapsed = oin.readInt();
      mStartTime = SystemClock.uptimeMillis() - mElapsed;
      int[] historyFrom = (int[])oin.readObject();
      int[] historyToBegin = (int[])oin.readObject();
      int[] historyToEnd = (int[])oin.readObject();
      int[] historyCount = (int[])oin.readObject();
      int[] historyFlags = (int[])oin.readObject();
      for (int i = historyFrom.length - 1; i >= 0; i--) {
        mMoveHistory.push(new Move(historyFrom[i], historyToBegin[i], historyToEnd[i],
                                   historyCount[i], historyFlags[i]));
      }

      oin.close();

      mGameStarted = !mMoveHistory.isEmpty();
      mRules = Rules.CreateRules(type, map, this, mMoveHistory, mAnimateCard);
      setDisplayTime(getSettings().getBoolean("DisplayTime", true));
      mCardAnchor = mRules.GetAnchorArray();
      resize(type);
      mTimePaused = false;
      return true;
      
    } catch (FileNotFoundException e) {
      Log.e("SolitaireView.java", "loadSave(): File not found");
    } catch (StreamCorruptedException e) {
      Log.e("SolitaireView.java", "loadSave(): Stream Corrupted");
    } catch (IOException e) {
      Log.e("SolitaireView.java", "loadSave(): IOException");
    } catch (ClassNotFoundException e) {
      Log.e("SolitaireView.java", "loadSave(): Class not found exception");
    }
    mTimePaused = false;
    mPaused = false;
    return false;
  }

  /**
   * Handle when the game is resumed, called from main activity onResume
   */
  public void onResume() {
    mStartTime = SystemClock.uptimeMillis() - mElapsed;
    mRefreshHandler.setRunning(true);
    mRefreshThread = new Thread(mRefreshHandler);
    mRefreshThread.start();
    mRules.SetIgnoreEvents(false);
    mPaused = false;
  }

  public void refresh() {
    mRefreshHandler.singleRefresh();
  }

  /**
   * Set the text view
   * @param textView text view
   */
  public void setTextView(final TextView textView) {
    mTextView = textView;
  }

  /**
   * Handle when the game size changes, screen size changes
   * called from main activity onSizeChanged
   * {@inheritDoc}
   */
  @Override
  protected void onSizeChanged(final int w,
                               final int h,
                               final int oldw,
                               final int oldh) {
    mDrawMaster.setScreenSize(w, h);
    mRules.Resize(w, h);
    mSelectCard.setHeight(h);
  }

  /**
   * Display game help
   */
  public void displayHelp() {
    mTextView.setTextColor(Color.WHITE);
    mTextView.setTextSize(15);
    mTextView.setGravity(Gravity.START);
    displayText(mContext.getResources().getText(R.string.help_text));
  }

  /**
   * Display window
   */
  public void displayWin() {
    markWin();
    mTextView.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.font_size));
    mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
    displayText(mContext.getResources().getText(R.string.win_text));
    changeViewMode(MODE_WIN);
    mTextView.setVisibility(View.VISIBLE);
    mRules.SetIgnoreEvents(true);
    mReplay.startReplay(mMoveHistory, mCardAnchor);
  }

  /**
   * Restart game
   */
  public void restartGame() {
    mRules.SetIgnoreEvents(true);
    while (!mMoveHistory.empty()) {
      undo();
    }
    mRules.SetIgnoreEvents(false);
    refresh();
  }

  /**
   * Display text, used by help
   */
  public void displayText(final CharSequence text) {
    changeViewMode(MODE_TEXT);
    mTextView.setVisibility(View.VISIBLE);
    mTextView.setText(text);
    ViewGroup parentView = (ViewGroup) getParent();
    parentView.removeView(mTextView);
    parentView.addView(mTextView);
    refresh();
  }

  /**
   * Draw game board
   */
  public void drawBoard() {
      Canvas boardCanvas = mDrawMaster.getBoardCanvas();
      mDrawMaster.drawBackground(boardCanvas);
      for (CardAnchor ca : mCardAnchor) {
          ca.Draw(mDrawMaster, boardCanvas);
      }
  }

  /**
   * Deal a hand
   */
  public void deal() {
    if (mViewMode == MODE_TEXT) {
      changeViewMode(MODE_NORMAL);
    } else if (mViewMode == MODE_NORMAL) {
      mRules.EventAlert(Rules.EVENT_DEAL, mCardAnchor[0]);
      refresh();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onDraw(final Canvas canvas) {

    // Only draw the stagnant stuff if it may have changed
    if (mViewMode == MODE_NORMAL) {
      // sanityCheck is for debug use only.
      // sanityCheck();
      drawBoard();
    }
    mDrawMaster.drawLastBoard(canvas);
    if (mDisplayTime) {
      mDrawMaster.drawTime(canvas, mElapsed);
    }
    if (mRules.HasString()) {
      mDrawMaster.drawScore(canvas, mRules.GetString());
    }

    switch (mViewMode) {
      case MODE_MOVE_CARD:
        mMoveCard.draw(mDrawMaster, canvas);
        break;
      case MODE_CARD_SELECT:
        mSelectCard.draw(mDrawMaster, canvas);
        break;
      case MODE_WIN:
        if (mReplay.isPlaying()) {
          mAnimateCard.draw(mDrawMaster, canvas);
        }
      case MODE_WIN_STOP:
      case MODE_TEXT:
        mDrawMaster.drawShade(canvas);
        break;
      case MODE_ANIMATE:
        mAnimateCard.draw(mDrawMaster, canvas);
    }

    mRules.HandleEvents();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(final int keyCode,
                           final KeyEvent msg) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_DPAD_CENTER:
    case KeyEvent.KEYCODE_SEARCH:
      deal();
      return true;
    }
    mRules.HandleEvents();
    return super.onKeyDown(keyCode, msg);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyLongPress(final int keyCode,
                                final KeyEvent msg) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        mLongKeyPress = true;
        return true;
      default:
        return super.onKeyLongPress(keyCode, msg);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyUp(final int keyCode,
                         final KeyEvent msg) {
      switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
              if(mLongKeyPress) {
                ((Activity) mContext).openOptionsMenu();
                mLongKeyPress = false;
              } else {
                undo();
              }
            return true;
        default:
          return super.onKeyUp(keyCode, msg);
      }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean performClick() {
    super.performClick();
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    boolean ret = false;

    // Yes you can get touch events while in the "paused" state.
    if (mPaused) {
      return false;
    }

    // Text mode only handles clicks
    if (mViewMode == MODE_TEXT) {
      if (event.getAction() == MotionEvent.ACTION_UP && mTextViewDown) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("SolitairePreferences", 0).edit();
        editor.putBoolean("PlayedBefore", true);
        editor.apply();
        mTextViewDown = false;
        changeViewMode(MODE_NORMAL);
      } if (event.getAction() == MotionEvent.ACTION_DOWN) {
        mTextViewDown = true;
      }
      performClick();
      return true;
    }

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mHasMoved = false;
        mSpeed.reset();
        ret = onDown(event.getX(), event.getY());
        mDownPoint.set(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        ret = onRelease(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_MOVE:
        if (!mHasMoved) {
          checkMoved(event.getX(), event.getY());
        }
        ret = onMove(mLastPoint.x - event.getX(), mLastPoint.y - event.getY(),
                     event.getX(), event.getY());
        break;
    }
    mLastPoint.set(event.getX(), event.getY());

    if (!mGameStarted && !mMoveHistory.empty()) {
      mGameStarted = true;
      markAttempt();
    }

    mRules.HandleEvents();
    return ret;
  }

  /**
   * Handle release of a movement
   * @param x x coordinate
   * @param y y coordinate
   * @return true on release, false when not
   */
  private boolean onRelease(final float x, final float y) {
    switch (mViewMode) {
      case MODE_NORMAL:
        if (!mHasMoved) {
            for (CardAnchor ca : mCardAnchor) {
                if (ca.expandStack(x, y)) {
                    mSelectCard.initFromAnchor(ca);
                    changeViewMode(MODE_CARD_SELECT);
                    return true;
                } else if (ca.tapCard(x, y)) {
                    refresh();
                    return true;
                }
            }
        }
        break;
      case MODE_MOVE_CARD:
        for (int close = 0; close < 2; close++) {
          CardAnchor prevAnchor = mMoveCard.getAnchor();
          boolean unhide = (prevAnchor.getVisibleCount() == 0 &&
                            prevAnchor.getCount() > 0);
          int count = mMoveCard.getCount();

          for (int i = 0; i < mCardAnchor.length; i++) {
            if (mCardAnchor[i] != prevAnchor) {
              if (mCardAnchor[i].canDropCard(mMoveCard, close)) {
                mMoveHistory.push(new Move(prevAnchor.getNumber(), i, count, false, unhide));
                mCardAnchor[i].addMoveCard(mMoveCard);
                if (mViewMode == MODE_MOVE_CARD) {
                  changeViewMode(MODE_NORMAL);
                }
                return true;
              }
            }
          }
        }
        if (!mMoveCard.hasMoved()) {
          CardAnchor anchor = mMoveCard.getAnchor();
          mMoveCard.release();
          if (anchor.expandStack(x, y)) {
            mSelectCard.initFromAnchor(anchor);
            changeViewMode(MODE_CARD_SELECT);
          } else {
            changeViewMode(MODE_NORMAL);
          }
        } else if (mSpeed.isFast() && mMoveCard.getCount() == 1) {
          if (!mRules.Fling(mMoveCard)) {
            changeViewMode(MODE_NORMAL);
          }
        } else {
          mMoveCard.release();
          changeViewMode(MODE_NORMAL);
        }
        return true;
      case MODE_CARD_SELECT:
        if (!mSelectCard.isOnCard() && !mHasMoved) {
          mSelectCard.release();
          changeViewMode(MODE_NORMAL);
          return true;
        }
        break;
    }

    return false;
  }

  /**
   * Handle on down event
   * @param x x coordinate
   * @param y y coordinate
   * @return true on down, false when not
   */
  public boolean onDown(final float x, final float y) {
    switch (mViewMode) {
      case MODE_NORMAL:
        Card card = null;
          for (CardAnchor ca : mCardAnchor) {
              card = ca.grabCard(x, y);
              if (card != null) {
                  if (y < card.getY() + Card.HEIGHT / 4) {
                      boolean lastIgnore = mRules.GetIgnoreEvents();
                      mRules.SetIgnoreEvents(true);
                      ca.addCard(card);
                      mRules.SetIgnoreEvents(lastIgnore);
                      if (ca.expandStack(x, y)) {
                          mMoveCard.initFromAnchor(ca, x - Card.WIDTH / 2, y - Card.HEIGHT / 2);
                          changeViewMode(MODE_MOVE_CARD);
                          break;
                      }
                      card = ca.popCard();
                  }
                  mMoveCard.setAnchor(ca);
                  mMoveCard.addCard(card);
                  changeViewMode(MODE_MOVE_CARD);
                  break;
              }
          }
        break;
      case MODE_CARD_SELECT:
        mSelectCard.tap(x, y);
        break;
    }
    return true;
  }

  /**
   * Handle on move
   * @param dx destination x coordinate
   * @param dy destination y coordinate
   * @param x x coordinate
   * @param y y coordinate
   * @return true on movement, false when no movement
   */
  public boolean onMove(final float dx,
                        final float dy,
                        final float x,
                        final float y) {
    mSpeed.addSpeed(dx, dy);
    switch (mViewMode) {
      case MODE_NORMAL:
        if (Math.abs(mDownPoint.x - x) > 15 || Math.abs(mDownPoint.y - y) > 15) {
            for (CardAnchor ca : mCardAnchor) {
                if (ca.canMoveStack(mDownPoint.x, mDownPoint.y)) {
                    mMoveCard.initFromAnchor(ca, x - Card.WIDTH / 2, y - Card.HEIGHT / 2);
                    changeViewMode(MODE_MOVE_CARD);
                    return true;
                }
            }
        }
        break;
      case MODE_MOVE_CARD:
        mMoveCard.movePosition(dx, dy);
        return true;
      case MODE_CARD_SELECT:
        if (mSelectCard.isOnCard() && Math.abs(mDownPoint.x - x) > 30) {
          mMoveCard.initFromSelectCard(mSelectCard, x, y);
          changeViewMode(MODE_MOVE_CARD);
        } else {
          mSelectCard.scroll(dy);
          if (!mSelectCard.isOnCard()) {
            mSelectCard.tap(x, y);
          }
        }
        return true;
    }

    return false;
  }

  /**
   * Check if there has been movement of cards
   * @param x x coordinate
   * @param y y coordinate
   * @return true if moved, false if not
   */
  private void checkMoved(final float x, final float y) {
    if (x >= mDownPoint.x - 30 && x <= mDownPoint.x + 30 &&
        y >= mDownPoint.y - 30 && y <= mDownPoint.y + 30) {
      mHasMoved = false;
    } else {
      mHasMoved = true;
    }
  }

  /**
   * Start animation
   */
  public void startAnimating() {
    drawBoard();
    if (mViewMode != MODE_WIN && mViewMode != MODE_ANIMATE) {
      changeViewMode(MODE_ANIMATE);
    }
  }

  /**
   * Stop animation
   */
  public void stopAnimating() {
    if (mViewMode == MODE_ANIMATE) {
      changeViewMode(MODE_NORMAL);
    } else if (mViewMode == MODE_WIN) {
      changeViewMode(MODE_WIN_STOP);
    }
  }

  /**
   * Undo card movement
   */
  public void undo() {
    if (mViewMode != MODE_NORMAL && mViewMode != MODE_WIN) {
      return;
    }
    boolean oldIgnore = mRules.GetIgnoreEvents();
    mRules.SetIgnoreEvents(true);

    mMoveCard.release();
    mSelectCard.release();

    if (!mMoveHistory.empty()) {
      Move move = mMoveHistory.pop();
      int count = 0;
      int from = move.getFrom();
      if (move.getToBegin() != move.getToEnd()) {
        for (int i = move.getToBegin(); i <= move.getToEnd(); i++) {
          for (int j = 0; j < move.getCount(); j++) {
            mUndoStorage[count++] = mCardAnchor[i].popCard();
          }
        }
      } else {
        for (int i = 0; i < move.getCount(); i++) {
          mUndoStorage[count++] = mCardAnchor[move.getToBegin()].popCard();
        }
      }
      if (move.getUnhide()) {
        mCardAnchor[from].setHiddenCount(mCardAnchor[from].getHiddenCount() + 1);
      }
      if (move.getInvert()) {
        for (int i = 0; i < count; i++) {
          mCardAnchor[from].addCard(mUndoStorage[i]);
        }
      } else {
        for (int i = count-1; i >= 0; i--) {
          mCardAnchor[from].addCard(mUndoStorage[i]);
        }
      }
      if (move.getAddDealCount()) {
        mRules.AddDealCount();
      }
      if (mUndoStorage[0].getValue() == 1) {
        for (int i = 0; i < mCardAnchor[from].getCount(); i++) {
          Card card = mCardAnchor[from].getCards()[i];
        }
      }
      refresh();
    }
    mRules.SetIgnoreEvents(oldIgnore);
  }

  /**
   * Mark an attempt, record a game play attempt
   */
  private void markAttempt() {
    String gameAttemptString = mRules.GetGameTypeString() + "Attempts";
    int attempts = getSettings().getInt(gameAttemptString, 0);
    SharedPreferences.Editor editor = getSettings().edit();
    editor.putInt(gameAttemptString, attempts + 1);
    editor.apply();
  }

  /**
   * Mark a win, record a game win
   */
  private void markWin() {
    String gameWinString = mRules.GetGameTypeString() + "Wins";
    String gameTimeString = mRules.GetGameTypeString() + "Time";
    int wins = getSettings().getInt(gameWinString, 0);
    int bestTime = getSettings().getInt(gameTimeString, -1);
    SharedPreferences.Editor editor = getSettings().edit();

    if (bestTime == -1 || mElapsed < bestTime) {
      editor.putInt(gameTimeString, mElapsed);
    }

    editor.putInt(gameWinString, wins + 1);
    editor.apply();
    if (mRules.HasScore()) {
      mWinningScore = mRules.GetScore();
      if (mWinningScore > getSettings().getInt(mRules.GetGameTypeString() + "Score", -52)) {
        editor.putInt(mRules.GetGameTypeString() + "Score", mWinningScore);
      }
    }
  }

  /**
   * Simple function to check for a consistent state in Solitaire
   */
  private void sanityCheck() {
    int cardCount;
    int matchCount;
    String type = mRules.GetGameTypeString();
      switch (type) {
          case "Spider1Suit":
              cardCount = 13;
              matchCount = 8;
              break;
          case "Spider2Suit":
              cardCount = 26;
              matchCount = 4;
              break;
          case "Spider4Suit":
              cardCount = 52;
              matchCount = 2;
              break;
          case "Forty Thieves":
              cardCount = 52;
              matchCount = 2;
              break;
          default:
              cardCount = 52;
              matchCount = 1;
              break;
      }
    
    int[] cards = new int[cardCount];
    for (int i = 0; i < cardCount; i++) {
      cards[i] = 0;
    }
      for (CardAnchor ca : mCardAnchor) {
          for (int j = 0; j < ca.getCount(); j++) {
              Card card = ca.getCards()[j];
              int idx = card.getSuit() * 13 + card.getValue() - 1;
              if (cards[idx] >= matchCount) {
                  mTextView.setTextSize(20);
                  mTextView.setGravity(Gravity.CENTER);
                  displayText("Sanity Check Failed\nExtra: " + card.getValue() + " " + card.getSuit());
                  return;
              }
              cards[idx]++;
          }
      }
    for (int i = 0; i < cardCount; i++) {
      if (cards[i] != matchCount) {
        mTextView.setTextSize(20);
        mTextView.setGravity(Gravity.CENTER);
        displayText("Sanity Check Failed\nMissing: " + (i %13 + 1) + " " + i / 13);
        return;
      }
    }
  }

  /**
   * This is called for option changes that require
   * a refresh, but not a new game type
   */
  public void refreshOptions() {
    mRules.RefreshOptions();
    setDisplayTime(getSettings().getBoolean("DisplayTime", true));
  }
}

class RefreshHandler implements Runnable {
  public static final int NO_REFRESH = 1;
  public static final int SINGLE_REFRESH = 2;
  public static final int LOCK_REFRESH = 3;

  private static final int FPS = 30;

  private boolean mRun;
  private int mRefresh;
  private SolitaireView mView;

  public RefreshHandler(final SolitaireView solitaireView) {
    mView = solitaireView;
    mRun = true;
    mRefresh = NO_REFRESH;
  }

  public void setRefresh(final int refresh) {
    synchronized (this) {
      mRefresh = refresh;
    }
  }

  public void singleRefresh() {
    synchronized (this) {
      if (mRefresh == NO_REFRESH) {
        mRefresh = SINGLE_REFRESH;
      }
    }
  }

  public void setRunning(final boolean run) {
    mRun = run;
  }

  public void run() {
    while (mRun) {
      try {
        Thread.sleep(1000 / FPS);
      } catch (InterruptedException ignored) {
      }
      mView.updateTime();
      if (mRefresh != NO_REFRESH) {
        mView.postInvalidate();
        if (mRefresh == SINGLE_REFRESH) {
          setRefresh(NO_REFRESH);
        }
      }
    }
  }
}

class Speed {
  private static final int SPEED_COUNT = 4;
  private static final float SPEED_THRESHOLD = 10*10;
  private float[] mSpeed;
  private int mIdx;

  public Speed() {
    mSpeed = new float[SPEED_COUNT];
    reset();
  }
  public void reset() {
    mIdx = 0;
    for (int i = 0; i < SPEED_COUNT; i++) {
      mSpeed[i] = 0;
    }
  }
  public void addSpeed(final float dx, final float dy) {
    mSpeed[mIdx] = dx*dx + dy*dy;
    mIdx = (mIdx + 1) % SPEED_COUNT;
  }
  public boolean isFast() {
    for (int i = 0; i < SPEED_COUNT; i++) {
      if (mSpeed[i] > SPEED_THRESHOLD) {
        return true;
      }
    }
    return false;
  }
}

