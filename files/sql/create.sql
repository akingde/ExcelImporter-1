CREATE DATABASE XLSIMPTEST CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER 'XLSIMPTEST'@'localhost' IDENTIFIED BY 'XLSIMPTEST';
GRANT ALL PRIVILEGES ON * . * TO 'XLSIMPTEST'@'localhost';
FLUSH PRIVILEGES;


CREATE TABLE XLSIMPTEST.`IMP1` (
  `col1` varchar(100) DEFAULT NULL,
  `col2` varchar(100) DEFAULT NULL,
  `col3` varchar(100) DEFAULT NULL,
  `col4` varchar(100) DEFAULT NULL,
  `ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
