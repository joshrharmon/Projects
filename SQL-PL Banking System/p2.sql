-- Stored Procedures
-- Joshua Harmon

-- Notes:
-- sqlnum key:
-- CUST_CRT = 1, CUST_LOGIN = 2, 
-- ACCT_OPN = 3, ACCT_CLS = 4, 
-- ACCT_DEP = 5, ACCT_WTH = 6, 
-- ACCT_TRX = 7, ADD_INTEREST = 7
-- Code goes: [ProcedureNum]000[ErrorNum], i.e. first number is the procedure number
	-- defined above and last number is the error it encountered (if any). If there is 
	-- no error and the procedure completes successfully, it will show [ProcedureNum]0000, except
	-- in the case of ADD_INTEREST where there are three success scenarios. In that case, the second
	-- to last number denotes each success case.
	
-- Example: A successful run of CUST_CRT will show sqlnum = 10000
-- Example: An invald PIN in CUST_LOGIN will show sqlnum = 200002
-- Example: A valid checking rate but invalid savings rate in ADD_INTEREST will show sqlnum = 70010
-- Example: A valid checking and savings rate in ADD_INTEREST will show sqlnum = 70030

CONNECT TO CS157A@ 

CREATE OR REPLACE PROCEDURE p2.CUST_CRT (
	IN name 	CHAR(15),
	IN gender 	CHAR(1),
	IN age 		INTEGER,
	IN pin 		INTEGER,
	OUT id		INTEGER,
	OUT sqlnum	INTEGER,
	OUT err_msg	VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE encrPIN INTEGER;
		IF LENGTH(name) > 15 
			THEN 
				SET sqlnum = 10001;
				SET err_msg = 'Name more than 15 characters or blank.';
		ELSEIF (gender <> 'M' AND gender <> 'F')
			THEN
				SET sqlnum = 10002;
				SET err_msg = 'Gender not M or F.';
		ELSEIF age < 0
			THEN
				SET sqlnum = 10003;
				SET err_msg = 'Age less than zero or invalid.';
		ELSEIF pin < 0
			THEN
				SET sqlnum = 10004;
				SET err_msg = 'PIN less than zero or invalid.';
		ELSE
			SET encrPIN = p2.encrypt(pin);
			INSERT INTO P2.CUSTOMER(name, gender, age, pin) VALUES (name, gender, age, encrPIN); 
			SET id = (SELECT MAX(ID) FROM p2.customer);
			SET sqlnum = 10000; 
		END IF;
END@

	
CREATE OR REPLACE PROCEDURE p2.CUST_LOGIN (
	IN inputid		INTEGER,
	IN inputpin		INTEGER,
	OUT	valid	INTEGER,
	OUT sqlnum	INTEGER,
	OUT err_msg	VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existCust BOOLEAN;
		DECLARE decrPIN INTEGER;
		SET existCust = FALSE;
		
		IF EXISTS(SELECT ID FROM p2.customer WHERE inputid=id)
			THEN 
				SET existCust = true;
		ELSE
			SET sqlnum = 20001;
			SET valid = 0;
			SET err_msg = 'You do not exist in the system.';
		END IF;
		
		IF existCust = true
			THEN
			SET decrPIN = p2.decrypt((SELECT PIN FROM p2.CUSTOMER WHERE inputid=id));
			IF (decrPIN = inputpin)
				THEN 
					SET valid = '1';
					SET sqlnum = 20000;
			ELSE 
				SET sqlnum = 20002;
				SET valid = '0';
				SET err_msg = 'PIN incorrect, try again.';
			END IF;
		END IF;				
END@
	
CREATE OR REPLACE PROCEDURE p2.ACCT_OPN (
	IN inputid			INTEGER,
	IN inputbalance		INTEGER,
	IN inputtype		CHAR(1),
	OUT outputnumber	INTEGER,
	OUT sqlnum			INTEGER,
	OUT err_msg			VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existCust BOOLEAN;
		SET existCust = FALSE;
		
		IF EXISTS(SELECT ID FROM p2.customer WHERE inputid=id)
			THEN 
				SET existCust = true;
		ELSE
			SET sqlnum = 30001;
			SET err_msg = 'You do not exist in the system.';
		END IF;
		
		IF(existCust = TRUE)
			THEN
				IF(inputbalance < 0) 
					THEN 
						SET sqlnum = 30002;
						SET err_msg = 'Cannot have a balance less than zero.';
				ELSEIF(inputtype <> 'C' AND inputtype <> 'S')
					THEN
						SET sqlnum = 30003;
						SET err_msg = 'Account must be Checkings (C) or Savings (S), please choose one.';
				ELSE
					SET sqlnum = 30000;
					INSERT INTO P2.ACCOUNT(id, type, balance, status) VALUES (inputid, inputtype, inputbalance, 'A');
					SET outputnumber = (SELECT MAX(NUMBER) FROM p2.account WHERE inputid=ID);
				END IF;
		END IF;
END@
	
CREATE OR REPLACE PROCEDURE p2.ACCT_CLS (
	IN inputnumber	INTEGER,
	OUT sqlnum	INTEGER,
	OUT err_msg	VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existAcct BOOLEAN;
		SET existAcct = FALSE;
		
		IF EXISTS(SELECT NUMBER FROM p2.account WHERE inputnumber=NUMBER)
			THEN 
				SET existAcct = true;
		ELSE
			SET sqlnum = 30001;
			SET err_msg = 'This account does not exist in the system.';
		END IF;
		
		IF existAcct = TRUE
			THEN 
				SET sqlnum = 30000;
				UPDATE P2.ACCOUNT SET BALANCE = 0, STATUS = 'I' WHERE NUMBER=inputnumber;
		END IF;
END@
	
CREATE OR REPLACE PROCEDURE p2.ACCT_DEP (
	IN inputnumber	INTEGER,
	IN inputamt		INTEGER,
	OUT sqlnum 	INTEGER,
	OUT err_msg	VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existAcct BOOLEAN;
		SET existAcct = FALSE;
		
		IF EXISTS(SELECT NUMBER FROM p2.account WHERE inputnumber=NUMBER)
			THEN 
				SET existAcct = true;
		ELSE
			SET sqlnum = 40001;
			SET err_msg = 'This account does not exist in the system.';
		END IF;
		
		IF existAcct = TRUE AND inputamt > 0
			THEN 
				SET sqlnum = 40000;
				UPDATE P2.ACCOUNT SET BALANCE = BALANCE + inputamt WHERE NUMBER=inputnumber;
		ELSE
			SET sqlnum = 40002;
			SET err_msg = 'The amount you have entered is less than zero.';
		END IF;
END@
	
CREATE OR REPLACE PROCEDURE p2.ACCT_WTH (
	IN inputnumber 	INTEGER,
	IN inputamt	  	INTEGER,
	OUT sqlnum	INTEGER,
	OUT err_msg	VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existAcct BOOLEAN; 
		DECLARE currentBal INTEGER; 
		SET existAcct = FALSE; 
		SET currentBal = (SELECT BALANCE FROM p2.account WHERE inputnumber=NUMBER);
		
		IF EXISTS(SELECT NUMBER FROM p2.account WHERE inputnumber=NUMBER)
			THEN 
				SET existAcct = true;
		ELSE
			SET sqlnum = 50001;
			SET err_msg = 'This account does not exist in the system.';
		END IF;
		
		IF (existAcct = TRUE AND (inputamt < currentBal) AND inputamt > 0)
			THEN 
				SET sqlnum = 50000;
				UPDATE P2.ACCOUNT SET BALANCE = BALANCE - inputamt WHERE NUMBER=inputnumber;
		ELSE
			SET sqlnum = 50002;
			SET err_msg = 'The amount you have entered is more than your current balance or less than zero.';
		END IF;
END@
	
CREATE OR REPLACE PROCEDURE p2.ACCT_TRX (
	IN src_acct		INTEGER,
	IN dest_acct 	INTEGER,
	IN inputamt		INTEGER,
	OUT sqlnum		INTEGER,
	OUT err_msg		VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE existSrcAcct BOOLEAN; 
		DECLARE existDstAcct BOOLEAN;
		DECLARE currentSrcBal INTEGER; 
		SET existSrcAcct = FALSE; 
		SET existDstAcct = FALSE;
		SET currentSrcBal = (SELECT BALANCE FROM p2.account WHERE src_acct=NUMBER);
	
		IF EXISTS(SELECT NUMBER FROM p2.account WHERE src_acct=NUMBER)
			THEN 
				SET existSrcAcct = true;
		END IF;
		IF EXISTS(SELECT NUMBER FROM p2.account WHERE dest_acct=NUMBER)
			THEN 
				SET existDstAcct = true;
		ELSE
			SET sqlnum = 60001;
			SET err_msg = 'The source and/or destination account does not exist in the system.';
		END IF;
		
		IF (existSrcAcct = TRUE AND existDstAcct = TRUE AND currentSrcBal > inputamt)
			THEN
				CALL p2.ACCT_WTH(src_acct, inputamt, sqlnum, err_msg);
				CALL p2.ACCT_DEP(dest_acct, inputamt, sqlnum, err_msg);
				SET sqlnum = 60000;
		ELSE
			SET sqlnum = 60002;
			SET err_msg = 'You do not have the money to transfer or value was less than zero.';
		END IF;
END@

CREATE OR REPLACE PROCEDURE p2.ADD_INTEREST (
	IN 	savings_rate		FLOAT,
	IN 	checking_rate		FLOAT,
	OUT sqlnum				INTEGER,
	OUT err_msg				VARCHAR(100)
	)
	LANGUAGE SQL
	BEGIN
		DECLARE validCRate BOOLEAN;
		DECLARE validSRate BOOLEAN;
		SET validCRate = FALSE;
		SET validSRate = FALSE;
		
		
		IF(savings_rate > 0) 
			THEN
				SET validSRate = TRUE;
		ELSE	
			SET sqlnum = 70001;
			SET err_msg = 'The savings rate is not greater than zero, checking rate will be applied.';
		END IF;
		
		IF(checking_rate > 0)
			THEN
				SET validCRate = TRUE;
		ELSE
			SET sqlnum = 70002;
			SET err_msg = 'The checking rate is not greater than zero, savings rate will be applied.';
		END IF;
		
		IF (validCRate = TRUE AND validSRate = FALSE)
			THEN
				UPDATE p2.account 
					SET BALANCE = BALANCE * (1 + checking_rate)
					WHERE type = 'C' AND status = 'A';
				SET sqlnum = 70010;
		ELSEIF (validCRate = FALSE AND validSRate = TRUE)
			THEN
				UPDATE p2.account 
					SET BALANCE = BALANCE * (1 + savings_rate)
					WHERE type = 'S' AND status = 'A';
				SET sqlnum = 70020;
		ELSEIF (validCRate = TRUE AND validSRate = TRUE)
			THEN 
				UPDATE p2.account 
					SET BALANCE = BALANCE * (1 + checking_rate)
					WHERE type = 'C' AND status = 'A';
				UPDATE p2.account 
					SET BALANCE = BALANCE * (1 + savings_rate)
					WHERE type = 'S' AND status = 'A';
				SET sqlnum = 70030;
		ELSE
			SET sqlnum = 70003;
			SET err_msg = 'Both the checking and savings rate are less than or equal to zero, invalid.';
		END IF;
END@
