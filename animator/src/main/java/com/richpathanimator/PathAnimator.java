package com.richpathanimator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.support.annotation.IntDef;
import android.view.animation.Interpolator;

import com.richpath.RichPath;

import java.util.ArrayList;
import java.util.List;


public class PathAnimator {

    private long duration = -1;
    private long startDelay = -1;
    private Interpolator interpolator;

    private List<AnimationBuilder> animationBuilders = new ArrayList<>();

    private AnimatorSet animatorSet;

    private PathAnimator prev;
    private PathAnimator next;

    private AnimationListener animationListener;


    @IntDef({RESTART, REVERSE})
    public @interface RepeatMode {
    }

    /**
     * When the animation reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the animation restarts from the beginning.
     */
    public static final int RESTART = 1;
    /**
     * When the animation reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the animation reverses direction on every iteration.
     */
    public static final int REVERSE = 2;


    private PathAnimator() {
    }

    AnimationBuilder addAnimationBuilder(RichPath... paths) {
        AnimationBuilder animationBuilder = new AnimationBuilder(this, paths);
        animationBuilders.add(animationBuilder);
        return animationBuilder;
    }

    public static AnimationBuilder animate(RichPath... paths) {
        PathAnimator viewAnimator = new PathAnimator();
        return viewAnimator.addAnimationBuilder(paths);
    }

    AnimationBuilder thenAnimate(RichPath... paths) {
        PathAnimator nextPathAnimator = new PathAnimator();
        this.next = nextPathAnimator;
        nextPathAnimator.prev = this;
        return nextPathAnimator.addAnimationBuilder(paths);
    }


    private AnimatorSet createAnimatorSet() {
        List<Animator> animators = new ArrayList<>();
        for (AnimationBuilder animationBuilder : animationBuilders) {
            List<ValueAnimator> animatorList = animationBuilder.getAnimators();
            animators.addAll(animatorList);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);

        if (duration != -1) {
            animatorSet.setDuration(duration);
        }

        if (startDelay != -1) {
            animatorSet.setStartDelay(startDelay);
        }

        if (interpolator != null) {
            animatorSet.setInterpolator(interpolator);
        }

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (animationListener != null) {
                    animationListener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationListener != null) {
                    animationListener.onStop();
                }
                if (next != null) {
                    next.prev = null;
                    next.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        return animatorSet;
    }

    PathAnimator start() {
        if (prev != null) {
            prev.start();
        } else {
            animatorSet = createAnimatorSet();

            animatorSet.start();

        }
        return this;
    }

    public void cancel() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        if (next != null) {
            next.cancel();
            next = null;
        }
    }

    PathAnimator duration(long duration) {
        this.duration = duration;
        return this;
    }

    PathAnimator startDelay(long startDelay) {
        this.startDelay = startDelay;
        return this;
    }

    PathAnimator interpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    void setAnimationListener(AnimationListener animationListener) {
        this.animationListener = animationListener;
    }
}