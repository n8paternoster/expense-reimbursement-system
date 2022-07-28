create table if not exists users (
	userID int primary key generated always as identity (start with 1000000),
	password varchar(20) not null,
	userType varchar(20) not null,
	firstName varchar(20) not null,
	lastName varchar(20) not null,
	email varchar(50),
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


insert into users (password, userType, firstName, lastName, email, dob) values ('password', 'Manager', 'Test', 'Tester', 'ttester@test.com', '2000-01-01 00:00:00') returning userID;
insert into requests (submitterID, resolverID, amount, timeSubmitted, category, description, status) values (1000000, null, 50000, '2000-01-01 00:00:00', 'Mileage', null, 'Pending') returning requestID;

select userID, password, userType, firstName, lastName, email, dob from users where userID=1000000 and password='password';

