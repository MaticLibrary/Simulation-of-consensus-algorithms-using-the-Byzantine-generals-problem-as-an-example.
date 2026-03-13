package com.example.animation.send;

import javafx.animation.PathTransition;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;

public class SendAnimationFactory {
    private final String defaultAttackImageURL = new ClassPathResource("icons/sword.png").getPath();
    private final String defaultDefenseImageURL = new ClassPathResource("icons/shield.png").getPath();
    @Setter
    private Image attackImage = new Image(defaultAttackImageURL, 30, 30, false, false);
    @Setter
    private Image defenseImage = new Image(defaultDefenseImageURL, 30, 30, false, false);
    @Setter
    private Duration duration;
    @Getter
    private final Duration baseDuration;

    public SendAnimationFactory(Duration duration){
        baseDuration = duration;
        this.duration = duration;
    }

    public PathTransition getAttackAnimation(Point2D from, Point2D to) {
        return getSendAnimation(from, to, new ImageView(attackImage));
    }

    public PathTransition getDefenseAnimation(Point2D from, Point2D to) {
        return getSendAnimation(from, to, new ImageView(defenseImage));
    }

    private PathTransition getSendAnimation(Point2D from, Point2D to, ImageView image) {
        image.setX(from.getX());
        image.setY(from.getY());

        Path path = new Path();
        path.getElements().add(new MoveTo(from.getX(), from.getY()));
        path.getElements().add(new LineTo(to.getX(), to.getY()));

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(duration);
        pathTransition.setNode(image);
        pathTransition.setPath(path);

        return pathTransition;
    }
}
