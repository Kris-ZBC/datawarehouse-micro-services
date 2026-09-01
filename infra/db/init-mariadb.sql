CREATE DATABASE IF NOT EXISTS `sopinfo_login_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'login'@'%' IDENTIFIED BY 'login';
GRANT ALL PRIVILEGES ON `sopinfo_login_db`.* TO 'login'@'%';
FLUSH PRIVILEGES;

