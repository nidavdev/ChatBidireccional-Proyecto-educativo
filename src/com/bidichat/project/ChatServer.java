package com.bidichat.project;

// ChatServer.java
// Atiende varios clientes por sockets, registra sus nombres de usuario, muestra la lista de conectados y hace de puente para que dos usuarios puedan chatear.

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatServer {

	public static final int PORT = 8000;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("No se pudo abrir el puerto " + PORT);
			System.exit(1);
		}

		System.out.println("Servidor de Chat iniciado. Esperando clientes en el puerto " + PORT + "...");

		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Nueva conexion entrante: " + clientSocket.getInetAddress());

				// Un hilo por cliente, para seguir aceptando conexiones y permitir varios chats a la vez.
				new Thread(new ClientHandler(clientSocket)).start();

			} catch (IOException e) {
				System.err.println("Error aceptando una conexion: " + e.getMessage());
			}
		}
	}

	// Usuarios conectados: clave = nombre de usuario, valor = hilo que lo atiende.
	static final Map<String, ClientHandler> connectedClients = new LinkedHashMap<String, ClientHandler>();

	// Registra al usuario si el nombre esta libre y le envia la lista de conectados.
	static synchronized boolean registerUser(String username, ClientHandler handler) {
		if (connectedClients.containsKey(username)) {
			return false;
		}
		connectedClients.put(username, handler);
		handler.send("USER_LIST:" + userList());
		showConnectedClients();
		return true;
	}

	static synchronized void showConnectedClients() {
		System.out.println("- - - - -");
		System.out.println("Usuarios conectados (" + connectedClients.size() + "):");
		for (String user : connectedClients.keySet()) {
			System.out.println("  - " + user);
		}
		System.out.println("- - - - -");
	}

	// Devuelve la lista de usuarios conectados separados por comas.
	static synchronized String userList() {
		StringBuilder sb = new StringBuilder();
		for (String user : connectedClients.keySet()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(user);
		}
		return sb.toString();
	}

	// Empareja a "requester" con "targetName". Devuelve null si salio bien, o el texto del error.
	static synchronized String pair(ClientHandler requester, String targetName) {
		ClientHandler target = connectedClients.get(targetName);

		if (target == null) {
			return "El usuario '" + targetName + "' no existe o se ha desconectado.";
		}
		if (requester.partner != null) {
			return "Usted ya esta en un chat.";
		}
		if (target.partner != null) {
			return "El usuario '" + targetName + "' ya esta en otro chat.";
		}

		requester.partner = target;
		target.partner = requester;
		System.out.println(requester.getUsername() + " inicio un chat con " + targetName);
		return null;
	}

	// Quita al usuario del mapa y lo desempareja. Devuelve su companero (o null) para avisarle fuera del bloque synchronized.
	static synchronized ClientHandler removeUser(ClientHandler handler) {
		ClientHandler former = handler.partner;
		handler.partner = null;
		if (former != null) {
			former.partner = null;
		}

		String username = handler.getUsername();
		if (username != null && connectedClients.get(username) == handler) {
			connectedClients.remove(username);
			showConnectedClients();
		}
		return former;
	}
}
