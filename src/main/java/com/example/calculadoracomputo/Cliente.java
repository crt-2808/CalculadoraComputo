package com.example.calculadoracomputo;

import java.io.*;
import java.net.Socket;

public class Cliente {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // Dirección IP del servidor
    private static final int SERVER_PORT = 3025; // Puerto del servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Conectado al servidor en " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Flujo de salida para enviar datos al servidor
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Flujo de entrada para recibir datos del servidor
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Ejemplo de envío de mensaje al servidor
            enviarMensaje(dataOutputStream, 1, "Este es un mensaje de prueba.");

            while (true) {
                // Recibe y procesa mensajes del servidor
                int tipoMensaje = dataInputStream.readShort();
                int longitud = dataInputStream.readShort();

                byte[] buffer = new byte[longitud];
                dataInputStream.readFully(buffer);

                String mensaje = new String(buffer, "UTF-8");

                System.out.println("Tipo de mensaje: " + tipoMensaje);
                System.out.println("Mensaje recibido: " + mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensaje(DataOutputStream dataOutputStream, int tipoMensaje, String mensaje) throws IOException {
        byte[] mensajeBytes = mensaje.getBytes("UTF-8");
        int longitud = mensajeBytes.length;

        // Envía el tipo de mensaje (2 bytes)
        dataOutputStream.writeShort(tipoMensaje);

        // Envía la longitud de la información (2 bytes)
        dataOutputStream.writeShort(longitud);

        // Envía la información de la operación
        dataOutputStream.write(mensajeBytes);

        dataOutputStream.flush();
    }
}
