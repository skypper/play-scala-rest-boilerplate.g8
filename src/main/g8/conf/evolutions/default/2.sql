-- Credentials AuthInfo schema

-- !Ups
CREATE TABLE CredentialsAuthInfo (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hasher VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    password_salt VARCHAR(100)
) ENGINE=InnoDB;

ALTER TABLE CredentialsAuthInfo
ADD CONSTRAINT FK_CredentialsAuthInfoUser
FOREIGN KEY (email) REFERENCES User(email)
ON DELETE CASCADE;

-- !Downs

ALTER TABLE CredentialsAuthInfo
DROP FOREIGN KEY FK_CredentialsAuthInfoUser;

DROP TABLE CredentialsAuthInfo;
