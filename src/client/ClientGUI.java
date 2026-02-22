package client;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
// import common.Message;

public class ClientGUI extends JFrame {
	private static final long serialVersionUID = 1L;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField usernameField;
    private ChatClient client;
    private boolean isConnected;
    
    public ClientGUI() {
        super("Chat Client");
        initializeGUI();
        isConnected = false;
    }
    
    private void initializeGUI() {
        // Configuration de la fenêtre
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel de connexion
        JPanel connectionPanel = new JPanel(new FlowLayout());
        connectionPanel.add(new JLabel("Nom d'utilisateur:"));
        usernameField = new JTextField(15);
        connectionPanel.add(usernameField);
        
        connectButton = new JButton("Se connecter");
        connectButton.addActionListener(e -> connectToServer());
        connectionPanel.add(connectButton);
        
        // Zone de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // Panel d'envoi de message
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Envoyer");
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());
        
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        
        // Ajout des composants à la fenêtre
        add(connectionPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);
        
        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        setVisible(true);
    }
    
    private void connectToServer() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez entrer un nom d'utilisateur!", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        client = new ChatClient(username);
        
        // Démarrer la connexion dans un thread séparé
        new Thread(() -> {
            if (client.connect()) {
                SwingUtilities.invokeLater(() -> {
                    isConnected = true;
                    usernameField.setEnabled(false);
                    connectButton.setEnabled(false);
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);
                    appendToChat("✅ Connecté au serveur en tant que " + username);
                    
                    // Démarrer la réception des messages
                    startMessageReceiver();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Impossible de se connecter au serveur!", 
                        "Erreur", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void startMessageReceiver() {
        new Thread(() -> {
            // Ici, vous devriez implémenter la réception des messages
            // Pour l'exemple, nous simulerons la réception
            try {
                while (isConnected) {
                    // Dans la vraie implémentation, vous recevriez des messages du serveur
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        if (!message.isEmpty() && client != null) {
            // Dans la vraie implémentation, vous enverriez le message via le client
            appendToChat("Vous: " + message);
            messageField.setText("");
            
            // Simuler la réception d'un message
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        appendToChat("Serveur: Message reçu!");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private void appendToChat(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI();
        });
    }
}