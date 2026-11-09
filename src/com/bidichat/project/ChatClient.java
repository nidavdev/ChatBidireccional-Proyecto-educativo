package com.bidichat.project;

// ChatClient.java
// Se conecta al ChatServer, se identifica con un nombre de usuario, elige con quien chatear y envia/recibe mensajes por consola hasta escribir "chao".

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ChatClient {

	// Si se deja vacio, el cliente se conecta al servidor en la misma maquina y al puerto que usa ChatServer.
	private static final String DEFAULT_HOST = "localhost";

	public static void main(String[] args) {
		Scanner console = new Scanner(System.in);

		System.out.print("Ingrese la IP del servidor (vacio = " + DEFAULT_HOST + "): ");
		String host = console.nextLine().trim();
		if (host.isEmpty()) {
			host = DEFAULT_HOST;
		}

		int port = askPort(console);

		try (Socket socket = new Socket(host, port)) {
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("Conectado al servidor de chat " + host + ":" + port);
			login(console, input, output);

			// El servidor decide cuando empieza y termina cada chat (puede avisar en cualquier momento, tambien en el lobby), asi que un hilo escucha al servidor todo el tiempo y otro lee la consola.
			Thread listenerThread = new Thread(() -> listenToServer(input));
			listenerThread.start();
			Thread consoleThread = new Thread(() -> readConsole(console, output));
			consoleThread.setDaemon(true);
			consoleThread.start();

			// El programa termina cuando el servidor cierra la conexion.
			listenerThread.join();

		} catch (IOException e) {
			System.err.println("Error de comunicacion con el servidor: " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			// No se cierra console, cerrar System.in mientras otro hilo lee de el puede bloquearse.
			System.out.println("Fin del programa cliente.");
		}
	}

	// Pide el puerto hasta que sea un numero valido (vacio = el puerto por defecto del servidor).
	private static int askPort(Scanner console) {
		while (true) {
			System.out.print("Ingrese el puerto (vacio = " + ChatServer.PORT + "): ");
			String text = console.nextLine().trim();
			if (text.isEmpty()) {
				return ChatServer.PORT;
			}
			try {
				int port = Integer.parseInt(text);
				if (port >= 1 && port <= 65535) {
					return port;
				}
			} catch (NumberFormatException e) {
				// cae al aviso de abajo
			}
			System.out.println("Puerto invalido, debe ser un numero entre 1 y 65535.");
		}
	}

	// Pide el nombre de usuario y lo envia al servidor hasta que sea aceptado (no vacio y no repetido).
	private static void login(Scanner console, BufferedReader input, PrintWriter output) throws IOException {
		while (true) {
			System.out.print("Ingrese su nombre de usuario (ej: user01): ");
			output.println(console.nextLine());

			String response = input.readLine();
			if (response == null) {
				throw new IOException("El servidor cerro la conexion durante el login.");
			}
			if (response.startsWith("USER_LIST:")) {
				showUserList(response);
				return;
			}
			System.out.println(response.substring("ERROR:".length())); // nombre rechazado: se pide otro
		}
	}

	private static void showUserList(String response) {
		String list = response.substring("USER_LIST:".length());
		System.out.println("Usuarios conectados en este momento:");
		for (String user : list.split(",")) {
			System.out.println("  - " + user);
		}
		System.out.println("Escriba el nombre del usuario con el que desea chatear ('/lista' actualiza la lista, 'chao' sale).");
		System.out.println("Si otro usuario lo elige a usted, el chat empezara automaticamente.");
	}

	// Corre en un hilo separado: solo escucha lo que envia el servidor y lo muestra en pantalla.
	private static void listenToServer(BufferedReader input) {
		try {
			String line;
			while ((line = input.readLine()) != null) {
				if (line.startsWith("MSG:")) {
					String rest = line.substring("MSG:".length());
					int separator = rest.indexOf(":");
					if (separator < 0) {
						System.out.println(rest);
					} else {
						System.out.println(rest.substring(0, separator) + "--> " + rest.substring(separator + 1));
					}
				} else if (line.startsWith("CHAT_STARTED:")) {
					System.out.println(">> Chat iniciado con " + line.substring("CHAT_STARTED:".length())
							+ ". Ya puede escribir. Escriba 'chao' para salir.");
				} else if (line.startsWith("USER_LIST:")) {
					showUserList(line);
				} else if (line.startsWith("INFO:") || line.startsWith("ERROR:")) {
					System.out.println(">> " + line.substring(line.indexOf(":") + 1));
				} else if (line.equals("END")) {
					System.out.println(">> Conexion finalizada por el servidor.");
					return;
				}
			}
		} catch (IOException e) {
		}
		System.out.println(">> Se perdio la conexion con el servidor.");
	}

	// Corre en un hilo separado: lee la consola y envia cada linea al servidor hasta que se escriba "chao".
	private static void readConsole(Scanner console, PrintWriter output) {
		try {
			while (true) {
				String message = console.nextLine();
				output.println(message);

				if (message.equalsIgnoreCase("chao")) {
					return;
				}
			}
		} catch (NoSuchElementException e) {
			// Se cerro la entrada (Ctrl+D / Ctrl+Z) sale como si se hubiera escrito "chao".
			output.println("chao");
		}
	}
}
