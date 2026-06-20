package com.example.security;

import com.example.security.core.Role;
import com.example.security.core.User;
import com.example.security.core.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PerformanceBenchmarkTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    public void benchmarkSaveVsSaveAll() {
        System.out.println("Starting benchmark...");

        // Warmup
        List<User> warmupUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            warmupUsers.add(new User("warmup" + i, passwordEncoder.encode("password"), Role.USER));
        }
        userRepository.saveAll(warmupUsers);
        userRepository.deleteAll();

        // Benchmark individual saves
        long startSave = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            userRepository.save(new User("user_save_" + i, "encoded_pass", Role.USER));
        }
        long endSave = System.currentTimeMillis();
        long saveDuration = endSave - startSave;
        System.out.println("Time taken for 1000 individual saves: " + saveDuration + " ms");
        userRepository.deleteAll();

        // Benchmark saveAll
        List<User> saveAllUsers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            saveAllUsers.add(new User("user_saveall_" + i, "encoded_pass", Role.USER));
        }
        long startSaveAll = System.currentTimeMillis();
        userRepository.saveAll(saveAllUsers);
        long endSaveAll = System.currentTimeMillis();
        long saveAllDuration = endSaveAll - startSaveAll;
        System.out.println("Time taken for saveAll of 1000 users: " + saveAllDuration + " ms");

        System.out.println("Improvement: " + (saveDuration - saveAllDuration) + " ms");
    }
}
