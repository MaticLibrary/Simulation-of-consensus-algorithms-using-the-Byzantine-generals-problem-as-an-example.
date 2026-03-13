package com.example.animation.choose;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class ChooseAnimationFactory {
    @Setter
    private Duration duration;
    @Getter
    private final Duration baseDuration;
    @Setter
    private double scale = 1.5;

    public ChooseAnimationFactory(Duration duration){
        baseDuration = duration;
        this.duration = duration;
    }

    public Animation getChooseOpinionAnimation(Node node, Consumer<ActionEvent> chooseAction) {
        ScaleTransition scaleUp = new ScaleTransition(duration.divide(2), node);
        scaleUp.setToX(scale);
        scaleUp.setToY(scale);

        scaleUp.setOnFinished(chooseAction::accept);

        ScaleTransition scaleDown = new ScaleTransition(duration.divide(2), node);
        scaleDown.setToX(1);
        scaleDown.setToY(1);

        return new SequentialTransition(scaleUp, scaleDown);
    }
}
