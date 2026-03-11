package aeron.marketdata;

import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class MarketDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataApplication.class, args);
    }

    public static String aeronDir() {
        return new File(System.getProperty("user.dir"), "aeron-marketdata").getAbsolutePath();
    }

    @Bean(destroyMethod = "close")
    public MediaDriver mediaDriver() {
        return MediaDriver.launchEmbedded(
                new MediaDriver.Context().dirDeleteOnStart(true).aeronDirectoryName(aeronDir()));
    }

    @Bean(destroyMethod = "close")
    public Aeron aeron(MediaDriver mediaDriver) {
        return Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
    }
}
