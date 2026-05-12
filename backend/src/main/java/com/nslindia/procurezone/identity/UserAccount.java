package com.nslindia.procurezone.identity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_user_master")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String username;

    @Column(name = "user_password", nullable = false, length = 500)
    private String passwordHash;

    @Column(name = "user_login_ip", length = 100)
    private String lastLoginIp;

    @Column(name = "user_status", nullable = false)
    private Integer status;

    @Column(name = "user_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "user_lmu", nullable = false)
    private Integer lastModifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_number", nullable = false)
    private Employee employee;

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Integer getLastModifiedUser() {
        return lastModifiedUser;
    }

    public Employee getEmployee() {
        return employee;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setLastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setLastModifiedUser(Integer lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // Business methods
    public boolean isActive() {
        return Objects.equals(status, 1);
    }
}
