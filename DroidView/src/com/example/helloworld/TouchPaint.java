package com.example.helloworld;
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Demonstrates the handling of touch screen, stylus, mouse and trackball events
 * to implement a simple painting app.
 * <p>
 * Drawing with a touch screen is accomplished by drawing a point at the
 * location of the touch. When pressure information is available, it is used to
 * change the intensity of the color. When size and orientation information is
 * available, it is used to directly adjust the size and orientation of the
 * brush.
 * </p>
 * <p>
 * Drawing with a stylus is similar to drawing with a touch screen, with a few
 * added refinements. First, there may be multiple tools available including an
 * eraser tool. Second, the tilt angle and orientation of the stylus can be used
 * to control the direction of paint. Third, the stylus buttons can be used to
 * perform various actions. Here we use one button to cycle colors and the other
 * to airbrush from a distance.
 * </p>
 * <p>
 * Drawing with a mouse is similar to drawing with a touch screen, but as with a
 * stylus we have extra buttons. Here we use the primary button to draw, the
 * secondary button to cycle colors and the tertiary button to airbrush.
 * </p>
 * <p>
 * Drawing with a trackball is a simple matter of using the relative motions of
 * the trackball to move the paint brush around. The trackball may also have a
 * button, which we use to cycle through colors.
 * </p>
 */
public class TouchPaint extends GraphicsActivity {
	/** Used as a pulse to gradually fade the contents of the window. */
	private static final int MSG_FADE = 1;

	/** Menu ID for the command to clear the window. */
	private static final int CLEAR_ID = Menu.FIRST;

	/** Menu ID for the command to toggle fading. */
	private static final int FADE_ID = Menu.FIRST + 1;
	
	/** Menu ID for the command to toggle fading. */
	private static final int SEND_ID = Menu.FIRST + 2;
	
	private static final int TEST_ID = Menu.FIRST + 3;

	/** How often to fade the contents of the window (in ms). */
	private static final int FADE_DELAY = 100;

	/** Colors to cycle through. */
	static final int[] COLORS = new int[] { Color.WHITE, Color.RED,
			Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, };

	/** Background color. */
	static final int BACKGROUND_COLOR = Color.BLACK;

	/** The view responsible for drawing the window. */
	PaintView mView;

	/** Is fading mode enabled? */
	boolean mFading;

	/** The index of the current color to use. */
	int mColorIndex;
	
	FileTransferClient mFClient = null;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try
		{
			mFClient = new FileTransferClient("127.0.0.1", 8000);
		}
		catch(UnknownHostException e)
		{
		}
		catch(IOException e)
		{
		}		
		// Create and attach the view that is responsible for painting.
		mView = new PaintView(this);
		setContentView(mView);
		mView.requestFocus();
		// Restore the fading option if we are being thawed from a
		// previously saved state. Note that we are not currently remembering
		// the contents of the bitmap.
		if (savedInstanceState != null) {
			mFading = savedInstanceState.getBoolean("fading", false);
			mColorIndex = savedInstanceState.getInt("color", 0);
		} else {
			mFading = false;
			mColorIndex = 0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLEAR_ID, 0, "Clear");
		menu.add(0, FADE_ID, 0, "Fade").setCheckable(true);
		menu.add(0, SEND_ID, 0, "Send");
		menu.add(0, TEST_ID, 0, "Test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(FADE_ID).setChecked(mFading);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_ID:
			mView.clear();
			return true;
		case FADE_ID:
			mFading = !mFading;
			if (mFading) {
				startFading();
			} else {
				stopFading();
			}
			return true;
		case SEND_ID:
			mView.send();
			return true;
		case TEST_ID:
			mView.test();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If fading mode is enabled, then as long as we are resumed we want
		// to run pulse to fade the contents.
		if (mFading) {
			startFading();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save away the fading state to restore if needed later. Note that
		// we do not currently save the contents of the display.
		outState.putBoolean("fading", mFading);
		outState.putInt("color", mColorIndex);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Make sure to never run the fading pulse while we are paused or
		// stopped.
		stopFading();
	}

	/**
	 * Start up the pulse to fade the screen, clearing any existing pulse to
	 * ensure that we don't have multiple pulses running at a time.
	 */
	void startFading() {
		mHandler.removeMessages(MSG_FADE);
		scheduleFade();
	}

	/**
	 * Stop the pulse to fade the screen.
	 */
	void stopFading() {
		mHandler.removeMessages(MSG_FADE);
	}

	/**
	 * Schedule a fade message for later.
	 */
	void scheduleFade() {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_FADE),
				FADE_DELAY);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Upon receiving the fade pulse, we have the view perform a
			// fade and then enqueue a new message to pulse at the desired
			// next time.
			case MSG_FADE: {
				mView.fade();
				scheduleFade();
				break;
			}
			default:
				super.handleMessage(msg);
			}
		}
	};

	enum PaintMode {
		Draw, Splat, Erase,
	}

	/**
	 * This view implements the drawing canvas.
	 * 
	 * It handles all of the input events and drawing functions.
	 */
	class PaintView extends View {
		private static final int FADE_ALPHA = 0x06;
		private static final int MAX_FADE_STEPS = 256 / FADE_ALPHA + 4;
		private static final int TRACKBALL_SCALE = 10;

		private static final int SPLAT_VECTORS = 40;

		private final Random mRandom = new Random();
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Paint mPaint;
		private final Paint mFadePaint;
		private float mCurX;
		private float mCurY;
		private int mOldButtonState;
		private int mFadeSteps = MAX_FADE_STEPS;
				
        private Path mPath;
        private PathData mPathData;
        
        private ArrayList<Path> pathVec;
        private PathData currentPath = new PathData();
        long lastpointtime = System.currentTimeMillis();
		long currentpointtime;
		private float mLastX = 0;
		private float mLastY = 0;
        
		public PaintView(TouchPaint c) {
			
            super(c);			
			
            setFocusable(true);
            
			/*
			  setFocusableInTouchMode(true);

	            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	            mPaint.setStyle(Paint.Style.STROKE);
	            mPaint.setStrokeWidth(6);

	            mPath = makeFollowPath();

	            mEffects = new PathEffect[6];

	            mColors = new int[] { Color.BLACK, Color.RED, Color.BLUE,
	                                  Color.GREEN, Color.MAGENTA, Color.BLACK
	                                };
*/
			mPaint = new Paint();
			mPaint.setAntiAlias(true);

			mFadePaint = new Paint();
			mFadePaint.setColor(BACKGROUND_COLOR);
			mFadePaint.setAlpha(FADE_ALPHA);
			
			//TODO get this from server on creation
			pathVec = new ArrayList<Path>();
			
		}

		public void clear() {
			if (mCanvas != null) {
				mPaint.setColor(BACKGROUND_COLOR);
				mCanvas.drawPaint(mPaint);
				invalidate();

				mFadeSteps = MAX_FADE_STEPS;
			}
		}
		
		/**
		 * Send the path at index ind to the server
		 * Should always be called for the current last path
		 */
		private void sendUpdate(int ind){
			//TODO
		}
		
		private void initFromServer(){			
			int lastIndex = getLatestId();
			//TODO
		}
		
		private int getLatestId(){
			//TODO
			return 0;
		}

		public void fade() {
			if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
				mCanvas.drawPaint(mFadePaint);
				invalidate();

				mFadeSteps++;
			}
		}
		
		public void send()
		{
			if (mFClient != null)
			{
				mFClient.sendPathData(mPathData);
			}
			return;
		}
		
		public void test()
		{
			ArrayList<Float> xcoors = new ArrayList<Float>();
			ArrayList<Float> ycoors = new ArrayList<Float>();
			xcoors.add((float)24);
/*			xcoors.add((float)40);
			xcoors.add((float)43);
			xcoors.add((float)34);
			xcoors.add((float)32);
			xcoors.add((float)112);
			ycoors.add((float)10);
			ycoors.add((float)50);
			ycoors.add((float)83);
			ycoors.add((float)6);
			ycoors.add((float)56);*/ 
			ycoors.add((float)123);
			mPathData = new PathData(xcoors, ycoors);
			mPath = mPathData.constructPath();
			Paint mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint2.setStyle(Paint.Style.STROKE);
            mPaint2.setStrokeWidth(6);
			mPaint2.setColor(Color.RED);
			mCanvas.drawPath(mPath, mPaint2);
			invalidate();
			return;
		}
		
		public void drawFromPathData(PathData _pathdata)
		{
			mPath = _pathdata.constructPath();
			Paint mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint2.setStyle(Paint.Style.STROKE);
            mPaint2.setStrokeWidth(6);
			mPaint2.setColor(Color.RED);
			mCanvas.drawPath(mPath, mPaint2);
			invalidate();
			return;
		}
		

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
			mCanvas = newCanvas;
			mFadeSteps = MAX_FADE_STEPS;
		}

		@Override
		protected void onDraw(Canvas canvas) {
	/*		
	           canvas.drawColor(Color.BLACK);

	            RectF bounds = new RectF();
	            mPath.computeBounds(bounds, false);
	            canvas.translate(10 - bounds.left, 10 - bounds.top);

	            makeEffects(mEffects, mPhase);
	            mPhase += 1;

	            for (int i = 1; i < 2; i++) {
	                mPaint.setPathEffect(mEffects[i]);
	                mPaint.setColor(mColors[i]);
	                canvas.drawPath(mPath, mPaint);
	                canvas.translate(0, 28);
	            }
	            
	            canvas.setMatrix(null);
		*/	
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			}
			
  //          invalidate();

		}

		@Override
		public boolean onTrackballEvent(MotionEvent event) {
			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN) {
				// Advance color when the trackball button is pressed.
				advanceColor();
			}

			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE) {
				final int N = event.getHistorySize();
				final float scaleX = event.getXPrecision() * TRACKBALL_SCALE;
				final float scaleY = event.getYPrecision() * TRACKBALL_SCALE;
				for (int i = 0; i < N; i++) {
					moveTrackball(event.getHistoricalX(i) * scaleX,
							event.getHistoricalY(i) * scaleY);
				}
				moveTrackball(event.getX() * scaleX, event.getY() * scaleY);
			}
			return true;
		}

		private void moveTrackball(float deltaX, float deltaY) {
			final int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			final int curH = mBitmap != null ? mBitmap.getHeight() : 0;

			mCurX = Math.max(Math.min(mCurX + deltaX, curW - 1), 0);
			mCurY = Math.max(Math.min(mCurY + deltaY, curH - 1), 0);
			paint(PaintMode.Draw, mCurX, mCurY);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return onTouchOrHoverEvent(event, true /* isTouch */);
		}

		@Override
		public boolean onHoverEvent(MotionEvent event) {
			return onTouchOrHoverEvent(event, false /* isTouch */);
		}

		private boolean onTouchOrHoverEvent(MotionEvent event, boolean isTouch) {
			final int buttonState = event.getButtonState();
			int pressedButtons = buttonState & ~mOldButtonState;
			mOldButtonState = buttonState;

			if ((pressedButtons & MotionEvent.BUTTON_SECONDARY) != 0) {
				// Advance color when the right mouse button or first stylus
				// button
				// is pressed.
				advanceColor();
			}

			PaintMode mode;
			if ((buttonState & MotionEvent.BUTTON_TERTIARY) != 0) {
				// Splat paint when the middle mouse button or second stylus
				// button is pressed.
				mode = PaintMode.Splat;
			} else if (isTouch
					|| (buttonState & MotionEvent.BUTTON_PRIMARY) != 0) {
				// Draw paint when touching or if the primary button is pressed.
				mode = PaintMode.Draw;
			} else {
				// Otherwise, do not paint anything.
				return false;
			}

			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE
					|| action == MotionEvent.ACTION_HOVER_MOVE) {
				final int N = event.getHistorySize();
				final int P = event.getPointerCount();
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < P; j++) {
						paint(getPaintModeForTool(event.getToolType(j), mode),
								event.getHistoricalX(j, i),
								event.getHistoricalY(j, i),
								event.getHistoricalPressure(j, i),
								event.getHistoricalTouchMajor(j, i),
								event.getHistoricalTouchMinor(j, i),
								event.getHistoricalOrientation(j, i),
								event.getHistoricalAxisValue(
										MotionEvent.AXIS_DISTANCE, j, i),
								event.getHistoricalAxisValue(
										MotionEvent.AXIS_TILT, j, i));
					}
				}
				for (int j = 0; j < P; j++) {
					paint(getPaintModeForTool(event.getToolType(j), mode),
							event.getX(j), event.getY(j), event.getPressure(j),
							event.getTouchMajor(j), event.getTouchMinor(j),
							event.getOrientation(j),
							event.getAxisValue(MotionEvent.AXIS_DISTANCE, j),
							event.getAxisValue(MotionEvent.AXIS_TILT, j));
				}
				mCurX = event.getX();
				mCurY = event.getY();
			}
			return true;
		}

		private PaintMode getPaintModeForTool(int toolType,
				PaintMode defaultMode) {
			if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
				return PaintMode.Erase;
			}
			return defaultMode;
		}

		private void advanceColor() {
			mColorIndex = (mColorIndex + 1) % COLORS.length;
		}

		private void paint(PaintMode mode, float x, float y) {
			paint(mode, x, y, 1.0f, 0, 0, 0, 0, 0);
		}

		private void paint(PaintMode mode, float x, float y, float pressure,
				float major, float minor, float orientation, float distance,
				float tilt) {
			
			currentpointtime = System.currentTimeMillis();
			
			if (mBitmap != null) {
				if (major <= 0 || minor <= 0) {
					// If size is not available, use a default value.
					major = minor = 16;
				}

				switch (mode) {
				case Draw:
					mPaint.setColor(COLORS[mColorIndex]);
					mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
					drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
					if(true)
					{
						if ((currentpointtime-lastpointtime)>1000 
								|| (Math.abs(this.mLastX - x)+Math.abs(this.mLastY-y)) > 40)
						{
							this.drawFromPathData(this.currentPath);
							//send out this path;
							this.currentPath.clear();
							//lastpointtime = 0;
						}
					}
					this.currentPath.addPoint(x, y);
					lastpointtime = currentpointtime;
					mLastX = x;
					mLastY = y;
					break;

				case Erase:
					mPaint.setColor(BACKGROUND_COLOR);
					mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
					drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
					break;

				case Splat:
					mPaint.setColor(COLORS[mColorIndex]);
					mPaint.setAlpha(64);
					drawSplat(mCanvas, x, y, orientation, distance, tilt,
							mPaint);
					break;
				}
			}
			mFadeSteps = 0;
			invalidate();
		}

		/**
		 * Draw an oval.
		 * 
		 * When the orienation is 0 radians, orients the major axis vertically,
		 * angles less than or greater than 0 radians rotate the major axis left
		 * or right.
		 */
		private final RectF mReusableOvalRect = new RectF();

		private void drawOval(Canvas canvas, float x, float y, float major,
				float minor, float orientation, Paint paint) {
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate((float) (orientation * 180 / Math.PI), x, y);
			mReusableOvalRect.left = x - minor / 2;
			mReusableOvalRect.right = x + minor / 2;
			mReusableOvalRect.top = y - major / 2;
			mReusableOvalRect.bottom = y + major / 2;
			canvas.drawOval(mReusableOvalRect, paint);
			canvas.restore();
		}

		/**
		 * Splatter paint in an area.
		 * 
		 * Chooses random vectors describing the flow of paint from a round
		 * nozzle across a range of a few degrees. Then adds this vector to the
		 * direction indicated by the orientation and tilt of the tool and
		 * throws paint at the canvas along that vector.
		 * 
		 * Repeats the process until a masterpiece is born.
		 */
		private void drawSplat(Canvas canvas, float x, float y,
				float orientation, float distance, float tilt, Paint paint) {
			float z = distance * 2 + 10;

			// Calculate the center of the spray.
			float nx = (float) (Math.sin(orientation) * Math.sin(tilt));
			float ny = (float) (-Math.cos(orientation) * Math.sin(tilt));
			float nz = (float) Math.cos(tilt);
			if (nz < 0.05) {
				return;
			}
			float cd = z / nz;
			float cx = nx * cd;
			float cy = ny * cd;

			for (int i = 0; i < SPLAT_VECTORS; i++) {
				// Make a random 2D vector that describes the direction of a
				// speck of paint
				// ejected by the nozzle in the nozzle's plane, assuming the
				// tool is
				// perpendicular to the surface.
				double direction = mRandom.nextDouble() * Math.PI * 2;
				double dispersion = mRandom.nextGaussian() * 0.2;
				double vx = Math.cos(direction) * dispersion;
				double vy = Math.sin(direction) * dispersion;
				double vz = 1;

				// Apply the nozzle tilt angle.
				double temp = vy;
				vy = temp * Math.cos(tilt) - vz * Math.sin(tilt);
				vz = temp * Math.sin(tilt) + vz * Math.cos(tilt);

				// Apply the nozzle orientation angle.
				temp = vx;
				vx = temp * Math.cos(orientation) - vy * Math.sin(orientation);
				vy = temp * Math.sin(orientation) + vy * Math.cos(orientation);

				// Determine where the paint will hit the surface.
				if (vz < 0.05) {
					continue;
				}
				float pd = (float) (z / vz);
				float px = (float) (vx * pd);
				float py = (float) (vy * pd);

				// Throw some paint at this location, relative to the center of
				// the spray.
				mCanvas.drawCircle(x + px - cx, y + py - cy, 1.0f, paint);
			}
		}
		
/*
		
        private PathEffect makeDash(float phase) {
            return new DashPathEffect(new float[] { 15, 5, 8, 5 }, phase);
        }
        
        
        private void makeEffects(PathEffect[] e, float phase) {
            e[0] = null;     // no effect
            e[1] = new CornerPathEffect(10);
            e[2] = new DashPathEffect(new float[] {10, 5, 5, 5}, phase);
            e[3] = new PathDashPathEffect(makePathDash(), 12, phase,
                                          PathDashPathEffect.Style.ROTATE);
            e[4] = new ComposePathEffect(e[2], e[1]);
            e[5] = new ComposePathEffect(e[3], e[1]);
        }

        @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mPath = makeFollowPath();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        
        private Path makeFollowPath() {
            Path p = new Path();
            p.moveTo(0, 0);
            for (int i = 1; i <= 15; i++) {
                p.lineTo(i*20, (float)Math.random() * 35);
            }
            return p;
        } 

        private Path makePathDash() {
            Path p = new Path();
            p.moveTo(24, 10);
            p.lineTo(40, 50);
            p.lineTo(43, 83);
            p.lineTo(34, 6);
            p.lineTo(32, 56);
            p.lineTo(12, 23);
            return p;
        }*/
	}
}
