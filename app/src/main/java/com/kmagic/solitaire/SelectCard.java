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

class SelectCard {

  private static final int MAX_CARDS = 13;

  private boolean mValid;
  private int mSelected;
  private Card[] mCard;
  private int mCardCount;
  private CardAnchor mCardAnchor;
  private float mLeftEdge;
  private float mRightEdge;
  private int mHeight;

  /**
   * Create new instances
   */
  public SelectCard() {
    mHeight = 1;
    mCard = new Card[MAX_CARDS];
    clear();
  }

  /**
   * Clear selected cards, empty array
   */
  private void clear() {
    mValid = false;
    mSelected = -1;
    mCardCount = 0;
    mLeftEdge = -1;
    mRightEdge = -1;
    mCardAnchor = null;
    for (int i = 0; i < MAX_CARDS; i++) {
      mCard[i] = null;
    }
  }

  /**
   * Set height (presently has no effect)
   * @param height height to set to
   */
  public void setHeight(final int height) { mHeight = height; }

  /**
   * Get selected card anchor
   * @return selected card anchor
   */
  public CardAnchor getAnchor() { return mCardAnchor; }

  /**
   * Get count of cards not selected
   * @return count of cards not selected
   */
  public int getCount() {
    if (mSelected == -1)
      return mCardCount;
    return mCardCount - mSelected;
  }

  /**
   * Draw selected cards
   * @param drawMaster draw master instance
   * @param canvas canvas to draw on
   */
  public void draw(final DrawMaster drawMaster,
                   final Canvas canvas) {
    drawMaster.drawLightShade(canvas);
    for (int i = 0; i < mCardCount; i++) {
      drawMaster.drawCard(canvas, mCard[i]);
    }
  }

  /**
   * Initials from card anchor
   * @param cardAnchor card anchor instance
   */
  public void initFromAnchor(final CardAnchor cardAnchor) {
    mValid = true;
    mSelected = -1;
    mCardAnchor = cardAnchor;
    Card[] card = cardAnchor.getCardStack();
    for (int i = 0; i < card.length; i++) {
      mCard[i] = card[i];
    }
    mCardCount = card.length;

    int mid = mCardCount / 2;
    if (mCardCount % 2 == 0) {
      mid--;
    }
    float x = mCard[0].getX();
    float y = mCard[mid].getY();
    if (y - mid * (Card.HEIGHT + 5) < 0) {
      mid = 0;
      y = 5;
    }

    for (int i = 0; i < mCardCount; i++) {
      mCard[i].setPosition(x, y + (i - mid) * (Card.HEIGHT + 5));
    }

    mLeftEdge = cardAnchor.getLeftEdge();
    mRightEdge = cardAnchor.getRightEdge();
  }

  /**
   * Select nearby cards
   * @param x x coordinate
   * @param y y coordinate
   * @return true of cards selected, false if not
   */
  public boolean tap(final float x, final float y) {
    float left = mLeftEdge == -1 ? mCard[0].getX() : mLeftEdge;
    float right = mRightEdge == -1 ? mCard[0].getX() + Card.WIDTH : mRightEdge;
    mSelected = -1;
    if (x >= left && x <= right) {
      for (int i = 0; i < mCardCount; i++) {
        if (y >= mCard[i].getY() && y <= mCard[i].getY() + Card.HEIGHT) {
          mSelected = i;
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Release selected cards, and clear
   */
  public void release() {
    if (mValid) {
      mValid = false;
      for (int i = 0; i < mCardCount; i++) {
        mCardAnchor.addCard(mCard[i]);
      }
      clear();
    }
  }

  /**
   * Dump selected cards and clear
   * @return array of cards
   */
  public Card[] dumpCards() {
    Card[] ret = null;
    if (mValid) {
      mValid = false;
      if (mSelected > 0) {
        for (int i = 0; i < mCardCount; i++) {
          if (i < mSelected) {
            mCardAnchor.addCard(mCard[i]);
          } else if (i == mSelected) {
            for (int j = 0; i < mCardCount; i++, j++) {
              mCard[j] = mCard[i];
            }
            break;
          }
        }
      }

      ret = new Card[getCount()];
      for (int i = 0; i < getCount(); i++) {
        ret[i] = mCard[i];
      }
      clear();
    }
    return ret;
  }

  /**
   * Scroll the selected cards to y
   * @param dy destination y coordinate
   */
  public void scroll(final float dy) {
    float x, y;
    for (int i = 0; i < mCardCount; i++) {
      x = mCard[i].getX();
      y = mCard[i].getY() - dy;
      mCard[i].setPosition(x, y);
    }
  }

  /**
   * Is selected card on a card
   * @return true of on another card, false if not
   */
  public boolean isOnCard() { return mSelected != -1; }
}


