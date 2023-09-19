package com.example.calculadoracomputo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class Server {

    private static final int PORT = 3025;
    private static LinkedList<PrintWriter> clientWriters = new LinkedList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante desde " + clientSocket.getInetAddress());

                // Crea un hilo para manejar al cliente
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private Scanner in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new Scanner(clientSocket.getInputStream());

                // Agrega el escritor del cliente a la lista de escritores
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                while (true) {
                    if (!in.hasNextLine()) {
                        // Cliente desconectado
                        break;
                    }

                    String message = in.nextLine();

                    // Reenvía el mensaje a todos los clientes excepto al que lo envió
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) {
                                writer.println(message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Elimina al escritor del cliente de la lista cuando se desconecta
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
