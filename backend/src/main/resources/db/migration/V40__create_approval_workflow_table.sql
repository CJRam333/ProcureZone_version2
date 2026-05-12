-- Create approval workflow table for tracking indent approval history
CREATE TABLE IF NOT EXISTS tbl_approval_workflow (
    workflow_id INT AUTO_INCREMENT PRIMARY KEY,
    indent_id INT NOT NULL,
    approver_emp_number INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    action_date DATETIME NOT NULL,
    remarks TEXT,
    level INT NOT NULL,
    info_requested TEXT,
    FOREIGN KEY (indent_id) REFERENCES tbl_indent_master(indent_id),
    FOREIGN KEY (approver_emp_number) REFERENCES tbl_emp_master(emp_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
