package co.smartreceipts.android.apis.hosts;


import android.net.TrafficStats;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * To avoid StrictMode violations that occur in Android O, we tag all of our OkHttp requests with
 * a predetermined traffic stats tag as detailed here:
 *
 * https://github.com/square/okhttp/issues/3537
 */
public class TrafficStatsSocketFactory extends SocketFactory {

    private static final int TRAFFIC_STATS_TAG = 1;
    private final SocketFactory socketFactory;

    public TrafficStatsSocketFactory() {
        this.socketFactory = SocketFactory.getDefault();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        return socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        return socketFactory.createSocket(address, port, localAddress, localPort);
    }
}
