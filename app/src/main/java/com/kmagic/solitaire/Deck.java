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

import java.util.Random;

/**
 * Deck of Playing Cards
 */
public class Deck {

  private Card[] mCard;
  private int mCardCount;
  private int mTotalCards;

  /**
   * Create a new deck of 4 suit cards
   * @param decks the amount of decks
   */
  public Deck(int decks) {
    init(decks, 4);
  }

  /**
   * Create a new deck with the specified
   * amount of suits of cards
   * @param decks the amount of decks
   * @param suits the amount of suits
   */
  public Deck(int decks, int suits) {
    if (suits == 2) {
      decks *= 2;
    } else if (suits == 1) {
      decks *= 4;
    }
    init(decks, suits);
  }

  /**
   * Initialize one or more decks of cards
   * @param decks the number of decks of 13 cards
   * @param suits the number of suits
   */
  private void init(int decks, int suits) {
    mCardCount = decks * 13 * suits;
    mTotalCards = mCardCount;
    mCard = new Card[mCardCount];
    for (int deck = 0; deck < decks; deck++) {
      for (int suit = Card.CLUBS; suit < suits; suit++) {
        for (int value = 0; value < 13; value++) {
          mCard[deck*suits*13 + suit*Card.KING + value] = new Card(value+1, suit);
        }
      }
    }

    shuffle();
    shuffle();
    shuffle();
  }

  /**
   * Get last card from the deck
   * @return the last card
   */
  public Card popCard() {
    if (mCardCount > 0) {
      return mCard[--mCardCount];
    }
    return null;
  }

  /**
   * Check if the deck is empty or not
   * @return true if empty false if not
   */
  public boolean isEmpty() {
    return mCardCount == 0;
  }

  /**
   * Shuffle cards in deck
   */
  public void shuffle() {
    int lastIdx = mCardCount - 1;
    int swapIdx;
    Card swapCard;
    Random rand = new Random();

    while (lastIdx > 1) {
      swapIdx = rand.nextInt(lastIdx);
      swapCard = mCard[swapIdx];
      mCard[swapIdx] = mCard[lastIdx];
      mCard[lastIdx] = swapCard;
      lastIdx--;
    }
  }
}
