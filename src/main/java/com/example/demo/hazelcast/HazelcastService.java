package com.example.demo.hazelcast;

import com.example.demo.Estudiante;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HazelcastService {

    private Estudiante estudiante;
    @Autowired
    private HazelcastInstance hazelcastInstance;

}