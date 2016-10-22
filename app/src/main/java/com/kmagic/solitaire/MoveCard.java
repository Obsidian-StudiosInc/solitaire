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
import android.graphics.PointF;

/**
 * Used to move cards
 */
class MoveCard {

  private static final int MAX_CARDS = 13;

  private boolean mValid;
  private Card[] mCard;
  private int mCardCount;
  private CardAnchor mCardAnchor;
  private PointF mOriginalPoint;

  /**
   * Create a new instance
   */
  public MoveCard() {
    mCard = new Card[MAX_CARDS];
    mOriginalPoint = new PointF(1, 1);
    clear();
  }

  /**
   * Get card anchor
   * @return card anchor
   */
  public CardAnchor getAnchor() { return mCardAnchor; }

  /**
   * Get card count
   * @return count of cards
   */
  public int getCount() { return mCardCount; }

  /**
   * Get the top card
   * @return card on top
   */
  public Card getTopCard() { return mCard[0]; }

  /**
   * Set the card anchor
   * @param anchor card anchor
   */
  public void setAnchor(CardAnchor anchor) {
    mCardAnchor = anchor;
  }

  /**
   * Draw cards
   * @param drawMaster drawmaster instance
   * @param canvas canvas to draw on
   */
  public void draw(final DrawMaster drawMaster,
                   final Canvas canvas) {
    for (int i = 0; i < mCardCount; i++) {
      drawMaster.drawCard(canvas, mCard[i]);
    }
  }

  /**
   * Clear movement of all cards
   */
  private void clear() {
    mValid = false;
    mCardCount = 0;
    mCardAnchor = null;
    for (int i = 0; i < MAX_CARDS; i++) {
      mCard[i] = null;
    }
  }

  /**
   * release movement of all cards, then clear
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
   *
   * @param card
   */
  public void addCard(final Card card) {
    if (mCardCount == 0) {
      mOriginalPoint.set(card.getX(), card.getY());
    }
    mCard[mCardCount++] = card;
    mValid = true;
  }

  /**
   * Move position of cards to destination x,y coordinates
   * @param dx destination x coordinate
   * @param dx destination y coordinate
   */
  public void movePosition(final float dx, final float dy) {
    for (int i = 0; i < mCardCount; i++) {
      mCard[i].movePosition(dx, dy);
    }
  }

  /**
   * Dump cards showing top card
   * @return array of cards
   */
  public Card[] dumpCards() {
    return dumpCards(true);
  }

  /**
   * Dump cards and un-hide top card
   * @param unhide if true un-hides the top card
   * @return array of cards
   */
  public Card[] dumpCards(final boolean unhide) {
    Card[] ret = null;
    if (mValid) {
      mValid = false;
      if (unhide) {
        mCardAnchor.unhideTopCard();
      }
      ret = new Card[mCardCount];
      for (int i = 0; i < mCardCount; i++) {
        ret[i] = mCard[i];
      }
      clear();
    }
    return ret;
  }

  /**
   * Initialize card movement from a select card
   * @param selectCard a select card
   * @param x x coordinate
   * @param y y coordinate
   */
  public void initFromSelectCard(final SelectCard selectCard,
                                 final float x,
                                 final float y) {
    int count = selectCard.getCount();
    mCardAnchor = selectCard.getAnchor();
    Card[] cards = selectCard.dumpCards();

    for (int i = 0; i < count; i++) {
      cards[i].setPosition(x - Card.WIDTH/2, y - Card.HEIGHT/2 + 15*i);
      addCard(cards[i]);
    }
    mValid = true;
  }

  /**
   * Initialize card movement from anchor
   * @param cardAnchor a card anchor
   * @param x x coordinate
   * @param y y coordinate
   */
  public void initFromAnchor(final CardAnchor cardAnchor,
                             final float x,
                             final float y) {
    mCardAnchor = cardAnchor;
    Card[] cards = cardAnchor.getCardStack();

    for (int i = 0; i < cards.length; i++) {
      cards[i].setPosition(x, y + 15*i);
      addCard(cards[i]);
    }
    mValid = true;
  }

  /**
   * Has the card moved?
   * @return true of the card moved, false if not
   */
  public boolean hasMoved() {
    float x = mCard[0].getX();
    float y = mCard[0].getY();

    return !(x >= mOriginalPoint.x - 2 && x <= mOriginalPoint.x + 2 &&
            y >= mOriginalPoint.y - 2 && y <= mOriginalPoint.y + 2);
  }
}

