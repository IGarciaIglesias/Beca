package aeron.pricing;

import io.aeron.Aeron;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public Aeron aeron() {
        String dir = new File(System.getProperty("user.dir"), "aeron-marketdata").getAbsolutePath();
        return Aeron.connect(new Aeron.Context().aeronDirectoryName(dir));
    }
}
