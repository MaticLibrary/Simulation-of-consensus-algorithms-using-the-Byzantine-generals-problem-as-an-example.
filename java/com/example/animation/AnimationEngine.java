package com.example.animation;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode;
import com.example.algorithm.VertexRole;
import com.example.algorithm.operations.ChooseOperation;
import com.example.algorithm.operations.Operation;
import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.animation.choose.ChooseAnimationFactory;
import com.example.animation.send.SendAnimationFactory;
import com.example.controller.GraphController;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.concurrent.Semaphore;

@Service
public class AnimationEngine {
    @Setter
    protected GraphController graphController;
    private final AnimationRunner animationRunner = new AnimationRunner();
    private final SendAnimationFactory sendAnimationFactory = new SendAnimationFactory(new Duration(1500));
    private final ChooseAnimationFactory chooseAnimationFactory = new ChooseAnimationFactory(new Duration(3000));

    public AnimationEngine(GraphController graphController) {
        this.graphController = graphController;
    }

    public void animate(Operation operation) {
        animationRunner.runAnimation(getAnimation(operation));
    }

    public void animate(StepReport report) {
        highlightRoles(report.getRoles());
        for (OperationsBatch batch : report.getOperationsBatches()) {
            try {
                animateBatch(batch);
            } catch (AnimationInterruptedException e) {
                break;
            }
        }
    }

    private void animateBatch(OperationsBatch batch) {
        List<Animation> animations = batch.getOperations()
                .stream()
                .map(this::getAnimation)
                .toList();
        animationRunner.runAnimationsConcurrently(animations);
    }

    private void highlightRoles(Map<Vertex<Integer>, VertexRole> roles) {
        for (Map.Entry<Vertex<Integer>, VertexRole> entry : roles.entrySet()) {
            graphController.highlightRole(entry.getKey(), entry.getValue());
        }
    }

    private Animation getAnimation(Operation operation) {
        switch (operation.getType()) {
            case SEND -> {
                SendOperation sendOperation = (SendOperation) operation;
                Point2D fromPosition = runOnFxThreadAndWait(() -> graphController.getVertexPosition(sendOperation.getFrom()));
                Point2D toPosition = runOnFxThreadAndWait(() -> graphController.getVertexPosition(sendOperation.getTo()));
                return getSendAnimation(fromPosition, toPosition, sendOperation.getSentOpinion().get());
            }
            case CHOOSE -> {
                ChooseOperation chooseOperation = (ChooseOperation) operation;
                SmartGraphVertexNode<Integer> node = runOnFxThreadAndWait(
                        () -> (SmartGraphVertexNode<Integer>) graphController.getGraphView().getStylableVertex(chooseOperation.getVertex().element())
                );
                return getChooseOpinionAnimation(node);
            }
        }
        throw new IllegalArgumentException(operation.getType() + " is not allowed");
    }

    private Animation getSendAnimation(Point2D from, Point2D to, boolean attack) {

        PathTransition animation = attack ? sendAnimationFactory.getAttackAnimation(from, to) : sendAnimationFactory.getDefenseAnimation(from, to);

        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            try {
                graphController.addNodeToView(animation.getNode());
                semaphore.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        animation.setOnFinished(
                e -> Platform.runLater(() -> graphController.removeNodeFromView(animation.getNode())));

        return animation;
    }

    private Animation getChooseOpinionAnimation(SmartGraphVertexNode<Integer> vertex) {
        return chooseAnimationFactory.getChooseOpinionAnimation(vertex, e -> graphController.changeVertexStrokeStyle(vertex.getUnderlyingVertex()));
    }

    public void setAnimationsSpeed(Double multiplier) {
        sendAnimationFactory.setDuration(sendAnimationFactory.getBaseDuration().divide(multiplier));
        chooseAnimationFactory.setDuration(chooseAnimationFactory.getBaseDuration().divide(multiplier));
    }

    private <T> T runOnFxThreadAndWait(Supplier<T> supplier) {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<RuntimeException> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (RuntimeException e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }
}
