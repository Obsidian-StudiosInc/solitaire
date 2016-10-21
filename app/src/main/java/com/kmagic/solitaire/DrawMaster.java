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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Display;
import android.view.WindowManager;

/**
 * Handles all drawing for the game
 * Backgrounds, cards, suits, etc
 */
public class DrawMaster {

  private Context mContext;
  private Resources mResources;

  // Background
  private int mScreenWidth;
  private int mScreenHeight;
  private Paint mBGPaint;

  // Card stuff
  private final Paint mSuitPaint = new Paint();
  private Bitmap[] mCardBitmap;
  private Bitmap mCardHidden;

  private Paint mEmptyAnchorPaint;
  private Paint mDoneEmptyAnchorPaint;
  private Paint mShadePaint;
  private Paint mLightShadePaint;
  
  private Paint mTimePaint;
  private int mLastSeconds;
  private String mTimeString;

  private Bitmap mBoardBitmap;
  private Canvas mBoardCanvas;

  private int mFontSize;

  /**
   * Create a new instance of DrawMaster
   * @param context
   */
  public DrawMaster(final Context context) {

    mContext = context;
    mResources = mContext.getResources();
    Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    mScreenWidth = (size.x > size.y) ? size.x : size.y;
    mScreenHeight = (size.x < size.y) ? size.x : size.y;
    if(mScreenWidth<=0)
      mScreenWidth = 480;
    if(mScreenHeight<=0)
      mScreenHeight = 295;

    // Background
    mBGPaint = new Paint();
    mBGPaint.setARGB(255, 0, 128, 0);

    mShadePaint = new Paint();
    mShadePaint.setARGB(200, 0, 0, 0);

    mLightShadePaint = new Paint();
    mLightShadePaint.setARGB(100, 0, 0, 0);

    // Card related stuff
    mEmptyAnchorPaint = new Paint();
    mEmptyAnchorPaint.setARGB(255, 0, 64, 0);
    mDoneEmptyAnchorPaint = new Paint();
    mDoneEmptyAnchorPaint.setARGB(128, 255, 0, 0);

    mFontSize = mResources.getDimensionPixelSize(R.dimen.font_size);

    mTimePaint = getTextPaint(mFontSize,Paint.Align.RIGHT);
    mLastSeconds = -1;

    mCardBitmap = new Bitmap[52];
    drawCards(false);
    mBoardBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  /**
   * Get the screen's width
   * @return the screen width
   */
  public int getWidth() { return mScreenWidth; }

  /**
   * Get the screen's height
   * @return the screen height
   */
  public int getHeight() { return mScreenHeight; }

  /**
   * Get the playing board canvas
   * @return board canvas
   */
  public Canvas getBoardCanvas() { return mBoardCanvas; }

  /**
   * Draw a card
   * @param canvas canvas to draw on
   * @param card card to draw
   */
  public void drawCard(final Canvas canvas, final Card card) {
    float x = card.getX();
    float y = card.getY();
    int idx = card.getSuit()*13+(card.getValue()-1);
    canvas.drawBitmap(mCardBitmap[idx], x, y, mSuitPaint);
  }

  /**
   * Draw a hidden card
   * @param canvas canvas to draw on
   * @param card hidden card to draw
   */
  public void drawHiddenCard(final Canvas canvas, final Card card) {
    float x = card.getX();
    float y = card.getY();
    canvas.drawBitmap(mCardHidden, x, y, mSuitPaint);
  }

  /**
   * Draw an empty anchor
   * @param canvas canvas to draw on
   * @param x x coordinate of the anchor
   * @param y y coordinate of the anchor
   * @param done anchor done with any movement
   */
  public void drawEmptyAnchor(final Canvas canvas,
                              final float x,
                              final float y,
                              final boolean done) {
    RectF pos = new RectF(x, y, x + Card.WIDTH, y + Card.HEIGHT);
    if (!done) {
      canvas.drawRoundRect(pos, 4, 4, mEmptyAnchorPaint);
    } else {
      canvas.drawRoundRect(pos, 4, 4, mDoneEmptyAnchorPaint);
    }
  }

  /**
   * Draw game background, green board
   * @param canvas canvas to draw on
   */
  public void drawBackground(final Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mBGPaint);
  }

  /**
   * Draw shade background
   * @param canvas canvas to draw on
   */
  public void drawShade(final Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mShadePaint);
  }

  /**
   * Draw light shade background
   * @param canvas canvas to draw on
   */
  public void drawLightShade(final Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mLightShadePaint);
  }

  public void drawLastBoard(final Canvas canvas) {
    canvas.drawBitmap(mBoardBitmap, 0, 0, mSuitPaint);
  }

  /**
   * Set the game screen size
   * @param width width of the screen
   * @param height height of the screen
   */
  public void setScreenSize(final int width, final int height) {
    mScreenWidth = width;
    mScreenHeight = height;
    mBoardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  /**
   * Get a base paint object with anti alias
   * @return paint object
   */
  public static Paint getPaint() {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    return(paint);
  }

  /**
   * Get red paint for general use
   * @return black paint object
   */
  public static Paint getBlackPaint() {
    final Paint paint = getPaint();
    paint.setARGB(255, 0, 0, 0);
    return(paint);
  }

  /**
   * Get red paint for general use
   * @return red paint object
   */
  public static Paint getRedPaint() {
    final Paint paint = getPaint();
    paint.setARGB(255, 255, 0, 0);
    return(paint);
  }

  /**
   * Get paint for text use
   * @return red paint object
   */
  public static Paint getTextPaint(final float fontSize,
                                   final Paint.Align align) {
    Paint paint = getPaint();
    paint.setTextSize(fontSize);
    paint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
                                      Typeface.BOLD));
    paint.setTextAlign(align);
    return(paint);
  }

  /**
   * Draw a pedestal ( clubs and spades stand on this )
   * @param canvas canvas to draw on
   * @param width width of the pedestal
   * @param height height of the pedestal
   */
  public void drawPedestal(final Canvas canvas,
                           final float width,
                           final float height) {
    final Paint paint = getBlackPaint();
    final float width_half = width/2;
    final float width_fifth = width/5;
    final float width_3_5ths = width_fifth*3;
    final float height_5th = height/5;
    canvas.drawRect(width_3_5ths,height_5th,width-width_3_5ths,height,paint);
    final Path path = new Path();
    path.moveTo(width_fifth,height);
    path.lineTo(width_half,height-height_5th);
    path.lineTo(width-width_fifth,height);
    path.lineTo(width_fifth,height);
    path.close();
    canvas.drawPath(path,paint);
  }

  /**
   * Draw a club
   * @param canvas canvas to draw on
   * @param width width of the club
   * @param height height of the club
   */
  public void drawClub(final Canvas canvas,
                       final float width,
                       final float height) {
    final Paint paint = getBlackPaint();
    final float width_half = width/2;
    final float width_fifth = width/5;
    final float height_5th = height/5;
    canvas.drawCircle(width_half,height_5th,width_fifth,paint);
    canvas.drawCircle(width_fifth,height/2,width_fifth,paint);
    canvas.drawCircle(width-width_fifth,height/2,width_fifth,paint);
    drawPedestal(canvas,width,height);
  }

  /**
   * Draw a diamond
   * @param canvas canvas to draw on
   * @param width width of the diamond
   * @param height height of the diamond
   */
  public void drawDiamond(final Canvas canvas,
                          final float width,
                          final float height) {
    final Paint paint = getRedPaint();
    final Path path = new Path();
    path.moveTo(width/2,0);
    final float offset = height/5;
    path.lineTo(offset,height/2);
    path.lineTo(width/2,height);
    path.lineTo(width-offset,height/2);
    path.lineTo(width/2,0);
    path.close();
    canvas.drawPath(path,paint);
  }

  /**
   * Draw a heart
   * @param canvas canvas to draw on
   * @param width width of the heart
   * @param height height of the heart
   */
  public void drawHeart(final Canvas canvas,
                        final float width,
                        final float height) {
    final Paint paint = getRedPaint();
    final float width_half = width/2;
    final float width_quarter = width/4;
    final float height_quarter = height/4;
    final float height_third = height/3;
    canvas.drawCircle(width_quarter,height_quarter,width_quarter,paint);
    canvas.drawCircle(width_quarter*3,height_quarter,width_quarter,paint);
    final Path path = new Path();
    path.moveTo(0,height_third);
    path.lineTo(width_half,height);
    path.lineTo(width,height_third);
    path.lineTo(0,height_third);
    path.close();
    canvas.drawPath(path,paint);
  }

  /**
   * Draw a spade
   * @param canvas canvas to draw on
   * @param width width of the spade
   * @param height height of the spade
   */
  public void drawSpade(final Canvas canvas,
                        final float width,
                        final float height) {
    final Paint paint = getBlackPaint();
    final float width_half = width/2;
    final float width_quarter = width/4;
    final float width_fifth = width/5;
    final float width_25th = width/25;
    final float height_3_5th = height/5*3;
    final Path path = new Path();
    path.moveTo(width_half,0);
    path.lineTo(width-width_25th,height_3_5th);
    path.lineTo(width_25th,height_3_5th);
    path.lineTo(width_half,0);
    path.close();
    canvas.drawPath(path,paint);
    canvas.drawCircle(width_quarter,height_3_5th,width_fifth,paint);
    canvas.drawCircle(width_quarter*3,height_3_5th,width_fifth,paint);
    drawPedestal(canvas,width,height);
  }

  /**
   * Draw a card suit, wrapper method for the various suit types
   * @param suit suit to draw
   * @param canvas canvas to draw on
   * @param size size of the suit to draw
   */
  public void drawSuit(final int suit,
                       final Canvas canvas,
                       final float size) {
    if(suit==0) {
      drawClub(canvas, size, size);
    } else if(suit==1) {
      drawDiamond(canvas, size, size);
    } else if(suit==2) {
      drawSpade(canvas, size, size);
    } else if(suit==3) {
      drawHeart(canvas, size, size);
    }
  }

  /**
   * Draw regular cards with suits representing values on card
   * @param r application resources reference
   * @param size the size multiplier with regular being 0.5
   */
  private void drawBigCards(final Resources r,
                            final float size) {

    final Bitmap[] bigSuit = new Bitmap[4];
    final Bitmap[] suit = new Bitmap[4];
    Canvas canvas;
    final String[] card_values = mResources.getStringArray(R.array.card_values);
    final Paint cardFrontPaint = new Paint();
    final Paint cardBorderPaint = new Paint();
    final Paint textPaintLeft = getTextPaint(mFontSize*size,Paint.Align.LEFT);
    float textSize = textPaintLeft.getTextSize();

    Drawable drawable = ResourcesCompat.getDrawable(r, R.drawable.cardback, null);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_8888);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    final float smallSuit = textSize/2;
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap((int)smallSuit, (int)smallSuit, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(suit[i]);
      drawSuit(i,canvas,smallSuit);
    }

    final float largeSuit = textSize;
    for (int i = 0; i < 4; i++) {
      bigSuit[i] = Bitmap.createBitmap((int)largeSuit, (int)largeSuit, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(bigSuit[i]);
      drawSuit(i,canvas,largeSuit);
    }

    cardBorderPaint.setARGB(255, 0, 0, 0);
    cardFrontPaint.setARGB(255, 255, 255, 255);
    RectF pos = new RectF();
    for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
      for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
        mCardBitmap[suitIdx*13+valueIdx] = Bitmap.createBitmap(
                Card.WIDTH, Card.HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mCardBitmap[suitIdx*13+valueIdx]);
        pos.set(0, 0, Card.WIDTH, Card.HEIGHT);
        canvas.drawRoundRect(pos, 4, 4, cardBorderPaint);
        pos.set(1, 1, Card.WIDTH-1, Card.HEIGHT-1);
        canvas.drawRoundRect(pos, 4, 4, cardFrontPaint);

        if ((suitIdx & 1) == 1) {
          textPaintLeft.setARGB(255, 255, 0, 0);
        } else {
          textPaintLeft.setARGB(255, 0, 0, 0);
        }
        // Top
        canvas.drawText(card_values[valueIdx], 3, textSize, textPaintLeft);
        canvas.drawBitmap(suit[suitIdx], Card.WIDTH-smallSuit-4, 4, mSuitPaint);
        // Middle
        canvas.drawBitmap(bigSuit[suitIdx], Card.WIDTH/2-largeSuit/2, Card.HEIGHT/2-largeSuit/2, mSuitPaint);
        // Bottom
        canvas.save();
        canvas.rotate(180);
        canvas.drawBitmap(suit[suitIdx], -smallSuit-4, -Card.HEIGHT+4, mSuitPaint);
        canvas.drawText(card_values[valueIdx], -Card.WIDTH+3, -Card.HEIGHT+textSize, textPaintLeft);
      }
    }
  }

  /**
   * Create a face card bitmap from resources
   * @param r application resources reference
   * @param id drawable resource id (R.drawable.id)
   * @param width width of the bitmap
   * @param height height of the bitmap
   * @return bitmap of the face card resource
   */
  private Bitmap createFaceBitmap(final Resources r,
                                  final int id,
                                  final int width,
                                  final int height) {
    Drawable drawable = ResourcesCompat.getDrawable(r, id, null);
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, width, height);
    drawable.draw(canvas);
    return(bitmap);
  }

  /**
   * Draw regular cards with suits representing values on card
   * @param r application resources reference
   * @param size the size multiplier with regular being 0.5
   */
  private void drawCards(final Resources r, final float size) {

    Paint cardFrontPaint = new Paint();
    Paint cardBorderPaint = new Paint();
    Bitmap[] suit = new Bitmap[4];
    Bitmap[] revSuit = new Bitmap[4];
    Bitmap[] smallSuit = new Bitmap[4];
    Bitmap redJack;
    Bitmap redQueen;
    Bitmap redKing;
    Bitmap blackJack;
    Bitmap blackQueen;
    Bitmap blackKing;
    Canvas canvas;
    final int width = Card.WIDTH;
    final int height = Card.HEIGHT;

    final String[] card_values = mResources.getStringArray(R.array.card_values);
    final Paint textPaintLeft = getTextPaint(mFontSize*size,Paint.Align.LEFT);
    float textSize = textPaintLeft.getTextSize();
    Drawable drawable = ResourcesCompat.getDrawable(r, R.drawable.cardback, null);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_8888);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    final float suitsSize = textSize;
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap((int)suitsSize, (int)suitsSize, Bitmap.Config.ARGB_8888);
      revSuit[i] = Bitmap.createBitmap((int)suitsSize, (int)suitsSize, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(suit[i]);
      drawSuit(i,canvas,suitsSize);
      canvas = new Canvas(revSuit[i]);
      canvas.rotate(180,suitsSize/2,suitsSize/2);
      drawSuit(i,canvas,suitsSize);
    }

    final float suitsSmallSize = textSize/2;
    for (int i = 0; i < 4; i++) {
      smallSuit[i] = Bitmap.createBitmap((int)suitsSmallSize, (int)suitsSmallSize, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(smallSuit[i]);
      drawSuit(i,canvas,suitsSmallSize);
    }

    final int faceWidth = width - 20;
    final int faceHeight = height/2 - (int)suitsSize;
    blackJack = createFaceBitmap(r,R.drawable.blackjack, faceWidth, faceHeight);
    blackQueen = createFaceBitmap(r,R.drawable.blackqueen, faceWidth, faceHeight);
    blackKing = createFaceBitmap(r,R.drawable.blackking, faceWidth, faceHeight);
    redJack = createFaceBitmap(r,R.drawable.redjack, faceWidth, faceHeight);
    redQueen = createFaceBitmap(r,R.drawable.redqueen, faceWidth, faceHeight);
    redKing = createFaceBitmap(r,R.drawable.redking, faceWidth, faceHeight);

    cardBorderPaint.setARGB(255, 0, 0, 0);
    cardFrontPaint.setARGB(255, 255, 255, 255);
    RectF pos = new RectF();
    for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
      for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
        mCardBitmap[suitIdx*13+valueIdx] = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mCardBitmap[suitIdx*13+valueIdx]);
        pos.set(0, 0, width, height);
        canvas.drawRoundRect(pos, suitsSmallSize, suitsSmallSize, cardBorderPaint);
        pos.set(1, 1, width-1, height-1);
        canvas.drawRoundRect(pos, suitsSmallSize, suitsSmallSize, cardFrontPaint);

        if ((suitIdx & 1) == 1) {
          textPaintLeft.setARGB(255, 255, 0, 0);
        } else {
          textPaintLeft.setARGB(255, 0, 0, 0);
        }
        final float halfSuitsSmallSize = suitsSmallSize/2;
        canvas.drawText(card_values[valueIdx], halfSuitsSmallSize, textSize, textPaintLeft);
        canvas.drawBitmap(smallSuit[suitIdx],
                          halfSuitsSmallSize,
                          halfSuitsSmallSize+textSize,
                          mSuitPaint);
        canvas.save();
        canvas.rotate(180,Card.WIDTH/2,Card.HEIGHT/2);
        canvas.drawBitmap(smallSuit[suitIdx],
                          halfSuitsSmallSize,
                          halfSuitsSmallSize+textSize,
                          mSuitPaint);
        canvas.drawText(card_values[valueIdx], halfSuitsSmallSize, textSize, textPaintLeft);
        canvas.restore();

        if (valueIdx >= 10) {
          canvas.drawBitmap(suit[suitIdx], suitsSize, suitsSize, mSuitPaint);
          canvas.drawBitmap(revSuit[suitIdx],
                            width-suitsSize*2,
                            height-suitsSize*2,
                            mSuitPaint);
        }

        final float height_7th = height/7;
        final float height_9th = height/9;
        final float suitsSizeHalf = suitsSize/2;
        // Columns
        final float width_5th = width/5;
        final float[] suitX = {width_5th,
                               width/2-suitsSizeHalf,
                               width-width_5th-suitsSize};
        // Rows
        final float[] suitY = {height_7th, // row 1
                               height_9th*3, // row 2
                               height-(height_9th*4)-suitsSizeHalf/2, // row 4
                               height-(height_7th*2)}; // row 5
        // Center
        final float suitMidY = height/2 - suitsSizeHalf;
        switch (valueIdx+1) {
          case 1:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            break;
          case 2:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], mSuitPaint);
            break;
          case 3:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], mSuitPaint);
            break;
          case 4:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 5:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 6:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 7:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 8:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3]+suitMidY)/2, mSuitPaint);
            break;
          case 9:
            for (int i = 0; i < 4; i++) {
              canvas.drawBitmap(suit[suitIdx], suitX[(i%2)*2], suitY[i/2], mSuitPaint);
              canvas.drawBitmap(revSuit[suitIdx], suitX[(i%2)*2], suitY[i/2+2], mSuitPaint);
            }
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            break;
          case 10:
            for (int i = 0; i < 4; i++) {
              canvas.drawBitmap(suit[suitIdx], suitX[(i%2)*2], suitY[i/2], mSuitPaint);
              canvas.drawBitmap(revSuit[suitIdx], suitX[(i%2)*2], suitY[i/2+2], mSuitPaint);
            }
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY+suitY[0])/2-suitsSizeHalf, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3]+suitMidY)/2+suitsSizeHalf/2, mSuitPaint);
            break;

          case Card.JACK:
            if ((suitIdx & 1) == 1) {
              drawFaceBitmap(canvas, redJack, suitsSmallSize, suitsSize, mSuitPaint);
            } else {
              drawFaceBitmap(canvas, blackJack, suitsSmallSize, suitsSize, mSuitPaint);
            }
            break;
          case Card.QUEEN:
            if ((suitIdx & 1) == 1) {
              drawFaceBitmap(canvas, redQueen, suitsSmallSize, suitsSize, mSuitPaint);
            } else {
              drawFaceBitmap(canvas, blackQueen, suitsSmallSize, suitsSize, mSuitPaint);
            }
            break;
          case Card.KING:
            if ((suitIdx & 1) == 1) {
              drawFaceBitmap(canvas, redKing, suitsSmallSize, suitsSize, mSuitPaint);
            } else {
              drawFaceBitmap(canvas, blackKing, suitsSmallSize, suitsSize, mSuitPaint);
            }
            break;
        }
      }
    }
  }

  /**
   * Draw cards wrapper, draw big or regular cards
   * @param bigCards display big cards if true, regular otherwise
   */
  public void drawCards(boolean bigCards) {
    if (bigCards) {
      drawBigCards(mContext.getResources(),0.75f);
    } else {
      drawCards(mContext.getResources(),0.5f);
    }
  }

  /**
   * Draw face card bitmap, with its top/left corner at (x,y),
   * using the specified paint
   * @param canvas canvas to draw on
   * @param bitmap bitmap image to draw
   * @param left left side of the bitmap being drawn (y)
   * @param top top side of the bitmap being drawn (x)
   * @param paint paint used to draw the bitmap
   */
  public void drawFaceBitmap(final Canvas canvas,
                             final Bitmap bitmap,
                             final float left,
                             final float top,
                             final Paint paint) {
    canvas.drawBitmap(bitmap, left, top, paint);
    canvas.rotate(180,Card.WIDTH/2,Card.HEIGHT/2);
    canvas.drawBitmap(bitmap, left, top, paint);
  }

  /**
   * Draw time, elapsed game time
   * @param canvas canvas to draw on
   * @param millis the time in millis to draw
   */
  public void drawTime(final Canvas canvas, final int millis) {
    int seconds = (millis / 1000) % 60;
    int minutes = millis / 60000;
    if (seconds != mLastSeconds) {
      mLastSeconds = seconds;
      // String.format is insanely slow (~15ms)
      if (seconds < 10) {
        mTimeString = minutes + ":0" + seconds;
      } else {
        mTimeString = minutes + ":" + seconds;
      }
    }
    mTimePaint.setARGB(255, 20, 20, 20);
    final int textX = mScreenHeight-mFontSize;
    final int textY = mScreenWidth-mFontSize;
    canvas.drawText(mTimeString, textY, textX, mTimePaint);
    mTimePaint.setARGB(255, 0, 0, 0);
    canvas.drawText(mTimeString, textY-1, textX-1, mTimePaint);
  }

  /**
   * Draw score
   * @param canvas canvas to draw on
   * @param score the score to draw
   */
  public void drawScore(final Canvas canvas, final String score) {
    mTimePaint.setARGB(255, 20, 20, 20);
      final int textX = mScreenHeight-(mFontSize*5)/2;
      final int textY = mScreenWidth-mFontSize;
    canvas.drawText(score, textY, textX, mTimePaint);
    if (score.charAt(0) == '-') {
      mTimePaint.setARGB(255, 255, 0, 0);
    } else {
      mTimePaint.setARGB(255, 0, 0, 0);
    }
    canvas.drawText(score, textY-1, textX-1, mTimePaint);

  }
}
