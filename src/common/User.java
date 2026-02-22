package common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String ipAddress;
    private int port;
    
    public User(String username, String ipAddress, int port) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return username + " (" + ipAddress + ":" + port + ")";
    }
}