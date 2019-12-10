connect to cs157a;
--
-- drop previous definition first
drop specific function p2.encrypt;
drop specific function p2.decrypt;
drop table p2.account;
drop table p2.customer;
--
-- Without column constraint on Age & Pin, need logic in application to handle
--
create table p2.customer
(
  ID		integer generated always as identity (start with 100, increment by 1),
  Name		varchar(15) not null,
  Gender	char not null check (Gender in ('M','F')),
  Age		integer not null,
  Pin		integer not null check (Pin >= 0),
  primary key (ID)
);
--
-- Without column constraint on Balance, Type, Status, need logic in application to handle
--
create table p2.account
(
  Number	integer generated always as identity (start with 1000, increment by 1),
  ID		integer not null references p2.customer (ID),
  Balance	integer not null,
  Type		char not null,
  Status	char not null,
  primary key (Number)
);
--
-- p2.encrypt takes in an integer and output an "encrypted" integer
--
CREATE FUNCTION p2.encrypt ( pin integer )
  RETURNS integer
  SPECIFIC p2.encrypt
  LANGUAGE SQL
  DETERMINISTIC
  NO EXTERNAL ACTION
  READS SQL DATA
  RETURN
    CASE
      WHEN
        pin >= 0
      THEN
        pin * pin + 1000
      ELSE
        -1
    END;
-- 
-- p2.decrypt takes in an integer and output an "unencrypted" integer
--
CREATE FUNCTION p2.decrypt ( pin integer )
  RETURNS integer
  SPECIFIC p2.decrypt
  LANGUAGE SQL
  DETERMINISTIC
  NO EXTERNAL ACTION
  READS SQL DATA
  RETURN 
    CASE
      WHEN 
        pin >= 0
      THEN
        SQRT(pin - 1000)
      ELSE
        -1
    END;
--
commit;

-- create customer
CALL p2.CUST_CRT('CUS1', 'M', 20, 1111, ?,?,?);
CALL p2.CUST_CRT('CUS2', 'F', 25, 2222, ?,?,?);
CALL p2.CUST_CRT('CUS3', 'M', 40, 3333, ?,?,?);


-- customer login
CALL p2.CUST_LOGIN(100, 1111, ?,?,?);
--customer login error cases
CALL p2.CUST_LOGIN(102, 1111, ?,?,?);
CALL p2.CUST_LOGIN(999, 9999, ?,?,?);

-- open account
CALL p2.ACCT_OPN(100, 100, 'C',?,?,?);
CALL p2.ACCT_OPN(100, 200, 'S',?,?,?);
CALL p2.ACCT_OPN(101, 300, 'C',?,?,?);
CALL p2.ACCT_OPN(101, 400, 'S',?,?,?);
CALL p2.ACCT_OPN(102, 500, 'C',?,?,?);
CALL p2.ACCT_OPN(102, 600, 'S',?,?,?);

-- open with invalid id
CALL p2.ACCT_OPN(999, 500, 'C',?,?,?);
-- invalid balance
CALL p2.ACCT_OPN(100, -100, 'C',?,?,?);

-- close account
CALL p2.ACCT_CLS(1004,?,?);
SELECT NUMBER, BALANCE, STATUS FROM p2.ACCOUNT WHERE NUMBER = 1004;
-- close invalid account
CALL p2.ACCT_CLS(9999,?,?);

-- deposit into account 
CALL p2.ACCT_DEP(1000, 33, ?,?);
-- deposit into invalid account
CALL p2.ACCT_DEP(9999, 44, ?,?);
-- deposit with negative balance
CALL p2.ACCT_DEP(1001, -44, ?,?);
CALL p2.ACCT_DEP(1004, 99, ?,?);
SELECT NUMBER, BALANCE FROM p2.account where NUMBER IN(1000, 1001, 1004);

-- withdraw from account
CALL p2.ACCT_WTH(1000, 22, ?, ?);
-- over drawn
CALL p2.ACCT_WTH(1002, 2000, ?, ?);
CALL p2.ACCT_WTH(1003, -88, ?, ?);
SELECT NUMBER, BALANCE FROM p2.account where NUMBER IN(1000, 1002);

UPDATE p2.account set Balance = 100 where number = 1000;
UPDATE p2.account set Balance = 200 where number = 1001;
UPDATE p2.account set Balance = 300 where number = 1002;
UPDATE p2.account set Balance = 400 where number = 1003;

-- transfer to another account
CALL p2.ACCT_TRX(1003, 1002, 66, ?,?);
-- different customer
CALL p2.ACCT_TRX(1005, 1000, 99, ?,?);
-- Try to transfer more than you have
CALL p2.ACCT_TRX(1000, 10001, 20000000, ?, ?);
-- Transfer negative amount
CALL p2.ACCT_TRX(1000, 10001, -1, ?, ?);
SELECT NUMBER, BALANCE FROM p2.account where NUMBER IN(1000, 1002, 1003, 1005);

UPDATE p2.account set Balance = 100 where number = 1000;
UPDATE p2.account set Balance = 200 where number = 1001;
UPDATE p2.account set Balance = 300 where number = 1002;
UPDATE p2.account set Balance = 400 where number = 1003;
UPDATE p2.account set Balance = 500 where number = 1004;
UPDATE p2.account set Balance = 600 where number = 1005;

-- interest
SELECT NUMBER, BALANCE FROM p2.account;
CALL p2.ADD_INTEREST (0.5, 0.1,?,?);

-- invalid checkings rate valid savings rate
CALL p2.ADD_INTEREST (-.4, 0.4, ?, ?);

-- invalid savings rate, valid checkings rate
CALL p2.ADD_INTEREST (0.4, -0.4, ?, ?);

-- both invalid
CALL p2.ADD_INTEREST (-.5, -.5, ?, ?);

SELECT NUMBER, BALANCE FROM p2.account;
