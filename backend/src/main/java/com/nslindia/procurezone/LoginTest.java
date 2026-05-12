package com.nslindia.procurezone;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.nslindia.procurezone.auth.service.AuthService;
import com.nslindia.procurezone.auth.dto.LoginRequest;

public class LoginTest {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ProcurezoneBackendApplication.class, args);
        AuthService authService = context.getBean(AuthService.class);
        System.out.println("----- TRIGGERING LOGIN -----");
        try {
            authService.login(new LoginRequest("rajesh.kumar", "password123"));
        } catch (Exception e) {
            System.out.println("Login Failed: " + e.getMessage());
        }
        System.out.println("----- LOGIN FINISHED -----");
        System.exit(0);
    }
}
