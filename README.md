# ChatBidireccional
**Proyecto educativo:** desarrollo de un chat bidireccional utilizando Java y sockets.

- **Paquete:** `com.bidichat.project`
- **Ubicación de los archivos:** `src/com/bidichat/project/`

`ChatServer.java` Programa servidor que administra las conexiones.
`ClientHandler.java` Hilo encargado de atender a cada cliente conectado.
`ChatClient.java` Programa cliente que permite interactuar con el chat.

---

## Cómo ejecutar el proyecto
Todos los comandos deben ejecutarse desde la carpeta `src/`.

### 1. Compilar el proyecto
```bash
javac -d out com/bidichat/project/*.java
```

Este comando compila los archivos Java y genera los archivos `.class` dentro de la carpeta `out/`.

### 2. Iniciar el servidor
```bash
java -cp out com.bidichat.project.ChatServer
```

El servidor quedará a la espera de nuevas conexiones en el **puerto 8000**.

### 3. Iniciar un cliente
Abre otra consola y ejecuta:

```bash
java -cp out com.bidichat.project.ChatClient
```

Al iniciar, el cliente solicitará los siguientes datos:
1. **Dirección IP del servidor:** presiona `Enter` para utilizar `localhost`.
2. **Puerto:** presiona `Enter` para utilizar el puerto `8000`.
3. **Nombre de usuario:** introduce el nombre con el que deseas participar en el chat.

> [!TIP]
> Puedes abrir varias terminales para simular diferentes usuarios.
---

## Flujo de uso
### 1. Conexión de usuarios
Cada cliente que se conecta debe introducir un nombre de usuario.
El servidor muestra en su consola la lista de usuarios conectados cada vez que alguien entra o sale.
Por su parte, cada cliente recibe la lista de usuarios disponibles para iniciar una conversación.

### 2. Opciones disponibles en el lobby
Mientras el usuario se encuentra en el lobby, puede utilizar los siguientes comandos:
`<nombre>` Inicia un chat con el usuario indicado.
`/lista` Muestra nuevamente los usuarios conectados.
`chao` Finaliza la conexión y sale del programa.

### 3. Inicio de una conversación
Cuando el usuario A selecciona al usuario B:
- El chat comienza automáticamente para ambos usuarios, ambos pueden comenzar a escribir inmediatamente.
- Los mensajes enviados por cualquiera de los dos aparecen en la consola del otro usuario.
No es posible seleccionar a un usuario que ya esté participando en una conversación con otra persona.

### 4. Finalización de una conversación
Cuando cualquiera de los participantes escribe `chao`:
1. El servidor finaliza la conexión del usuario que escribió el comando.
2. El otro participante recibe una notificación.
3. El usuario restante regresa automáticamente al lobby.
4. Desde allí puede iniciar una conversación con otro usuario o salir del programa escribiendo `chao`.

### 5. Desconexión inesperada
Si un usuario se desconecta sin escribir `chao`, por ejemplo, porque cierra la consola, pierde la conexión de red o utiliza `Ctrl + C`:
- El servidor detecta la desconexión.
- El otro participante recibe el aviso correspondiente.
- El usuario restante regresa automáticamente al lobby.

---
Tecnologías utilizadas: Java, sockets TCP y programación multihilo (Multithreading).
