import java.util.*;
import java.awt.Menu;
import java.io.*;
import java.sql.*;

public class P1 {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	
	// JDBC Objects
	private static Connection con;

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
			con.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE\n");
			} catch (Exception e) {
				System.out.println(":: TEST - FAILED CONNECTED TO DATABASE\n");
				e.printStackTrace();
			}
	  }
	
	public static String validVal(int checkType, String input, String iden, Scanner sca) {
		if(checkType == 1) {
			boolean error = true;
			
			while(error) {
				try {
					System.out.print(iden + ": ");
					input		= sca.nextLine();
					
					int intTest = Integer.parseInt(input);
					
					while(input.isEmpty() | !input.chars().allMatch(Character::isDigit)) {
						System.out.println("Invalid " + iden + ", try again.");
						System.out.print("Value: ");
						input 	= sca.nextLine();
					}
					
					while(Integer.parseInt(input) < 0) {
						System.out.print("Invalid " + iden + ", try again: ");
						System.out.print("Value: ");
						input 	= sca.nextLine();
					}
					
					error = false;
				} catch (NumberFormatException n) {
					System.out.println("Number error, try again.");
				}
			}
		}
		return input;
	}
	
	public static void main(String argv[]) {
		System.out.println(":: P1 START");
		
		if(argv.length < 1) {
			System.out.println("Need database properties.");
		} else {
			BankingSystem.init(argv[0]);
			P1.init(argv[0]);
			BankingSystem.testConnection();
			P1.testConnection();
		}

		boolean active = true;
		String screen = "1";
		String loginID = "";
		
		// Main program loop, will keep going as long as user wants to continue doing actions.
		while(active) {
			try {
				con = DriverManager.getConnection(url, username, password);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			if(screen.contentEquals("1")) {
				// Init landing
				System.out.println("::::::");
				System.out.println("\nWelcome to the Self-Service Banking System! - Main Menu");
				System.out.println("If you're a new customer, type: new");
				System.out.println("If you're an existing customer, type: login");
				System.out.println("If you would like to exit, type: exit");
				System.out.print("Input: ");
				
				// Init scanner
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
			
				// New customer UI
				if(input.equalsIgnoreCase("new")) {
					System.out.println("You're new, great!\n");
					
					String name = "";
					String gender = "";
					String age = "";
					String pin = "";
					
					System.out.print("Enter your name (No longer than 15 characters): ");
					name 	= sc.nextLine();
					
					// Catch if name is blank, contains numbers or symbols or is longer than fifteen characters
					while(name.isEmpty() | !name.chars().allMatch(Character::isLetter) | name.length() > 15) {
							System.out.print("Invalid name, try again: ");
							name 	= sc.nextLine();
					}
					
					System.out.print("\nGender (M or F): ");
					gender 	= sc.nextLine();
					
					// Catch if gender is blank or does not equal M or F
					while(gender.isEmpty() | (!gender.contentEquals("M") && !gender.contentEquals("F"))) {
							System.out.print("Invalid gender, try again: ");
							gender 	= sc.nextLine();
					}
					
					// Catch if age is empty, not numbers, or less than zero.
					age = validVal(1, age, "Age", sc);
					
					// Catch if pin is empty, not numbers, or less than zero.
					pin = validVal(1, pin, "PIN", sc);
					
					BankingSystem.newCustomer(name, gender, age, pin);
					screen = "1";
					System.out.println();
					}
					
					// Login UI
					else if(input.equalsIgnoreCase("login")) {
						System.out.println("You have an account with us, great! Log in here.\n");
						String id = "";
						String pin = "";
						
						boolean exists = false;
						
						// Error check
						while(!exists) {
							try {
								Statement stmt = con.createStatement();
								
								id = validVal(1, id, "ID", sc);
								
								ResultSet idExists = stmt.executeQuery("SELECT ID FROM P1.CUSTOMER WHERE ID=" + id);
								if(idExists.next() | Integer.parseInt(id) == 0) {
									exists = true;
								}
							} catch (SQLException e) {
								System.out.println("Invalid ID, try again.");
							}
						}

						boolean success = false;
						while(!success) {
							try {
								Statement stmt = con.createStatement();

								// Error check
								pin = validVal(1, pin, "PIN", sc);
								
								ResultSet pinCheck = stmt.executeQuery(	"SELECT ID,PIN FROM P1.CUSTOMER WHERE ID=" + id + 
																		" AND PIN=" + pin);
								if(pinCheck.next()) {
									success = true;
									if(!(Integer.parseInt(pin) == 0)) {
										System.out.println("Login succeeded! Moving to customer screen...\n");
										loginID = id;
										screen = "2";
									}
								} 
								else if(Integer.parseInt(id) == 0 && Integer.parseInt(pin) == 0) {	
									success = true;
									System.out.println("Login succeeded! Moving to admin screen...\n");
									screen = "3";							
								} else {
									System.out.println("Try again.");
								}
							} catch (SQLException e) {
								System.out.println("Invalid pin, try again.");
							}
						}
					}
					
					else if(input.equalsIgnoreCase("exit")) {
						System.out.println("Thanks for using our service, bye!");
						active = false;
					}
				}
			
			/**
			 * Screen 2. Customer Main Menu
			 * Takes care of opening/closing an account,
			 * depositing/withdrawing/transferring and 
			 * displaying the account summary.
			 */
			else if(screen.contentEquals("2")) {
				// Init landing
				System.out.println("::::::");
				System.out.println("Welcome to the Customer Main Menu");
				System.out.println("Current logged in ID: " + loginID);
				System.out.println("If you would like to open an account, type: open");
				System.out.println("If you would like to close an account, type: close");
				System.out.println("If you would like to deposit money, type: deposit");
				System.out.println("If you would like to withdraw money, type: withdraw");
				System.out.println("If you would like to transfer money, type: transfer");
				System.out.println("If you would like to see a summary of your accounts, type: summary");
				System.out.println("If you would like to exit, type: exit");
				System.out.print("Input: ");
				
				// Init scanner
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
				
				// Open account UI
				if(input.equalsIgnoreCase("open")) {
					String type = "";
					String initbal = "";
					if(!loginID.contentEquals("0")) {
						System.out.println("Great, let's open an account for you.\n");
						
						System.out.print("Type (Checkings (C) or Savings (S)): ");
						type 	= sc.nextLine();
						
						// Catch if type is blank or does not equal C or S
						while(type.isEmpty() | (!type.equalsIgnoreCase("C") && !type.equalsIgnoreCase("S"))) {
								System.out.print("Invalid type, try again: ");
								type 	= sc.nextLine();
						}
						System.out.println("Enter the balance you would like to start with.");
						initbal = validVal(1, initbal, "Initial balance", sc);	
						BankingSystem.openAccount(loginID, type, initbal);
					}
				}
				else if(input.equalsIgnoreCase("close")) {
					String closeAccNum = "";
					try {
						Statement stmt = con.createStatement();
						ResultSet yourAcc = stmt.executeQuery("SELECT NUMBER FROM P1.ACCOUNT WHERE ID=" + loginID + " AND STATUS='A'");
						System.out.println("Let's close one of your accounts.\n");
						// If the person is trying to close an account without having made one.
						if(yourAcc.next()) {
							// Will hold all account numbers
							ArrayList<String> yourAccList = new ArrayList<String>();

							// While there are accounts to be added, at them to the arraylist
							yourAccList.add(yourAcc.getString(1));
							while(yourAcc.next()) {
								yourAccList.add(yourAcc.getString(1));
							}

							// While the user enters numbers that don't belong to their ID, keep re-prompting
							while(!yourAccList.contains(closeAccNum)) {
								System.out.println("You can close account(s): ");
								for(String s: yourAccList) {
									System.out.print(s + ", ");
								}
								System.out.println("Choose one.");
								closeAccNum = validVal(1, closeAccNum, "Account number", sc);
							}
							
							BankingSystem.closeAccount(closeAccNum);
							System.out.println("Closing of account succeeded! Returning...\n");
							screen = "2";
						}
					} catch (SQLException e) {
						System.out.println("You have no accounts to close, try making one!\n");
					}
				}
				else if(input.equalsIgnoreCase("deposit")) {
					String depositAccNum = "";
					String amount = "";
					
					boolean exists = false;
					
					while(!exists) {
						try {
							Statement stmt = con.createStatement();
							System.out.println("Enter the account number you would like to deposit to.");
							depositAccNum = validVal(1, depositAccNum, "Deposit account number", sc);
							
							ResultSet destExists = stmt.executeQuery("SELECT NUMBER FROM P1.ACCOUNT WHERE NUMBER=" + depositAccNum + " AND STATUS = 'A'");
							if(destExists.next()) {
								exists = true;
								System.out.println("Found destination account #: " + depositAccNum);
							}
						} catch (SQLException e) {
							System.out.println("Invalid account number, try again.");
						}
					}
					
					amount = validVal(1, amount, "Amount", sc);
					BankingSystem.deposit(depositAccNum, amount);
					
				}
				else if(input.equalsIgnoreCase("withdraw")) {
					String withdrawAccNum = "";
					String amount = "";
					try {
						Statement stmt = con.createStatement();
						ResultSet withExists = stmt.executeQuery("SELECT NUMBER FROM P1.ACCOUNT WHERE ID=" + loginID + " AND STATUS='A'");
						System.out.println("Let's withdraw from one of your accounts.");
						if(withExists.next()) {
							// Will hold all account numbers
							ArrayList<String> yourAccList = new ArrayList<String>();

							// While there are accounts to be added, at them to the arraylist
							yourAccList.add(withExists.getString(1));
							while(withExists.next()) {
								yourAccList.add(withExists.getString(1));
							}

							// While the user enters numbers that don't belong to their ID, keep re-prompting
							while(!yourAccList.contains(withdrawAccNum)) {
								System.out.println("You can withdraw from account(s): ");
								for(String s: yourAccList) {
									System.out.print(s + ", ");
								}
								System.out.println("Choose one.");
								withdrawAccNum = validVal(1, withdrawAccNum, "Withdraw account number", sc);
								
								System.out.println("How much would you like to withdraw?");
								amount = validVal(1, amount, "Amount", sc);
								
								BankingSystem.withdraw(withdrawAccNum, amount);
							}
						}
						System.out.println("You have no accounts to withdraw from, try making one!\n");
					} 
					catch (SQLException e) {
						System.out.println("You have no accounts to withdraw from, try making one!\n");
					}
				}
				else if(input.equalsIgnoreCase("transfer")) {
					String srcAcc = "";
					String dstAcc = "";
					String amount = "";
					
					boolean dstExists = false;

					try {
						Statement stmt = con.createStatement();
						
						ResultSet src = stmt.executeQuery("SELECT NUMBER FROM P1.ACCOUNT WHERE ID=" + loginID + " AND STATUS='A'");
						if(src.next()) {
							// Will hold all account numbers
							ArrayList<String> yourAccList = new ArrayList<String>();

							// While there are accounts to be added, at them to the arraylist
							yourAccList.add(src.getString(1));
							while(src.next()) {
								yourAccList.add(src.getString(1));
							}

							// While the user enters numbers that don't belong to their ID, keep re-prompting
							while(!yourAccList.contains(srcAcc)) {
								System.out.println("You can transfer from account(s): ");
								for(String s: yourAccList) {
									System.out.print(s + ", ");
								}
								System.out.print("Choose one: ");
								srcAcc = validVal(1, srcAcc, "Source account", sc);
								
								while(!dstExists) {
									try {
										System.out.print("Enter the account number you would like to transfer to: ");
										dstAcc = validVal(1, dstAcc, "Destination account", sc);
										
										ResultSet src1 = stmt.executeQuery("SELECT NUMBER FROM P1.ACCOUNT WHERE NUMBER=" + dstAcc);
										if(src1.next()) {
											dstExists = true;
										}
										else if(!src1.next()) {
											System.out.println("You are trying to transfer to an account that does not exist.\n");
										}
									} catch (SQLException e) {
										System.out.println("Invalid account number, try again.");
									}
								}
								System.out.print("How much would you like to transfer?: ");
								amount = validVal(1, amount, "Amount", sc);
								BankingSystem.transfer(srcAcc, dstAcc, amount);
							}
						}
						System.out.println("You have no accounts to transfer from, try making one!\n");
					} catch (SQLException e) {
						System.out.println("You have no accounts to transfer from, try making one!\n");
					}
				}
				else if(input.equalsIgnoreCase("summary")) {
					BankingSystem.accountSummary(loginID);
				}
				else if(input.equalsIgnoreCase("exit")) {
					System.out.println("Returning to the previous menu...\n");
					screen = "1";
				}
			}
			
			/**
			 * Screen 3. Admin Menu
			 * Takes care of seeing an account summary of all customers, seeing a 
			 * specific customer's accounts, and finding the average balance between
			 * age groups.
			 */
			else if(screen.contentEquals("3")) {
				System.out.println("::::::");
				System.out.println("Welcome to the Admin Main Menu");
				System.out.println("If you would like to see a specific customer's accounts, type: summary");
				System.out.println("If you would like to see an account summary of all customers, type: reporta");
				System.out.println("If you would like to find the average balance between age groups, type: reportb");
				System.out.println("If you would like to exit, type: exit");
				System.out.print("Input: ");
				
				// Init scanner
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
				
				if(input.equalsIgnoreCase("summary")) {
					String lookupID = "";
					
					boolean exists = false;
					
					// Check if ID is first a set of digits and not empty, then if it exists in the system.
					while(!exists) {
						try {
							Statement stmt = con.createStatement();
							System.out.print("\nType ID you would like to see: ");
							lookupID 	= validVal(1, lookupID, "Lookup ID", sc);
							
							ResultSet idExists = stmt.executeQuery("SELECT ID FROM P1.CUSTOMER WHERE ID=" + lookupID);
							if(idExists.next()) {
								exists = true;
							}
						} catch (SQLException e) {
							System.out.println("Invalid ID, try again.");
						}
					}
					
					BankingSystem.accountSummary(lookupID);
				}
				else if(input.equalsIgnoreCase("reporta")) {
					BankingSystem.reportA();
				}
				else if(input.equalsIgnoreCase("reportb")) {
					String minAge = "";
					String maxAge = "";
					
					minAge = validVal(1, minAge, "Minimum age", sc);
				
					maxAge = validVal(1, maxAge, "Maximum age", sc);
					
					while(Integer.parseInt(minAge) > Integer.parseInt(maxAge)) {
						System.out.println("Enter a maximum age higher than the minimum age.");
						maxAge = validVal(1, maxAge, "Maximum age", sc);
					}
					
					BankingSystem.reportB(minAge, maxAge);
				}
				else if(input.equalsIgnoreCase("exit")) {
					System.out.println("Returning to previous menu...\n");
					screen = "1";
				}
			}
		}
		System.out.println("Program exit.");
	}
}
