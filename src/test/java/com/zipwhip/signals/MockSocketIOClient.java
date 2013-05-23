package com.zipwhip.signals;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.parser.Packet;
import com.corundumstudio.socketio.parser.PacketType;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Date: 5/14/13
 * Time: 7:41 PM
 *
 * @author Michael
 * @version 1
 */
public class MockSocketIOClient implements SocketIOClient {

    private CountDownLatch latch = new CountDownLatch(1);
    private Packet packet;
    private UUID sessionId = UUID.randomUUID();

    public MockSocketIOClient(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public MockSocketIOClient() {
    }

    @Override
    public Transport getTransport() {
        return null;
    }

    @Override
    public void sendEvent(String name, Object data, AckCallback<?> ackCallback) {
        Packet packet = new Packet(PacketType.EVENT);

        packet.setName(name);
        packet.setData(data);

        send(packet, ackCallback);
    }

    @Override
    public void send(Packet packet, AckCallback<?> ackCallback) {
        this.packet = packet;

        this.latch.countDown();
    }

    @Override
    public void sendJsonObject(Object object, AckCallback<?> ackCallback) {
        Packet packet = new Packet(PacketType.JSON);

        packet.setData(object);

        send(packet, ackCallback);
    }

    @Override
    public void sendMessage(String message, AckCallback<?> ackCallback) {
        Packet packet = new Packet(PacketType.MESSAGE);

        packet.setData(message);

        send(packet, ackCallback);
    }

    @Override
    public SocketIONamespace getNamespace() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String message) {
        Packet packet = new Packet(PacketType.MESSAGE);

        packet.setData(message);

        send(packet);
    }

    @Override
    public void sendJsonObject(Object object) {
        Packet packet = new Packet(PacketType.JSON);

        packet.setData(object);

        send(packet);
    }

    @Override
    public void send(Packet packet) {
        send(packet, null);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendEvent(String name, Object data) {
        Packet packet = new Packet(PacketType.EVENT);

        packet.setName(name);
        packet.setData(data);

        send(packet);
    }

    public void reset() {
        latch = new CountDownLatch(1);
        packet = null;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
