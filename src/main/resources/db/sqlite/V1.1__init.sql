CREATE TABLE Submission (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  account_name varchar(250) not null,
  login_session varchar(250),
  num int,
  title varchar(250),
  artist varchar(250),
  album varchar(250),
  len int,
  mbid varchar(250),
  submission_time timestamp not null,
  created timestamp not null,
  foreign key (account_name) references Account(account_name)
);
