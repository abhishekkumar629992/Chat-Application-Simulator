// ==========================
// 1. SIMPLE CHAT APPLICATION 
// ==========================

class UserProfile {
    // Basic properties using primitive and standard types
    public String userId;
    public String username;
    public String userRole; // "Admin", "Moderator", "User"
    public boolean isOnline;
    public boolean isBanned;

    // Standard constructor
    public UserProfile(String username, String userRole) {
        // Simple unique ID generation using system time digits
        this.userId = "UID-" + (System.currentTimeMillis() % 10000);
        this.username = username;
        this.userRole = userRole;
        this.isOnline = true;
        this.isBanned = false;
    }
}

class TransactionalMessage {
    private static int idCounter = 100; // Easy counter for message IDs
    
    public int messageId;
    public String senderName;
    public String receiverName;
    public String messageText;
    public String channelType; // "GLOBAL", "DM", "SYSTEM"
    public String timestamp;

    // Constructor for regular Chat Messages (Global / Direct Messages)
    public TransactionalMessage(UserProfile sender, String receiverName, String messageText, String channelType) {
        this.messageId = idCounter++;
        this.senderName = sender.username;
        this.receiverName = receiverName;
        this.messageText = messageText;
        this.channelType = channelType;
        this.timestamp = "10:00 AM"; // Simplified static time instead of complex LocalDateTime format
    }

    // Constructor for System Alerts
    public TransactionalMessage(String systemNotification) {
        this.messageId = idCounter++;
        this.senderName = "SYSTEM";
        this.receiverName = "ALL";
        this.messageText = systemNotification;
        this.channelType = "SYSTEM";
        this.timestamp = "SYSTEM TIME";
    }

    // Simple display method using simple if-else instead of switches
    public void printMessage() {
        if (channelType.equals("SYSTEM")) {
            System.out.println("[" + timestamp + "] SYSTEM NOTICE: " + messageText);
        } 
        else if (channelType.equals("DM")) {
            System.out.println("ID #" + messageId + " [DM] " + senderName + " -> " + receiverName + ": " + messageText);
        } 
        else {
            System.out.println("ID #" + messageId + " [GLOBAL] " + senderName + ": " + messageText);
        }
    }
}

class ChatRoomManager {
    public String roomName;
    
    // Fixed arrays used instead of complex Collections/ArrayLists
    public TransactionalMessage[] messageHistory;
    public UserProfile[] userList;
    
    // Tracking array indexes manually
    public int totalMessages = 0;
    public int totalUsers = 0;

    // Initialization
    public ChatRoomManager(String roomName) {
        this.roomName = roomName;
        this.messageHistory = new TransactionalMessage[100]; // capacity limit
        this.userList = new UserProfile[10];
        
        // Starting system broadcast
        sendSystemAlert("Server '" + roomName + "' is up and running successfully.");
    }

    // Function to add new users
    public void addUser(UserProfile newUser) {
        if (totalUsers < userList.length) {
            userList[totalUsers] = newUser;
            totalUsers++;
            sendSystemAlert("New user registered: " + newUser.username + " (" + newUser.userRole + ")");
        } else {
            System.out.println("Server full! Cannot add user: " + newUser.username);
        }
    }

    // Basic method to broadcast messages to everyone
    public void sendGlobalMessage(UserProfile sender, String text) {
        // Simple manual validation checks
        if (sender.isBanned) {
            System.out.println("ERROR: " + sender.username + " is banned and cannot send messages.");
            return;
        }
        
        TransactionalMessage msg = new TransactionalMessage(sender, "ALL", text, "GLOBAL");
        saveMessageToHistory(msg);
    }

    // Basic method to send direct private messages
    public void sendPrivateMessage(UserProfile sender, String targetUser, String text) {
        if (sender.isBanned) {
            System.out.println("ERROR: " + sender.username + " is banned and cannot send messages.");
            return;
        }

        // Linear array search to check if user exists
        boolean found = false;
        for (int i = 0; i < totalUsers; i++) {
            if (userList[i].username.equalsIgnoreCase(targetUser)) {
                found = true;
                break;
            }
        }

        if (found) {
            TransactionalMessage msg = new TransactionalMessage(sender, targetUser, text, "DM");
            saveMessageToHistory(msg);
        } else {
            System.out.println("ERROR: Receiver '" + targetUser + "' not found on this server.");
        }
    }

    // Admin function to ban rule breakers
    public void banUser(UserProfile adminUser, String badUser) {
        // Checking basic string match for rights instead of enum levels
        if (!adminUser.userRole.equals("Admin") && !adminUser.userRole.equals("Moderator")) {
            System.out.println("ERROR: Access Denied! Only Admin/Moderators can ban users.");
            return;
        }

        // Loop to find and ban the user
        for (int i = 0; i < totalUsers; i++) {
            if (userList[i].username.equalsIgnoreCase(badUser)) {
                userList[i].isBanned = true;
                userList[i].isOnline = false;
                sendSystemAlert("Security Action: '" + badUser + "' has been banned by '" + adminUser.username + "'");
                return;
            }
        }
    }

    // Helper methods to keep array data structured
    private void sendSystemAlert(String alertText) {
        if (totalMessages < messageHistory.length) {
            messageHistory[totalMessages] = new TransactionalMessage(alertText);
            totalMessages++;
        }
    }

    private void saveMessageToHistory(TransactionalMessage msg) {
        if (totalMessages < messageHistory.length) {
            messageHistory[totalMessages] = msg;
            totalMessages++;
        } else {
            System.out.println("History Full! Message dropped.");
        }
    }

    // Print all chat logs sequentially
    public void printServerLogs() {
        System.out.println("\n-------------------------------------------");
        System.out.println("        SERVER CHAT LOGS FOR: " + roomName.toUpperCase());
        System.out.println("-------------------------------------------");
        for (int i = 0; i < totalMessages; i++) {
            messageHistory[i].printMessage();
        }
        System.out.println("-------------------------------------------\n");
    }
}

// Main Driving Class
public class ChatApp {
    public static void main(String[] args) {
        // Initializing the manager class object
        ChatRoomManager myChatRoom = new ChatRoomManager("College Group Chat");

        // Creating student profiles with basic string roles
        UserProfile principal = new UserProfile("Alice_Principal", "Admin");
        UserProfile professor = new UserProfile("Bob_Prof", "Moderator");
        UserProfile student1 = new UserProfile("Charlie_Student", "User");
        UserProfile student2 = new UserProfile("David_Intern", "User");

        // Onboarding users to array
        myChatRoom.addUser(principal);
        myChatRoom.addUser(professor);
        myChatRoom.addUser(student1);
        myChatRoom.addUser(student2);

        System.out.println("\n--- CHAT APPLICATION SIMULATOR STARTING ---\n");

        // Normal execution without complex try-catch handlers
        myChatRoom.sendGlobalMessage(student1, "Hello everyone! The notes are uploaded.");
        myChatRoom.sendPrivateMessage(student1, "Bob_Prof", "Respected sir, please verify my project.");
        myChatRoom.sendPrivateMessage(professor, "Charlie_Student", "Yes, it looks correct.");

        // Spammer script execution simulation
        myChatRoom.sendGlobalMessage(student2, "Click here for free marks! 100% fake virus link!");
        
        // Administrative reaction simulation
        myChatRoom.banUser(principal, "David_Intern");

        // Action block check validation test
        System.out.println("\n[RETRACTING BLOCKED USER ACTIONS]");
        myChatRoom.sendGlobalMessage(student2, "Please unban me!");

        // Printing results directly
        myChatRoom.printServerLogs();
    }
}
