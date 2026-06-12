import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Enumeration for User Roles
enum Role {
    ADMIN, MODERATOR, REGULAR
}

// Enumeration for Message Types
enum MessageType {
    PUBLIC, PRIVATE, SYSTEM
}

class User {
    private String username;
    private boolean isOnline;
    private boolean isBanned;
    private Role role;

    public User(String username, Role role) {
        this.username = username;
        this.role = role;
        this.isOnline = true;
        this.isBanned = false;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { this.isOnline = online; }
    public boolean isBanned() { return isBanned; }
    public void setBanned(boolean banned) { this.isBanned = banned; }
    public Role getRole() { return role; }
}

class Message {
    private static int idCounter = 1;
    private int messageId;
    private User sender;
    private String content;
    private String timestamp;
    private MessageType type;
    private String recipientName; // Used for Direct Messages

    // Constructor for Public/System messages
    public Message(User sender, String content, MessageType type) {
        this.messageId = idCounter++;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.recipientName = "ALL";
        this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    // Constructor for Direct Messages
    public Message(User sender, String recipientName, String content) {
        this.messageId = idCounter++;
        this.sender = sender;
        this.content = content;
        this.type = MessageType.PRIVATE;
        this.recipientName = recipientName;
        this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    public void display() {
        switch (type) {
            case SYSTEM:
                System.out.println(String.format("[SYSTEM] %s", content));
                break;
            case PRIVATE:
                System.out.println(String.format("#%03d [%s] (DM) %s -> %s: %s", 
                    messageId, timestamp, sender.getUsername(), recipientName, content));
                break;
            case PUBLIC:
            default:
                String prefix = (sender.getRole() != Role.REGULAR) ? "[" + sender.getRole() + "] " : "";
                System.out.println(String.format("#%03d [%s] %s%s: %s", 
                    messageId, timestamp, prefix, sender.getUsername(), content));
                break;
        }
    }
}

class ChatRoom {
    private String roomName;
    private Message[] globalMessages;
    private User[] registeredUsers;
    private int messageCount = 0;
    private int userCount = 0;

    public ChatRoom(String roomName, int maxMessages, int maxUsers) {
        this.roomName = roomName;
        this.globalMessages = new Message[maxMessages];
        this.registeredUsers = new User[maxUsers];
    }

    public void registerUser(User user) {
        if (userCount >= registeredUsers.length) {
            logSystemMessage("Failed to add " + user.getUsername() + ". Room capacity full.");
            return;
        }
        registeredUsers[userCount++] = user;
        logSystemMessage(user.getUsername() + " joined [" + roomName + "] as " + user.getRole());
    }

    // Send a message to the entire chatroom
    public void sendPublicMessage(User sender, String text) {
        if (!validateUser(sender)) return;

        if (messageCount < globalMessages.length) {
            globalMessages[messageCount++] = new Message(sender, text, MessageType.PUBLIC);
        }
    }

    // Send a private Direct Message (DM) to a specific user inside the array
    public void sendDirectMessage(User sender, String recipientName, String text) {
        if (!validateUser(sender)) return;

        User recipient = findUser(recipientName);
        if (recipient == null) {
            System.out.println(">> [ERROR] User '" + recipientName + "' not found in this room.");
            return;
        }

        if (messageCount < globalMessages.length) {
            globalMessages[messageCount++] = new Message(sender, recipientName, text);
        }
    }

    // Moderation Action: Kick/Ban a user
    public void moderationKick(User admin, String targetUsername) {
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.MODERATOR) {
            System.out.println(">> [DENIED] Only Admins/Moderators can kick users.");
            return;
        }

        User target = findUser(targetUsername);
        if (target != null) {
            target.setBanned(true);
            target.setOnline(false);
            logSystemMessage(target.getUsername() + " has been banned by " + admin.getUsername());
        }
    }

    // Helper Validations
    private boolean validateUser(User user) {
        if (user.isBanned()) {
            System.out.println(">> [REJECTED] " + user.getUsername() + " is banned from this server.");
            return false;
        }
        if (!user.isOnline()) {
            System.out.println(">> [REJECTED] " + user.getUsername() + " is offline.");
            return false;
        }
        return true;
    }

    private User findUser(String username) {
        for (int i = 0; i < userCount; i++) {
            if (registeredUsers[i].getUsername().equalsIgnoreCase(username)) {
                return registeredUsers[i];
            }
        }
        return null;
    }

    private void logSystemMessage(String text) {
        if (messageCount < globalMessages.length) {
            globalMessages[messageCount++] = new Message(null, text, MessageType.SYSTEM);
        }
    }

    // Display the structured logs
    public void displayChatHistory() {
        System.out.println("\n==============================================");
        System.out.println("          " + roomName.toUpperCase() + " LIVE FEED          ");
        System.out.println("==============================================");
        for (int i = 0; i < messageCount; i++) {
            globalMessages[i].display();
        }
        System.out.println("==============================================\n");
    }
}

public class ChatApp {
    public static void main(String[] args) {
        // Create server instance
        ChatRoom devSquad = new ChatRoom("Dev Squad HQ", 50, 10);

        // Instantiating users with different Privilege Roles
        User adminUser = new User("Alice", Role.ADMIN);
        User modUser = new User("Bob", Role.MODERATOR);
        User regularUser1 = new User("Charlie", Role.REGULAR);
        User regularUser2 = new User("David", Role.REGULAR);

        // Registering network profiles
        devSquad.registerUser(adminUser);
        devSquad.registerUser(modUser);
        devSquad.registerUser(regularUser1);
        devSquad.registerUser(regularUser2);

        // 1. Regular Chat flow
        devSquad.sendPublicMessage(regularUser1, "Hey team! Is the server deployment up?");
        devSquad.sendPublicMessage(modUser, "Yes Charlie, production pipeline looks stable.");

        // 2. Testing Direct Messaging (DMs)
        devSquad.sendDirectMessage(regularUser1, "David", "Hey bro, check your email for the secret credentials.");
        devSquad.sendDirectMessage(regularUser2, "Charlie", "Got it, thanks!");

        // 3. Toxic behavior / Moderation simulation
        devSquad.sendPublicMessage(regularUser2, "I am going to post malicious links here!");
        
        // Admin steps in and issues a ban command
        devSquad.moderationKick(adminUser, "David");

        // Failsafe checks: Banned user tries to converse again
        devSquad.sendPublicMessage(regularUser2, "Let me try typing again..."); 

        // 4. Output the definitive centralized chat engine log
        devSquad.displayChatHistory();
    }
}
