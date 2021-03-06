package com.mindorks.placeholderview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.os.CountDownTimer;
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
    private SwipeDecor mSwipeDecor;
    private boolean hasInterceptedEvent = false;

    /**
     *
     * @param resolver
     */
    public SwipeViewBinder(T resolver) {
        super(resolver);
    }

    /**
     *
     * @param promptsView
     * @param position
     * @param swipeType
     * @param widthSwipeDistFactor
     * @param heightSwipeDistFactor
     * @param decor
     * @param callback
     */
    protected void bindView(V promptsView, int position, int swipeType, float widthSwipeDistFactor,
                            float heightSwipeDistFactor, SwipeDecor decor, SwipeCallback callback) {
        super.bindView(promptsView, position);
        mLayoutView = promptsView;
        mSwipeType = swipeType;
        mSwipeDecor = decor;
        mWidthSwipeDistFactor = widthSwipeDistFactor;
        mHeightSwipeDistFactor = heightSwipeDistFactor;
        mCallback = callback;
    }

    /**
     *
     */
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

    /**
     *
     * @param resolver
     */
    private void bindSwipeIn(final T resolver){
        for(final Method method : resolver.getClass().getDeclaredMethods()) {
            SwipeIn annotation = method.getAnnotation(SwipeIn.class);
            if(annotation != null) {
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

    /**
     *
     * @param resolver
     */
    private void bindSwipeOut(final T resolver){
        for(final Method method : resolver.getClass().getDeclaredMethods()) {
            SwipeOut annotation = method.getAnnotation(SwipeOut.class);
            if(annotation != null) {
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

    /**
     *
     */
    protected void bindSwipeInState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            SwipeInState annotation = method.getAnnotation(SwipeInState.class);
            if(annotation != null) {
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

    /**
     *
     */
    protected void bindSwipeOutState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            SwipeOutState annotation = method.getAnnotation(SwipeOutState.class);
            if(annotation != null) {
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

    /**
     *
     */
    protected void bindSwipeCancelState(){
        for(final Method method : getResolver().getClass().getDeclaredMethods()) {
            SwipeCancelState annotation = method.getAnnotation(SwipeCancelState.class);
            if(annotation != null) {
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

    /**
     *
     */
    @Override
    protected void unbind() {
        super.unbind();
    }

    /**
     *
     */
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

    /**
     *
     * @param view
     */
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

                if(!hasInterceptedEvent){
                    pointerCurrentPoint.set(event.getRawX(), event.getRawY());
                    activePointerId = event.getPointerId(0);
                    resetDone = false;
                    FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                    originalTopMargin = layoutParamsOriginal.topMargin;
                    originalLeftMargin = layoutParamsOriginal.leftMargin;
                    dx = pointerCurrentPoint.x - layoutParamsOriginal.leftMargin;
                    dy = pointerCurrentPoint.y - layoutParamsOriginal.topMargin;
                    hasInterceptedEvent = true;
                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
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
                                        .setInterpolator(new AccelerateInterpolator(mSwipeDecor.getSwipeAnimFactor()))
                                        .setDuration((long)(mSwipeDecor.getSwipeAnimTime() * 1.25))
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();
                            }
                            new CountDownTimer(mSwipeDecor.getSwipeAnimTime(), mSwipeDecor.getSwipeAnimTime()) {
                                public void onTick(long millisUntilFinished) {}
                                public void onFinish() {hasInterceptedEvent = false;}
                            }.start();
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

    /**
     *
     * @param view
     */
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

                if(!hasInterceptedEvent){
                    x = event.getRawX();
                    activePointerId = event.getPointerId(0);
                    resetDone = false;
                    FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                    originalLeftMargin = layoutParamsOriginal.leftMargin;
                    dx = x - layoutParamsOriginal.leftMargin;
                    hasInterceptedEvent = true;
                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerId(event.getActionIndex()) == activePointerId && !resetDone) {}
                        else{break;}
                    case MotionEvent.ACTION_UP:
                        if(!resetDone) {
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
                                        .setInterpolator(new AccelerateInterpolator(mSwipeDecor.getSwipeAnimFactor()))
                                        .setDuration((long)(mSwipeDecor.getSwipeAnimTime() * 1.25))
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();
                            }
                            new CountDownTimer(mSwipeDecor.getSwipeAnimTime(), mSwipeDecor.getSwipeAnimTime()) {
                                public void onTick(long millisUntilFinished) {}
                                public void onFinish() {hasInterceptedEvent = false;}
                            }.start();
                            resetDone = true;
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

    /**
     *
     * @param view
     */
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

                if(!hasInterceptedEvent){
                    y = event.getRawY();
                    activePointerId = event.getPointerId(0);
                    resetDone = false;
                    FrameLayout.LayoutParams layoutParamsOriginal = (FrameLayout.LayoutParams) v.getLayoutParams();
                    originalTopMargin = layoutParamsOriginal.topMargin;
                    dy = y - layoutParamsOriginal.topMargin;
                    hasInterceptedEvent = true;
                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
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
                                        .setInterpolator(new AccelerateInterpolator(mSwipeDecor.getSwipeAnimFactor()))
                                        .setDuration((long)(mSwipeDecor.getSwipeAnimTime() * 1.25))
                                        .setListener(mViewRemoveAnimatorListener)
                                        .start();

                            }
                            new CountDownTimer(mSwipeDecor.getSwipeAnimTime(), mSwipeDecor.getSwipeAnimTime()) {
                                public void onTick(long millisUntilFinished) {}
                                public void onFinish() {hasInterceptedEvent = false;}
                            }.start();
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

    /**
     *
     * @param v
     * @param originalTopMargin
     * @param originalLeftMargin
     * @param swipeType
     */
    private void animateSwipeRestore(final View v, int originalTopMargin, int originalLeftMargin, int swipeType){
        final FrameLayout.LayoutParams layoutParamsFinal = (FrameLayout.LayoutParams) v.getLayoutParams();

        ValueAnimator animatorX = null;
        ValueAnimator animatorY = null;
        int animTime = mSwipeDecor.getSwipeAnimTime();
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(mSwipeDecor.getSwipeAnimFactor());
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

    /**
     *
     * @param isSwipeIn
     */
    protected void doSwipe(boolean isSwipeIn){
        if(mLayoutView != null && mViewRemoveAnimatorListener != null) {
            mLayoutView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent rawEvent) {
                    return false;
                }
            });

            DisplayMetrics displayMetrics = mLayoutView.getResources().getDisplayMetrics();
            ViewPropertyAnimator animator = mLayoutView.animate();

            float transX = displayMetrics.widthPixels;
            float transY = displayMetrics.heightPixels;
            switch (mSwipeType){
                case SwipePlaceHolderView.SWIPE_TYPE_DEFAULT:
                    if(isSwipeIn){
                        bindSwipeIn(getResolver());
                        animator.rotation(-mSwipeDecor.getSwipeRotationAngle());
                    }else{
                        bindSwipeOut(getResolver());
                        transX = -mLayoutView.getWidth();
                        animator.rotation(mSwipeDecor.getSwipeRotationAngle());
                    }
                    animator.translationX(transX).translationY(transY);
                    break;
                case SwipePlaceHolderView.SWIPE_TYPE_HORIZONTAL:
                    if(isSwipeIn){
                        bindSwipeIn(getResolver());
                    }else{
                        bindSwipeOut(getResolver());
                        transX = -mLayoutView.getWidth();
                    }
                    animator.translationX(transX);
                    break;
                case SwipePlaceHolderView.SWIPE_TYPE_VERTICAL:
                    if(isSwipeIn){
                        bindSwipeIn(getResolver());
                    }else{
                        bindSwipeOut(getResolver());
                        transY = -mLayoutView.getHeight();
                    }
                    animator.translationY(transY);
                    break;
            }

            animator.setDuration((long) (mSwipeDecor.getSwipeAnimTime() * 1.25))
                    .setInterpolator(new AccelerateInterpolator(mSwipeDecor.getSwipeAnimFactor()))
                    .setListener(mViewRemoveAnimatorListener)
                    .start();
        }
    }

    /**
     *
     * @return
     */
    protected View getSwipeInMsgView() {
        return mSwipeInMsgView;
    }

    /**
     *
     * @param swipeInMsgView
     */
    protected void setSwipeInMsgView(View swipeInMsgView) {
        mSwipeInMsgView = swipeInMsgView;
    }

    /**
     *
     * @return
     */
    protected View getSwipeOutMsgView() {
        return mSwipeOutMsgView;
    }

    /**
     *
     * @param swipeOutMsgView
     */
    protected void setSwipeOutMsgView(View swipeOutMsgView) {
        mSwipeOutMsgView = swipeOutMsgView;
    }

    /**
     *
     * @return
     */
    protected V getLayoutView() {
        return mLayoutView;
    }

    /**
     *
     * @param <T>
     */
    protected interface SwipeCallback<T extends SwipeViewBinder<?, ? extends FrameLayout>>{
        void onRemoveView(T swipeViewBinder);
        void onResetView(T swipeViewBinder);
        void onAnimateView(float distXMoved, float distYMoved, float finalXDist, float finalYDist, T swipeViewBinder);
    }
}
