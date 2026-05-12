-- Update test user password to 'password123'
UPDATE tbl_user_master
SET
    user_password = '$2a$10$N4vl3iEJQWjHBjBQV8zOt.jFQ3sYQJk3y0m.O/hQH4k4eL9X8Q7tC'
WHERE
    user_name = 'rajesh.kumar';

SELECT user_name, 'Password updated to: password123' as status
FROM tbl_user_master
WHERE
    user_name = 'rajesh.kumar';