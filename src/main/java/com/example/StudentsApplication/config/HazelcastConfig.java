package com.example.StudentsApplication.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración explícita de Hazelcast para garantizar que exista un HazelcastInstance en el contexto.
 * Mantengo un cluster embebido y defino los mapas que usa StudentCache.
 */
@Configuration
public class HazelcastConfig {

    @Bean(name = "hazelcastCoreConfig")
    public Config hazelcastCoreConfig() {
        Config cfg = new Config();
        cfg.setClusterName("students-cluster");
        cfg.addMapConfig(new MapConfig("students").setTimeToLiveSeconds(0));
        cfg.addMapConfig(new MapConfig("students_list").setTimeToLiveSeconds(300));
        return cfg;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(@Qualifier("hazelcastCoreConfig") Config cfg) {
        return Hazelcast.newHazelcastInstance(cfg);
    }
}