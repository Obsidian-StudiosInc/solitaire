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

  public DrawMaster(Context context) {

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
    DrawCards(false);
    mBoardBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  public int GetWidth() { return mScreenWidth; }
  public int GetHeight() { return mScreenHeight; }
  public Canvas GetBoardCanvas() { return mBoardCanvas; }

  public void DrawCard(Canvas canvas, Card card) {
    float x = card.GetX();
    float y = card.GetY();
    int idx = card.GetSuit()*13+(card.GetValue()-1);
    canvas.drawBitmap(mCardBitmap[idx], x, y, mSuitPaint);
  }

  public void DrawHiddenCard(Canvas canvas, Card card) {
    float x = card.GetX();
    float y = card.GetY();
    canvas.drawBitmap(mCardHidden, x, y, mSuitPaint);
  }

  public void DrawEmptyAnchor(Canvas canvas, float x, float y, boolean done) {
    RectF pos = new RectF(x, y, x + Card.WIDTH, y + Card.HEIGHT);
    if (!done) {
      canvas.drawRoundRect(pos, 4, 4, mEmptyAnchorPaint);
    } else {
      canvas.drawRoundRect(pos, 4, 4, mDoneEmptyAnchorPaint);
    }
  }

  public void DrawBackground(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mBGPaint);
  }

  public void DrawShade(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mShadePaint);
  }

  public void DrawLightShade(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mLightShadePaint);
  }

  public void DrawLastBoard(Canvas canvas) {
    canvas.drawBitmap(mBoardBitmap, 0, 0, mSuitPaint);
  }

  public void SetScreenSize(int width, int height) {
    mScreenWidth = width;
    mScreenHeight = height;
    mBoardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  public static Paint getPaint() {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    return(paint);
  }

  public static Paint getBlackPaint() {
    final Paint paint = getPaint();
    paint.setARGB(255, 0, 0, 0);
    return(paint);
  }

  public static Paint getRedPaint() {
    final Paint paint = getPaint();
    paint.setARGB(255, 255, 0, 0);
    return(paint);
  }

  public static Paint getTextPaint(float fontSize,Paint.Align align) {
    Paint paint = new Paint();
    paint.setTextSize(fontSize);
    paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    paint.setTextAlign(align);
    paint.setAntiAlias(true);
    return(paint);
  }

  public void drawClub(final Canvas canvas, final float width, final float height) {
    final Paint paint = getBlackPaint();
    final float width_half = width/2;
    final float width_fifth = width/5;
    final float width_3_5ths = width_fifth*3;
    final float height_5th = height/5;
    canvas.drawCircle(width_half,height_5th,width_fifth,paint);
    canvas.drawCircle(width_fifth,height/2,width_fifth,paint);
    canvas.drawCircle(width-width_fifth,height/2,width_fifth,paint);
    canvas.drawRect(width_3_5ths,height_5th,width-width_3_5ths,height,paint);
    final Path path = new Path();
    path.moveTo(width_fifth,height);
    path.lineTo(width_half,height-height_5th);
    path.lineTo(width-width_fifth,height);
    path.lineTo(width_fifth,height);
    path.close();
    canvas.drawPath(path,paint);
  }

  public void drawDiamond(final Canvas canvas, final float width, final float height) {
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

  public void drawHeart(final Canvas canvas, final float width, final float height) {
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

  public void DrawCards(boolean bigCards) {
    if (bigCards) {
      DrawBigCards(mContext.getResources());
    } else {
      DrawCards(mContext.getResources());

    }
  }

  private void DrawBigCards(Resources r) {

    Paint cardFrontPaint = new Paint();
    Paint cardBorderPaint = new Paint();
    Bitmap[] bigSuit = new Bitmap[4];
    Bitmap[] suit = new Bitmap[4];
    Canvas canvas;

    Drawable drawable = ResourcesCompat.getDrawable(r, R.drawable.cardback, null);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_8888);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.suits, null);
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(suit[i]);
      if(i==0) {
        drawClub(canvas, 10, 10);
      } else if(i==1) {
        drawDiamond(canvas, 10, 10);
      } else if(i==3) {
        drawHeart(canvas, 10, 10);
      } else {
        drawable.setBounds(-i*10, 0, -i*10+40, 10);
        drawable.draw(canvas);
      }
    }

    drawable = ResourcesCompat.getDrawable(r, R.drawable.bigsuits, null);
    for (int i = 0; i < 4; i++) {
      bigSuit[i] = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(bigSuit[i]);
      if(i==0) {
        drawClub(canvas, 25, 25);
      } else if(i==1) {
        drawDiamond(canvas, 25, 25);
      } else if(i==3) {
        drawHeart(canvas, 25, 25);
      } else {
        drawable.setBounds(-i * 25, 0, -i * 25 + 100, 25);
        drawable.draw(canvas);
      }
    }

    String[] card_values = mResources.getStringArray(R.array.card_values);
    Paint textPaintLeft = getTextPaint(mFontSize/2,Paint.Align.LEFT);
    float textSize = textPaintLeft.getTextSize();

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
        canvas.drawBitmap(suit[suitIdx], Card.WIDTH-14, 4, mSuitPaint);
        // Middle
        canvas.drawBitmap(bigSuit[suitIdx], Card.WIDTH/2-12, Card.HEIGHT/2-13, mSuitPaint);
        // Bottom
        canvas.save();
        canvas.rotate(180);
        canvas.drawBitmap(suit[suitIdx], -14, -Card.HEIGHT+4, mSuitPaint);
        canvas.drawText(card_values[valueIdx], -Card.WIDTH+3, -Card.HEIGHT+textSize, textPaintLeft);
      }
    }
  }

  private void DrawCards(Resources r) {

    Paint cardFrontPaint = new Paint();
    Paint cardBorderPaint = new Paint();
    Bitmap[] suit = new Bitmap[4];
    Bitmap[] revSuit = new Bitmap[4];
    Bitmap[] smallSuit = new Bitmap[4];
    Bitmap[] revSmallSuit = new Bitmap[4];
    Bitmap[] blackFont = new Bitmap[13];
    Bitmap[] revBlackFont = new Bitmap[13];
    Bitmap[] redFont = new Bitmap[13];
    Bitmap[] revRedFont = new Bitmap[13];
    Bitmap redJack;
    Bitmap redRevJack;
    Bitmap redQueen;
    Bitmap redRevQueen;
    Bitmap redKing;
    Bitmap redRevKing;
    Bitmap blackJack;
    Bitmap blackRevJack;
    Bitmap blackQueen;
    Bitmap blackRevQueen;
    Bitmap blackKing;
    Bitmap blackRevKing;
    Canvas canvas;
    int width = Card.WIDTH;
    int height = Card.HEIGHT;
    int fontWidth;
    int fontHeight;
    float[] faceBox = { 9,8,width-10,8,
                        width-10,8,width-10,height-9,
                        width-10,height-9,9,height-9,
                        9,height-8,9,8
                      };
    Drawable drawable = ResourcesCompat.getDrawable(r, R.drawable.cardback, null);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_8888);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.suits, null);
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
      revSuit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(suit[i]);
      drawable.setBounds(-i*10, 0, -i*10+40, 10);
      drawable.draw(canvas);
      canvas = new Canvas(revSuit[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*10-10, -10, -i*10+30, 0);
      drawable.draw(canvas);
    }

    drawable = ResourcesCompat.getDrawable(r, R.drawable.smallsuits, null);
    for (int i = 0; i < 4; i++) {
      smallSuit[i] = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888);
      revSmallSuit[i] = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(smallSuit[i]);
      drawable.setBounds(-i*5, 0, -i*5+20, 5);
      drawable.draw(canvas);
      canvas = new Canvas(revSmallSuit[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*5-5, -5, -i*5+15, 0);
      drawable.draw(canvas);
    }

    drawable = ResourcesCompat.getDrawable(r, R.drawable.medblackfont, null);
    fontWidth = 7;
    fontHeight = 9;
    for (int i = 0; i < 13; i++) {
      blackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_8888);
      revBlackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(blackFont[i]);
      drawable.setBounds(-i*fontWidth, 0, -i*fontWidth+13*fontWidth, fontHeight);
      drawable.draw(canvas);
      canvas = new Canvas(revBlackFont[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*fontWidth-fontWidth, -fontHeight, -i*fontWidth+(12*fontWidth), 0);
      drawable.draw(canvas);
    }

    drawable = ResourcesCompat.getDrawable(r, R.drawable.medredfont, null);
    for (int i = 0; i < 13; i++) {
      redFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_8888);
      revRedFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_8888);
      canvas = new Canvas(redFont[i]);
      drawable.setBounds(-i*fontWidth, 0, -i*fontWidth+13*fontWidth, fontHeight);
      drawable.draw(canvas);
      canvas = new Canvas(revRedFont[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*fontWidth-fontWidth, -fontHeight, -i*fontWidth+(12*fontWidth), 0);
      drawable.draw(canvas);
    }

    int faceWidth = width - 20;
    int faceHeight = height/2 - 9;
    drawable = ResourcesCompat.getDrawable(r, R.drawable.redjack, null);
    redJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    redRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(redJack);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevJack);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.redqueen, null);
    redQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    redRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(redQueen);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevQueen);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.redking, null);
    redKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    redRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(redKing);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevKing);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.blackjack, null);
    blackJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    blackRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(blackJack);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevJack);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.blackqueen, null);
    blackQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    blackRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(blackQueen);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevQueen);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    drawable = ResourcesCompat.getDrawable(r, R.drawable.blackking, null);
    blackKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    blackRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(blackKing);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevKing);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    cardBorderPaint.setARGB(255, 0, 0, 0);
    cardFrontPaint.setARGB(255, 255, 255, 255);
    RectF pos = new RectF();
    for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
      for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
        mCardBitmap[suitIdx*13+valueIdx] = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mCardBitmap[suitIdx*13+valueIdx]);
        pos.set(0, 0, width, height);
        canvas.drawRoundRect(pos, 4, 4, cardBorderPaint);
        pos.set(1, 1, width-1, height-1);
        canvas.drawRoundRect(pos, 4, 4, cardFrontPaint);

        if ((suitIdx & 1) == 1) {
          canvas.drawBitmap(redFont[valueIdx], 2, 4, mSuitPaint);
          canvas.drawBitmap(revRedFont[valueIdx], width-fontWidth-2, height-fontHeight-4,
                            mSuitPaint);
        } else {
          canvas.drawBitmap(blackFont[valueIdx], 2, 4, mSuitPaint);
          canvas.drawBitmap(revBlackFont[valueIdx], width-fontWidth-2, height-fontHeight-4,
                            mSuitPaint);
        }
        if (fontWidth > 6) {
          canvas.drawBitmap(smallSuit[suitIdx], 3, 5+fontHeight, mSuitPaint);
          canvas.drawBitmap(revSmallSuit[suitIdx], width-7, height-11-fontHeight,
                            mSuitPaint);
        } else {
          canvas.drawBitmap(smallSuit[suitIdx], 2, 5+fontHeight, mSuitPaint);
          canvas.drawBitmap(revSmallSuit[suitIdx], width-6, height-11-fontHeight,
                            mSuitPaint);
        }

        if (valueIdx >= 10) {
          canvas.drawBitmap(suit[suitIdx], 10, 9, mSuitPaint);
          canvas.drawBitmap(revSuit[suitIdx], width-21, height-20,
                            mSuitPaint);
        }

        int[] suitX = {9,width/2-5,width-20};
        int[] suitY = {7,2*height/5-5,3*height/5-5,height-18};
        int suitMidY = height/2 - 6;
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
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitY[1]+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3]+suitY[2])/2, mSuitPaint);
            break;

          case Card.JACK:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redJack, 10, 9, mSuitPaint);
              canvas.drawBitmap(redRevJack, 10, height-faceHeight-9, mSuitPaint);
            } else {
              canvas.drawBitmap(blackJack, 10, 9, mSuitPaint);
              canvas.drawBitmap(blackRevJack, 10, height-faceHeight-9, mSuitPaint);
            }
            break;
          case Card.QUEEN:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redQueen, 10, 9, mSuitPaint);
              canvas.drawBitmap(redRevQueen, 10, height-faceHeight-9, mSuitPaint);
            } else {
              canvas.drawBitmap(blackQueen, 10, 9, mSuitPaint);
              canvas.drawBitmap(blackRevQueen, 10, height-faceHeight-9, mSuitPaint);
            }
            break;
          case Card.KING:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redKing, 10, 9, mSuitPaint);
              canvas.drawBitmap(redRevKing, 10, height-faceHeight-9, mSuitPaint);
            } else {
              canvas.drawBitmap(blackKing, 10, 9, mSuitPaint);
              canvas.drawBitmap(blackRevKing, 10, height-faceHeight-9, mSuitPaint);
            }
            break;
        }
      }
    }
  }

    public void DrawTime(Canvas canvas, int millis) {
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

  public void DrawRulesString(Canvas canvas, String score) {
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
