package com.nslindia.procurezone;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import com.nslindia.procurezone.auth.service.AuthService;
import com.nslindia.procurezone.auth.dto.LoginRequest;

@Component
public class LoginTester {
    @Autowired
    private AuthService authService;

    @PostConstruct
    public void testLogin() {
        System.out.println("----- TRIGGERING LOGIN -----");
        try {
            authService.login(new LoginRequest("rajesh.kumar", "password123"));
            System.out.println("Login Succeeded!");
        } catch (Exception e) {
            System.out.println("Login Failed: " + e.getMessage());
        }
        System.out.println("----- LOGIN FINISHED -----");
    }
}
