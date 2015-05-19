DROP TABLE User;
CREATE TABLE User 
	(id INTEGER PRIMARY KEY AUTOINCREMENT,
		username CHAR(100) NOT NULL,
		email CHAR(100) NOT NULL UNIQUE,
		password_hash CHAR(512) NOT NULL,
		last_ip CHAR(15)
		);

