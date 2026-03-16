package aeron.indicator;

import io.aeron.Aeron;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IndicatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndicatorApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public Aeron aeron() {
        String dir = System.getProperty("aeron.dir", "/tmp/aeron-backtest");
        return Aeron.connect(new Aeron.Context().aeronDirectoryName(dir));
    }
}
