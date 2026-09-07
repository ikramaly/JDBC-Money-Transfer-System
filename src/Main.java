import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL ="jdbc:mysql://localhost:3306/lenden";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "ikramsql@123";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--------------MONEY TRANSFER SYSTEM-----------------");
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            connection.setAutoCommit(false);
            while (true){
                System.out.print("Enter Sender Account Number: ");
                int senderAccount = sc.nextInt();
                // Check sender account
                if (!accountExists(connection, senderAccount)) {
                    System.out.println("Sender account does not exist.");
                    return;
                }
                System.out.print("Enter Receiver Account Number: ");
                int receiverAccount = sc.nextInt();
                // Check receiver account
                if (!accountExists(connection, receiverAccount)) {
                    System.out.println("Receiver account does not exist.");
                    return;
                }
                // Same account validation check
                if (senderAccount == receiverAccount) {
                    System.out.println("Sender and receiver accounts cannot be the same.");
                    return;
                }
                System.out.print("Enter Transfer Amount: ");
                double amount = sc.nextDouble();
                // Amount validation check
                if (amount <= 0) {
                    System.out.println("Amount must be greater than 0.");
                    return;
                }
                // Check balance BEFORE debit
                if (!isSufficient(connection, senderAccount, amount)) {
                    System.out.println("Insufficient Balance.");
                    return;
                }

                String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                try (PreparedStatement debitPs = connection.prepareStatement(debitQuery);
                     PreparedStatement creditPs = connection.prepareStatement(creditQuery);)
                {
                    // Debit
                    debitPs.setDouble(1, amount);
                    debitPs.setInt(2, senderAccount);
                    int debitRows = debitPs.executeUpdate();

                    // Credit
                    creditPs.setDouble(1, amount);
                    creditPs.setInt(2, receiverAccount);
                    int creditRows = creditPs.executeUpdate();

                    if (debitRows == 1 && creditRows == 1) {
                        connection.commit();
                        System.out.println("Rs" + amount +" transferred successfully from Account " + senderAccount + " to Account " + receiverAccount);

                        System.out.print("Do you want to see full transaction Details  (Y/N) : ");
                        String choice = sc.next();
                        if(choice.equals("Y") || choice.equals("y")){
                            System.out.println("--------------Sender Full Details--------------------");
                            showFullDetails(connection,senderAccount);
                            System.out.println("---------------Reciver Full Details---------------------");
                            showFullDetails(connection,receiverAccount);
                            System.out.println();
                            System.out.print("Do You want to transfer money again (Y/N) : ");
                            String choiceAgain = sc.next();
                            if (choiceAgain.equals("Y") || choiceAgain.equals("y")){
                                continue;
                            }else {
                                System.out.println("Thanks for using service.");
                                break;
                            }
                        }else {
                            System.out.println("Thanks for using service.");
                        }
                    } else {
                        connection.rollback();
                        System.out.println("Transaction failed.");
                    }

                } catch (SQLException e) {
    //                connection.rollback();
                    System.out.println("Transaction failed: " + e.getMessage());
                }


            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
        sc.close();
    }

// balance check
    static boolean isSufficient(Connection connection,int accountNumber,double amount) throws SQLException {
        String query ="SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    return currentBalance >= amount;
                }
            }
        }
        return false;
    }

// account chek
    static boolean accountExists(Connection connection,int accountNumber) throws SQLException {
        String query ="SELECT account_number FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
//    ShowFullDetails of transction
    public  static  void showFullDetails(Connection connection, int account_number) throws SQLException{
        String detailsQuery = "SELECT id, account_number, name , balance FROM accounts WHERE account_number = ?";
        try(PreparedStatement detailsPs = connection.prepareStatement(detailsQuery)){
            detailsPs.setInt(1,account_number);
            try(ResultSet rs = detailsPs.executeQuery()){
                if (rs.next()){
//                    System.out.println();
                    System.out.println("Id : "+rs.getInt("id"));
                    System.out.println("Account Holder Name : "+rs.getString("name"));
                    System.out.println("Account Number : "+rs.getInt("account_number"));
                    System.out.println("Balance : "+rs.getDouble("balance"));
                }
            }
        }
    }
}