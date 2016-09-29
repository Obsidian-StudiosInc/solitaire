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

class Card {

  public static final int CLUBS = 0;
  public static final int DIAMONDS = 1;
  public static final int SPADES = 2;
  public static final int HEARTS = 3;

  public static final int ACE = 1;
  public static final int JACK = 11;
  public static final int QUEEN = 12;
  public static final int KING = 13;

  public static int WIDTH = 45;
  public static int HEIGHT = 64;

  private int mValue;
  private int mSuit;
  private float mX;
  private float mY;

    /**
     * Set card size, based on game type and screen dimensions
     *
     * @param type game type
     * @param screenWidth screen width
     * @param screenHeight screen height
     */
    public static void SetSize(int type, int screenWidth, int screenHeight) {
      if (type == Rules.SOLITAIRE) {
          WIDTH = screenHeight/10;
          HEIGHT = screenWidth/10;
      } else if (type == Rules.FREECELL) {
          WIDTH = screenHeight/11;
          HEIGHT = screenWidth/11;
      } else {
          WIDTH = screenHeight/12;
          HEIGHT = screenWidth/12;
      }
    }

  public Card(int value, int suit) {
    mValue = value;
    mSuit = suit;
    mX = 1;
    mY = 1;
  }

  public float GetX() { return mX; }
  public float GetY() { return mY; }
  public int GetValue() { return mValue; }
  public int GetSuit() { return mSuit; }

  public void SetPosition(float x, float y) {
    mX = x;
    mY = y;
  }

  public void MovePosition(float dx, float dy) {
    mX -= dx;
    mY -= dy;
  }
}


