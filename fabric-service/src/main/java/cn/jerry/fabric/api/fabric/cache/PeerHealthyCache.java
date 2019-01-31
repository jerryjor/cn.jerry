package cn.jerry.fabric.api.fabric.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PeerHealthyCache {
    private static Logger log = LogManager.getLogger();

    private static Map<String, List<PeerHealthy>> channelsHealthy = new HashMap<>();
    private static Map<String, Map<Peer, List<Long>>> latestHealthy = new HashMap<>();
    private static final long CLEAR_INTERNAL = 6L;

    static {
        // 每5秒刷新健康数据
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new RefreshThread(), 5,
                5, TimeUnit.SECONDS);
        // 每6小时清除健康数据
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ClearThread(), CLEAR_INTERNAL,
                CLEAR_INTERNAL, TimeUnit.HOURS);
    }

    public static Peer getHealthyPeer(Channel channel, String ccName, String method) {
        List<PeerHealthy> healthyList = channelsHealthy.computeIfAbsent(genKey(channel.getName(), ccName, method),
                k -> new ArrayList<>());
        if (healthyList.isEmpty()) {
            channel.getPeers().forEach((peer) -> healthyList.add(new PeerHealthy(peer, 0L)));
        }
        return healthyList.get(0).peer;
    }

    public static void logPeerHealthyData(String channelName, String ccName, String method, Peer peer, long respTimeMs) {
        Map<Peer, List<Long>> healthyMap = latestHealthy.computeIfAbsent(genKey(channelName, ccName, method),
                k -> new HashMap<>());
        List<Long> respMsList = healthyMap.computeIfAbsent(peer, k -> new ArrayList<>());
        respMsList.add(respTimeMs);
    }

    public static void removeUnhealthyPeer(String channelName, String ccName, String method, Peer peer) {
        List<PeerHealthy> healthyList = channelsHealthy.get(genKey(channelName, ccName, method));
        if (healthyList == null || healthyList.isEmpty()) return;

        PeerHealthy peerHealthy = new PeerHealthy(peer, 0L);
        healthyList.remove(peerHealthy);
    }

    private static String genKey(String channelName, String ccName, String method) {
        return String.format("%s-%s-%s", channelName, ccName, method);
    }

    static class PeerLatestHealthy {
        private Peer peer;
        private List<Long> respMsList;

        public PeerLatestHealthy(Peer peer, List<Long> respMsList) {
            this.peer = peer;
            this.respMsList = respMsList;
        }
    }

    static class PeerHealthy implements Comparable<PeerHealthy> {
        private Peer peer;
        private long lastRespMs;

        PeerHealthy(Peer peer, long lastRespMs) {
            this.peer = peer;
            this.lastRespMs = lastRespMs;
        }

        @Override
        public int compareTo(PeerHealthy o) {
            return Long.compare(this.lastRespMs, o.lastRespMs);
        }

        @Override
        public int hashCode() {
            return this.peer.getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PeerHealthy && this.peer.getName().equals(((PeerHealthy) obj).peer.getName());
        }
    }

    static class RefreshThread implements Runnable {

        @Override
        public void run() {
            Map<String, Map<Peer, List<Long>>> finalHealthy = latestHealthy;
            latestHealthy = new HashMap<>();
            finalHealthy.forEach((key, latestMap) -> {
                List<PeerHealthy> healthyList = channelsHealthy.computeIfAbsent(key, k -> new ArrayList<>());
                latestMap.forEach((peer, respMsList) -> {
                    // 计算平均响应时间
                    BigDecimal avg = BigDecimal.ZERO;
                    for (int i = 0; i < respMsList.size(); i++) {
                        avg = avg.multiply(new BigDecimal(i).divide(new BigDecimal(i + 1), 5, BigDecimal.ROUND_HALF_UP));
                        avg = avg.add(new BigDecimal(respMsList.get(i)).divide(new BigDecimal(i + 1), 5, BigDecimal.ROUND_HALF_UP));
                    }
                    PeerHealthy temp = new PeerHealthy(peer, avg.setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
                    healthyList.remove(temp);
                    healthyList.add(temp);
                });
                Collections.sort(healthyList);
            });
        }
    }

    static class ClearThread implements Runnable {

        @Override
        public void run() {
            log.info("clearing peers healthy data...");
            channelsHealthy.clear();
        }
    }
}
