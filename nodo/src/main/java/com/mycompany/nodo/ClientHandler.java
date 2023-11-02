/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.nodo;

import static com.mycompany.nodo.Nodo.active_connections;
import static com.mycompany.nodo.Nodo.active_output_streams;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ramos
 */
 public class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ObjectOutputStream oos;
        private final ObjectInputStream ois;
        // Constructor
        public ClientHandler(Socket pSocket, ObjectOutputStream pOOS, ObjectInputStream pOIS) throws IOException
    {
            this.clientSocket = pSocket;
            this.oos = pOOS;
            this.ois = pOIS;
            active_output_streams.add(oos); // add oos a la lista
            active_connections.add(pSocket);
        }
  
        public void run()
        {
            try {
                
                while(true){
                    
                    //convert ObjectInputStream object to String
                    String message = (String) ois.readObject();
                    System.out.println("[nodo] (" + clientSocket.getRemoteSocketAddress() + ") Mensaje recibido: " + message);
                    
                    String parts[] = message.split(",");
                    
                    // Add "1" to flag
                    message = String.join(",", parts[0], parts[1], Integer.toString(Integer.parseInt(parts[2])+1), parts[3]);
                    
                    // Broadcast to all active clients
                    for (int i = 0; i < active_output_streams.size(); i++) 
                    {
                        ObjectOutputStream temp_oos = active_output_streams.get(i);
                        temp_oos.writeObject(message);
                        System.out.println("[nodo] Enviando mensaje: " + message + " a " + active_connections.get(i));

                    }
                    

                }
                
            }
            catch (IOException e) {
                System.out.println("*[nodo] Conexion finalizada con: " + clientSocket.getRemoteSocketAddress());
                
                active_connections.remove(clientSocket);
                active_output_streams.remove(oos);
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }