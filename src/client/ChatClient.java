package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import common.Message;
import common.Constants;

public class ChatClient {
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private ObjectOutputStream tcpOut;
    private ObjectInputStream tcpIn;
    private InetAddress serverAddress;
    private String username;
    private boolean isConnected;
    private Scanner scanner;
    
    public ChatClient(String username) {
        this.username = username;
        this.isConnected = false;
        this.scanner = new Scanner(System.in);
    }
    
    public boolean connect() {
        System.out.println("🔗 Connexion au serveur " + Constants.SERVER_IP + ":" + Constants.TCP_PORT + "...");
        
        try {
            // Connexion TCP
            tcpSocket = new Socket();
            tcpSocket.connect(new InetSocketAddress(Constants.SERVER_IP, Constants.TCP_PORT), 5000);
            
            // IMPORTANT: Ordre correct des flux
            tcpOut = new ObjectOutputStream(tcpSocket.getOutputStream());
            tcpOut.flush(); // CRITIQUE !
            
            tcpIn = new ObjectInputStream(tcpSocket.getInputStream());
            
            // Connexion UDP
            udpSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName(Constants.SERVER_IP);
            
            // Envoyer le nom d'utilisateur - vérifier qu'il n'est pas vide
            if (username == null || username.trim().isEmpty()) {
                username = "User" + System.currentTimeMillis();
            }
            
            System.out.println("📤 Envoi du nom d'utilisateur: " + username);
            tcpOut.writeObject(username);
            tcpOut.flush();
            
            isConnected = true;
            System.out.println("✅ Connecté au serveur avec succès!");
            
            return true;
            
        } catch (SocketTimeoutException e) {
            System.err.println("❌ Timeout de connexion - Serveur non disponible");
            return false;
        } catch (ConnectException e) {
            System.err.println("❌ Serveur non démarré ou inaccessible");
            return false;
        } catch (IOException e) {
            System.err.println("❌ Impossible de se connecter au serveur: " + e.getMessage());
            return false;
        }
    }
    
    public void start() {
        if (!connect()) {
            System.out.println("Appuyez sur Entrée pour quitter...");
            scanner.nextLine();
            return;
        }
        
        // Thread pour recevoir les messages
        Thread receiverThread = new Thread(() -> receiveMessages());
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        // Afficher les instructions
        showHelp();
        
        // Boucle d'envoi des messages
        while (isConnected) {
            try {
                System.out.print("Vous (" + username + ")> ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                if (input.equals(Constants.CMD_QUIT)) {
                    disconnect();
                    break;
                } else if (input.equals(Constants.CMD_HELP)) {
                    showHelp();
                } else if (input.startsWith(Constants.CMD_UDP + " ")) {
                    String udpMessage = input.substring(5);
                    sendUDPMessage(udpMessage);
                } else if (input.equals(Constants.CMD_LIST)) {
                    sendMessage(new Message(username, "/list"));
                } else {
                    sendMessage(new Message(username, input));
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur dans la boucle d'envoi: " + e.getMessage());
                break;
            }
        }
    }
    
    private void sendMessage(Message message) {
        try {
            if (tcpOut != null && isConnected) {
                tcpOut.writeObject(message);
                tcpOut.flush();
                tcpOut.reset(); // Évite la corruption d'objets
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur d'envoi du message: " + e.getMessage());
            disconnect();
        }
    }
    
    private void sendUDPMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, serverAddress, Constants.UDP_PORT);
            udpSocket.send(packet);
            System.out.println("📨 Message UDP envoyé: " + message);
        } catch (IOException e) {
            System.err.println("❌ Erreur d'envoi UDP: " + e.getMessage());
        }
    }
    
    private void receiveMessages() {
        while (isConnected) {
            try {
                Message message = (Message) tcpIn.readObject();
                if (message != null) {
                    System.out.println("\n" + message.toString());
                    System.out.print("Vous (" + username + ")> ");
                }
            } catch (EOFException e) {
                System.out.println("\n🔌 Déconnecté du serveur");
                disconnect();
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    System.err.println("\n❌ Erreur de réception: " + e.getMessage());
                    disconnect();
                }
                break;
            }
        }
    }
    
    private void showHelp() {
        System.out.println("\n📋 COMMANDES DISPONIBLES:");
        System.out.println("  /help           - Afficher cette aide");
        System.out.println("  /list           - Lister les utilisateurs connectés");
        System.out.println("  /udp [message]  - Envoyer un message via UDP");
        System.out.println("  /quit           - Quitter le chat");
        System.out.println();
    }
    
    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            
            try {
                // Envoyer un message de déconnexion si possible
                if (tcpOut != null && tcpSocket != null && !tcpSocket.isClosed()) {
                    try {
                        Message quitMsg = new Message(username, Constants.CMD_QUIT);
                        tcpOut.writeObject(quitMsg);
                        tcpOut.flush();
                    } catch (Exception e) {
                        // Ignorer
                    }
                }
                
                if (tcpOut != null) tcpOut.close();
                if (tcpIn != null) tcpIn.close();
                if (tcpSocket != null && !tcpSocket.isClosed()) {
                    tcpSocket.close();
                }
                if (udpSocket != null && !udpSocket.isClosed()) {
                    udpSocket.close();
                }
            } catch (IOException e) {
                // Ignorer
            }
            
            System.out.println("👋 Déconnecté du serveur.");
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}