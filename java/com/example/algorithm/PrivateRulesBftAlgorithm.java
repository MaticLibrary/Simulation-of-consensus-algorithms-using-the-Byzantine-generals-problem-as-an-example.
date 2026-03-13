package com.example.algorithm;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.algorithm.operations.ChooseOperation;
import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import com.example.settings.AlgorithmSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.*;

public class PrivateRulesBftAlgorithm implements Algorithm {
    private MyGraph<Integer, Integer> graph;
    private AlgorithmPhase phase = AlgorithmPhase.SEND;
    private int round = 0;
    private int maxRounds = 5;
    private int f = 0;
    private int timeout = 1;
    private int sendElapsed = 0;
    private MyVertex<Integer> leader;

    private final Map<MyVertex<Integer>, PrivateRuleType> rules = new HashMap<>();
    private final Map<MyVertex<Integer>, NodeState> states = new HashMap<>();
    private final Map<Integer, KeyPair> keyPairs = new HashMap<>();
    private final Map<SignatureKey, byte[]> signatureCache = new HashMap<>();
    private final Map<MyVertex<Integer>, List<Message>> inbox = new HashMap<>();
    private final Map<MyVertex<Integer>, List<Message>> nextInbox = new HashMap<>();
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);

    private enum MessageType {
        PROPOSAL,
        ECHO,
        ALARM
    }

    private static final class SignatureEntry {
        private final int id;
        private final byte[] signature;

        private SignatureEntry(int id, byte[] signature) {
            this.id = id;
            this.signature = signature;
        }
    }

    private static final class Message {
        private final MessageType type;
        private final int sender;
        private final int round;
        private final boolean value;
        private final List<SignatureEntry> signatures;
        private final String reason;

        private Message(MessageType type, int sender, int round, boolean value, List<SignatureEntry> signatures, String reason) {
            this.type = type;
            this.sender = sender;
            this.round = round;
            this.value = value;
            this.signatures = signatures;
            this.reason = reason;
        }

        private static Message proposal(int sender, int round, boolean value, byte[] signature) {
            return new Message(MessageType.PROPOSAL, sender, round, value,
                    List.of(new SignatureEntry(sender, signature)), null);
        }

        private static Message echo(int sender, int round, boolean value, List<SignatureEntry> signatures) {
            return new Message(MessageType.ECHO, sender, round, value, signatures, null);
        }

        private static Message alarm(int sender, int round, String reason) {
            return new Message(MessageType.ALARM, sender, round, false, List.of(), reason);
        }
    }

    private static final class NodeState {
        private final Map<Boolean, Map<Integer, byte[]>> collected = new HashMap<>();
        private final Set<Boolean> sentEcho = new HashSet<>();
        private final Set<String> seenMessages = new HashSet<>();
        private boolean alarmTriggered = false;

        private NodeState() {
            collected.put(Boolean.TRUE, new HashMap<>());
            collected.put(Boolean.FALSE, new HashMap<>());
        }
    }

    private static final class SignatureKey {
        private final int signerId;
        private final int round;
        private final boolean value;

        private SignatureKey(int signerId, int round, boolean value) {
            this.signerId = signerId;
            this.round = round;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SignatureKey that = (SignatureKey) o;
            return signerId == that.signerId && round == that.round && value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(signerId, round, value);
        }
    }

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.PRIVATE_BFT;
    }

    @Override
    public void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings) {
        this.graph = graph;
        this.round = 0;
        this.phase = AlgorithmPhase.SEND;
        this.isFinished.set(false);
        this.sendElapsed = 0;

        if (settings != null && settings.getSettings() != null) {
            this.f = (int) settings.getSettings().getOrDefault("f", new com.example.settings.AlgorithmSetting<>("f", 0, Integer.class, v -> true)).getValue();
            this.timeout = (int) settings.getSettings().getOrDefault("timeout", new com.example.settings.AlgorithmSetting<>("timeout", 1, Integer.class, v -> true)).getValue();
            this.maxRounds = (int) settings.getSettings().getOrDefault("maxRounds", new com.example.settings.AlgorithmSetting<>("maxRounds", 5, Integer.class, v -> true)).getValue();
        }

        if (timeout < 2) {
            timeout = 2;
        }

        initializeKeys();
        assignRules();
        resetRoundState();
    }

    @Override
    public StepReport step() {
        if (graph == null || graph.numVertices() == 0) {
            StepReport report = new StepReport();
            report.setAlgorithmPhase(phase);
            report.getProperties().put("runda", "-");
            report.getProperties().put("lider", "-");
            report.getProperties().put("f", String.valueOf(f));
            report.getProperties().put("timeout", String.valueOf(timeout));
            report.getProperties().put("maks_rund", String.valueOf(maxRounds));
            isFinished.set(true);
            return report;
        }

        return phase == AlgorithmPhase.SEND ? performSendPhase() : performChoosePhase();
    }

    private StepReport performSendPhase() {
        if (sendElapsed == 0) {
            resetRoundState();
        }

        StepReport report = new StepReport();
        report.setAlgorithmPhase(AlgorithmPhase.SEND);

        OperationsBatch proposalBatch = new OperationsBatch();
        OperationsBatch echoBatch = new OperationsBatch();

        if (sendElapsed == 0) {
            enqueueLeaderProposals(proposalBatch);
        }

        processInbox(echoBatch);

        report.addBatch(proposalBatch);
        report.addBatch(echoBatch);
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        fillRoles(report, Collections.emptySet());
        fillCommonProperties(report);
        report.getProperties().put("zbieranie", (sendElapsed + 1) + "/" + timeout);

        sendElapsed++;
        if (sendElapsed >= timeout) {
            phase = AlgorithmPhase.CHOOSE;
        }

        return report;
    }

    private StepReport performChoosePhase() {
        StepReport report = new StepReport();
        report.setAlgorithmPhase(AlgorithmPhase.CHOOSE);

        OperationsBatch decisionBatch = new OperationsBatch();
        Set<MyVertex<Integer>> alarmNodes = new HashSet<>();

        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> v = (MyVertex<Integer>) vertex;
            Map<Boolean, Map<Integer, byte[]>> collected = states.get(v).collected;
            int n0 = collected.get(false).size();
            int n1 = collected.get(true).size();
            Boolean leaderValue = resolveLeaderValueFor(v);

            boolean decisionGlobal = decideGlobal(n0, n1, leaderValue);
            boolean decisionPrivate = rules.get(v).evaluate(n0, n1, leaderValue, graph.numVertices());
            boolean alarm = !v.isTraitor().get() && decisionGlobal != decisionPrivate;

            if (alarm) {
                alarmNodes.add(v);
                states.get(v).alarmTriggered = true;
            }

            boolean finalDecision = v.isTraitor().get()
                    ? v.getNextOpinion(v).get()
                    : decisionGlobal;
            v.setIsSupporting(finalDecision);
            decisionBatch.add(new ChooseOperation(v, v.getIsSupporting()));
        }

        report.addBatch(decisionBatch);
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        fillRoles(report, alarmNodes);

        report.getProperties().put("alarmy", String.valueOf(alarmNodes.size()));
        fillCommonProperties(report);

        if (alarmNodes.isEmpty() || round + 1 >= maxRounds) {
            isFinished.set(true);
        } else {
            round++;
            phase = AlgorithmPhase.SEND;
            sendElapsed = 0;
        }

        return report;
    }

    private void processInbox(OperationsBatch echoBatch) {
        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> receiver = (MyVertex<Integer>) vertex;
            List<Message> messages = inbox.getOrDefault(receiver, List.of());
            for (Message message : messages) {
                handleMessage(receiver, message, echoBatch);
            }
        }

        inbox.clear();
        inbox.putAll(nextInbox);
        nextInbox.clear();
    }

    private void handleMessage(MyVertex<Integer> receiver, Message message, OperationsBatch echoBatch) {
        if (message.round != round) {
            return;
        }
        if (message.type == MessageType.PROPOSAL && (leader == null || message.sender != leader.element())) {
            return;
        }
        if (!verifyMessage(message)) {
            return;
        }

        NodeState state = states.get(receiver);
        String key = messageKey(message);
        if (!state.seenMessages.add(key)) {
            return;
        }

        for (SignatureEntry entry : message.signatures) {
            addSignature(receiver, message.value, entry.id, entry.signature);
        }

        if (message.type == MessageType.PROPOSAL || message.type == MessageType.ECHO) {
            maybeSendEcho(receiver, message, echoBatch);
        }
    }

    private boolean verifyMessage(Message message) {
        if (message.type == MessageType.ALARM) {
            return true;
        }
        if (message.signatures == null || message.signatures.isEmpty()) {
            return false;
        }
        Set<Integer> seen = new HashSet<>();
        for (SignatureEntry entry : message.signatures) {
            if (!seen.add(entry.id)) {
                return false;
            }
            if (!verifySignature(entry.id, message.round, message.value, entry.signature)) {
                return false;
            }
        }
        return true;
    }

    private void maybeSendEcho(MyVertex<Integer> sender, Message message, OperationsBatch echoBatch) {
        NodeState state = states.get(sender);
        if (state.sentEcho.contains(message.value)) {
            return;
        }
        if (message.signatures.size() >= f + 1) {
            state.sentEcho.add(message.value);
            return;
        }

        byte[] ownSignature = signValue(sender.element(), round, message.value);
        if (ownSignature == null) {
            return;
        }

        List<SignatureEntry> baseSignatures = new ArrayList<>(message.signatures);
        baseSignatures.add(new SignatureEntry(sender.element(), ownSignature));
        state.sentEcho.add(message.value);

        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> receiver = (MyVertex<Integer>) vertex;
            boolean valueToSend = message.value;
            List<SignatureEntry> signaturesToSend = baseSignatures;

            if (sender.isTraitor().get()) {
                boolean traitorValue = sender.getNextOpinion(receiver).get();
                if (traitorValue != message.value) {
                    byte[] traitorSignature = signValue(sender.element(), round, traitorValue);
                    if (traitorSignature == null) {
                        continue;
                    }
                    valueToSend = traitorValue;
                    signaturesToSend = List.of(new SignatureEntry(sender.element(), traitorSignature));
                }
            }

            Message echo = Message.echo(sender.element(), round, valueToSend, new ArrayList<>(signaturesToSend));
            queueMessage(receiver, echo, false);
            echoBatch.add(new SendOperation(sender, receiver, new SimpleBooleanProperty(valueToSend)));
        }
    }

    private void enqueueLeaderProposals(OperationsBatch proposalBatch) {
        if (leader == null) {
            return;
        }
        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> receiver = (MyVertex<Integer>) vertex;
            boolean value = leaderOpinionFor(receiver);
            byte[] signature = signValue(leader.element(), round, value);
            if (signature == null) {
                continue;
            }
            Message proposal = Message.proposal(leader.element(), round, value, signature);
            queueMessage(receiver, proposal, true);
            proposalBatch.add(new SendOperation(leader, receiver, new SimpleBooleanProperty(value)));
        }
    }

    private void queueMessage(MyVertex<Integer> receiver, Message message, boolean immediate) {
        Map<MyVertex<Integer>, List<Message>> target = immediate ? inbox : nextInbox;
        target.computeIfAbsent(receiver, key -> new ArrayList<>()).add(message);
    }

    private String messageKey(Message message) {
        StringBuilder builder = new StringBuilder();
        builder.append(message.type).append("|")
                .append(message.round).append("|")
                .append(message.sender).append("|")
                .append(message.value ? "1" : "0").append("|");
        for (SignatureEntry entry : message.signatures) {
            builder.append(entry.id).append(":").append(Arrays.hashCode(entry.signature)).append(",");
        }
        return builder.toString();
    }

    private void fillCommonProperties(StepReport report) {
        report.getProperties().put("runda", String.valueOf(round));
        report.getProperties().put("lider", leader == null ? "-" : leader.element().toString());
        report.getProperties().put("f", String.valueOf(f));
        report.getProperties().put("timeout", String.valueOf(timeout));
        report.getProperties().put("maks_rund", String.valueOf(maxRounds));
        int totalRules = PrivateRuleType.values().length;
        report.getProperties().put("reguły", totalRules + " (przypisanie: ID mod " + totalRules + ")");
    }

    private void initializeKeys() {
        keyPairs.clear();
        signatureCache.clear();
        if (graph == null) {
            return;
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            for (Vertex<Integer> vertex : graph.vertices()) {
                MyVertex<Integer> v = (MyVertex<Integer>) vertex;
                keyPairs.put(v.element(), generator.generateKeyPair());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Nie udało się zainicjalizować kluczy.", e);
        }
    }

    private byte[] signValue(int signerId, int round, boolean value) {
        SignatureKey key = new SignatureKey(signerId, round, value);
        byte[] cached = signatureCache.get(key);
        if (cached != null) {
            return cached;
        }
        KeyPair keyPair = keyPairs.get(signerId);
        if (keyPair == null) {
            return null;
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(signaturePayload(round, value));
            byte[] result = signature.sign();
            signatureCache.put(key, result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean verifySignature(int signerId, int round, boolean value, byte[] signatureBytes) {
        SignatureKey key = new SignatureKey(signerId, round, value);
        byte[] cached = signatureCache.get(key);
        if (cached != null && Arrays.equals(cached, signatureBytes)) {
            return true;
        }
        KeyPair keyPair = keyPairs.get(signerId);
        if (keyPair == null || signatureBytes == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(keyPair.getPublic());
            signature.update(signaturePayload(round, value));
            boolean verified = signature.verify(signatureBytes);
            if (verified && cached == null) {
                signatureCache.put(key, signatureBytes);
            }
            return verified;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] signaturePayload(int round, boolean value) {
        String payload = round + "|" + (value ? "1" : "0");
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private void fillRoles(StepReport report, Set<MyVertex<Integer>> alarms) {
        for (Vertex<Integer> v : graph.vertices()) {
            MyVertex<Integer> vertex = (MyVertex<Integer>) v;
            if (alarms.contains(vertex)) {
                report.getRoles().put(v, VertexRole.ALARM);
            } else if (leader != null && v.equals(leader)) {
                report.getRoles().put(v, VertexRole.COMMANDER);
            } else {
                report.getRoles().put(v, VertexRole.NONE);
            }
        }
    }

    private boolean leaderOpinionFor(MyVertex<Integer> receiver) {
        if (leader == null) return receiver.isSupportingOpinion().get();
        return leader.getNextOpinion(receiver).get();
    }

    private void addSignature(MyVertex<Integer> receiver, boolean value, int signerId, byte[] signature) {
        if (!verifySignature(signerId, round, value, signature)) {
            return;
        }
        Map<Boolean, Map<Integer, byte[]>> valueMap = states.get(receiver).collected;
        valueMap.get(value).putIfAbsent(signerId, signature);
    }

    private Boolean resolveLeaderValueFor(MyVertex<Integer> receiver) {
        if (leader == null) return null;
        int leaderId = leader.element();
        Map<Boolean, Map<Integer, byte[]>> collected = states.get(receiver).collected;
        boolean leaderSentOne = collected.get(true).containsKey(leaderId);
        boolean leaderSentZero = collected.get(false).containsKey(leaderId);
        if (leaderSentOne && !leaderSentZero) return true;
        if (leaderSentZero && !leaderSentOne) return false;
        return null;
    }

    private boolean decideGlobal(int n0, int n1, Boolean leaderValue) {
        if (n1 > n0) return true;
        if (n0 > n1) return false;
        return leaderValue != null ? leaderValue : false;
    }

    private void assignRules() {
        rules.clear();
        PrivateRuleType[] allRules = PrivateRuleType.values();
        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> v = (MyVertex<Integer>) vertex;
            int index = Math.floorMod(v.element(), allRules.length);
            rules.put(v, allRules[index]);
        }
    }

    private void resetRoundState() {
        states.clear();
        inbox.clear();
        nextInbox.clear();
        sendElapsed = 0;

        for (Vertex<Integer> vertex : graph.vertices()) {
            MyVertex<Integer> v = (MyVertex<Integer>) vertex;
            states.put(v, new NodeState());
        }

        leader = resolveLeader();
    }

    private MyVertex<Integer> resolveLeader() {
        int size = graph.numVertices();
        if (size == 0) return null;
        int leaderIndex = Math.floorMod(round, size);
        return (MyVertex<Integer>) graph.vertices().stream().toList().get(leaderIndex);
    }

    @Override
    public boolean isFinished() {
        return isFinished.get();
    }

    @Override
    public BooleanProperty getIsFinishedProperty() {
        return isFinished;
    }
}
