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

import android.util.Log;
import java.util.Stack;

/**
 * Handles reply of game play
 */
public class Replay implements Runnable {
  private Stack<Move> mMoveStack;
  private SolitaireView mView;
  private AnimateCard mAnimateCard;
  private CardAnchor[] mCardAnchor;
  private boolean mIsPlaying;

  private Card[] mSinkCard;
  private CardAnchor mSinkFrom;
  private boolean mSinkUnhide;

  /**
   * Create replace instance
   * @param view parent view
   * @param animateCard true if cards should be animated, false if not
   */
  public Replay(final SolitaireView view,
                final AnimateCard animateCard) {
    mView = view;
    mAnimateCard = animateCard;
    mIsPlaying = false;
    mMoveStack = new Stack<>();
    mSinkCard = new Card[104];
  }

  /**
   * Is replay playing
   * @return true if replay is playing, false if not
   */
  public boolean isPlaying() { return mIsPlaying; }

  /**
   * Stop playing replay
   */
  public void stopPlaying() { mIsPlaying = false; }

  /**
   * Start replay
   * @param history history of card movements
   * @param anchor card anchors
   */
  public void startReplay(final Stack<Move> history,
                          final CardAnchor[] anchor) {
    mCardAnchor = anchor;
    mMoveStack.clear();
    while (!history.empty()) {
      Move move = history.peek();
      if (move.getToBegin() != move.getToEnd()) {
        for (int i = move.getToEnd(); i >= move.getToBegin(); i--) {
          mMoveStack.push(new Move(move.getFrom(), i, 1, false, false));
        }
      } else {
        mMoveStack.push(move);
      }
      mView.undo();
    }
    mView.drawBoard();
    mIsPlaying = true;
    playNext();
  }

  /**
   * Play next replay
   */
  public void playNext() {
    int mSinkCount;
    CardAnchor mSinkAnchor;

    if (!mIsPlaying || mMoveStack.empty()) {
      mIsPlaying = false;
      mView.stopAnimating();
      return;
    }
    Move move = mMoveStack.pop();

    if (move.getToBegin() == move.getToEnd()) {
      mSinkCount = move.getCount();
      mSinkAnchor = mCardAnchor[move.getToBegin()];
      mSinkUnhide = move.getUnhide();
      mSinkFrom = mCardAnchor[move.getFrom()];

      if (move.getInvert()) {
        for (int i = 0; i < mSinkCount; i++) {
          mSinkCard[i] = mSinkFrom.popCard();
        }
      } else {
        for (int i = mSinkCount-1; i >= 0; i--) {
          mSinkCard[i] = mSinkFrom.popCard();
        }
      }
      mAnimateCard.moveCards(mSinkCard, mSinkAnchor, mSinkCount, this);
    } else {
      Log.e("Replay.java", "Invalid move encountered, aborting.");
      mIsPlaying = false;
    }
  }

  /**
   * Run the replay
   */
  public void run() {
    if (mIsPlaying) {
      if (mSinkUnhide) {
        mSinkFrom.unhideTopCard();
      }
      playNext();
    }
  }
}
