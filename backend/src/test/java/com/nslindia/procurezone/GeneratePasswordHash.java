package com.nslindia.procurezone;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        System.out.println("BCrypt hash for 'password123': " + hash);
        System.out.println("Verify: " + encoder.matches("password123", hash));
    }
}
