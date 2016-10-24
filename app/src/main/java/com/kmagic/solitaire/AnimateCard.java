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

import android.graphics.Canvas;
import java.lang.Runnable;
import java.lang.Math;

/**
 * AnimateCard handles card animations, moving cards around
 */
public class AnimateCard {

  // Animation Speed, pixels per frame
  private static final float PPF = 40;

  protected SolitaireView mView;
  private Card[] mCard;
  private CardAnchor mCardAnchor;
  private int mCount;
  private int mFrames;
  private float mDx;
  private float mDy;
  private boolean mAnimate;
  private Runnable mCallback;

  public AnimateCard(SolitaireView view) {
    mView = view;
    mAnimate = false;
    mCard = new Card[104];
    mCallback = null;
  }

  /**
   * Return animation status
   * @return true if animated, false if not
   */
  public boolean isAnimated() { return mAnimate; }

  /**
   * Draw one or more card(s), actual animation
   * @param drawMaster DrawMaster instance to use
   * @param canvas canvas to draw on, passed to DrawMaster
   */
  public void draw(DrawMaster drawMaster, Canvas canvas) {
    if (mAnimate) {
      for (int j = 0; j < mCount; j++) {
        mCard[j].movePosition(-mDx, -mDy);
      }
      for (int i = 0; i < mCount; i++) {
        drawMaster.drawCard(canvas, mCard[i]);
      }
      mFrames--;
      if (mFrames <= 0) {
        mAnimate = false;
        finish();
      }
    }
  }

  /**
   * Move cards to a given anchor's location
   * @param cards the cards to move
   * @param anchor the card anchor to move the card to
   * @param count the count/number of cards to move
   * @param callback callback to alert calling class
   */
  public void moveCards(Card[] cards,
                        CardAnchor anchor,
                        int count,
                        Runnable callback) {
    float x = anchor.getX();
    float y = anchor.getNewY();
    mCardAnchor = anchor;
    mCallback = callback;
    mAnimate = true;

    for (int i = 0; i < count; i++) {
      mCard[i] = cards[i];
    }
    mCount = count;
    move(mCard[0], x, y);
  }

  /**
   * Move card to a given anchor's location
   * @param card the card to move
   * @param anchor the card anchor to move the card to
   */
  public void moveCard(Card card, CardAnchor anchor) {
    float x = anchor.getX();
    float y = anchor.getNewY();
    mCardAnchor = anchor;
    mCallback = null;
    mAnimate = true;

    mCard[0] = card;
    mCount = 1;
    move(card, x, y);
  }

  /**
   * Move a card
   * @param card the card to move
   * @param x the x coordinate to move to
   * @param y the y coordinate to move to
   */
  private void move(Card card, float x, float y) {
    float dx = x - card.getX();
    float dy = y - card.getY();

    mFrames = Math.round((float)Math.sqrt(dx * dx + dy * dy) / PPF);
    if (mFrames == 0) {
      mFrames = 1;
    }
    mDx = dx / mFrames;
    mDy = dy / mFrames;

    mView.startAnimating();
    if (!mAnimate) {
      finish();
    }
  }

  /**
   * Finish animation
   */
  private void finish() {
    for (int i = 0; i < mCount; i++) {
      mCardAnchor.addCard(mCard[i]);
      mCard[i] = null;
    }
    mCardAnchor = null;
    mView.drawBoard();
    if (mCallback != null) {
      mCallback.run();
    }
  }

  /**
   * Cancel animation
   */
  public void cancel() {
    if (mAnimate) {
      for (int i = 0; i < mCount; i++) {
        mCardAnchor.addCard(mCard[i]);
        mCard[i] = null;
      }
      mCardAnchor = null;
      mAnimate = false;
    }
  }
}
