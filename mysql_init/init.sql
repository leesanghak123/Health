create user 'health'@'%' identified by 'sang0412';
create user 'logging'@'%' identified by 'sang0412';

GRANT ALL PRIVILEGES ON healthApp.* TO 'health'@'%';
GRANT ALL PRIVILEGES ON loggingApp.* TO 'logging'@'%';

CREATE DATABASE healthApp CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE loggingApp CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

FLUSH PRIVILEGES;