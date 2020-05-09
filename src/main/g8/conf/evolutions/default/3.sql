-- Social AuthInfo schema

-- !Ups
CREATE TABLE AuthToken (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  token_id BINARY(16) NOT NULL UNIQUE,
  user_id BINARY(16) NOT NULL UNIQUE,
  expiry text NOT NULL
) ENGINE=InnoDB;

ALTER TABLE AuthToken
ADD CONSTRAINT FK_AuthTokenUser
FOREIGN KEY (user_id) REFERENCES User(user_id)
ON DELETE CASCADE;

-- !Downs
ALTER TABLE AuthToken
DROP FOREIGN KEY FK_AuthTokenUser;

DROP TABLE AuthToken;
