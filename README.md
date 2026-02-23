# Application de Chat Client/Serveur en Java

## Description du projet

Application de chat en temps reel permettant la communication entre plusieurs clients via un serveur central. Developpee en Java avec les sockets TCP/UDP, cette application offre une interface console pour echanger des messages texte.

Objectif pedagogique : Mettre en oeuvre les concepts de programmation reseau, de multithreading et de communication client-serveur.

## Fonctionnalites

- Communication en temps reel entre plusieurs clients
- Support des protocoles TCP (fiable) et UDP (rapide)
- Gestion simultanee de multiples clients
- Interface console intuitive
- Commandes speciales pour interagir avec le systeme
- Notification des connexions/deconnexions
- Diffusion des messages a tous les participants

## Technologies utilisees

- Java 8+
- Sockets TCP/UDP
- Multithreading
- Serialisation d'objets
- Programmation orientee objet

## Structure du projet
ChatApplication/
├── src/
│ ├── common/
│ │ ├── Constants.java # Configuration des ports et constantes
│ │ ├── Message.java # Modele de message serialisable
│ │ └── User.java # Modele d'utilisateur
│ ├── server/
│ │ ├── ChatServer.java # Serveur principal
│ │ ├── ClientHandler.java # Gestionnaire de clients
│ │ └── ServerLauncher.java # Point d'entree du serveur
│ └── client/
│ ├── ChatClient.java # Client de chat
│ └── ClientLauncher.java # Point d'entree du client
├── .gitignore
└── README.md


## Guide d'installation et d'execution

### Prerequis

- Java JDK 8 ou superieur
- Eclipse IDE (ou tout autre IDE Java)
- Git (optionnel, pour cloner le depot)

### Installation

1. Cloner le depot 
git clone https://github.com/Jasmine-AI-prog/chat-application.git


2. Importer dans Eclipse
- Ouvrir Eclipse
- File -> Import -> Existing Projects into Workspace
- Selectionner le dossier du projet
- Finish

### Execution

#### 1. Demarrer le serveur
- Dans Eclipse, clic droit sur ServerLauncher.java (dans src/server/)
- Run As -> Java Application
- La console doit afficher :
[OK] Serveur TCP demarre sur le port 12345
[OK] Serveur UDP demarre sur le port 12346
[INFO] En attente de connexions TCP...


#### 2. Demarrer les clients
- Clic droit sur ClientLauncher.java (dans src/client/)
- Run As -> Java Application
- Entrer un nom d'utilisateur
- Repeter l'operation pour chaque client supplementaire

#### 3. Basculer entre les clients
- Utiliser les onglets de console en haut pour passer d'un client a l'autre
- Chaque client a sa propre console avec son propre prompt

## Utilisation

### Commandes disponibles

| Commande | Description | Exemple |
|----------|-------------|---------|
| /help | Affiche l'aide | /help |
| /list | Liste les utilisateurs connectes | /list |
| /udp [message] | Envoie un message en UDP | /udp Message rapide |
| /quit | Quitte le chat | /quit |

### Exemple de session

Client 1 (Alice)

Entrez votre nom d'utilisateur: Alice
[OK] Connecte au serveur en tant que Alice
Vous (Alice)> Bonjour tout le monde !

Client 2 (Bob)

Entrez votre nom d'utilisateur: Bob
[OK] Connecte au serveur en tant que Bob
[14:30:25] Alice: Bonjour tout le monde !
Vous (Bob)> Salut Alice !