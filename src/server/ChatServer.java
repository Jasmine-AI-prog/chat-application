package server;

import java.io.*;
import java.net.*;
import java.util.*;
import common.Message;
import common.Constants;

public class ChatServer {
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;
    private List<ClientHandler> clients;
    private boolean isRunning;
    
    public ChatServer() {
        this.clients = Collections.synchronizedList(new ArrayList<>());
        this.isRunning = true;
    }
    
    public void start() {
        System.out.println("=== DEMARRAGE DU SERVEUR DE CHAT ===");
        System.out.println("Date: " + new Date());
        System.out.println("====================================\n");
        
        try {
            // Essayer de libérer le port si nécessaire
            releasePorts();
            
            // Démarrer le serveur TCP
            tcpServerSocket = new ServerSocket(Constants.TCP_PORT);
            System.out.println("[OK] Serveur TCP demarre sur le port " + Constants.TCP_PORT);
            
            // Démarrer le serveur UDP
            udpSocket = new DatagramSocket(Constants.UDP_PORT);
            System.out.println("[OK] Serveur UDP demarre sur le port " + Constants.UDP_PORT);
            
            // Ajouter un hook d'arrêt
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            
            // Thread pour accepter les connexions TCP
            Thread tcpThread = new Thread(() -> {
                System.out.println("[INFO] En attente de connexions TCP...");
                acceptTCPConnections();
            });
            tcpThread.setDaemon(true);
            tcpThread.start();
            
            // Thread pour gérer les messages UDP
            Thread udpThread = new Thread(() -> {
                System.out.println("[INFO] En attente de messages UDP...");
                handleUDPMessages();
            });
            udpThread.setDaemon(true);
            udpThread.start();
            
            // Console d'administration
            adminConsole();
            
        } catch (BindException e) {
            System.err.println("[ERREUR] Le port " + Constants.TCP_PORT + " est deja utilise!");
            System.err.println("   Causes possibles:");
            System.err.println("   1. Une instance du serveur est deja en cours d'execution");
            System.err.println("   2. Le port n'a pas ete libere apres un arret brutal");
            System.err.println("\nSolutions:");
            System.err.println("   1. Fermez l'autre instance du serveur");
            System.err.println("   2. Changez le port dans Constants.java");
            System.err.println("   3. Attendez 30 secondes que le port se libere");
            System.err.println("\n   Ou executez la commande suivante dans un terminal:");
            System.err.println("   Windows: netstat -ano | findstr :" + Constants.TCP_PORT);
            System.err.println("   Linux/Mac: lsof -i :" + Constants.TCP_PORT);
            
        } catch (IOException e) {
            System.err.println("[ERREUR] Erreur lors du demarrage du serveur: " + e.getMessage());
        }
    }
    
    /**
     * Tente de libérer les ports s'ils sont bloqués
     */
    private void releasePorts() {
        try {
            // Tester si le port TCP est libre
            ServerSocket testSocket = new ServerSocket(Constants.TCP_PORT);
            testSocket.close();
            System.out.println("[INFO] Port TCP " + Constants.TCP_PORT + " est libre");
        } catch (IOException e) {
            System.out.println("[ATTENTION] Port TCP " + Constants.TCP_PORT + " est occupe - tentative de liberation...");
            try {
                // Forcer la libération
                new ServerSocket(Constants.TCP_PORT).close();
                Thread.sleep(500);
                System.out.println("[OK] Port TCP libere");
            } catch (Exception ex) {
                System.out.println("[ERREUR] Impossible de liberer le port TCP");
            }
        }
        
        try {
            // Tester si le port UDP est libre
            DatagramSocket testSocket = new DatagramSocket(Constants.UDP_PORT);
            testSocket.close();
            System.out.println("[INFO] Port UDP " + Constants.UDP_PORT + " est libre");
        } catch (IOException e) {
            System.out.println("[ATTENTION] Port UDP " + Constants.UDP_PORT + " est occupe");
        }
    }
    
    private void acceptTCPConnections() {
        while (isRunning) {
            try {
                Socket clientSocket = tcpServerSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("[CONNEXION] Nouvelle connexion TCP depuis: " + clientAddress);
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                
            } catch (SocketException e) {
                if (isRunning) {
                    System.err.println("[ERREUR] Socket ferme: " + e.getMessage());
                }
                break;
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("[ERREUR] Erreur d'acceptation de connexion: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleUDPMessages() {
        byte[] buffer = new byte[Constants.BUFFER_SIZE];
        
        while (isRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                
                System.out.println("[UDP] Message UDP recu de " + clientAddress + ":" + clientPort);
                broadcastUDPMessage(message, clientAddress, clientPort);
                
            } catch (SocketException e) {
                if (isRunning) {
                    System.err.println("[ERREUR] Socket UDP ferme: " + e.getMessage());
                }
                break;
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("[ERREUR] Erreur UDP: " + e.getMessage());
                }
            }
        }
    }
    
    public synchronized void broadcastMessage(Message message, ClientHandler sender) {
        synchronized(clients) {
            Iterator<ClientHandler> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientHandler client = iterator.next();
                if (client != sender && client.isConnected()) {
                    client.sendMessage(message);
                }
            }
        }
    }
    
    public synchronized void broadcastUDPMessage(String message, InetAddress senderAddress, int senderPort) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.isConnected()) {
                    client.sendUDPMessage(message);
                }
            }
        }
    }
    
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("[DECONNEXION] Client deconnecte. Clients connectes: " + clients.size());
        
        if (client.getUsername() != null) {
            Message disconnectMsg = new Message(
                Constants.SYSTEM_SENDER, 
                client.getUsername() + Constants.DISCONNECT_MESSAGE,
                Message.MessageType.DISCONNECT
            );
            broadcastMessage(disconnectMsg, null);
        }
    }
    
    public synchronized List<String> getConnectedUsers() {
        List<String> users = new ArrayList<>();
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.getUsername() != null) {
                    users.add(client.getUsername());
                }
            }
        }
        return users;
    }
    
    private void adminConsole() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== CONSOLE D'ADMINISTRATION ===");
        System.out.println("Commandes disponibles:");
        System.out.println("  /list - Afficher les clients connectes");
        System.out.println("  /stop - Arreter le serveur");
        System.out.println("  /ports - Verifier l'etat des ports");
        System.out.println("  /help - Afficher cette aide");
        System.out.println("=================================\n");
        
        while (isRunning) {
            System.out.print("serveur> ");
            String command = scanner.nextLine().trim();
            
            switch (command) {
                case "/list":
                    List<String> users = getConnectedUsers();
                    System.out.println("Clients connectes (" + users.size() + "):");
                    for (String user : users) {
                        System.out.println("  - " + user);
                    }
                    break;
                    
                case "/ports":
                    System.out.println("Etat des ports:");
                    System.out.println("  TCP " + Constants.TCP_PORT + ": " + 
                        (tcpServerSocket != null && !tcpServerSocket.isClosed() ? "[ACTIF]" : "[INACTIF]"));
                    System.out.println("  UDP " + Constants.UDP_PORT + ": " + 
                        (udpSocket != null && !udpSocket.isClosed() ? "[ACTIF]" : "[INACTIF]"));
                    break;
                    
                case "/stop":
                    System.out.println("Arret du serveur en cours...");
                    stop();
                    break;
                    
                case "/help":
                    System.out.println("Commandes: /list, /ports, /stop, /help");
                    break;
                    
                default:
                    if (!command.isEmpty()) {
                        System.out.println("[ERREUR] Commande inconnue. Tapez /help pour l'aide.");
                    }
            }
        }
        
        scanner.close();
    }
    
    public void stop() {
        if (!isRunning) return;
        
        isRunning = false;
        System.out.println("\nArret du serveur...");
        
        // Déconnecter tous les clients
        synchronized(clients) {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
        }
        
        // Fermer le serveur TCP
        try {
            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
                System.out.println("[OK] Serveur TCP arrete");
            }
        } catch (IOException e) {
            System.err.println("[ERREUR] Erreur fermeture TCP: " + e.getMessage());
        }
        
        if (udpSocket != null && !udpSocket.isClosed()) {
		    udpSocket.close();
		    System.out.println("[OK] Serveur UDP arrete");
		}
        
        System.out.println("[OK] Serveur arrete proprement.");
        System.exit(0);
    }
}