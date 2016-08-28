package com.mindorks.placeholderview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.*;
import android.widget.FrameLayout;


import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by janisharali on 26/08/16.
 */
public class SwipeViewBinder<T, V extends FrameLayout> extends ViewBinder<T, V>{

    private V mLayoutView;
    private SwipeCallback mCallback;
    private Animator.AnimatorListener mViewRemoveAnimatorListener;
    private Animator.AnimatorListener mViewRestoreAnimatorListener;
    private int mSwipeType = SwipePlaceHolderView.SWIPE_TYPE_DEFAULT;
    private float mWidthSwipeDistFactor;
    private float mHeightSwipeDistFactor;
    private View mSwipeInMsgView;
    private View mSwipeOutMsgView;

    public SwipeViewBinder(T resolver) {
        super(resolver);
    }

    protected void bindView(V promptsView, int position, int swipeType, float widthSwipeDistFactor,
                            float heightSwipeDistFactor, SwipeCallback callback) {
        super.bindView(promptsView, position);
        mLayoutView = promptsView;
        mSwipeType = swipeType;
        mWidthSwipeDistFactor = widthSwipeDistFactor;
        mHeightSwipeDistFactor = heightSwipeDistFactor;
        mCallback = callback;
    }

    protected void setOnTouch(){
        switch (mSwipeType){
            case SwipePlaceHolderView.SWIPE_TYPE_DEFAULT:
                setDefaultTouchListener(mLayoutView);
                break;
            case SwipePlaceHolderView.SWIPE_TYPE_HORIZONTAL:
                setHorizontalTouchListener(mLayoutView);
                break;
            case SwipePlaceHolderView.SWIPE_TYPE_VERTICAL:
                setVerticalTouchListener(mLayoutView);
                break;
        }
    }

    private void bindSwipeIn(final T resolver){
        for(final Method method : resolver.getClass().getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(SwipeIn.class);
            if(annotation instanceof SwipeIn) {
                try {
                    method.setAccessible(true);
                    method.invoke(resolver);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void bindSwipeOut(final T resolver){
        for(final Method method : resolver.getClass().getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(SwipeOut.class);
            if(annotation instanceof SwipeOut) {
                try {
                    method.setAccessible(true);
                    method.invoke(resolver);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void bindSwipeInState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(SwipeInState.class);
            if(annotation instanceof SwipeInState) {
                try {
                    method.setAccessible(true);
                    method.invoke(getResolver());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void bindSwipeOutState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(SwipeOutState.class);
            if(annotation instanceof SwipeOutState) {
                try {
                    method.setAccessible(true);
                    method.invoke(getResolver());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void bindSwipeCancelState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(SwipeCancelState.class);
            if(annotation instanceof SwipeCancelState) {
                try {
                    method.setAccessible(true);
                    method.invoke(getResolver());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void unbind() {
        super.unbind();
    }

    private void serAnimatorListener(){
        mViewRemoveAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mCallback != null){
                    mCallback.onRemoveView(SwipeViewBinder.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        };

        mViewRestoreAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mCallback != null){
                    mCallback.onResetView(SwipeViewBinder.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        };
    }

    private void setDefaultTouchListener(final V view){
        serAnimatorListener();
        final DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        view.setOnTouchListener(new View.OnTouchListener() {
            private float dx;
            private float dy;
            int originalTopMargin;
            int originalLeftMargin;
            private int activePointerId = SwipeDecor.PRIMITIVE_NULL;
            private boolean resetDone = false;
            private PointF pointerCurrentPoint = new PointF();
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        pointerCurrentPoint.set(event.getRawX(), event.getRawY());
                        activePointerId = event.getPointerId(0);
                        resetDone = false;

                        FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                        originalTopMargin = layoutParamsOriginal.topMargin;
                        originalLeftMargin = layoutParamsOriginal.leftMargin;
                        dx = pointerCurrentPoint.x - layoutParamsOriginal.leftMargin;
                        dy = pointerCurrentPoint.y - layoutParamsOriginal.topMargin;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerId(event.getActionIndex()) == activePointerId && !resetDone) {}
                        else{break;}
                    case MotionEvent.ACTION_UP:
                        if(!resetDone) {
                            float distSlideX = pointerCurrentPoint.x - dx;
                            float distSlideY = pointerCurrentPoint.y - dy;

                            distSlideX = distSlideX < 0 ? -distSlideX : distSlideX;
                            distSlideY = distSlideY < 0 ? -distSlideY : distSlideY;
                            if (distSlideX < displayMetrics.widthPixels / mWidthSwipeDistFactor
                                    && distSlideY < displayMetrics.heightPixels / mHeightSwipeDistFactor) {
                                animateSwipeRestore(v, originalTopMargin, originalLeftMargin, mSwipeType);
                            }
                            else {
                                mLayoutView.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent rawEvent) {
                                        return false;
                                    }
                                });

                                float transX = displayMetrics.widthPixels;
                                float transY = displayMetrics.heightPixels;

                                if (pointerCurrentPoint.x > displayMetrics.widthPixels / 2
                                        && pointerCurrentPoint.y > displayMetrics.heightPixels / 2) {
                                    bindSwipeIn(getResolver());
                                } else if (pointerCurrentPoint.x > displayMetrics.widthPixels / 2
                                        && pointerCurrentPoint.y < displayMetrics.heightPixels / 2) {
                                    transY = -v.getHeight();
                                    bindSwipeIn(getResolver());
                                } else if (pointerCurrentPoint.x < displayMetrics.widthPixels / 2
                                        && pointerCurrentPoint.y > displayMetrics.heightPixels / 2) {
                                    transX = -v.getWidth();
                                    bindSwipeOut(getResolver());
                                } else if (pointerCurrentPoint.x < displayMetrics.widthPixels / 2
                                        && pointerCurrentPoint.y < displayMetrics.heightPixels / 2) {
                                    transY = -v.getHeight();
                                    transX = -v.getWidth();
                                    bindSwipeOut(getResolver());
                                }

                                view.animate()
                                        .translationX(transX)
                                        .translationY(transY)
                                        .setInterpolator(new DecelerateInterpolator(0.3f))
                                        .setDuration(150)
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();
                            }
                            resetDone = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!resetDone && event.findPointerIndex(activePointerId) != SwipeDecor.PRIMITIVE_NULL) {
                            pointerCurrentPoint.set(event.getRawX(), event.getRawY());
                            FrameLayout.LayoutParams layoutParamsTemp = (FrameLayout.LayoutParams) v.getLayoutParams();
                            layoutParamsTemp.topMargin = (int) (pointerCurrentPoint.y - dy);
                            layoutParamsTemp.leftMargin = (int) (pointerCurrentPoint.x - dx);
                            v.setLayoutParams(layoutParamsTemp);

                            int distanceMovedTop = layoutParamsTemp.topMargin - originalTopMargin;
                            int distanceMovedLeft = layoutParamsTemp.leftMargin - originalLeftMargin;
                            mCallback.onAnimateView(distanceMovedLeft, distanceMovedTop, displayMetrics.widthPixels / mWidthSwipeDistFactor,
                                    displayMetrics.heightPixels / mHeightSwipeDistFactor, SwipeViewBinder.this);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void setHorizontalTouchListener(final V view){
        serAnimatorListener();
        final DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        view.setOnTouchListener(new View.OnTouchListener() {
            private float x;
            private float dx;
            int originalLeftMargin;
            private int activePointerId = SwipeDecor.PRIMITIVE_NULL;
            private boolean resetDone = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX();
                        activePointerId = event.getPointerId(0);
                        resetDone = false;

                        FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                        originalLeftMargin = layoutParamsOriginal.leftMargin;
                        dx = x - layoutParamsOriginal.leftMargin;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerId(event.getActionIndex()) == activePointerId && !resetDone) {}
                        else{break;}
                    case MotionEvent.ACTION_UP:
                        if(!resetDone) {
                            resetDone = true;
                            float distSlideX = x - dx;
                            distSlideX = distSlideX < 0 ? -distSlideX : distSlideX;
                            if (distSlideX < displayMetrics.widthPixels / mWidthSwipeDistFactor) {
                                animateSwipeRestore(v, 0, originalLeftMargin, mSwipeType);
                            } else {
                                mLayoutView.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent rawEvent) {
                                        return false;
                                    }
                                });

                                float transX = displayMetrics.widthPixels;
                                if (x < displayMetrics.widthPixels / 2) {
                                    transX = -v.getWidth();
                                    bindSwipeOut(getResolver());
                                } else {
                                    bindSwipeIn(getResolver());
                                }
                                view.animate()
                                        .translationX(transX)
                                        .setInterpolator(new DecelerateInterpolator(0.3f))
                                        .setDuration(150)
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!resetDone && event.findPointerIndex(activePointerId) != SwipeDecor.PRIMITIVE_NULL) {
                            x = event.getRawX();
                            FrameLayout.LayoutParams layoutParamsTemp = (FrameLayout.LayoutParams) v.getLayoutParams();
                            layoutParamsTemp.leftMargin = (int) (x - dx);
                            v.setLayoutParams(layoutParamsTemp);
                            int distanceMoved = layoutParamsTemp.leftMargin - originalLeftMargin;
                            mCallback.onAnimateView(distanceMoved, 0, displayMetrics.widthPixels / mWidthSwipeDistFactor,
                                    displayMetrics.heightPixels / mHeightSwipeDistFactor, SwipeViewBinder.this);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void setVerticalTouchListener(final V view){
        serAnimatorListener();
        final DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        view.setOnTouchListener(new View.OnTouchListener() {
            private float y;
            private float dy;
            int originalTopMargin;
            private int activePointerId = SwipeDecor.PRIMITIVE_NULL;
            private boolean resetDone = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        y = event.getRawY();
                        activePointerId = event.getPointerId(0);
                        resetDone = false;
                        FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                        originalTopMargin = layoutParamsOriginal.topMargin;
                        dy = y - layoutParamsOriginal.topMargin;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerId(event.getActionIndex()) == activePointerId && !resetDone) {}
                        else{break;}
                    case MotionEvent.ACTION_UP:
                        if(!resetDone) {
                            float distSlideY = y - dy;
                            distSlideY = distSlideY < 0 ? -distSlideY : distSlideY;
                            if (distSlideY < displayMetrics.heightPixels / mHeightSwipeDistFactor) {
                                animateSwipeRestore(v, originalTopMargin, 0, mSwipeType);
                            } else {
                                mLayoutView.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent rawEvent) {
                                        return false;
                                    }
                                });

                                float transY = displayMetrics.heightPixels;
                                if (y < displayMetrics.heightPixels / 2) {
                                    transY = -v.getHeight();
                                    bindSwipeOut(getResolver());
                                } else {
                                    bindSwipeIn(getResolver());
                                }
                                view.animate()
                                        .translationY(transY)
                                        .setInterpolator(new DecelerateInterpolator(0.3f))
                                        .setDuration(150)
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();

                            }
                            resetDone = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!resetDone && event.findPointerIndex(activePointerId) != SwipeDecor.PRIMITIVE_NULL) {
                            y = event.getRawY();
                            FrameLayout.LayoutParams layoutParamsTemp = (FrameLayout.LayoutParams) v.getLayoutParams();
                            layoutParamsTemp.topMargin = (int) (y - dy);
                            v.setLayoutParams(layoutParamsTemp);

                            int distanceMoved = layoutParamsTemp.topMargin - originalTopMargin;
                            mCallback.onAnimateView(0, distanceMoved, displayMetrics.widthPixels / mWidthSwipeDistFactor,
                                    displayMetrics.heightPixels / mHeightSwipeDistFactor, SwipeViewBinder.this);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void animateSwipeRestore(final View v, int originalTopMargin, int originalLeftMargin, int swipeType){
        final FrameLayout.LayoutParams layoutParamsFinal = (FrameLayout.LayoutParams) v.getLayoutParams();

        ValueAnimator animatorX = null;
        ValueAnimator animatorY = null;
        int animTime = 200;
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.75f);
        ViewPropertyAnimator animatorR = v.animate()
                .rotation(0)
                .setInterpolator(decelerateInterpolator)
                .setDuration(animTime)
                .setListener(mViewRestoreAnimatorListener);

        if(swipeType == SwipePlaceHolderView.SWIPE_TYPE_DEFAULT
                || swipeType == SwipePlaceHolderView.SWIPE_TYPE_HORIZONTAL){
            animatorX = ValueAnimator.ofInt(layoutParamsFinal.leftMargin, originalLeftMargin);
            animatorX.setInterpolator(decelerateInterpolator);
            animatorX.setDuration(animTime);
            animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    layoutParamsFinal.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                    v.setLayoutParams(layoutParamsFinal);
                }

            });
        }
        if(swipeType == SwipePlaceHolderView.SWIPE_TYPE_DEFAULT
                || swipeType == SwipePlaceHolderView.SWIPE_TYPE_VERTICAL){
            animatorY = ValueAnimator.ofInt(layoutParamsFinal.topMargin, originalTopMargin);
            animatorY.setInterpolator(decelerateInterpolator);
            animatorY.setDuration(animTime);
            animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    layoutParamsFinal.topMargin = (Integer) valueAnimator.getAnimatedValue();
                    v.setLayoutParams(layoutParamsFinal);
                }
            });
        }

        if(animatorX != null){
            animatorX.start();
        }
        if(animatorY != null){
            animatorY.start();
        }
        animatorR.start();
    }

    protected View getSwipeInMsgView() {
        return mSwipeInMsgView;
    }

    protected void setSwipeInMsgView(View swipeInMsgView) {
        mSwipeInMsgView = swipeInMsgView;
    }

    protected View getSwipeOutMsgView() {
        return mSwipeOutMsgView;
    }

    protected void setSwipeOutMsgView(View swipeOutMsgView) {
        mSwipeOutMsgView = swipeOutMsgView;
    }

    protected V getLayoutView() {
        return mLayoutView;
    }

    protected interface SwipeCallback<T extends SwipeViewBinder<Object, FrameLayout>>{
        void onRemoveView(T swipeViewBinder);
        void onResetView(T swipeViewBinder);
        void onAnimateView(float distXMoved, float distYMoved, float finalXDist, float finalYDist, T swipeViewBinder);
    }
}