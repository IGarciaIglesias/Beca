package aeron.marketdata;

import aeron.common.AeronChannels;
import aeron.common.MessageFormat;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class CsvPricePublisher {

    private final Aeron aeron;
    private final ResourceLoader resourceLoader;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocate(512));
    private final IdleStrategy idle = new SleepingIdleStrategy(1);

    public CsvPricePublisher(Aeron aeron, ResourceLoader resourceLoader) {
        this.aeron = aeron;
        this.resourceLoader = resourceLoader;
    }

    @org.springframework.context.event.EventListener(ApplicationReadyEvent.class)
    public void startPublishing() {
        String path = System.getProperty("csv.path", "classpath:data/HitBTC_BTCUSD_1h.csv");
        Thread t = new Thread(() -> run(path), "csv-publisher");
        t.setDaemon(false);
        t.start();
    }

    private void run(String path) {
        try (Publication pub = aeron.addPublication(AeronChannels.CHANNEL_IPC, AeronChannels.STREAM_PRICES)) {
            long sent = 0;
            Resource r = path.startsWith("classpath:") ? resourceLoader.getResource(path) : resourceLoader.getResource("file:" + path);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                String symbol = System.getProperty("symbol", "EURUSD");
                System.out.println("[market-data] publishing prices from: " + path);
                while ((line = br.readLine()) != null) {
                    String[] c = line.split("[,;\t]");
                    if (c.length < 5) continue;
                    if (isHeaderOrCommentRow(c)) continue;
                    long ts = parseTimestamp(c[0]);
                    double close;
                    try {
                        close = Double.parseDouble(c[4].trim());
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    String msg = MessageFormat.price(ts, symbol, close);
                    buffer.putBytes(0, MessageFormat.toBytes(msg));
                    while (pub.offer(buffer, 0, msg.length()) < 0) idle.idle();
                    sent++;
                    if (sent % 20 == 0) {
                        System.out.println("[market-data] sent prices: " + sent);
                    }
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                }
                System.out.println("[market-data] finished publishing. Total prices: " + sent);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long parseTimestamp(String s) {
        s = s.trim();
        if (s.length() == 10 && s.charAt(4) == '-') {
            return java.time.LocalDate.parse(s).atStartOfDay(java.time.ZoneId.of("UTC")).toEpochSecond();
        }
        try { return Long.parseLong(s); } catch (NumberFormatException ignored) { }
        try { return (long) Double.parseDouble(s); } catch (NumberFormatException ignored) { }
        return System.currentTimeMillis() / 1000;
    }

    private static boolean isHeaderOrCommentRow(String[] cols) {
        String c0 = cols[0].trim().toLowerCase();
        String c1 = cols.length > 1 ? cols[1].trim().toLowerCase() : "";
        return c0.isEmpty()
                || c0.startsWith("timezones")
                || c0.startsWith("unix timestamp")
                || c0.equals("date")
                || c1.equals("date");
    }
}
