import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class User {
    private String username;
    private boolean isOnline;

    // Constructor
    public User(String username) {
        this.username = username;
        this.isOnline = true; // Users are online by default
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
    }
}

class Message {
    private static int idCounter = 1; // Auto-incrementing ID tracker
    private int messageId;
    private User sender;
    private String content;
    private String timestamp;

    // Constructor
    public Message(User sender, String content) {
        this.messageId = idCounter++;
        this.sender = sender;
        this.content = content;

        // Generate current time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        this.timestamp = LocalTime.now().format(formatter);
    }

    // Display message with ID
    public void display() {
        System.out.println(String.format("#%03d [%s] %s: %s", 
            messageId, timestamp, sender.getUsername(), content));
    }
}

class ChatRoom {
    private String roomName;
    private Message[] messages;
    private User[] activeUsers; // New Array to track room members
    private int messageCount;
    private int userCount;

    // Constructor
    public ChatRoom(String roomName, int maxMessages, int maxUsers) {
        this.roomName = roomName;
        this.messages = new Message[maxMessages];
        this.activeUsers = new User[maxUsers];
        this.messageCount = 0;
        this.userCount = 0;
    }

    // Add a user to the chatroom array
    public void registerUser(User user) {
        if (userCount < activeUsers.length) {
            activeUsers[userCount] = user;
            userCount++;
            System.out.println(">> System: " + user.getUsername() + " joined " + roomName);
        } else {
            System.out.println(">> System: Cannot add " + user.getUsername() + ". Room member limit reached.");
        }
    }

    // Enhanced sendMessage method with validations
    public void sendMessage(User user, String messageText) {
        // Validation 1: Check if user is registered in this room
        if (!isUserInRoom(user)) {
            System.out.println(">> [ERROR] " + user.getUsername() + " is not a member of this room.");
            return;
        }

        // Validation 2: Check if user is online
        if (!user.isOnline()) {
            System.out.println(">> [ERROR] " + user.getUsername() + " cannot send message while offline.");
            return;
        }

        // Validation 3: Check storage space
        if (messageCount < messages.length) {
            Message msg = new Message(user, messageText);
            messages[messageCount] = msg;
            messageCount++;
        } else {
            System.out.println(">> System: Chat log history is completely full!");
        }
    }

    // Helper method to scan the user array
    private boolean isUserInRoom(User targetUser) {
        for (int i = 0; i < userCount; i++) {
            if (activeUsers[i].getUsername().equalsIgnoreCase(targetUser.getUsername())) {
                return true;
            }
        }
        return false;
    }

    // Display chat history neatly
    public void displayChat() {
        System.out.println("\n--- " + roomName.toUpperCase() + " HISTORY ---");
        if (messageCount == 0) {
            System.out.println("(No messages yet)");
        }
        for (int i = 0; i < messageCount; i++) {
            messages[i].display();
        }
        System.out.println("-----------------------------\n");
    }
}

public class ChatApp {
    public static void main(String[] args) {
        // 1. Initialize room with caps: max 100 messages, max 5 users
        ChatRoom programmingHub = new ChatRoom("Programming Hub", 100, 5);

        // 2. Create users
        User alice = new User("Alice");
        User bob = new User("Bob");
        User charlie = new User("Charlie");
        User intruder = new User("MaliciousHacker"); // Will test unregistered check

        // 3. Register valid users to the chat room array
        programmingHub.registerUser(alice);
        programmingHub.registerUser(bob);
        programmingHub.registerUser(charlie);

        // 4. Regular conversations
        programmingHub.sendMessage(alice, "Hello everyone!");
        programmingHub.sendMessage(bob, "Hi Alice, what's cooking?");
        
        // 5. Test scenario: Unregistered user tries to type
        programmingHub.sendMessage(intruder, "I want to spam this chat!"); 

        // 6. Test scenario: User goes offline and tries to type
        charlie.setOnline(false);
        System.out.println(">> System: Charlie changed status to offline.");
        programmingHub.sendMessage(charlie, "Good morning guys!"); // Should fail

        // 7. Charlie comes back online
        charlie.setOnline(true);
        System.out.println(">> System: Charlie changed status to online.");
        programmingHub.sendMessage(charlie, "Sorry, connection dropped! Good morning!");

        // 8. Print out the final logs
        programmingHub.displayChat();
    }
}
