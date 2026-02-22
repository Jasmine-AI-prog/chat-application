package server;

import java.io.*;
import java.net.*;
import java.util.List;
import common.Message;
import common.Constants;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String clientAddress;
    private int clientPort;
    private boolean isConnected;
    
    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.clientAddress = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        this.isConnected = true;
    }
    
    @Override
    public void run() {
        try {
            // IMPORTANT: Ordre des flux - d'abord OutputStream puis InputStream
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush(); // Très important !
            in = new ObjectInputStream(clientSocket.getInputStream());
            
            // Recevoir le nom d'utilisateur avec gestion d'erreur
            Object obj = in.readObject();
            if (obj instanceof String) {
                username = (String) obj;
            } else {
                username = "Anonyme" + System.currentTimeMillis();
            }
            
            // Vérifier que le nom n'est pas vide
            if (username == null || username.trim().isEmpty()) {
                username = "Client" + clientPort;
            }
            
            System.out.println("👤 " + username + " a rejoint le chat (" + clientAddress + ")");
            
            // Envoyer message de bienvenue
            Message welcomeMsg = new Message(
                Constants.SYSTEM_SENDER,
                "Bienvenue " + username + "!",
                Message.MessageType.INFO
            );
            sendMessage(welcomeMsg);
            
            // Notifier les autres clients
            Message connectMsg = new Message(
                Constants.SYSTEM_SENDER,
                username + Constants.CONNECT_MESSAGE,
                Message.MessageType.CONNECT
            );
            server.broadcastMessage(connectMsg, this);
            
            // Envoyer la liste des utilisateurs
            sendUserList();
            
            // Boucle principale de réception des messages
            while (isConnected) {
                try {
                    Message message = (Message) in.readObject();
                    
                    if (message != null && message.getContent() != null) {
                        System.out.println("💬 [" + username + "] " + message.getContent());
                        
                        // Vérifier si c'est une commande
                        if (message.getContent().startsWith("/")) {
                            handleCommand(message.getContent());
                        } else {
                            // Diffuser le message normal
                            server.broadcastMessage(message, this);
                        }
                    }
                    
                } catch (EOFException e) {
                    // Fin normale de la connexion
                    System.out.println("👋 " + username + " s'est déconnecté proprement");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("❌ Erreur de désérialisation: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ Erreur avec " + (username != null ? username : "client inconnu") + 
                             ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Erreur de lecture du nom d'utilisateur: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleCommand(String command) {
        if (command.equals("/list")) {
            sendUserList();
        } else if (command.startsWith("/msg ")) {
            handlePrivateMessage(command);
        } else if (command.equals("/help")) {
            sendHelp();
        } else if (command.equals("/quit")) {
            disconnect();
        }
    }
    
    private void sendUserList() {
        List<String> users = server.getConnectedUsers();
        StringBuilder userList = new StringBuilder("👥 Utilisateurs connectés (" + users.size() + "):\n");
        for (String user : users) {
            userList.append("  • ").append(user).append("\n");
        }
        Message userListMsg = new Message(
            Constants.SYSTEM_SENDER,
            userList.toString(),
            Message.MessageType.INFO
        );
        sendMessage(userListMsg);
    }
    
    private void handlePrivateMessage(String command) {
        String[] parts = command.substring(5).split(" ", 2);
        if (parts.length == 2) {
            String recipient = parts[0];
            String privateMsg = parts[1];
            Message infoMsg = new Message(
                Constants.SYSTEM_SENDER,
                "Message privé pour " + recipient + ": " + privateMsg,
                Message.MessageType.INFO
            );
            sendMessage(infoMsg);
        }
    }
    
    private void sendHelp() {
        String helpText = "📋 Commandes disponibles:\n" +
                         "  /list - Voir les utilisateurs connectés\n" +
                         "  /msg [user] [message] - Envoyer un message privé\n" +
                         "  /help - Afficher cette aide\n" +
                         "  /quit - Quitter le chat";
        Message helpMsg = new Message(
            Constants.SYSTEM_SENDER,
            helpText,
            Message.MessageType.INFO
        );
        sendMessage(helpMsg);
    }
    
    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
                out.reset(); // Important pour éviter la corruption d'objets
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur d'envoi à " + username + ": " + e.getMessage());
            disconnect();
        }
    }
    
    public void sendUDPMessage(String message) {
        // Implémentez selon vos besoins
    }
    
    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            
            // Notifier les autres avant de se déconnecter
            if (username != null) {
                server.removeClient(this);
            }
            
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Ignorer les erreurs de fermeture
            }
            
            System.out.println("🔌 Déconnexion de " + (username != null ? username : "client"));
        }
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getAddress() {
        return clientAddress;
    }
    
    public int getPort() {
        return clientPort;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}