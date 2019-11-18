import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manage connection to database and perform SQL statements.
 */
public class BankingSystem {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	
	// JDBC Objects
	private static Connection con;
	private static Statement stmt;
	private static ResultSet rs;

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
	public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);		// Create a new FileInputStream object using our filename parameter
			props.load(input);											// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");					// Load the driver
			url = props.getProperty("jdbc.url");						// Load the url
			username = props.getProperty("jdbc.username");				// Load the username
			password = props.getProperty("jdbc.password");				// Load the password
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test database connection.
	 */
	public static void testConnection() {
		System.out.println(":: TEST - CONNECTING TO DATABASE");
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, password);
			//con.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE");
			} catch (Exception e) {
				System.out.println(":: TEST - FAILED CONNECTED TO DATABASE\n");
				e.printStackTrace();
			}
	  }

	/**
	 * Create a new customer.
	 * @param name customer name
	 * @param gender customer gender
	 * @param age customer age
	 * @param pin customer pin
	 */
	public static void newCustomer(String name, String gender, String age, String pin) 
	{
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("INSERT INTO P1.CUSTOMER(name, gender, age, pin) VALUES ('" + name + "','" + gender + "', " + age + ", " + pin + ")"); 
			System.out.println("You entered the values: " + name + ", " + gender + ", " + age + ", and " + pin);
			ResultSet sqlID = stmt.executeQuery("SELECT MAX(ID) FROM P1.CUSTOMER");
			
			if(sqlID.next()) {
				System.out.println("Your Customer ID number is: " + sqlID.getInt(1));
			}
			
			System.out.println(":: CREATE NEW CUSTOMER - SUCCESS");
			System.out.println("---\n");
		} catch (SQLException e) {
			System.out.println("A SQL error occurred with insertion.");
		}
		
	}

	/**
	 * Open a new account.
	 * @param id customer id
	 * @param type type of account
	 * @param amount initial deposit amount
	 */
	public static void openAccount(String id, String type, String amount) 
	{
		// Try to create the new account.
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("INSERT INTO P1.ACCOUNT(id, type, balance, status) VALUES ('" + id + "','" + type + "','" + amount + "','A')");
			ResultSet rsOpenSuc = stmt.executeQuery("SELECT MAX(NUMBER) FROM P1.ACCOUNT WHERE id='" + id + "'"); 
			
			if(rsOpenSuc.next()) {
				System.out.println("The account was opened successfully. Your account number is: " + rsOpenSuc.getInt(1));
			}
			System.out.println(":: OPEN ACCOUNT - SUCCESS");
			System.out.println("---\n");
		} catch (SQLException e) {
			System.out.println("A SQL error occurred with opening the account.");
		}
	}

	/**
	 * Close an account.
	 * @param accNum account number
	 */
	public static void closeAccount(String accNum) 
	{
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("UPDATE P1.ACCOUNT SET BALANCE = 0, STATUS = 'I' WHERE NUMBER=" + accNum);
			System.out.println(":: CLOSE ACCOUNT - SUCCESS");
			System.out.println("---\n");
		} catch (SQLException e) {
			System.out.println("A SQL error occurred with closing the account");
		}
	}

	/**
	 * Deposit into an account.
	 * @param accNum account number
	 * @param amount deposit amount
	 */
	public static void deposit(String accNum, String amount) 
	{
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("UPDATE P1.ACCOUNT SET BALANCE = BALANCE + " + amount + " WHERE NUMBER=" + accNum);
			System.out.println(":: DEPOSIT INTO " + accNum + " - SUCCESS");
			System.out.println("---\n");
		} catch(SQLException e) {
			System.out.println("A SQL error occurred with depositing the money.");
		}
	}

	/**
	 * Withdraw from an account.
	 * @param accNum account number
	 * @param amount withdraw amount
	 */
	public static void withdraw(String accNum, String amount) 
	{
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("UPDATE P1.ACCOUNT SET BALANCE = BALANCE - " + amount + " WHERE NUMBER =" + accNum);
			System.out.println(":: WITHDRAW OF " + amount + " FROM " + accNum + " - SUCCESS");
			System.out.println("---\n");
		} catch(SQLException e) {
			System.out.println("A SQL error occurred with withdrawing the money. You don't have enough money");
		}
	}

	/**
	 * Transfer amount from source account to destination account. 
	 * @param srcAccNum source account number
	 * @param destAccNum destination account number
	 * @param amount transfer amount
	 */
	public static void transfer(String srcAccNum, String destAccNum, String amount) 
	{
		try {
			Statement stmt = con.createStatement();
		
			stmt.executeUpdate("UPDATE P1.ACCOUNT SET BALANCE = BALANCE - " + amount + " WHERE NUMBER=" + srcAccNum);
			
			// Add money to destination
			stmt.executeUpdate("UPDATE P1.ACCOUNT SET BALANCE = BALANCE + " + amount + " WHERE NUMBER=" + destAccNum);
		
			System.out.println(":: TRANSFERRING " + amount + " from " + srcAccNum + " to " + destAccNum + " - SUCCESS");
			System.out.println("---\n");
			
		} catch(SQLException e) {
			System.out.println("A SQL Error occurred with transferring funds.");
		}
	}

	/**
	 * Display account summary.
	 * @param cusID customer ID
	 */
	public static void accountSummary(String cusID) 
	{
		try {
			
			Statement stmt = con.createStatement();
			ResultSet rsAcc = stmt.executeQuery("SELECT * FROM P1.ACCOUNT WHERE ID=" + cusID + " AND STATUS !='I'");
			System.out.println("Your accounts are: \n");
			
			int totalBal = 0;
			
			while(rsAcc.next()) {
				System.out.println("Acc#: " + rsAcc.getInt(1));
				System.out.println("ID#: " + rsAcc.getInt(2));
				System.out.println("Balance: " + rsAcc.getInt(3));
				System.out.println("Type: " + rsAcc.getString(4));
				System.out.println("Status: " + rsAcc.getString(5) + "\n");
				totalBal += rsAcc.getInt(3);
			}

			System.out.println("Your total balance across all accounts is: " + totalBal);
			System.out.println(":: ACCOUNT SUMMARY - SUCCESS");
			System.out.println("---\n");
		} catch (SQLException e) {
			System.out.println("A SQL error occurred with accessing the account summary.");
		}
	}

	/**
	 * Display Report A - Customer Information with Total Balance in Decreasing Order.
	 */
	public static void reportA() 
	{
		try {
			Statement stmt = con.createStatement();	
			ResultSet rsRepA 	= stmt.executeQuery("SELECT * FROM REPORTA ORDER BY TOTALBAL DESC NULLS LAST");
													
			while(rsRepA.next()) {
				System.out.println("\nID#: " + rsRepA.getInt(1));
				System.out.println("Name: " + rsRepA.getString(2));
				System.out.println("Age: " + rsRepA.getInt(3));
				System.out.println("Gender: " + rsRepA.getString(4));
				System.out.println("Total Balance: " + rsRepA.getInt(5) + "\n");
			}
			System.out.println(":: REPORT A - SUCCESS");
			System.out.println("---\n");
		} 
		catch (SQLException e) {
			System.out.println("A SQL error occurred with producing Report A");
		}
	}

	/**
	 * Display Report B - Customer Information with Total Balance in Decreasing Order.
	 * @param min minimum age
	 * @param max maximum age
	 */
	public static void reportB(String min, String max) 
	{
		try {
			Statement stmt = con.createStatement();
			ResultSet rsRepB = stmt.executeQuery("SELECT AVG(BALANCE) AS AVGBAL " + 
													"FROM REPORTB WHERE AGE BETWEEN " + min + " AND " + max);
			if(rsRepB.next()) {
				System.out.println("The average balance from ages " + min + " to " + max + " is: " + rsRepB.getInt(1));
			}
			System.out.println(":: REPORT B - SUCCESS");
			System.out.println("---\n");
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("An error has occurred with producing Report B");
		}
	}
}
