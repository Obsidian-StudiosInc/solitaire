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

/**
 * Used to track movement
 */
public class Move {
  private int mFrom;
  private int mToBegin;
  private int mToEnd;
  private int mCount;
  private int mFlags;

  private static final int FLAGS_INVERT = 0x0001;
  private static final int FLAGS_UNHIDE = 0x0002;
  private static final int FLAGS_ADD_DEAL_COUNT = 0x0004;

  /**
   * Create a new move instance
   * @param from move from
   * @param toBegin move to begin
   * @param toEnd move to end
   * @param count movement count
   * @param invert invert card
   * @param unhide un-hide card
   */
  public Move(final int from,
              final int toBegin,
              final int toEnd,
              final int count,
              final boolean invert,
              final boolean unhide) {
    mFrom = from;
    mToBegin = toBegin;
    mToEnd = toEnd;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
  }

  /**
   * Create a new move instance
   * @param from move from
   * @param to move to
   * @param count movement count
   * @param invert invert card
   * @param unhide un-hide card
   */
  public Move(final int from,
              final int to,
              final int count,
              final boolean invert,
              final boolean unhide) {
    mFrom = from;
    mToBegin = to;
    mToEnd = to;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
  }

  /**
   * Create a new move instance
   * @param from move from
   * @param to move to
   * @param count movement count
   * @param invert invert card
   * @param unhide un-hide card
   * @param addDealCount add to deal count
   */
  public Move(final int from,
              final int to,
              final int count,
              final boolean invert,
              final boolean unhide,
              final boolean addDealCount) {
    mFrom = from;
    mToBegin = to;
    mToEnd = to;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
    if (addDealCount)
      mFlags |= FLAGS_ADD_DEAL_COUNT;
  }

  /**
   * Create a new move instance
   * @param from move from
   * @param toBegin move to begin
   * @param toEnd move to end
   * @param count movement count
   * @param flags movement flags
   */
  public Move(final int from,
              final int toBegin,
              final int toEnd,
              final int count,
              final int flags) {
    mFrom = from;
    mToBegin = toBegin;
    mToEnd = toEnd;
    mCount = count;
    mFlags = flags;
  }

  /**
   * Get movement from
   * @return current movement from
   */
  public int getFrom() { return mFrom; }

  /**
   * Get movement begin
   * @return current movement begin
   */
  public int getToBegin() { return mToBegin; }

  /**
   * Get movement end
   * @return current movement end
   */
  public int getToEnd() { return mToEnd; }

  /**
   * Get movement count
   * @return current movement count
   */
  public int getCount() { return mCount; }

  /**
   * Get movement flags
   * @return current movement flags
   */
  public int getFlags() { return mFlags; }

  /**
   * Get if card be inverted as part of movement
   * @return true if card should be inverted, false if not
   */
  public boolean getInvert() { return (mFlags & FLAGS_INVERT) != 0; }

  /**
   * Get if card be un-hidden as part of movement
   * @return true if card should be revealed, false if not
   */
  public boolean getUnhide() { return (mFlags & FLAGS_UNHIDE) != 0; }

  /**
   * Get if card movement be added to the deal count
   * @return true if card should be added to deal count, false if not
   */
  public boolean getAddDealCount() {
    return (mFlags & FLAGS_ADD_DEAL_COUNT) != 0;
  }
}
