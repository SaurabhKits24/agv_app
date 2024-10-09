package com.reeman.agv.utils;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;

public class MulticastMessageSender {
    private final String host = "239.0.0.1";
    private final int port = 7979;
    private final InetAddress inetAddress;
    private final MulticastSocket multicastSocket;
    private Disposable disposable;

    public MulticastMessageSender() throws Exception {
        this.inetAddress = InetAddress.getByName(host);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(inetAddress);
    }

    public void startSendingMulticast(String msg) {
        disposable = Observable.interval(0,2, TimeUnit.SECONDS)
                .subscribe(tick -> sendMulticastMessage(msg));
    }

    private void sendMulticastMessage(String msg) {
        try {
            byte[] data = msg.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);
            multicastSocket.send(datagramPacket);
            Timber.d("组播 : %s",msg);
        } catch (Exception e) {
            Timber.w("组播失败");
            e.printStackTrace();
        }
    }

    public void stopSendingMulticast() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        try {
            multicastSocket.leaveGroup(inetAddress);
            multicastSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
