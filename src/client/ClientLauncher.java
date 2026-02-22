package client;

import java.util.Scanner;

public class ClientLauncher {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   CLIENT DE CHAT - Version Console");
        System.out.println("========================================");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        
        // Demander le nom d'utilisateur
        System.out.print("Entrez votre nom d'utilisateur: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            username = "Client" + System.currentTimeMillis() % 10000;
            System.out.println("📝 Nom généré automatiquement: " + username);
        }
        
        // Créer et démarrer le client
        ChatClient client = new ChatClient(username);
        
        // Ajouter un hook pour l'arrêt propre
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Arrêt du client...");
            client.disconnect();
        }));
        
        // Démarrer le client
        client.start();
        
        scanner.close();
    }
}