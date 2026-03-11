package aeron.common;

import java.nio.charset.StandardCharsets;

/**
 * Mensajes CSV sobre Aeron.
 * PRICE:  timestamp,symbol,close
 * SIGNAL: action,strength,price   (action=buy|sell)
 * ORDER:  action,price
 */
public final class MessageFormat {
    public static String price(long timestamp, String symbol, double close) {
        return "PRICE," + timestamp + "," + symbol + "," + close;
    }

    public static String signal(String action, double strength, double price) {
        return "SIGNAL," + action + "," + strength + "," + price;
    }

    public static String order(String action, double price) {
        return "ORDER," + action + "," + price;
    }

    public static byte[] toBytes(String msg) {
        return msg.getBytes(StandardCharsets.US_ASCII);
    }

    public static Parsed parse(String line) {
        if (line == null || line.isEmpty()) return null;
        String[] p = line.split(",", -1);
        if (p.length < 2) return null;
        return new Parsed(p[0], p);
    }

    public static final class Parsed {
        public final String type;
        public final String[] parts;

        Parsed(String type, String[] parts) {
            this.type = type;
            this.parts = parts;
        }
    }

    private MessageFormat() {}
}
