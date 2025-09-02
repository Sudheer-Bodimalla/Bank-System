import java.io.*;
import java.util.*;

// Transaction class
class Transaction implements Serializable {
    private String type;
    private double amount;
    private Date date;

    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.date = new Date();
    }

    @Override
    public String toString() {
        return date + " - " + type + " : " + amount;
    }
}

// Account class
class Account implements Serializable {
    private String accountNumber;
    private String holderName;
    private double balance;
    private List<Transaction> transactions = new ArrayList<>();

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
        transactions.add(new Transaction("Initial Deposit", balance));
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactions.add(new Transaction("Deposit", amount));
            System.out.println("Deposited: " + amount);
        } else {
            System.out.println("Invalid deposit amount!");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            transactions.add(new Transaction("Withdraw", amount));
            System.out.println("Withdrawn: " + amount);
        } else {
            System.out.println("Insufficient balance or invalid amount!");
        }
    }

    public void transfer(Account target, double amount) {
        if (amount > 0 && balance >= amount) {
            this.withdraw(amount);
            target.deposit(amount);
            transactions.add(new Transaction("Transfer to " + target.getHolderName(), amount));
            target.transactions.add(new Transaction("Transfer from " + this.holderName, amount));
            System.out.println("Transferred " + amount + " to " + target.getHolderName());
        } else {
            System.out.println("Transfer failed. Check balance and amount.");
        }
    }

    public void showTransactions() {
        System.out.println("\nTransaction History for " + holderName + ":");
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }

    public void displayInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Holder Name: " + holderName);
        System.out.println("Balance: " + balance);
    }
}

// Bank class (manages accounts and file persistence)
class Bank {
    private Map<String, Account> accounts = new HashMap<>();
    private static final String FILE_NAME = "bank_data.ser";

    public Bank() {
        loadData();
    }

    public void createAccount(String accNo, String name, double balance) {
        if (accounts.containsKey(accNo)) {
            System.out.println("Account already exists!");
            return;
        }
        Account acc = new Account(accNo, name, balance);
        accounts.put(accNo, acc);
        saveData();
        System.out.println("Account created successfully!");
    }

    public Account getAccount(String accNo) {
        return accounts.get(accNo);
    }

    public void deleteAccount(String accNo) {
        if (accounts.remove(accNo) != null) {
            saveData();
            System.out.println("Account deleted successfully.");
        } else {
            System.out.println("Account not found!");
        }
    }

    public void displayAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts available.");
        } else {
            for (Account acc : accounts.values()) {
                acc.displayInfo();
                System.out.println("----------------------");
            }
        }
    }

    // Save account data to file
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Load account data from file
    @SuppressWarnings("unchecked") // ✅ fixes unchecked cast warning
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            accounts = (Map<String, Account>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}

// Main App
public class BankApp {
    private static Scanner sc = new Scanner(System.in);
    private static Bank bank = new Bank();

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n===== Bank Management System =====");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Check Balance");
            System.out.println("6. Transaction History");
            System.out.println("7. Display All Accounts");
            System.out.println("8. Delete Account");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: createAccount(); break;
                case 2: depositMoney(); break;
                case 3: withdrawMoney(); break;
                case 4: transferMoney(); break;
                case 5: checkBalance(); break;
                case 6: showTransactions(); break;
                case 7: bank.displayAllAccounts(); break;
                case 8: deleteAccount(); break;
                case 0: System.out.println("Exiting... Thank you!"); break;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    private static void createAccount() {
        System.out.print("Enter Account Number: ");
        String accNo = sc.next();
        System.out.print("Enter Holder Name: ");
        String name = sc.next();
        System.out.print("Enter Initial Balance: ");
        double balance = sc.nextDouble();
        bank.createAccount(accNo, name, balance);
    }

    private static void depositMoney() {
        System.out.print("Enter Account Number: ");
        String accNo = sc.next();
        Account acc = bank.getAccount(accNo);
        if (acc != null) {
            System.out.print("Enter Amount to Deposit: ");
            acc.deposit(sc.nextDouble());
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void withdrawMoney() {
        System.out.print("Enter Account Number: ");
        String accNo = sc.next();
        Account acc = bank.getAccount(accNo);
        if (acc != null) {
            System.out.print("Enter Amount to Withdraw: ");
            acc.withdraw(sc.nextDouble());
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void transferMoney() {
        System.out.print("Enter From Account Number: ");
        String fromAccNo = sc.next();
        System.out.print("Enter To Account Number: ");
        String toAccNo = sc.next();

        Account fromAcc = bank.getAccount(fromAccNo);
        Account toAcc = bank.getAccount(toAccNo);

        if (fromAcc != null && toAcc != null) {
            System.out.print("Enter Amount to Transfer: ");
            fromAcc.transfer(toAcc, sc.nextDouble());
        } else {
            System.out.println("Invalid account(s)!");
        }
    }

    private static void checkBalance() {
        System.out.print("Enter Account Number: ");
        Account acc = bank.getAccount(sc.next());
        if (acc != null) {
            System.out.println("Balance: " + acc.getBalance());
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void showTransactions() {
        System.out.print("Enter Account Number: ");
        Account acc = bank.getAccount(sc.next());
        if (acc != null) {
            acc.showTransactions();
        } else {
            System.out.println("Account not found!");
        }
    }

    private static void deleteAccount() {
        System.out.print("Enter Account Number: ");
        bank.deleteAccount(sc.next());
    }
}
