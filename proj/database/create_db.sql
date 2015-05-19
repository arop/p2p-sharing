.mode columns
.headers on
.nullvalue NULL
 
PRAGMA foreign_keys = ON;

DROP TABLE UserInCircle;
DROP TABLE "Circle";
DROP TABLE Friend;
DROP TABLE User;

CREATE TABLE User 
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		username CHAR(100) NOT NULL,
		email CHAR(100) NOT NULL UNIQUE,
		password_hash CHAR(512) NOT NULL,
		last_ip CHAR(15),
		port INTEGER DEFAULT 4000
		);

-- user com id 1, tem user com id 2 como amigo
CREATE TABLE Friend
	(
		id1 INTEGER REFERENCES User(id), 
		id2 INTEGER REFERENCES User(id),
		PRIMARY KEY (id1, id2)
	);


CREATE TABLE "Circle"
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		owner_id INTEGER REFERENCES User(id),
		name char(100)
	);

CREATE TABLE UserInCircle
	(
		circle_id INTEGER REFERENCES "Circle"(id),
		user_id INTEGER REFERENCES User(id),
		PRIMARY KEY (circle_id, user_id)
	);


