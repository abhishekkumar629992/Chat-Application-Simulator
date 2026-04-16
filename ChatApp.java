import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class User {
    private String username;
    private boolean isOnline;

    // Constructor
    public User(String username) {
        this.username = username;
        this.isOnline = true;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return isOnline;
    }
}

class Message {
    private User sender;
    private String content;
    private String timestamp;

    // Constructor
    public Message(User sender, String content) {
        this.sender = sender;
        this.content = content;

        // Generate current time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        this.timestamp = LocalTime.now().format(formatter);
    }

    // Display message
    public void display() {
        System.out.println("[" + timestamp + "] " 
            + sender.getUsername() + ": " + content);
    }
}

class ChatRoom {
    private Message[] messages;
    private int messageCount;

    // Constructor
    public ChatRoom(int size) {
        messages = new Message[size];
        messageCount = 0;
    }

    // Step 4: sendMessage method
    public void sendMessage(User user, String messageText) {
        if (messageCount < messages.length) {
            Message msg = new Message(user, messageText);
            messages[messageCount] = msg;
            messageCount++;
        } else {
            System.out.println("Chat room is full!");
        }
    }

    // Step 6: Display chat history
    public void displayChat() {
        for (int i = 0; i < messageCount; i++) {
            messages[i].display();
        }
    }
}

public class ChatApp {
    public static void main(String[] args) {

        // Create users
        User user1 = new User("Alice");
        User user2 = new User("Bob");
        User user3 = new User("Charlie");

        // Create chat room
        ChatRoom chat = new ChatRoom(10);

        // Send messages
        chat.sendMessage(user1, "Hello everyone!");
        chat.sendMessage(user2, "Hi Alice!");
        chat.sendMessage(user3, "Good morning!");

        // Display chat history
        chat.displayChat();
    }
}