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


class CardAnchor {

  public static final int MAX_CARDS = 104;
  public static final int SEQ_SINK = 1;
  public static final int SUIT_SEQ_STACK = 2;
  public static final int DEAL_FROM = 3;
  public static final int DEAL_TO = 4;
  public static final int SPIDER_STACK = 5;
  public static final int FREECELL_STACK = 6;
  public static final int FREECELL_HOLD = 7;
  public static final int GENERIC_ANCHOR = 8;

  private int mNumber;
  protected Rules mRules;
  protected float mX;
  protected float mY;
  protected Card[] mCard;
  protected int mCardCount;
  protected int mHiddenCount;
  protected float mLeftEdge;
  protected float mRightEdge;
  protected float mBottom;
  protected boolean mDone;

  //Variables for GenericAnchor
  protected int mSTARTSEQ;
  protected int mBUILDSEQ;
  protected int mMOVESEQ;
  protected int mBUILDSUIT;
  protected int mMOVESUIT;
  protected boolean mBUILDWRAP;
  protected boolean mMOVEWRAP;
  protected int mDROPOFF;
  protected int mPICKUP;
  protected int mDISPLAY; 
  protected int mHACK;
  
  // ==========================================================================
  // Create a CardAnchor
  // -------------------
  public static CardAnchor CreateAnchor(int type, int number, Rules rules) {
    CardAnchor ret = null;
    switch (type) {
      case SEQ_SINK:
        ret = new SeqSink();
        break;
      case SUIT_SEQ_STACK:
        ret = new SuitSeqStack();
        break;
      case DEAL_FROM:
        ret = new DealFrom();
        break;
      case DEAL_TO:
        ret = new DealTo();
        break;
      case SPIDER_STACK:
        ret = new SpiderStack();
        break;
      case FREECELL_STACK:
        ret = new FreecellStack();
        break;
      case FREECELL_HOLD:
        ret = new FreecellHold();
        break;
      case GENERIC_ANCHOR:
        ret = new GenericAnchor();
        break;
    }
    ret.setRules(rules);
    ret.setNumber(number);
    return ret;
  }

  public CardAnchor() {
    mX = 1;
    mY = 1;
    mCard = new Card[MAX_CARDS];
    mCardCount = 0;
    mHiddenCount = 0;
    mLeftEdge = -1;
    mRightEdge = -1;
    mBottom = -1;
    mNumber = -1;
    mDone = false;
  }

  // ==========================================================================
  // Getters and Setters
  // -------------------
  public Card[] getCards() { return mCard; }
  public int getCount() { return mCardCount; }
  public int getHiddenCount() { return mHiddenCount; }
  public float getLeftEdge() { return mLeftEdge; }
  public int getNumber() { return mNumber; }
  public float getRightEdge() { return mRightEdge; }
  public int getVisibleCount() { return mCardCount - mHiddenCount; }
  public int getMovableCount() { return mCardCount > 0 ? 1 : 0; }
  public float getX() { return mX; }
  public float getNewY() { return mY; }

  public void setBottom(float edge) { mBottom = edge; }
  public void setHiddenCount(int count) { mHiddenCount = count; }
  public void setLeftEdge(float edge) { mLeftEdge = edge; }
  public void setMaxHeight(int maxHeight) { }
  public void setNumber(int number) { mNumber = number; }
  public void setRightEdge(float edge) { mRightEdge = edge; }
  public void setRules(Rules rules) { mRules = rules; }
  public void setShowing(int showing) {  }
  protected void setCardPosition(int idx) { mCard[idx].setPosition(mX, mY); }
  public void setDone(boolean done) { mDone = done; }

  //Methods for GenericAnchor
  public void setStartSeq(int seq){ mSTARTSEQ = seq; }
  public void setBuildSeq(int buildseq){ mBUILDSEQ = buildseq;  }
  public void setMoveSeq(int moveseq){ mMOVESEQ = moveseq;  }
  
  public void setWrap(boolean wrap){ mBUILDWRAP = wrap; mMOVEWRAP = wrap; }
  public void setBuildWrap(boolean buildwrap){ mBUILDWRAP = buildwrap;  }
  
  public void setSuit(int suit){ mBUILDSUIT = suit; mMOVESUIT = suit; }
  public void setBuildSuit(int buildsuit){ mBUILDSUIT = buildsuit;  }
  public void setMoveSuit(int movesuit){ mMOVESUIT = movesuit;  }
  
  public void setBehavior(int beh){ mDROPOFF = beh; mPICKUP = beh; }
  public void setDropoff(int dropoff){ mDROPOFF = dropoff;  }
  public void setPickup(int pickup){ mPICKUP = pickup;  }
  
  public void setDisplay(int display){ mDISPLAY = display;  }
  
  public void setHack(int hack){ mHACK = hack; }
  //End Methods for Generic Anchor  
  
  public void setPosition(float x, float y) {
    mX = x;
    mY = y;
    for (int i = 0; i < mCardCount; i++) {
      setCardPosition(i);
    }
  }

  // ==========================================================================
  // Functions to add cards
  // ----------------------
  public void addCard(Card card) {
    mCard[mCardCount++] = card;
    setCardPosition(mCardCount - 1);
  }

  public void addMoveCard(MoveCard moveCard) {
    int count = moveCard.GetCount();
    Card[] cards = moveCard.DumpCards();

    for (int i = 0; i < count; i++) {
      addCard(cards[i]);
    }
  }

  public boolean dropSingleCard(Card card) { return false; }
  public boolean canDropCard(MoveCard moveCard, int close) { return false; }

  // ==========================================================================
  // Functions to take cards
  // -----------------------
  public Card[] getCardStack() { return null; }

  public Card grabCard(float x, float y) {
    Card ret = null;
    if (mCardCount > 0 && isOverCard(x, y)) {
      ret = popCard();
    }
    return ret;
  }

  public Card popCard() {
    Card ret = mCard[--mCardCount];
    mCard[mCardCount] = null;
    return ret;
  }

  // ==========================================================================
  // Functions to interact with cards
  // --------------------------------
  public boolean tapCard(float x, float y) { return false; }

  public boolean unhideTopCard() {
    if (mCardCount  > 0 && mHiddenCount > 0 && mHiddenCount == mCardCount) {
      mHiddenCount--;
      return true;
    }
    return false;
  }
  public boolean expandStack(float x, float y) { return false; }
  public boolean canMoveStack(float x, float y) { return false; }


  // ==========================================================================
  // Functions to check locations
  // ----------------------------
  private boolean isOver(float x, float y, boolean deck, int close) {
    float clx = mCardCount == 0 ? mX : mCard[mCardCount - 1].getX();
    float leftX = mLeftEdge == -1 ? clx : mLeftEdge;
    float rightX = mRightEdge == -1 ? clx + Card.WIDTH : mRightEdge;
    float topY = (mCardCount == 0 || deck) ? mY : mCard[mCardCount-1].getY();
    float botY = mCardCount > 0 ? mCard[mCardCount - 1].getY() : mY;
    botY += Card.HEIGHT;

    leftX -= close*Card.WIDTH/2;
    rightX += close*Card.WIDTH/2;
    topY -= close*Card.HEIGHT/2;
    botY += close*Card.HEIGHT/2;
    if (mBottom != -1 && botY + 10 >= mBottom)
      botY = mBottom;

    return (x >= leftX && x <= rightX && y >= topY && y <= botY);
  }

  protected boolean isOverCard(float x, float y) {
    return isOver(x, y, false, 0);
  }
  protected boolean isOverCard(float x, float y, int close) {
    return isOver(x, y, false, close);
  }

  protected boolean isOverDeck(float x, float y) {
    return isOver(x, y, true, 0);
  }

  // ==========================================================================
  // Functions to Draw
  // ----------------------------
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.drawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.drawCard(canvas, mCard[mCardCount-1]);
    }
  }
}

// Straight up default
class DealTo extends CardAnchor {
  private int mShowing;
  public DealTo() {
    super();
    mShowing = 1;
  }

  @Override
  public void setShowing(int showing) { mShowing = showing; }

  @Override
  protected void setCardPosition(int idx) {
    if (mShowing == 1) {
      mCard[idx].setPosition(mX, mY);
    } else {
      if (idx < mCardCount - mShowing) {
        mCard[idx].setPosition(mX, mY);
      } else {
        int offset = mCardCount - mShowing;
        offset = offset < 0 ? 0 : offset;
        mCard[idx].setPosition(mX + (idx - offset) * Card.WIDTH/2, mY);
      }
    }
  }

  @Override
  public void addCard(Card card) {
    super.addCard(card);
    setPosition(mX, mY);
  }

  @Override
  public boolean unhideTopCard() {
    setPosition(mX, mY);
    return false;
  }

  @Override
  public Card popCard() {
    Card ret = super.popCard();
    setPosition(mX, mY);
    return ret;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.drawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      for (int i = mCardCount - mShowing; i < mCardCount; i++) {
        if (i >= 0) {
          drawMaster.drawCard(canvas, mCard[i]);
        }
      }
    }
  }
}

// Abstract stack anchor
class SeqStack extends CardAnchor {
  protected static final int SMALL_SPACING = 7;
  protected static final int HIDDEN_SPACING = 3;

  protected int mSpacing;
  protected boolean mHideHidden;
  protected int mMaxHeight;

  public SeqStack() {
    super();
    mSpacing = getMaxSpacing();
    mHideHidden = false;
    mMaxHeight = Card.HEIGHT;
  }

  @Override
  public void setMaxHeight(int maxHeight) {
    mMaxHeight = maxHeight;
    checkSizing();
    setPosition(mX, mY);
  }

  // This can't be a constant as Card.HEIGHT isn't constant.
  protected int getMaxSpacing() {
    return Card.HEIGHT/3;
  }

  @Override
  protected void setCardPosition(int idx) {
    if (idx < mHiddenCount) {
      if (mHideHidden) {
        mCard[idx].setPosition(mX, mY);
      } else {
        mCard[idx].setPosition(mX, mY + HIDDEN_SPACING * idx);
      }
    } else {
      int startY = mHideHidden ? HIDDEN_SPACING : mHiddenCount * HIDDEN_SPACING;
      int y = (int)mY + startY + (idx - mHiddenCount) * mSpacing;
      mCard[idx].setPosition(mX, y);
    }
  }

  @Override
  public void setHiddenCount(int count) {
    super.setHiddenCount(count);
    checkSizing();
    setPosition(mX, mY);
  }

  @Override
  public void addCard(Card card) {
    super.addCard(card);
    checkSizing();
  }

  @Override
  public Card popCard() {
    Card ret = super.popCard();
    checkSizing();
    return ret;
  }

  @Override
  public boolean expandStack(float x, float y) {
    if (isOverDeck(x, y)) {
      if (mHiddenCount >= mCardCount) {
        mHiddenCount = mCardCount == 0 ? 0 : mCardCount - 1;
      } else if (mCardCount - mHiddenCount > 1) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int getMovableCount() { return getVisibleCount(); }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.drawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      for (int i = 0; i < mCardCount; i++) {
        if (i < mHiddenCount) {
          drawMaster.drawHiddenCard(canvas, mCard[i]);
        } else {
          drawMaster.drawCard(canvas, mCard[i]);
        }
      }
    }
  }

  private void checkSizing() {
    if (mCardCount < 2 || mCardCount - mHiddenCount < 2) {
      mSpacing = getMaxSpacing();
      mHideHidden = false;
      return;
    }
    int max = mMaxHeight;
    int hidden = mHiddenCount;
    int showing = mCardCount - hidden;
    int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
    int spacing = spaceLeft / (showing - 1);

    if (spacing < SMALL_SPACING && hidden > 1) {
      mHideHidden = true;
      spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
      spacing = spaceLeft / (showing - 1);
    } else {
      mHideHidden = false;
      if (spacing > getMaxSpacing()) {
        spacing = getMaxSpacing();
      }
    }
    if (spacing != mSpacing) {
      mSpacing = spacing;
      setPosition(mX, mY);
    }
  }

  public float getNewY() {
    if (mCardCount == 0) {
      return mY;
    }
    return mCard[mCardCount-1].getY() + mSpacing;
  }
}

// Anchor where cards to deal come from
class DealFrom extends CardAnchor {

  @Override
  public Card grabCard(float x, float y) { return null; }

  @Override
  public boolean tapCard(float x, float y) {
    if (isOverCard(x, y)) {
      mRules.EventAlert(Rules.EVENT_DEAL, this);
      return true;
    }
    return false;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.drawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.drawHiddenCard(canvas, mCard[mCardCount-1]);
    }
  }
}

// Anchor that holds increasing same suited cards
class SeqSink extends CardAnchor {

  @Override
  public void addCard(Card card) {
    super.addCard(card);
    mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
  }

  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {
    Card card = moveCard.GetTopCard();
    float x = card.getX() + Card.WIDTH/2;
    float y = card.getY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
//    float my = mCardCount > 0 ? topCard.getY() : mY;

    if (isOverCard(x, y, close)) {
      if (moveCard.GetCount() == 1) {
        if ((topCard == null && card.getValue() == 1) ||
            (topCard != null && card.getSuit() == topCard.getSuit() &&
             card.getValue() == topCard.getValue() + 1)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean dropSingleCard(Card card) {
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    return (topCard == null && card.getValue() == 1) ||
            (topCard != null && card.getSuit() == topCard.getSuit() &&
                    card.getValue() == topCard.getValue() + 1);
  }
}

// Regular color alternating solitaire stack
class SuitSeqStack extends SeqStack {

  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.getX() + Card.WIDTH/2;
    float y = card.getY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.getY() : mY;

    if (isOverCard(x, y, close)) {
      if (topCard == null) {
        if (card.getValue() == Card.KING) {
          return true;
        }
      } else if ((card.getSuit()&1) != (topCard.getSuit()&1) &&
                 card.getValue() == topCard.getValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Card[] getCardStack() {
    int visibleCount = getVisibleCount();
    Card[] ret = new Card[visibleCount];

    for (int i = visibleCount-1; i >= 0; i--) {
      ret[i] = popCard();
    }
    return ret;
  }
  
  @Override
  public boolean canMoveStack(float x, float y) { return super.expandStack(x, y); }
}

// Spider solitaire style stack
class SpiderStack extends SeqStack {

  @Override
  public void addCard(Card card) {
    super.addCard(card);
    mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
  }

  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.getX() + Card.WIDTH/2;
    float y = card.getY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
//    float my = mCardCount > 0 ? topCard.getY() : mY;

    if (isOverCard(x, y, close)) {
      if (topCard == null || card.getValue() == topCard.getValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int getMovableCount() {
    if (mCardCount < 2)
      return mCardCount;

    int retCount = 1;
    int suit = mCard[mCardCount-1].getSuit();
    int val = mCard[mCardCount-1].getValue();

    for (int i = mCardCount-2; i >= mHiddenCount; i--, retCount++, val++) {
      if (mCard[i].getSuit() != suit || mCard[i].getValue() != val + 1) {
        break;
      }
    }

    return retCount;
  }

  @Override
  public Card[] getCardStack() {
    int retCount = getMovableCount();

    Card[] ret = new Card[retCount];

    for (int i = retCount-1; i >= 0; i--) {
      ret[i] = popCard();
    }

    return ret;
  }

  @Override
  public boolean expandStack(float x, float y) {
    if (super.expandStack(x, y)) {
      Card bottom = mCard[mCardCount-1];
      Card second = mCard[mCardCount-2];
      if (bottom.getSuit() == second.getSuit() &&
          bottom.getValue() == second.getValue() - 1) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canMoveStack(float x, float y) {
    if (super.expandStack(x, y)) {
      float maxY = mCard[mCardCount- getMovableCount()].getY();

      if (y >= maxY - Card.HEIGHT/2) {
        return true;
      }
    }
    return false;
  }

}

// Freecell stack
class FreecellStack extends SeqStack {

  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.getX() + Card.WIDTH/2;
    float y = card.getY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
//    float my = mCardCount > 0 ? topCard.getY() : mY;

    if (isOverCard(x, y, close)) {
      if (topCard == null) {
        if (mRules.CountFreeSpaces() >= moveCard.GetCount()) {
          return true;
        }
      } else if ((card.getSuit()&1) != (topCard.getSuit()&1) &&
                 card.getValue() == topCard.getValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int getMovableCount() {
    if (mCardCount < 2)
      return mCardCount;

    int retCount = 1;
    int maxMoveCount = mRules.CountFreeSpaces() + 1;

    for (int i = mCardCount - 2; i >= 0 && retCount < maxMoveCount; i--, retCount++) {
      if ((mCard[i].getSuit()&1) == (mCard[i+1].getSuit()&1) ||
          mCard[i].getValue() != mCard[i+1].getValue() + 1) {
        break;
      }
    }

    return retCount;
  }

  @Override
  public Card[] getCardStack() {
    int retCount = getMovableCount();
    Card[] ret = new Card[retCount];

    for (int i = retCount-1; i >= 0; i--) {
      ret[i] = popCard();
    }
    return ret;
  }

  @Override
  public boolean expandStack(float x, float y) {
    if (super.expandStack(x, y)) {
      if (mRules.CountFreeSpaces() > 0) {
        Card bottom = mCard[mCardCount-1];
        Card second = mCard[mCardCount-2];
        if ((bottom.getSuit()&1) != (second.getSuit()&1) &&
            bottom.getValue() == second.getValue() - 1) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canMoveStack(float x, float y) {
    if (super.expandStack(x, y)) {
      float maxY = mCard[mCardCount- getMovableCount()].getY();
      if (y >= maxY - Card.HEIGHT/2) {
        return true;
      }
    }
    return false;
  }
}

// Freecell holding pen
class FreecellHold extends CardAnchor {

  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {
    Card card = moveCard.GetTopCard();
    return mCardCount == 0 && moveCard.GetCount() == 1 &&
            isOverCard(card.getX() + Card.WIDTH / 2, card.getY() + Card.HEIGHT / 2, close);
  }

}

// New Abstract
class GenericAnchor extends CardAnchor {

  //Sequence start values
  public static final int START_ANY=1; // An empty stack can take any card.
  public static final int START_KING=2; // An empty stack can take only a king.

  //Value Sequences
  public static final int SEQ_ANY=1; //You can build as you like
  public static final int SEQ_SEQ=2;  //Building only allows sequential
  public static final int SEQ_ASC=3;  //Ascending only
  public static final int SEQ_DSC=4;  //Descending only
    
  //Suit Sequences that limits how adding cards to the stack works
  public static final int SUIT_ANY=1;  //Build doesn't care about suite
  public static final int SUIT_RB=2;  //Must alternate Red & Black
  public static final int SUIT_OTHER=3;//As long as different
  public static final int SUIT_COLOR=4;//As long as same color
  public static final int SUIT_SAME=5; //As long as same suit
    
  //Pickup & Dropoff Behavior
  public static final int PACK_NONE=1;  // Interaction in this mode not allowed
  public static final int PACK_ONE=2;  //Can only accept 1 card
  public static final int PACK_MULTI=3;  //Can accept multiple cards
  public static final int PACK_FIXED=4;  //Don't think this will ever be used
  public static final int PACK_LIMIT_BY_FREE=5; //For freecell style movement
    
  //Anchor Display (Hidden vs. Shown faces)
  public static final int DISPLAY_ALL=1;  //All cards are shown
  public static final int DISPLAY_HIDE=2; //All cards are hidden
  public static final int DISPLAY_MIX=3;  //Uses a mixture
  public static final int DISPLAY_ONE=4;  //Displays one only

  //Hack to fix Spider Dealing
  public static final int DEALHACK=1;
    
  protected static final int SMALL_SPACING = 7;
  protected static final int HIDDEN_SPACING = 3;

  protected int mSpacing;
  protected boolean mHideHidden;
  protected int mMaxHeight;
  
  public GenericAnchor(){
    super();
    setStartSeq(GenericAnchor.SEQ_ANY);
    setBuildSeq(GenericAnchor.SEQ_ANY);
    setBuildWrap(false);
    setBuildSuit(GenericAnchor.SUIT_ANY);
    setDropoff(GenericAnchor.PACK_NONE);
    setPickup(GenericAnchor.PACK_NONE);
    setDisplay(GenericAnchor.DISPLAY_ALL);
    mSpacing = getMaxSpacing();
    mHideHidden = false;
    mMaxHeight = Card.HEIGHT;
  }

  @Override
  public void setMaxHeight(int maxHeight) {
    mMaxHeight = maxHeight;
    checkSizing();
    setPosition(mX, mY);
  }

  @Override
  protected void setCardPosition(int idx) {
    if (idx < mHiddenCount) {
      if (mHideHidden) {
        mCard[idx].setPosition(mX, mY);
      } else {
        mCard[idx].setPosition(mX, mY + HIDDEN_SPACING * idx);
      }
    } else {
      int startY = mHideHidden ? HIDDEN_SPACING : mHiddenCount * HIDDEN_SPACING;
      int y = (int)mY + startY + (idx - mHiddenCount) * mSpacing;
      mCard[idx].setPosition(mX, y);
    }
  }

  @Override
  public void setHiddenCount(int count) {
    super.setHiddenCount(count);
    checkSizing();
    setPosition(mX, mY);
  }
  
  @Override
  public void addCard(Card card) {
    super.addCard(card);
    checkSizing();
    if (mHACK == GenericAnchor.DEALHACK){
      mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
    }
  }

  @Override
  public Card popCard() {
    Card ret = super.popCard();
    checkSizing();
    return ret;
  }
  
  @Override
  public boolean canDropCard(MoveCard moveCard, int close) {
    if (mDROPOFF == GenericAnchor.PACK_NONE){
      return false;
    }
    
    Card card = moveCard.GetTopCard();
    float x = card.getX() + Card.WIDTH/2;
    float y = card.getY() + Card.HEIGHT/2;
    //Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    //float my = mCardCount > 0 ? topCard.getY() : mY;
    if (isOverCard(x, y, close)) {
      return canBuildCard(card);
    }
    return false;
  }
  
  public boolean canBuildCard(Card card){
    // SEQ_ANY will allow all
    if (mBUILDSEQ == GenericAnchor.SEQ_ANY){
      return true;
    }
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    // Rules for empty stacks
    if (topCard == null) {
      switch (mSTARTSEQ) {
        case GenericAnchor.START_KING:
          return card.getValue() == Card.KING;
        case GenericAnchor.START_ANY:
        default:
          return true;
      }
    }
    int value = card.getValue();
    int suit = card.getSuit();
    int tvalue = topCard.getValue();
    int tsuit = topCard.getSuit();
    // Fail if sequence is wrong
    switch (mBUILDSEQ){
    //WRAP_NOWRAP=1; //Building stacks do not wrap
    //WRAP_WRAP=2;   //Building stacks wraps around
      case GenericAnchor.SEQ_ASC:
        if (value - tvalue != 1){
          return false;
        }
        break;
      case GenericAnchor.SEQ_DSC:
        if (tvalue - value != 1){
          return false;
        }
        break;
      case GenericAnchor.SEQ_SEQ:
        if (Math.abs(tvalue - value) != 1){
          return false;
        }
        break;
    }
    // Fail if suit is wrong
    switch (mBUILDSUIT){
      case GenericAnchor.SUIT_RB:
        if (Math.abs(tsuit - suit)%2 == 0){  return false;  }
        break;
      case GenericAnchor.SUIT_OTHER:
        if (tsuit == suit){  return false;  }
        break;
      case GenericAnchor.SUIT_COLOR:
        if (Math.abs(tsuit - suit) != 2){  return false;  }
        break;
      case GenericAnchor.SUIT_SAME:
        if (tsuit != suit){  return false;  }
        break;
    }
    // Passes all rules
    return true;
  }
  
  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.drawEmptyAnchor(canvas, mX, mY, mDone);
      return;
    }
    switch (mDISPLAY){
      case GenericAnchor.DISPLAY_ALL:
        for (int i = 0; i < mCardCount; i++) {
          drawMaster.drawCard(canvas, mCard[i]);
        }
        break;
      case GenericAnchor.DISPLAY_HIDE:
        for (int i = 0; i < mCardCount; i++) {
          drawMaster.drawHiddenCard(canvas, mCard[i]);
        }
        break;
      case GenericAnchor.DISPLAY_MIX:
        for (int i = 0; i < mCardCount; i++) {
          if (i < mHiddenCount) {
            drawMaster.drawHiddenCard(canvas, mCard[i]);
          } else {
            drawMaster.drawCard(canvas, mCard[i]);
          }
        }
        break;
      case GenericAnchor.DISPLAY_ONE:
        for (int i = 0; i < mCardCount; i++) {
          if (i < mCardCount-1) {
            drawMaster.drawHiddenCard(canvas, mCard[i]);
          } else {
            drawMaster.drawCard(canvas, mCard[i]);
          }
        }
        break;
    }
  }
  
  @Override
  public boolean expandStack(float x, float y) {
    if (isOverDeck(x, y)) {
      return (getMovableCount() > 0);
    }
    return false;
  }
  
  @Override
  public boolean canMoveStack(float x, float y) { return expandStack(x, y); }

  @Override
  public Card[] getCardStack() {
    int movableCount = getMovableCount();
    Card[] ret = new Card[movableCount];
    for (int i = movableCount-1; i >= 0; i--) {
      ret[i] = popCard();
    }
    return ret;
  }

  @Override
  public int getMovableCount() {
    int visibleCount = getVisibleCount();
    if (visibleCount == 0 || mPICKUP == GenericAnchor.PACK_NONE){
      return 0;
    }
    int seq_allowed = 1;
    if (visibleCount > 1){
      int i = mCardCount-1;
      boolean g;
      boolean h;      
      do {
        g = true;
        h = true;
        switch (mMOVESEQ){
          case GenericAnchor.SEQ_ANY:
            h = true;
            break;
          case GenericAnchor.SEQ_ASC:
            h = this.isSeqAsc(i-1, i, mMOVEWRAP);
            break;
          case GenericAnchor.SEQ_DSC:
            h = this.isSeqAsc(i, i-1, mMOVEWRAP);
            break;
          case GenericAnchor.SEQ_SEQ:
            h = (this.isSeqAsc(i, i-1, mMOVEWRAP) ||
                this.isSeqAsc(i-1, i, mMOVEWRAP));
            break;
        }
        if (!h){
          g = false;
        }
        switch (mMOVESUIT){
          case GenericAnchor.SUIT_ANY:
            h = true;
            break;
          case GenericAnchor.SUIT_COLOR:
            h = !this.isSuitRb(i-1,i);
            break;
          case GenericAnchor.SUIT_OTHER:
            h = this.isSuitOther(i-1, i);
            break;
          case GenericAnchor.SUIT_RB:
            h = this.isSuitRb(i-1, i);
            break;
          case GenericAnchor.SUIT_SAME:
            h = this.isSuitSame(i-1, i);
            break;
        }
        if (!h){
          g = false;
        }
        if (g){  seq_allowed++;  }
        i--;
      }while(g && (mCardCount - i) < visibleCount);
    }
    
    switch (mPICKUP){
      case GenericAnchor.PACK_NONE:
        return 0;
      case GenericAnchor.PACK_ONE:
        seq_allowed = Math.min(1, seq_allowed);
        break;
      case GenericAnchor.PACK_MULTI:
        break;
      case GenericAnchor.PACK_FIXED:
        //seq_allowed = Math.min( xmin, seq_allowed);
        break;
      case GenericAnchor.PACK_LIMIT_BY_FREE:
        seq_allowed = Math.min(mRules.CountFreeSpaces()+1, seq_allowed);
        break;
    }
    return seq_allowed;
  }
  
  public boolean isSeqAsc(int p1, int p2, boolean wrap){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int v1 = c1.getValue();
    int v2 = c2.getValue();
    
    if (v2 + 1 == v1){
      return true;
    }
    if (wrap){
      if (v2 == Card.KING && v1 == Card.ACE){
        return true;
      }
    }
    return false;
  }
  public boolean isSuitRb(int p1, int p2){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int s1 = c1.getSuit();
    int s2 = c2.getSuit();
    if (  (s1 == Card.CLUBS || s1 == Card.SPADES) &&
          (s2 == Card.HEARTS || s2 == Card.DIAMONDS)  ){
      return true;
    }
    return (s1 == Card.HEARTS || s1 == Card.DIAMONDS) &&
            (s2 == Card.CLUBS || s2 == Card.SPADES);
  }
  public boolean isSuitSame(int p1, int p2){
    return (mCard[p1].getSuit() == mCard[p2].getSuit());
  }
  public boolean isSuitOther(int p1, int p2){
    return (mCard[p1].getSuit() != mCard[p2].getSuit());
  }  

  private void checkSizing() {
    if (mCardCount < 2 || mCardCount - mHiddenCount < 2) {
      mSpacing = getMaxSpacing();
      mHideHidden = false;
      return;
    }
    int max = mMaxHeight;
    int hidden = mHiddenCount;
    int showing = mCardCount - hidden;
    int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
    int spacing = spaceLeft / (showing - 1);

    if (spacing < SMALL_SPACING && hidden > 1) {
      mHideHidden = true;
      spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
      spacing = spaceLeft / (showing - 1);
    } else {
      mHideHidden = false;
      if (spacing > getMaxSpacing()) {
        spacing = getMaxSpacing();
      }
    }
    if (spacing != mSpacing) {
      mSpacing = spacing;
      setPosition(mX, mY);
    }
  }
  // This can't be a constant as Card.HEIGHT isn't constant.
  protected int getMaxSpacing() {
    return Card.HEIGHT/3;
  }

  public float getNewY() {
    if (mCardCount == 0) {
      return mY;
    }
    return mCard[mCardCount-1].getY() + mSpacing;
  }
}
