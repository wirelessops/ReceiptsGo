package co.smartreceipts.android.widget.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

//Adapted from: https://github.com/MikeOrtiz/TouchImageView/blob/master/src/com/example/touch/TouchImageView.java
public class PinchToZoomImageView extends android.support.v7.widget.AppCompatImageView {

	private static final float DEFAULT_MIN_SCALE = 1f;
	private static final float DEFAULT_MAX_SCALE = 2.5f;
	private static final int CLICK = 3;
	
	private final ScaleGestureDetector mScaleDetector;
	private final Matrix mMatrix;
	private final PointF mLast, mStart;
	private final float[] mValues;
	private float mMinScale, mMaxScale, mOrigWidth, mOrigHeight, mSaveScale;
	private int mViewWidth, mViewHeight, mOldMeasuredHeight;
	private Mode mMode;
	
	private enum Mode {
		NONE, DRAG, ZOOM,
	}
	
	public PinchToZoomImageView(Context context) {
		super(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mStart = new PointF();
        mLast = new PointF();
        mValues = new float[9];
        mMinScale = DEFAULT_MIN_SCALE;
        mMaxScale = DEFAULT_MAX_SCALE;
        mSaveScale = 1f;
        setImageMatrix(mMatrix);
        setScaleType(ImageView.ScaleType.MATRIX);
	}
	
	public PinchToZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mStart = new PointF();
        mLast = new PointF();
        mValues = new float[9];
        mMinScale = DEFAULT_MIN_SCALE;
        mMaxScale = DEFAULT_MAX_SCALE;
        mSaveScale = 1f;
        setImageMatrix(mMatrix);
        setScaleType(ImageView.ScaleType.MATRIX);
	}
	
    public PinchToZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mStart = new PointF();
        mLast = new PointF();
        mValues = new float[9];
        mMinScale = DEFAULT_MIN_SCALE;
        mMaxScale = DEFAULT_MAX_SCALE;
        mSaveScale = 1f;
        setImageMatrix(mMatrix);
        setScaleType(ImageView.ScaleType.MATRIX);
	}
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	setScaleType(ImageView.ScaleType.MATRIX);
        mScaleDetector.onTouchEvent(event);
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mLast.set(curr);
                mStart.set(mLast);
                mMode = Mode.DRAG;
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (mMode == Mode.DRAG) {
                    float deltaX = curr.x - mLast.x;
                    float deltaY = curr.y - mLast.y;
                    float fixTransX = getFixDragTrans(deltaX, mViewWidth, mOrigWidth * mSaveScale);
                    float fixTransY = getFixDragTrans(deltaY, mViewHeight, mOrigHeight * mSaveScale);
                    mMatrix.postTranslate(fixTransX, fixTransY);
                    fixTrans();
                    mLast.set(curr.x, curr.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                mMode = Mode.NONE;
                int xDiff = (int) Math.abs(curr.x - mStart.x);
                int yDiff = (int) Math.abs(curr.y - mStart.y);
                if (xDiff < CLICK && yDiff < CLICK)
                    performClick();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mMode = Mode.NONE;
                break;
                
            default:
            	return false;
        }
        
        setImageMatrix(mMatrix);
        return true;
    }


    public void setMaximumZoomScale(float scale) {
        mMaxScale = scale;
    }
    
    public void setMinimumZoomScale(float scale) {
    	mMinScale = scale;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        
    	@Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMode = Mode.ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = mSaveScale;
            mSaveScale *= mScaleFactor;
            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                mScaleFactor = mMaxScale / origScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                mScaleFactor = mMinScale / origScale;
            }

            if (mOrigWidth * mSaveScale <= mViewWidth || mOrigHeight * mSaveScale <= mViewHeight)
                mMatrix.postScale(mScaleFactor, mScaleFactor, mViewWidth / 2, mViewHeight / 2);
            else
                mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }

    private void fixTrans() {
        mMatrix.getValues(mValues);
        float transX = mValues[Matrix.MTRANS_X];
        float transY = mValues[Matrix.MTRANS_Y];
        
        float fixTransX = getFixTrans(transX, mViewWidth, mOrigWidth * mSaveScale);
        float fixTransY = getFixTrans(transY, mViewHeight, mOrigHeight * mSaveScale);

        if (fixTransX != 0 || fixTransY != 0)
            mMatrix.postTranslate(fixTransX, fixTransY);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }
    
    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        if (mOldMeasuredHeight == mViewWidth && mOldMeasuredHeight == mViewHeight || mViewWidth == 0 || mViewHeight == 0)
            return;
        
        mOldMeasuredHeight = mViewHeight;

        if (mSaveScale == 1) {
            //Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            float scaleX = (float) mViewWidth / (float) bmWidth;
            float scaleY = (float) mViewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            mMatrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) mViewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) mViewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            mMatrix.postTranslate(redundantXSpace, redundantYSpace);

            mOrigWidth = mViewWidth - 2 * redundantXSpace;
            mOrigHeight = mViewHeight - 2 * redundantYSpace;
            setImageMatrix(mMatrix);
        }
        fixTrans();
    }
}