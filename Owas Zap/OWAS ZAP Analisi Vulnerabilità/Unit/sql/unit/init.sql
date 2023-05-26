CREATE SCHEMA `unitDS`;
CREATE USER 'javaclient'@'%' IDENTIFIED BY 'password1!';
GRANT ALL PRIVILEGES ON unitDS.* TO 'javaclient'@'%';
FLUSH PRIVILEGES;