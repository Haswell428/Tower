package com.droidplanner.widgets.HUD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder): - Improved consistency across
 * different screen sizes by replacing all fixed and density scaled size and
 * position values by percentual scaled values - Added functionality to show
 * dummy data for debugging purposes - Some minor layout changes
 */

public class HUDwidget extends SurfaceView implements SurfaceHolder.Callback,
		HudUpdatedListner {
	private static final float SCROLLER_WIDTH_FACTOR = .15f; // in relation to
																// width (total
																// HUD widget
																// width)
	private static final float SCROLLER_MAX_HEIGHT_FACTOR = .66f; // in relation
																	// to
																	// attHeightPx
	private static final float SCROLLER_FACTOR_TEXT = .048f; // in relation to
																// attHeightPx
	private static final float SCROLLER_FACTOR_TEXT_Y_OFFSET = -.16f;// in
																		// relation
																		// to
																		// scrollerSizePxText
	private static final float SCROLLER_FACTOR_ACTUAL_TEXT_MAGNIFICATION = 1.2f;// in
																				// relation
																				// to
																				// the
																				// resulting
																				// size
																				// of
																				// SCROLLER_FACTOR_TEXT
	private static final float SCROLLER_FACTOR_TEXT_X_OFFSET = .037f;// in
																		// relation
																		// to
																		// width
	private static final float SCROLLER_FACTOR_TIC_LENGTH = .025f; // in
																	// relation
																	// to width
	private static final float SCROLLER_FACTOR_ARROW_HEIGTH = 1.4f; // in
																	// relation
																	// to
																	// scrollerSizePxText
	private static final float SCROLLER_FACTOR_TARGET_BAR_WIDTH = .015f;// in
																		// relation
																		// to
																		// attHeightPx
	private static final int SCROLLER_VSI_RANGE = 12;
	private static final int SCROLLER_ALT_RANGE = 26;
	private static final int SCROLLER_SPEED_RANGE = 26;
	private static final float ROLL_FACTOR_TEXT = .038f; // in relation to
															// attHeightPx
	private static final float ROLL_FACTOR_TIC_LENGTH = .25f; // in relation to
																// rollTopOffsetPx
	private static final float ROLL_FACTOR_TEXT_Y_OFFSET = .8f; // in relation
																// to
																// rollSizePxTics
	private static final float PITCH_FACTOR_TEXT = .038f; // in relation to
															// attHeightPx
	private static final float PITCH_FACTOR_TEXT_Y_OFFSET = -.16f; // in
																	// relation
																	// to the
																	// resulting
																	// size of
																	// PITCH_FACTOR_TEXT
	private static final float PITCH_FACTOR_SCALE_Y_SPACE = 0.02f; // in
																	// relation
																	// to
																	// attHeightPx
	private static final float PITCH_FACTOR_SCALE_WIDHT_WIDE = 0.25f; // in
																		// relation
																		// to
																		// width
	private static final float PITCH_FACTOR_SCALE_WIDHT_NARROW = 0.1f; // in
																		// relation
																		// to
																		// width
	private static final float PITCH_FACTOR_SCALE_TEXT_X_OFFSET = 0.025f; // in
																			// relation
																			// to
																			// width

	private static final float ATT_FACTOR_INFOTEXT = .048f; // in relation to
															// attHeightPx
	private static final float ATT_FACTOR_INFOTEXT_Y_OFFSET = -.1f; // in
																	// relation
																	// to the
																	// resulting
																	// size of
																	// ATT_FACTOR_INFOTEXT
	private static final float ATT_FACTOR_INFOTEXT_X_OFFSET = .013f; // in
																		// relation
																		// to
																		// width
	private static final float ATT_FACTOR_INFOTEXT_CLEARANCE = .1f; // in
																	// relation
																	// to
																	// attSizePxInfoText//.3f

	private static final float HUD_FACTOR_BORDER_WIDTH = .0075f; // in relation
																	// to
																	// averaged
																	// of width
																	// and
																	// height
	private static final float HUD_FACTOR_RED_INDICATOR_WIDTH = .0075f; // in
																		// relation
																		// to
																		// averaged
																		// of
																		// width
																		// and
																		// height
	private static final float HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH = .005f; // in
																				// relation
																				// to
																				// averaged
																				// of
																				// width
																				// and
																				// height
	private static final float HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH = .0025f; // in
																				// relation
																				// to
																				// averaged
																				// of
																				// width
																				// and
																				// height
	private static final float HUD_FACTOR_CENTER_INDICATOR_SIZE = .0375f; // in
																			// relation
																			// to
																			// averaged
																			// of
																			// width
																			// and
																			// height

	private static final float FAILSAFE_FACTOR_TEXT = .093f; // in relation to
																// width
	private static final float FAILSAFE_FACTOR_BOX_PADDING = .27f; // in
																	// relation
																	// to the
																	// resulting
																	// size of
																	// FAILSAFE_FACTOR_TEXT

	private ScopeThread renderer;
	int width;
	int height;
	HudAtt data = new HudAtt();
	private HudScroller hudScroller = new HudScroller();
	private HudYaw hudYaw = new HudYaw();
	private HurRoll hudRoll = new HurRoll();
	private HudPitch hudPitch = new HudPitch();
	private int hudCenterIndicatorRadius;
	private int failsafeSizePxBoxPadding;

	private int armedCounter = 0;
	// private DisplayMetrics hudMetrics;

	static final boolean hudDebug = false;
	// hudDebug is the main switch for HUD debugging
	// |->false: Normal HUD operation.
	// '->true: HUD shows only the following dummy data! NO NORMAL OPERATION
	static final double hudDebugYaw = 42;
	private static final double hudDebugRoll = 45;
	private static final double hudDebugPitch = 11;
	private static final double hudDebugGroundSpeed = 4.3;
	private static final double hudDebugAirSpeed = 3.2;
	private static final double hudDebugTargetSpeed = 3;
	private static final double hudDebugAltitude = 8;
	private static final double hudDebugTargetAltitude = 20;
	private static final double hudDebugVerticalSpeed = 2.5;
	private static final double hudDebugBattRemain = 51;
	private static final double hudDebugBattCurrent = 40.5;
	private static final double hudDebugBattVolt = 12.32;
	private static final int hudDebugSatCount = 8;
	private static final int hudDebugFixType = 3;
	private static final double hudDebugGpsEPH = 2.4;
	private static final String hudDebugModeName = "Loiter";
	private static final int hudDebugWpNumber = 4;
	private static final double hudDebugDistToWp = 30.45;
	private static final int hudDebugDroneType = 2;
	private static final boolean hudDebugDroneArmed = false;

	// Paints
	Paint ground = new Paint();
	Paint sky = new Paint();
	Paint yawBg = new Paint();
	Paint rollText = new Paint();
	Paint pitchText = new Paint();
	Paint scrollerBg = new Paint();
	Paint scrollerText = new Paint();
	Paint scrollerActualText = new Paint();
	Paint whiteStroke = new Paint();
	Paint whiteBorder = new Paint();
	Paint whiteThickTics = new Paint();
	Paint whiteThinTics = new Paint();
	Paint FailsafeText = new Paint();
	Paint attInfoText = new Paint();
	Paint plane = new Paint();
	Paint blackSolid = new Paint();
	Paint blueVSI = new Paint();
	Paint greenPen = new Paint();

	Drone drone;

	@Override
	protected void onDraw(Canvas canvas) {
		if (drone == null) {
			return;
		}

		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));
		canvas.translate(width / 2, data.attHeightPx / 2 + hudYaw.yawHeightPx); // set
																	// center of
																	// HUD
																	// excluding
																	// YAW area

		// from now on each drawing routine has to undo all applied
		// transformations, clippings, etc by itself
		// this will improve performance because not every routine applies that
		// stuff, so general save and restore
		// is not necessary
		drawPitch(canvas);
		drawRoll(canvas);
		hudYaw.drawYaw(this, canvas);
		drawPlane(canvas);
		drawRightScroller(canvas);
		drawLeftScroller(canvas);
		drawAttitudeInfoText(canvas);
		drawFailsafe(canvas);
	}

	public HUDwidget(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);
		// hudMetrics = context.getResources().getDisplayMetrics();

		ground.setARGB(220, 148, 193, 31);
		sky.setARGB(220, 0, 113, 188);
		yawBg.setARGB(255, 0, 0, 0);// (64, 255, 255, 255);
		scrollerBg.setARGB(64, 255, 255, 255);// (255, 0, 0, 0);

		scrollerText.setColor(Color.WHITE);
		scrollerText.setAntiAlias(true);
		scrollerActualText.setColor(Color.WHITE);
		scrollerActualText.setAntiAlias(true);

		FailsafeText.setTextSize(37);
		FailsafeText.setAntiAlias(true);

		whiteStroke.setColor(Color.WHITE);
		whiteStroke.setStyle(Style.STROKE);
		whiteStroke.setStrokeWidth(3);
		whiteStroke.setAntiAlias(true);

		whiteBorder.setColor(Color.WHITE);
		whiteBorder.setStyle(Style.STROKE);
		whiteBorder.setStrokeWidth(3);
		whiteBorder.setAntiAlias(true);

		whiteThinTics.setColor(Color.WHITE);
		whiteThinTics.setStyle(Style.FILL);
		whiteThinTics.setStrokeWidth(1);
		whiteThinTics.setAntiAlias(true);

		whiteThickTics.setColor(Color.WHITE);
		whiteThickTics.setStyle(Style.FILL);
		whiteThickTics.setStrokeWidth(2);
		whiteThickTics.setAntiAlias(true);
		
		rollText.setColor(Color.WHITE);
		rollText.setAntiAlias(true);
		rollText.setTextAlign(Align.CENTER);

		pitchText.setColor(Color.WHITE);
		pitchText.setAntiAlias(true);
		pitchText.setTextAlign(Align.RIGHT);

		attInfoText.setColor(Color.WHITE);
		attInfoText.setTextAlign(Align.CENTER);
		attInfoText.setAntiAlias(true);

		greenPen.setColor(Color.GREEN);
		greenPen.setStrokeWidth(6);
		greenPen.setStyle(Style.STROKE);

		plane.setColor(Color.RED);
		plane.setStyle(Style.STROKE);
		plane.setStrokeWidth(3);
		plane.setAntiAlias(true);

		blackSolid.setColor(Color.BLACK);
		blackSolid.setAntiAlias(true);
		blueVSI.setARGB(255, 0, 50, 250);
		blueVSI.setAntiAlias(true);
	}

	private void drawPlane(Canvas canvas) {
		canvas.drawCircle(0, 0, hudCenterIndicatorRadius, plane);
		canvas.drawLine(-hudCenterIndicatorRadius, 0,
				-hudCenterIndicatorRadius * 2, 0, plane);
		canvas.drawLine(hudCenterIndicatorRadius, 0,
				hudCenterIndicatorRadius * 2, 0, plane);
		canvas.drawLine(0, -hudCenterIndicatorRadius, 0,
				-hudCenterIndicatorRadius * 2, plane);
	}

	private void drawRoll(Canvas canvas) {
		int r = Math.round(data.attHeightPx / 2 - hudRoll.rollTopOffsetPx);
		RectF rec = new RectF(-r, -r, r, r);

		// Draw the arc
		canvas.drawArc(rec, 225, 90, false, whiteBorder);

		// Draw center triangle
		Path arrow = new Path();
		int tempOffset = Math.round(plane.getStrokeWidth() / 2);
		arrow.moveTo(0, -data.attHeightPx / 2 + hudRoll.rollTopOffsetPx - tempOffset);
		arrow.lineTo(0 - hudRoll.rollTopOffsetPx / 3, -data.attHeightPx / 2
				+ hudRoll.rollTopOffsetPx / 2 - tempOffset);
		arrow.lineTo(0 + hudRoll.rollTopOffsetPx / 3, -data.attHeightPx / 2
				+ hudRoll.rollTopOffsetPx / 2 - tempOffset);
		arrow.close();
		canvas.drawPath(arrow, plane);

		// draw the ticks
		// The center of the circle is at: 0, 0
		for (int i = -45; i <= 45; i += 15) {
			if (i != 0) {
				// Draw ticks
				float dx = (float) Math.sin(i * Math.PI / 180) * r;
				float dy = (float) Math.cos(i * Math.PI / 180) * r;
				float ex = (float) Math.sin(i * Math.PI / 180)
						* (r + hudRoll.rollSizePxTics);
				float ey = (float) Math.cos(i * Math.PI / 180)
						* (r + hudRoll.rollSizePxTics);
				canvas.drawLine(dx, -dy, ex, -ey, whiteThickTics);
				// Draw the labels
				dx = (float) Math.sin(i * Math.PI / 180)
						* (r + hudRoll.rollSizePxTics + hudRoll.rollPosPxTextYOffset);
				dy = (float) Math.cos(i * Math.PI / 180)
						* (r + hudRoll.rollSizePxTics + hudRoll.rollPosPxTextYOffset);
				canvas.drawText(Math.abs(i) + "", dx, -dy, rollText);
			}
		}

		// current roll angle will be drawn by drawPitch()
	}

	private void drawPitch(Canvas canvas) {
		double pitch = drone.orientation.getPitch();
		double roll = drone.orientation.getRoll();

		if (hudDebug) {
			pitch = hudDebugPitch;
			roll = hudDebugRoll;
		}

		int pitchOffsetPx = (int) (pitch * hudPitch.pitchPixPerDegree);
		int rollTriangleBottom = -data.attHeightPx / 2 + hudRoll.rollTopOffsetPx / 2
				+ hudRoll.rollTopOffsetPx;

		canvas.rotate(-(int) roll);

		// Draw the background
		canvas.drawRect(-width, pitchOffsetPx, width,
				2 * height /* Go plenty low */, ground);
		canvas.drawRect(-width, -2 * height /* Go plenty high */, width,
				pitchOffsetPx, sky);
		canvas.drawLine(-width, pitchOffsetPx, width, pitchOffsetPx,
				whiteThinTics);

		// Draw roll triangle
		Path arrow = new Path();
		int tempOffset = Math.round(plane.getStrokeWidth()
				+ whiteBorder.getStrokeWidth() / 2);
		arrow.moveTo(0, -data.attHeightPx / 2 + hudRoll.rollTopOffsetPx + tempOffset);
		arrow.lineTo(0 - hudRoll.rollTopOffsetPx / 3, rollTriangleBottom + tempOffset);
		arrow.lineTo(0 + hudRoll.rollTopOffsetPx / 3, rollTriangleBottom + tempOffset);
		arrow.close();
		canvas.drawPath(arrow, plane);

		// Draw gauge
		int yPos;
		for (int i = -180; i <= 180; i += 5) {
			yPos = Math.round(-i * hudPitch.pitchPixPerDegree + pitchOffsetPx);
			if ((yPos < -rollTriangleBottom) && (yPos > rollTriangleBottom)
					&& (yPos != pitchOffsetPx)) {
				if (i % 2 == 0) {
					canvas.drawLine(-hudPitch.pitchScaleWideHalfWidth, yPos,
							hudPitch.pitchScaleWideHalfWidth, yPos, whiteThinTics);
					canvas.drawText(i + "", -hudPitch.pitchScaleWideHalfWidth
							- hudPitch.pitchScaleTextXOffset, yPos
							- hudPitch.pitchTextCenterOffsetPx, pitchText);
				} else
					canvas.drawLine(-hudPitch.pitchScaleNarrowHalfWidth, yPos,
							hudPitch.pitchScaleNarrowHalfWidth, yPos, whiteThinTics);
			}
		}

		canvas.rotate((int) roll);
	}

	private void drawRightScroller(Canvas canvas) {
		final float textHalfSize = scrollerText.getTextSize() / 2;
		scrollerText.setTextAlign(Paint.Align.LEFT);
		scrollerActualText.setTextAlign(Paint.Align.LEFT);

		double altitude = drone.altitude.getAltitude();
		double targetAltitude = drone.altitude.getTargetAltitude();
		double verticalSpeed = drone.speed.getVerticalSpeed();

		if (hudDebug) {
			altitude = hudDebugAltitude;
			targetAltitude = hudDebugTargetAltitude;
			verticalSpeed = hudDebugVerticalSpeed;
		}

		// Outside box
		RectF scroller = new RectF(width / 2 - hudScroller.scrollerWidthPx,
				-hudScroller.scrollerHeightPx / 2, width / 2, hudScroller.scrollerHeightPx / 2);

		// Draw Vertical speed indicator
		final float vsi_width = scroller.width() / 4;
		float linespace = scroller.height() / SCROLLER_VSI_RANGE;
		Path vsiBox = new Path();
		vsiBox.moveTo(scroller.left, scroller.top); // draw outside box
		vsiBox.lineTo(scroller.left - vsi_width, scroller.top + vsi_width);
		vsiBox.lineTo(scroller.left - vsi_width, scroller.bottom - vsi_width);
		vsiBox.lineTo(scroller.left, scroller.bottom);
		Path vsiFill = new Path();
		float vsiIndicatorEnd = scroller.centerY() - ((float) verticalSpeed)
				* linespace;
		vsiFill.moveTo(scroller.left, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width, vsiIndicatorEnd);
		vsiFill.lineTo(scroller.left, vsiIndicatorEnd);
		vsiFill.lineTo(scroller.left, scroller.centerY());
		canvas.drawPath(vsiBox, scrollerBg);
		canvas.drawPath(vsiFill, blueVSI);
		canvas.drawLine(scroller.left - vsi_width, vsiIndicatorEnd,
				scroller.left, vsiIndicatorEnd, whiteThinTics);
		canvas.drawPath(vsiBox, whiteBorder);

		for (int a = 1; a < SCROLLER_VSI_RANGE; a++) { // draw ticks
			float lineHeight = scroller.top + linespace * a;
			canvas.drawLine(scroller.left - vsi_width, lineHeight,
					scroller.left - vsi_width + vsi_width / 3, lineHeight,
					whiteThickTics);
		}

		// Draw Altitude Scroller
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height() / (float) SCROLLER_ALT_RANGE;
		int start = ((int) altitude - SCROLLER_ALT_RANGE / 2);

		if (start > targetAltitude) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((altitude + SCROLLER_SPEED_RANGE / 2) < targetAltitude) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}

		float targetAltPos = Float.MIN_VALUE;
		for (int a = start; a <= (altitude + SCROLLER_ALT_RANGE / 2); a += 1) { // go
																				// trough
																				// 1m
																				// steps

			float lineHeight = scroller.centerY() - space
					* (a - (int) altitude);

			if (a == ((int) targetAltitude) && targetAltitude != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right,
						lineHeight, greenPen);
				targetAltPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.left
						+ hudScroller.scrollerSizePxTicLength, lineHeight, whiteThickTics);
				canvas.drawText(Integer.toString(a), scroller.left
						+ hudScroller.scrollerSizePxTextXOffset, lineHeight + textHalfSize
						+ hudScroller.scrollerSizePxTextYOffset, scrollerText);
			}
		}

		// Arrow with current altitude
		String actualText = Integer.toString((int) altitude);
		int borderWidth = Math.round(whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.right, -hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + hudScroller.scrollerSizePxArrowHeight / 4
				+ borderWidth, -hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + borderWidth, 0);
		arrow.lineTo(scroller.left + hudScroller.scrollerSizePxArrowHeight / 4
				+ borderWidth, hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right, hudScroller.scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, blackSolid);
		if ((targetAltPos != Float.MIN_VALUE)
				&& (targetAltPos > -hudScroller.scrollerSizePxArrowHeight / 2)
				&& (targetAltPos < hudScroller.scrollerSizePxArrowHeight / 2)) {
			Rect actualTextRec = new Rect();
			scrollerActualText.getTextBounds(actualText, 0,
					actualText.length(), actualTextRec);
			canvas.drawLine(scroller.right, targetAltPos, scroller.left
					+ actualTextRec.width() + hudScroller.scrollerSizePxTextXOffset
					+ textHalfSize, targetAltPos, greenPen);
		}
		canvas.drawPath(arrow, plane);
		canvas.drawText(actualText, scroller.left + hudScroller.scrollerSizePxTextXOffset,
				scrollerActualText.getTextSize() / 2
						+ hudScroller.scrollerSizePxActualTextYOffset, scrollerActualText);
		// Reset clipping of Scroller
		canvas.clipRect(-width / 2, -height / 2, width / 2, height / 2,
				Region.Op.REPLACE);
		// Draw VSI center indicator
		canvas.drawLine(scroller.left + borderWidth, 0, scroller.left
				- vsi_width - borderWidth, 0, plane);
	}

	private void drawLeftScroller(Canvas canvas) {
		final float textHalfSize = scrollerText.getTextSize() / 2;
		scrollerText.setTextAlign(Paint.Align.RIGHT);
		scrollerActualText.setTextAlign(Paint.Align.RIGHT);

		double groundSpeed = drone.speed.getGroundSpeed();
		double airSpeed = drone.speed.getAirSpeed();
		double targetSpeed = drone.speed.getTargetSpeed();
		if (hudDebug) {
			groundSpeed = hudDebugGroundSpeed;
			airSpeed = hudDebugAirSpeed;
			targetSpeed = hudDebugTargetSpeed;
		}

		double speed = airSpeed;
		if (speed == 0)
			speed = groundSpeed;

		// Outside box
		RectF scroller = new RectF(-width / 2, -hudScroller.scrollerHeightPx / 2, -width
				/ 2 + hudScroller.scrollerWidthPx, hudScroller.scrollerHeightPx / 2);

		// Draw Scroll
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height() / (float) SCROLLER_SPEED_RANGE;
		int start = ((int) speed - SCROLLER_SPEED_RANGE / 2);

		if (start > targetSpeed) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((speed + SCROLLER_SPEED_RANGE / 2) < targetSpeed) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}

		float targetSpdPos = Float.MIN_VALUE;
		for (int a = start; a <= (speed + SCROLLER_SPEED_RANGE / 2); a += 1) {
			float lineHeight = scroller.centerY() - space * (a - (int) speed);

			if (a == ((int) targetSpeed) && targetSpeed != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right,
						lineHeight, greenPen);
				targetSpdPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.right, lineHeight, scroller.right
						- hudScroller.scrollerSizePxTicLength, lineHeight, whiteThickTics);
				canvas.drawText(Integer.toString(a), scroller.right
						- hudScroller.scrollerSizePxTextXOffset, lineHeight + textHalfSize
						+ hudScroller.scrollerSizePxTextYOffset, scrollerText);
			}
		}

		// Arrow with current speed
		String actualText = Integer.toString((int) speed);
		int borderWidth = Math.round(whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.left - borderWidth,
				-hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - hudScroller.scrollerSizePxArrowHeight / 4
				- borderWidth, -hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - borderWidth, 0);
		arrow.lineTo(scroller.right - hudScroller.scrollerSizePxArrowHeight / 4
				- borderWidth, hudScroller.scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left - borderWidth, hudScroller.scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, blackSolid);
		if ((targetSpdPos != Float.MIN_VALUE)
				&& (targetSpdPos > -hudScroller.scrollerSizePxArrowHeight / 2)
				&& (targetSpdPos < hudScroller.scrollerSizePxArrowHeight / 2)) {
			Rect actualTextRec = new Rect();
			scrollerActualText.getTextBounds(actualText, 0,
					actualText.length(), actualTextRec);
			canvas.drawLine(scroller.left, targetSpdPos, scroller.right
					- actualTextRec.width() - hudScroller.scrollerSizePxTextXOffset
					- textHalfSize, targetSpdPos, greenPen);
		}
		canvas.drawPath(arrow, plane);
		canvas.drawText(actualText, scroller.right - hudScroller.scrollerSizePxTextXOffset,
				scrollerActualText.getTextSize() / 2
						+ hudScroller.scrollerSizePxActualTextYOffset, scrollerActualText);
		// Reset clipping of Scroller
		canvas.clipRect(-width / 2, -height / 2, width / 2, height / 2,
				Region.Op.REPLACE);
	}

	private void drawAttitudeInfoText(Canvas canvas) {
		double battVolt = drone.battery.getBattVolt();
		double battCurrent = drone.battery.getBattCurrent();
		double battRemain = drone.battery.getBattRemain();
		double groundSpeed = drone.speed.getGroundSpeed();
		double airSpeed = drone.speed.getAirSpeed();
		int satCount = drone.GPS.getSatCount();
		int fixType = drone.GPS.getFixType();
		String modeName = drone.state.getMode().getName();
		int wpNumber = drone.mission.getWpno();
		double distToWp = drone.mission.getDisttowp();
		double gpsEPH = drone.GPS.getGpsEPH();

		if (hudDebug) {
			battVolt = hudDebugBattVolt;
			battCurrent = hudDebugBattCurrent;
			battRemain = hudDebugBattRemain;
			groundSpeed = hudDebugGroundSpeed;
			airSpeed = hudDebugAirSpeed;
			satCount = hudDebugSatCount;
			fixType = hudDebugFixType;
			modeName = hudDebugModeName;
			wpNumber = hudDebugWpNumber;
			distToWp = hudDebugDistToWp;
			gpsEPH = hudDebugGpsEPH;
		}

		// Left Top Text
		attInfoText.setTextAlign(Align.LEFT);

		if ((battVolt >= 0) || (battRemain >= 0))
			canvas.drawText(
					String.format("%2.1fV  %.0f%%", battVolt, battRemain),
					-width / 2 + data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextUpperTop, attInfoText);
		if (battCurrent >= 0)
			canvas.drawText(String.format("%2.1fA", battCurrent), -width / 2
					+ data.attPosPxInfoTextXOffset, data.attPosPxInfoTextUpperBottom,
					attInfoText);

		// Left Bottom Text
		canvas.drawText(String.format("AS %.1fms", airSpeed), -width / 2
				+ data.attPosPxInfoTextXOffset, data.attPosPxInfoTextLowerTop,
				attInfoText);
		canvas.drawText(String.format("GS %.1fms", groundSpeed), -width / 2
				+ data.attPosPxInfoTextXOffset, data.attPosPxInfoTextLowerBottom,
				attInfoText);

		// Right Top Text
		attInfoText.setTextAlign(Align.RIGHT);

		String gpsFix = "";
		if (satCount >= 0) {
			switch (fixType) {
			case 2:
				gpsFix = ("GPS2D(" + satCount + ")");
				break;
			case 3:
				gpsFix = ("GPS3D(" + satCount + ")");
				break;
			default:
				gpsFix = ("NoGPS(" + satCount + ")");
				break;
			}
		}
		canvas.drawText(gpsFix, width / 2 - data.attPosPxInfoTextXOffset,
				data.attPosPxInfoTextUpperTop, attInfoText);
		if (gpsEPH >= 0)
			canvas.drawText(String.format("hp%.1fm", gpsEPH), width / 2
					- data.attPosPxInfoTextXOffset, data.attPosPxInfoTextUpperBottom,
					attInfoText);

		// Right Bottom Text
		canvas.drawText(modeName, width / 2 - data.attPosPxInfoTextXOffset,
				data.attPosPxInfoTextLowerTop, attInfoText);
		if (wpNumber >= 0)
			canvas.drawText(String.format("%.0fm>WP#%d", distToWp, wpNumber),
					width / 2 - data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextLowerBottom, attInfoText);
	}

	private void drawFailsafe(Canvas canvas) {
		int droneType = drone.type.getType();
		boolean isArmed = drone.state.isArmed();

		if (hudDebug) {
			droneType = hudDebugDroneType;
			isArmed = hudDebugDroneArmed;
		}

		if (ApmModes.isCopter(droneType)) {
			if (isArmed) {
				if (armedCounter < 50) {
					FailsafeText.setColor(Color.RED);
					String text = "ARMED";
					Rect textRec = new Rect();
					FailsafeText.getTextBounds(text, 0, text.length(), textRec);
					textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
					RectF boxRec = new RectF(textRec.left
							- failsafeSizePxBoxPadding, textRec.top
							- failsafeSizePxBoxPadding, textRec.right
							+ failsafeSizePxBoxPadding, textRec.bottom
							+ failsafeSizePxBoxPadding);
					canvas.drawRoundRect(boxRec, failsafeSizePxBoxPadding,
							failsafeSizePxBoxPadding, blackSolid);
					canvas.drawText(text, textRec.left - 3, textRec.bottom - 1,
							FailsafeText);
					armedCounter++;
				}
			} else {
				FailsafeText.setColor(Color.GREEN);
				String text = "DISARMED";
				Rect textRec = new Rect();
				FailsafeText.getTextBounds(text, 0, text.length(), textRec);
				textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
				RectF boxRec = new RectF(textRec.left
						- failsafeSizePxBoxPadding, textRec.top
						- failsafeSizePxBoxPadding, textRec.right
						+ failsafeSizePxBoxPadding, textRec.bottom
						+ failsafeSizePxBoxPadding);
				canvas.drawRoundRect(boxRec, failsafeSizePxBoxPadding,
						failsafeSizePxBoxPadding, blackSolid);
				canvas.drawText(text, textRec.left - 3, textRec.bottom - 1,
						FailsafeText);
				armedCounter = 0;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		int tempAttTextClearance;
		int tempSize, tempOffset, tempAttSizeText;
		float hudScaleThickTicStrokeWidth, hudScaleThinTicStrokeWidth, hudBorderWidth, redIndicatorWidth;

		this.width = width;
		this.height = height;

		// do as much precalculation as possible here because it
		// takes some load off the onDraw() routine which is called much more
		// frequently

		hudCenterIndicatorRadius = Math.round((this.width + this.height) / 2
				* HUD_FACTOR_CENTER_INDICATOR_SIZE);

		hudScaleThickTicStrokeWidth = (this.width + this.height) / 2
				* HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH;
		if (hudScaleThickTicStrokeWidth < 1)
			hudScaleThickTicStrokeWidth = 1;
		whiteThickTics.setStrokeWidth(hudScaleThickTicStrokeWidth);

		hudScaleThinTicStrokeWidth = (this.width + this.height) / 2
				* HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH;
		if (hudScaleThinTicStrokeWidth < 1)
			hudScaleThinTicStrokeWidth = 1;
		whiteThinTics.setStrokeWidth(hudScaleThinTicStrokeWidth);

		hudBorderWidth = (this.width + this.height) / 2
				* HUD_FACTOR_BORDER_WIDTH;
		if (hudBorderWidth < 1)
			hudBorderWidth = 1;
		whiteBorder.setStrokeWidth(hudBorderWidth);

		redIndicatorWidth = (this.width + this.height) / 2
				* HUD_FACTOR_RED_INDICATOR_WIDTH;
		if (redIndicatorWidth < 1)
			redIndicatorWidth = 1;
		plane.setStrokeWidth(redIndicatorWidth);

		hudYaw.setupYaw(this, this);

		data.attHeightPx = this.height - hudYaw.yawHeightPx;
		tempAttSizeText = Math.round(data.attHeightPx * ATT_FACTOR_INFOTEXT);
		attInfoText.setTextSize(tempAttSizeText);
		tempOffset = Math.round(tempAttSizeText * ATT_FACTOR_INFOTEXT_Y_OFFSET);
		tempAttTextClearance = Math.round(tempAttSizeText
				* ATT_FACTOR_INFOTEXT_CLEARANCE);
		data.attPosPxInfoTextXOffset = Math.round(this.width
				* ATT_FACTOR_INFOTEXT_X_OFFSET);

		int scrollerMaxAvailHeight = data.attHeightPx - 4 * tempAttSizeText - 6
				* tempAttTextClearance;
		hudScroller.scrollerHeightPx = Math.round(data.attHeightPx * SCROLLER_MAX_HEIGHT_FACTOR);
		if (hudScroller.scrollerHeightPx > scrollerMaxAvailHeight) {
			hudScroller.scrollerHeightPx = scrollerMaxAvailHeight;
		} else {
			tempAttTextClearance = Math
					.round((data.attHeightPx - hudScroller.scrollerHeightPx - 4 * tempAttSizeText) / 6);
		}
		hudScroller.scrollerWidthPx = Math.round(this.width * SCROLLER_WIDTH_FACTOR);
		tempSize = Math.round(data.attHeightPx * SCROLLER_FACTOR_TEXT);
		scrollerText.setTextSize(tempSize);
		hudScroller.scrollerSizePxTextYOffset = Math.round(tempSize
				* SCROLLER_FACTOR_TEXT_Y_OFFSET);
		tempSize = Math.round(tempSize
				* SCROLLER_FACTOR_ACTUAL_TEXT_MAGNIFICATION);
		scrollerActualText.setTextSize(tempSize);
		hudScroller.scrollerSizePxActualTextYOffset = Math.round(tempSize
				* SCROLLER_FACTOR_TEXT_Y_OFFSET);
		hudScroller.scrollerSizePxArrowHeight = Math.round(tempSize
				* SCROLLER_FACTOR_ARROW_HEIGTH);
		hudScroller.scrollerSizePxTextXOffset = Math.round(this.width
				* SCROLLER_FACTOR_TEXT_X_OFFSET);
		hudScroller.scrollerSizePxTicLength = Math.round(this.width
				* SCROLLER_FACTOR_TIC_LENGTH);
		greenPen.setStrokeWidth(Math.round(data.attHeightPx
				* SCROLLER_FACTOR_TARGET_BAR_WIDTH));

		data.attPosPxInfoTextUpperTop = -data.attHeightPx / 2 + tempAttSizeText
				+ tempOffset + tempAttTextClearance;
		data.attPosPxInfoTextUpperBottom = -data.attHeightPx / 2 + 2 * tempAttSizeText
				+ tempOffset + 2 * tempAttTextClearance;
		data.attPosPxInfoTextLowerBottom = data.attHeightPx / 2 + tempOffset
				- tempAttTextClearance;
		data.attPosPxInfoTextLowerTop = data.attHeightPx / 2 - tempAttSizeText
				+ tempOffset - 2 * tempAttTextClearance;

		hudRoll.rollTopOffsetPx = hudYaw.yawHeightPx;
		rollText.setTextSize(Math.round(data.attHeightPx * ROLL_FACTOR_TEXT));
		hudRoll.rollSizePxTics = Math.round(hudRoll.rollTopOffsetPx * ROLL_FACTOR_TIC_LENGTH);
		hudRoll.rollPosPxTextYOffset = Math.round(hudRoll.rollSizePxTics
				* ROLL_FACTOR_TEXT_Y_OFFSET);

		tempSize = Math.round(data.attHeightPx * PITCH_FACTOR_TEXT);
		pitchText.setTextSize(tempSize);
		hudPitch.pitchTextCenterOffsetPx = Math.round(-tempSize / 2 - tempSize
				* PITCH_FACTOR_TEXT_Y_OFFSET);
		hudPitch.pitchScaleWideHalfWidth = Math.round(this.width
				* PITCH_FACTOR_SCALE_WIDHT_WIDE / 2);
		hudPitch.pitchScaleNarrowHalfWidth = Math.round(this.width
				* PITCH_FACTOR_SCALE_WIDHT_NARROW / 2);
		hudPitch.pitchScaleTextXOffset = Math.round(this.width
				* PITCH_FACTOR_SCALE_TEXT_X_OFFSET);
		hudPitch.pitchPixPerDegree = Math
				.round(data.attHeightPx * PITCH_FACTOR_SCALE_Y_SPACE);

		tempSize = Math.round(this.width * FAILSAFE_FACTOR_TEXT);
		FailsafeText.setTextSize(tempSize);
		failsafeSizePxBoxPadding = Math.round(tempSize
				* FAILSAFE_FACTOR_BOX_PADDING);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderer = new ScopeThread(getHolder(), this);
		if (!renderer.isRunning()) {
			renderer.setRunning(true);
			renderer.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		renderer.setRunning(false);
		while (retry) {
			try {
				renderer.join();
				renderer = null;
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	private class ScopeThread extends Thread {
		private SurfaceHolder _surfaceHolder;
		private HUDwidget scope;
		private volatile boolean running = false;
		private Object dirty = new Object();

		public ScopeThread(SurfaceHolder surfaceHolder, HUDwidget panel) {
			_surfaceHolder = surfaceHolder;
			scope = panel;
		}

		public boolean isRunning() {
			return running;

		}

		public void setRunning(boolean run) {
			running = run;
			setDirty();
		}

		/** We may need to redraw */
		public void setDirty() {
			synchronized (dirty) {
				dirty.notify();
			}
		}

		@SuppressLint("WrongCall")
		// TODO fix error
		@Override
		public void run() {
			Canvas c;
			while (running) {
				synchronized (dirty) {
					c = null;
					try {
						c = _surfaceHolder.lockCanvas(null);
						synchronized (_surfaceHolder) {
							if (c != null) {
								scope.onDraw(c);
							}
						}
					} finally {
						// do this in a finally so that if an exception is
						// thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							_surfaceHolder.unlockCanvasAndPost(c);
						}
					}

					// We do this wait at the _end_ to ensure we always draw at
					// least one frame of
					// HUD data
					try {
						// Log.d("HUD", "Waiting for change");
						dirty.wait(); // TODO - not quite ready
						// Log.d("HUD", "Handling change");
					} catch (InterruptedException e) {
						// We will try again and again
					}
				}
			}
		}
	}

	public void setDrone(Drone drone) {
		this.drone = drone;
		this.drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		if (renderer != null)
			renderer.setDirty();
	}
}
