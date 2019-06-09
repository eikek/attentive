-- initial database

CREATE TABLE IF NOT EXISTS Account (
  account_name varchar(250) not null primary key,
  active boolean not null,
  password varchar(250) not null,
  created timestamp not null
);

CREATE TABLE IF NOT EXISTS InvitationKey(
  invite varchar(250) not null primary key,
  created timestamp
);


--CREATE TABLE Submission is done per dbms

CREATE TABLE NowPlaying (
  account_name varchar(250) not null primary key,
  login_session varchar(250),
  num int,
  title varchar(250),
  artist varchar(250),
  album varchar(250),
  len int,
  created timestamp not null,
  foreign key (account_name) references Account(account_name)
);
