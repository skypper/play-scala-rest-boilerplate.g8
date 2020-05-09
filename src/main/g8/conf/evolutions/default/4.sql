-- Social AuthInfo schema

-- !Ups
CREATE TABLE SocialAuthInfo (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  provider varchar(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  access_token TEXT NOT NULL,
  token_type VARCHAR(30),
  expires_in INT,
  refresh_token TEXT
) ENGINE=InnoDB;

ALTER TABLE SocialAuthInfo
ADD CONSTRAINT FK_SocialAuthInfoUser
FOREIGN KEY (email) REFERENCES User(email)
ON DELETE CASCADE;

-- !Downs
ALTER TABLE SocialAuthInfo
DROP FOREIGN KEY FK_SocialAuthInfoUser;

DROP TABLE SocialAuthInfo;
