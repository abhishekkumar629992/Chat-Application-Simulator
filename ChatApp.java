import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ==========================================
// 1. SYSTEM DOMAIN DEFINITIONS & CONSTANTS
// ==========================================

enum UserRole {
    SYSTEM_AUTOMATION(3),
    SERVER_ADMINISTRATOR(2),
    COMMUNITY_MODERATOR(1),
    STANDARD_USER(0);

    private final int authorityLevel;
    UserRole(int authorityLevel) { this.authorityLevel = authorityLevel; }
    public int getAuthorityLevel() { return authorityLevel; }
}

enum TargetChannel {
    GLOBAL_BROADCAST,
    PEER_TO_PEER_DIRECT,
    SYSTEM_ALERT
}

/**
 * Custom infrastructure exception designed to elegantly bubble up system errors.
 */
class ChatEngineException extends Exception {
    public ChatEngineException(String coreDiagnostics) {
        super(">> [ENGINE EXCEPTION] " + coreDiagnostics);
    }
}

// ==========================================
// 2. DATA ENTITY IMPLEMENTATIONS (ENCAPSULATED)
// ==========================================

class UserProfile {
    private final String clientID;
    private final String username;
    private final UserRole securityRole;
    private boolean connectionStatus;
    private boolean restrictedAccess; // Replaces 'isBanned'

    public UserProfile(String username, UserRole securityRole) {
        this.clientID = "UID-" + System.nanoTime() % 10000;
        this.username = username;
        this.securityRole = securityRole;
        this.connectionStatus = true;
        this.restrictedAccess = false;
    }

    // High-integrity immutable getters and state modifiers
    public String getClientID() { return clientID; }
    public String getUsername() { return username; }
    public UserRole getSecurityRole() { return securityRole; }
    public boolean isOnline() { return connectionStatus; }
    public void setConnectionStatus(boolean status) { this.connectionStatus = status; }
    public boolean isAccessRestricted() { return restrictedAccess; }
    public void restrictAccess(boolean restrictionState) { this.restrictedAccess = restrictionState; }
}

class TransactionalMessage {
    private static int sequentialIDRegistry = 100;
    
    private final int messageSequenceID;
    private final UserProfile originSender;
    private final String payloadContent;
    private final TargetChannel transmissionChannel;
    private final String explicitRecipient;
    private final String operationalTimestamp;

    // Factory pattern alternative for global system logs
    public TransactionalMessage(String systemPayload) {
        this.messageSequenceID = sequentialIDRegistry++;
        this.originSender = new UserProfile("SYSTEM", UserRole.SYSTEM_AUTOMATION);
        this.payloadContent = systemPayload;
        this.transmissionChannel = TargetChannel.SYSTEM_ALERT;
        this.explicitRecipient = "ALL_CLIENTS";
        this.operationalTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Explicit constructor for Public/Direct messages
    public TransactionalMessage(UserProfile sender, String recipient, String payload, TargetChannel channel) {
        this.messageSequenceID = sequentialIDRegistry++;
        this.originSender = sender;
        this.payloadContent = payload;
        this.transmissionChannel = channel;
        this.explicitRecipient = recipient;
        this.operationalTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

    public void compileOutputLog() {
        switch (transmissionChannel) {
            case SYSTEM_ALERT:
                System.out.println(String.format("[%s] 📢 SYSTEM DISPATCH: %s", operationalTimestamp, payloadContent));
                break;
            case PEER_TO_PEER_DIRECT:
                System.out.println(String.format("🔒 ID: #%d | [%s] (DM) %s ➔ %s: %s", 
                    messageSequenceID, operationalTimestamp, originSender.getUsername(), explicitRecipient, payloadContent));
                break;
            case GLOBAL_BROADCAST:
            default:
                String roleTag = (originSender.getSecurityRole() != UserRole.STANDARD_USER) 
                    ? "{" + originSender.getSecurityRole() + "} " : "";
                System.out.println(String.format("🌐 ID: #%d | [%s] %s%s: %s", 
                    messageSequenceID, operationalTimestamp, roleTag, originSender.getUsername(), payloadContent));
                break;
        }
    }
}

// ==========================================
// 3. SERVICE CONTROL LAYER (BUSINESS LOGIC)
// ==========================================

class ChatRoomManager {
    private final String localizedNamespace;
    private final TransactionalMessage[] messageLedger;
    private final UserProfile[] registeredUserDirectory;
    private int trackingMessagePointer = 0;
    private int trackingUserPointer = 0;

    public ChatRoomManager(String localizedNamespace, int messageBacklogCap, int clientDirectoryCap) {
        this.localizedNamespace = localizedNamespace;
        this.messageLedger = new TransactionalMessage[messageBacklogCap];
        this.registeredUserDirectory = new UserProfile[clientDirectoryCap];
        executeSystemBroadcast("Initialization sequence complete for server instance: " + localizedNamespace);
    }

    public void onboardNewClient(UserProfile prospectiveClient) {
        if (trackingUserPointer >= registeredUserDirectory.length) {
            System.err.println(">> Infrastructure threshold breached. Refusing connection to: " + prospectiveClient.getUsername());
            return;
        }
        registeredUserDirectory[trackingUserPointer++] = prospectiveClient;
        executeSystemBroadcast(String.format("Client Profile Registered: %s assigned Security Clearance: %s", 
            prospectiveClient.getUsername(), prospectiveClient.getSecurityRole()));
    }

    public void dispatchGlobalBroadcast(UserProfile activeSender, String outboundPayload) throws ChatEngineException {
        assertClientIntegrity(activeSender);
        commitToLedger(new TransactionalMessage(activeSender, "ALL", outboundPayload, TargetChannel.GLOBAL_BROADCAST));
    }

    public void dispatchDirectPayload(UserProfile activeSender, String targetUsername, String outboundPayload) throws ChatEngineException {
        assertClientIntegrity(activeSender);
        UserProfile recipientNode = locateUserInDirectory(targetUsername);
        
        if (recipientNode == null) {
            throw new ChatEngineException("Routing Resolution Failed. Target recipient address unreachable: " + targetUsername);
        }
        commitToLedger(new TransactionalMessage(activeSender, targetUsername, outboundPayload, TargetChannel.PEER_TO_PEER_DIRECT));
    }

    public void invokeAdministrativeSanction(UserProfile administrativeActor, String targetUsername) throws ChatEngineException {
        if (administrativeActor.getSecurityRole().getAuthorityLevel() < UserRole.COMMUNITY_MODERATOR.getAuthorityLevel()) {
            throw new ChatEngineException("Security Exception. Access Denied for Action: User Isolation Protocol.");
        }

        UserProfile targetNode = locateUserInDirectory(targetUsername);
        if (targetNode != null) {
            targetNode.restrictAccess(true);
            targetNode.setConnectionStatus(false);
            executeSystemBroadcast(String.format("Security Alert: User '%s' has been network isolated by Administrator '%s'", 
                targetNode.getUsername(), administrativeActor.getUsername()));
        }
    }

    // Micro-validations & Internal Array Traversals
    private void assertClientIntegrity(UserProfile clientNode) throws ChatEngineException {
        if (clientNode.isAccessRestricted()) {
            throw new ChatEngineException("Access Denied: Account Token Suspended/Banned. Action Blocked for: " + clientNode.getUsername());
        }
        if (!clientNode.isOnline()) {
            throw new ChatEngineException("I/O Operations Interrupted: Client Node is Offline: " + clientNode.getUsername());
        }
    }

    private UserProfile locateUserInDirectory(String targetUsername) {
        for (int i = 0; i < trackingUserPointer; i++) {
            if (registeredUserDirectory[i].getUsername().equalsIgnoreCase(targetUsername)) {
                return registeredUserDirectory[i];
            }
        }
        return null;
    }

    private void executeSystemBroadcast(String broadcastString) {
        if (trackingMessagePointer < messageLedger.length) {
            messageLedger[trackingMessagePointer++] = new TransactionalMessage(broadcastString);
        }
    }

    private void commitToLedger(TransactionalMessage fullyFormedMessage) {
        if (trackingMessagePointer < messageLedger.length) {
            messageLedger[trackingMessagePointer++] = fullyFormedMessage;
        } else {
            System.err.println(">> System Ledger Overflow. Dropping incoming packets.");
        }
    }

    public void renderSystemDiagnostics() {
        System.out.println("\n=========================================================================================");
        System.out.println("  SYSTEM DIAGNOSTICS LOG FEED: " + localizedNamespace.toUpperCase());
        System.out.println("=========================================================================================");
        for (int i = 0; i < trackingMessagePointer; i++) {
            messageLedger[i].compileOutputLog();
        }
        System.out.println("=========================================================================================\n");
    }
}

// ==========================================
// 4. MAIN IMPLEMENTATION ARCHITECTURE
// ==========================================

public class ChatApp {
    public static void main(String[] args) {
        // Initialize Core Management Engine
        ChatRoomManager corporateCluster = new ChatRoomManager("Enterprise Alpha Infrastructure", 100, 10);

        // Instantiate Corporate Structural Users
        UserProfile headAdmin = new UserProfile("Alice_CEO", UserRole.SERVER_ADMINISTRATOR);
        UserProfile supervisor = new UserProfile("Bob_Manager", UserRole.COMMUNITY_MODERATOR);
        UserProfile developer1 = new UserProfile("Charlie_Dev", UserRole.STANDARD_USER);
        UserProfile developer2 = new UserProfile("David_Intern", UserRole.STANDARD_USER);

        // Map Node Users to Secure Service Array System
        corporateCluster.onboardNewClient(headAdmin);
        corporateCluster.onboardNewClient(supervisor);
        corporateCluster.onboardNewClient(developer1);
        corporateCluster.onboardNewClient(developer2);

        System.out.println("\n--- SIMULATION COMMENCING ---\n");

        try {
            // Execution Step 1: Baseline Communication Processing
            corporateCluster.dispatchGlobalBroadcast(developer1, "Deploying version 4.2.0 production hotfix live now.");
            corporateCluster.dispatchDirectPayload(developer1, "Bob_Manager", "Can you review the AWS CloudWatch logs for trace errors?");
            corporateCluster.dispatchDirectPayload(supervisor, "Charlie_Dev", "On it. Logs look clean from my end.");

            // Execution Step 2: Policy Infraction & Threat Containment Scenario
            corporateCluster.dispatchGlobalBroadcast(developer2, "Click here to download free memory ram! Totally safe link!");
            
            // Administrative Sanction applied to contain infrastructure node risk
            corporateCluster.invokeAdministrativeSanction(headAdmin, "David_Intern");

            // Execution Step 3: Proving Fault-Tolerance & Exception Catching
            System.out.println("\n[SIMULATING MALICIOUS ACTION RETRY]");
            corporateCluster.dispatchGlobalBroadcast(developer2, "Can I please bypass your network barrier?");

        } catch (ChatEngineException operationalFailureDiagnostics) {
            // Safely intercepts runtime errors cleanly, ensuring core systems don't experience crashes
            System.out.println(operationalFailureDiagnostics.getMessage());
        }

        // Output complete transaction chain records cleanly
        corporateCluster.renderSystemDiagnostics();
    }
}
