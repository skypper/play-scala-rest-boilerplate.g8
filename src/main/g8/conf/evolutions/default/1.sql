-- User schema

-- !Ups

CREATE TABLE User (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id BINARY(16) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    avatar_url VARCHAR(150),
    activated BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    invited_by INT,
    roles VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE User
ADD CONSTRAINT FK_UserInvitation
FOREIGN KEY (invited_by) REFERENCES User(id);

-- !Downs

ALTER TABLE User
DROP FOREIGN KEY FK_UserInvitation;

DROP TABLE User;
