package aeron.pricing;

import aeron.common.AeronChannels;
import aeron.common.MessageFormat;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PricingService {

    private final Aeron aeron;
    private final IdleStrategy idle = new SleepingIdleStrategy(1);

    public PricingService(Aeron aeron) {
        this.aeron = aeron;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread t = new Thread(this::run, "pricing");
        t.setDaemon(true);
        t.start();
    }

    private void run() {
        try (Subscription sub = aeron.addSubscription(AeronChannels.CHANNEL_IPC, AeronChannels.STREAM_SIGNALS)) {
            long lastStatusAt = System.currentTimeMillis();
            System.out.println("[pricing] listening to signals stream");
            FragmentHandler handler = (buffer, offset, length, header) -> {
                String line = buffer.getStringWithoutLengthAscii(offset, length);
                MessageFormat.Parsed p = MessageFormat.parse(line);
                if (p == null || !"SIGNAL".equals(p.type) || p.parts.length < 4) return;
                System.out.println("[pricing] " + line);
            };
            while (true) {
                int n = sub.poll(handler, 10);
                if (n <= 0) idle.idle();
                long now = System.currentTimeMillis();
                if (now - lastStatusAt >= 5000) {
                    System.out.println("[pricing] heartbeat subConnected=" + sub.isConnected());
                    lastStatusAt = now;
                }
            }
        }
    }
}
