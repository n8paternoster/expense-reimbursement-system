create table if not exists users (
	userID int primary key generated always as identity (start with 1000000),
	password varchar(20) not null,
	userType varchar(20) not null,
	firstName varchar(20) not null,
	lastName varchar(20) not null,
	email varchar(50) unique,
	dob timestamp
);

create table if not exists requests (
	requestID int primary key generated always as identity,
	submitterID int references users(userID) not null,
	resolverID int references users(userID),
	amount bigint not null,
	timeSubmitted timestamp not null,
	category varchar(20),
	description varchar(50),
	status varchar(20) not null
);


insert into users (password, userType, firstName, lastName, email, dob) values ('password', 'Employee', 'ZTest', 'ZTester', 'zttester@test.com', '2000-01-01 00:00:00') returning userID;
insert into users (password, userType, firstName, lastName, email, dob) values ('password', 'Manager', 'manager', 'manager', 'manager@mail.com', '2000-01-01 00:00:00') returning userID;
insert into requests (submitterID, resolverID, amount, timeSubmitted, category, description, status) values (1000000, null, 50000, '2000-01-01 00:00:00', 'Food', null, 'Denied') returning requestID;

select userID, password, userType, firstName, lastName, email, dob from users where userID=1000000 and password='password';
select userType, firstName, lastName, email, dob from users where userID=1000000;
select userID, firstName, lastName, email, dob from users where userType='Employee' order by userID asc;
select userID from users where email='ettester@test.com';
select requestID, resolverID, amount, timeSubmitted, category, description, status from requests where submitterID=1000000 order by timeSubmitted desc;
select requestID, resolverID, amount, timeSubmitted, category, description, status from requests where submitterID=1000000 and status in ('Approved', 'Denied') order by timeSubmitted desc;
select requestID, submitterID, resolverID, amount, timeSubmitted, category, description from requests where status='Pending' order by timeSubmitted desc;
select requestID, submitterID, resolverID, amount, timeSubmitted, category, description, status from requests order by timeSubmitted desc;

update users set password='newpassword', firstName='Java', lastName='Enterprise', email='newemail@java.com', dob='1999-12-31 00:00:00' where userID=1000000;
update requests set status='Denied' where requestID=2;