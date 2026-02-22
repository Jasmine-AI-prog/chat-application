package server;

public class ServerLauncher {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  LANCEUR DU SERVEUR DE CHAT");
        System.out.println("========================================");
        System.out.println();
        
        ChatServer server = new ChatServer();
        server.start();
    }
}