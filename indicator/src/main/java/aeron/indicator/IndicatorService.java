package aeron.indicator;

import aeron.common.AeronChannels;
import aeron.common.MessageFormat;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Random;

@Component
public class IndicatorService {

    private final Aeron aeron;
    private final IdleStrategy idle = new SleepingIdleStrategy(1);
    private final Random random = new Random();

    public IndicatorService(Aeron aeron) {
        this.aeron = aeron;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread t = new Thread(this::run, "indicator");
        t.setDaemon(true);
        t.start();
    }

    private void run() {
        try (Subscription sub = aeron.addSubscription(AeronChannels.CHANNEL_IPC, AeronChannels.STREAM_PRICES);
             Publication pub = aeron.addPublication(AeronChannels.CHANNEL_IPC, AeronChannels.STREAM_SIGNALS)) {
            while (!sub.isConnected()) idle.idle();
            long lastStatusAt = System.currentTimeMillis();
            System.out.println("[indicator] connected to prices stream");
            FragmentHandler handler = (buffer, offset, length, header) -> {
                String line = buffer.getStringWithoutLengthAscii(offset, length);
                MessageFormat.Parsed p = MessageFormat.parse(line);
                if (p == null || !"PRICE".equals(p.type) || p.parts.length < 4) return;
                double close = Double.parseDouble(p.parts[3].trim());
                onPrice(close, pub);
            };
            while (true) {
                int n = sub.poll(handler, 10);
                if (n <= 0) idle.idle();
                long now = System.currentTimeMillis();
                if (now - lastStatusAt >= 5000) {
                    System.out.println("[indicator] heartbeat subConnected=" + sub.isConnected() + " pubConnected=" + pub.isConnected());
                    lastStatusAt = now;
                }
            }
        }
    }

    private void onPrice(double close, Publication pub) {
        if (!pub.isConnected()) return;
        String action = random.nextBoolean() ? "buy" : "sell";
        double strength = 1.0;
        String msg = MessageFormat.signal(action, strength, close);
        org.agrona.concurrent.UnsafeBuffer buf = new org.agrona.concurrent.UnsafeBuffer(ByteBuffer.allocate(128));
        buf.putBytes(0, MessageFormat.toBytes(msg));
        while (pub.offer(buf, 0, msg.length()) < 0) idle.idle();
        System.out.println("[indicator] " + msg);
    }
}
