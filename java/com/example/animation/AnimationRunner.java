package com.example.animation;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AnimationRunner {

    public void runAnimationsConcurrently(List<Animation> animations) {
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(animations);
        runAnimation(parallelTransition);
    }

    public void runAnimation(Animation animation) {
        if (Platform.isFxApplicationThread()) {
            animation.play();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            animation.setOnFinished(e -> latch.countDown());
            animation.play();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            animation.jumpTo(animation.getTotalDuration().subtract(Duration.millis(1)));
            throw new AnimationInterruptedException();
        }
    }
}
