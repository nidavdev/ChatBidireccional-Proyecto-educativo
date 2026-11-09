package com.bidichat.project;

// ClientHandler.java
// Atiende a un cliente conectado: le pide su nombre de usuario, lo deja elegir con quien chatear y reenvia sus mensajes al otro usuario.
//
// Despues del login el cliente esta en uno de dos estados, que se deduce del campo "partner":
// 	LOBBY (partner == null): cada linea es un comando: "/lista", "chao" o el nombre del usuario con quien chatear.
// 	CHATEANDO (partner != null): cada linea es un mensaje para el partner, o "chao" para salir.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private final Socket socket;
	private final BufferedReader input;
	private final PrintWriter output;

	public ClientHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.output = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {
			if (login()) {
				mainLoop();
			}
		} catch (IOException e) {
			System.err.println("Se perdio la conexion con " + username + ": " + e.getMessage());
		} finally {
			cleanup();
		}
	}

	// Envia una linea a este cliente.
	void send(String message) {
		output.println(message);
	}

	private volatile String username;

	public String getUsername() {
		return username;
	}

	// Pide el nombre de usuario hasta que llegue uno valido (no vacio y no repetido).
	private boolean login() throws IOException {
		while (true) {
			String name = input.readLine();
			if (name == null) {
				return false; // el cliente cerro la conexion
			}
			name = name.trim();

			if (name.isEmpty()) {
				send("ERROR:El nombre de usuario no puede estar vacio.");
				continue;
			}

			username = name;
			if (ChatServer.registerUser(name, this)) {
				System.out.println("Usuario registrado: " + username);
				return true;
			}
			username = null;
			send("ERROR:El usuario '" + name + "' ya esta conectado, elija otro nombre.");
		}
	}

	// Con quien esta chateando (null = en el lobby). Solo se modifica dentro de metodos synchronized de ChatServer.
	volatile ClientHandler partner;

	// Lee lineas hasta que el cliente se va; segun el estado, cada linea es un comando (lobby) o un mensaje (chat).
	private void mainLoop() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			ClientHandler currentPartner = partner;
			boolean keepGoing = (currentPartner == null) ? handleLobby(line) : handleChat(line, currentPartner);
			if (!keepGoing) {
				return;
			}
		}
	}

	// Lobby: "chao" sale, "/lista" muestra los conectados, cualquier otra cosa es el nombre del usuario con quien chatear.
	// Devuelve false si hay que terminar la conexion.
	private boolean handleLobby(String line) {
		String command = line.trim();

		if (command.equalsIgnoreCase("chao")) {
			System.out.println(username + " envio 'chao' desde el lobby.");
			send("END");
			return false;
		}
		if (command.equalsIgnoreCase("/lista")) {
			send("USER_LIST:" + ChatServer.userList());
			return true;
		}

		String error = ChatServer.pair(this, command);
		if (error != null) {
			send("ERROR:" + error + " Lista actual: " + ChatServer.userList());
			return true;
		}

		// Los dos quedaron emparejados a la vez: el otro hilo ya lo ve como "chateando" sin tener que hacer nada.
		send("CHAT_STARTED:" + command);
		ClientHandler other = partner;
		if (other != null) {
			other.send("CHAT_STARTED:" + username);
		}
		return true;
	}

	// Chat: "chao" sale (cleanup avisa al partner) y cualquier otra linea se reenvia al partner.
	// Devuelve false si hay que terminar la conexion.
	private boolean handleChat(String line, ClientHandler currentPartner) {
		if (line.equalsIgnoreCase("chao")) {
			System.out.println(username + " envio 'chao', chat finalizado.");
			send("END");
			return false;
		}

		currentPartner.send("MSG:" + username + ":" + line);
		return true;
	}

	// Quita al usuario de la lista, cierra el socket y avisa a su partner (si tenia uno) que vuelve al lobby.
	private void cleanup() {
		ClientHandler former = ChatServer.removeUser(this);
		System.out.println("Se cerro la conexion de " + (username == null ? "un cliente sin nombre" : username));

		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Error cerrando el socket: " + e.getMessage());
		}

		// El aviso se envia fuera del bloque synchronized, para que un cliente lento no frene a los demas.
		if (former != null) {
			former.returnToLobby(username + " salio del chat.");
		}
	}

	// Se llama desde el hilo del partner cuando este se va: este usuario vuelve al lobby con la lista actualizada.
	void returnToLobby(String reason) {
		send("INFO:" + reason + " Ya puede elegir otro usuario.");
		send("USER_LIST:" + ChatServer.userList());
	}
}
