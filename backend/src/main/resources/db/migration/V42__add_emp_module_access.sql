CREATE TABLE IF NOT EXISTS tbl_map_emp_module_access (
    access_id INT NOT NULL AUTO_INCREMENT,
    access_emp INT NOT NULL,
    access_module VARCHAR(50) NOT NULL,
    access_enabled TINYINT(1) NOT NULL DEFAULT 1,
    access_granted_by INT NOT NULL,
    access_granted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (access_id),
    UNIQUE KEY uq_emp_module (access_emp, access_module),
    CONSTRAINT fk_ema_emp FOREIGN KEY (access_emp)
        REFERENCES tbl_emp_master(emp_number),
    CONSTRAINT fk_ema_granted FOREIGN KEY (access_granted_by)
        REFERENCES tbl_emp_master(emp_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
