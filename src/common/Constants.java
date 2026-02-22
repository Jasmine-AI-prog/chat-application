package common;

public class Constants {
    // Configuration des ports
    public static final int TCP_PORT = 12345;
    public static final int UDP_PORT = 12346;
    
    // Adresse du serveur
    public static final String SERVER_IP = "localhost";
    
    // Commandes spéciales
    public static final String CMD_QUIT = "/quit";
    public static final String CMD_LIST = "/list";
    public static final String CMD_UDP = "/udp";
    public static final String CMD_HELP = "/help";
    public static final String CMD_PRIVATE = "/msg";
    
    // Messages système
    public static final String SYSTEM_SENDER = "SERVEUR";
    public static final String CONNECT_MESSAGE = " s'est connecté(e)";
    public static final String DISCONNECT_MESSAGE = " s'est déconnecté(e)";
    
    // Tailles de buffer
    public static final int BUFFER_SIZE = 1024;
}