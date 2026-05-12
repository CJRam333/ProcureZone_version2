-- Update rajesh.kumar password to MD5 hash of 'password123'
UPDATE tbl_user_master
SET
    user_password = '482c811da5d5b4bc6d497ffa98491e38'
WHERE
    user_name = 'rajesh.kumar';

SELECT user_name, 'Password updated to: password123 (MD5)' as status
FROM tbl_user_master
WHERE
    user_name = 'rajesh.kumar';