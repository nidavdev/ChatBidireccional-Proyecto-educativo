# ChatBidireccional, Proyecto educativo
Un proyecto educativo sobre crear un chat bidireccional.

Paquete: com.bidichat.project
Ubicacion de los archivos: src/com/bidichat/project/

Archivos:
  - ChatServer.java -> programa servidor
  - ClientHandler.java -> hilo que atiende a cada cliente conectado
  - ChatClient.java -> programa cliente

## COMO EJECUTAR (desde consola):
1) Compilar desde src/ javac -d out com/bidichat/project/*.java

2) Iniciar el servidor (queda esperando conexiones en el puerto 8000). En la carpeta src/, ejecutar el comando:
	java -cp out com.bidichat.project.ChatServer

3) Iniciar un cliente. Al iniciar pide la IP del servidor (vacio = localhost), el puerto (vacio = 8000) y después el nombre de usuario. En la carpeta src/, ejecutar el comando:
	java -cp out com.bidichat.project.ChatClient

4) Repetir el paso 3 en otras consolas para simular varios usuarios
 	(user01, user02, user03, ...).

## FLUJO DE USO:
Cada cliente que se conecta escribe su nombre de usuario.
El servidor muestra en su consola la lista de usuarios conectados cada vez que alguien entra o sale.
El cliente recibe la lista de usuarios ya conectados. Estando en el "lobby" puede escribir:
	<nombre> -> inicia un chat con ese usuario
	/lista -> vuelve a mostrar los usuarios conectados
	chao -> sale del programa

Cuando un usuario A elige a B, el chat empieza para los dos a la vez: B no tiene que hacer nada, puede escribir de inmediato. No se puede elegir a un usuario que ya esta chateando con otra persona.

A partir de ahi el chat es bidireccional, cualquiera de los dos puede escribir y el otro ve el mensaje en su consola.

Cuando un usuario escribe "chao", el servidor termina su conexion y avisa al otro usuario, que vuelve al lobby y puede elegir a otra persona (o escribir "chao" para salir).

Si un usuario se desconecta sin escribir "chao" (cierra la consola, se cae la red, Ctrl+C), el otro recibe el mismo aviso y vuelve al lobby.
