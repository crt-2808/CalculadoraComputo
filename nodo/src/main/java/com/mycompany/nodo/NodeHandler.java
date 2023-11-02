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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author ramos
 */
public class NodeHandler implements Runnable {
    
    // Atributes
    private final Socket node_socket;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private TextArea logger = null;
    
    // Constructor
    public NodeHandler(Socket pSocket, ObjectOutputStream pOOS, ObjectInputStream pOIS, TextArea pLogger) throws IOException
    {
        this.node_socket = pSocket;
        this.oos = pOOS;
        this.ois = pOIS;
        this.logger = pLogger;
        active_output_streams.add(oos); // add oos a la lista
        active_connections.add(pSocket);
    }
    
    // Runnable logic
    public void run()
    {
        try {

            while(true){
                
                //convert ObjectInputStream object to String
                String message = (String) ois.readObject();
                final String tempMessage = message;
                
                System.out.println("[nodo] (" + node_socket.getRemoteSocketAddress() + ") Mensaje recibido: " + message);
                Platform.runLater(() -> {
                    logger.appendText("[nodo] (" + node_socket.getRemoteSocketAddress() + ") Mensaje recibido: " + tempMessage + "\n");
                });
                
                String parts[] = message.split(",");
                
                if( Integer.parseInt(parts[2]) < 2){
                    message = String.join(",", parts[0], parts[1], Integer.toString(Integer.parseInt(parts[2])+1), parts[3]);
                    // Broadcast to all active clients
                    for (int i = 0; i < active_output_streams.size(); i++) 
                    {
                        Thread.sleep(10);
                        ObjectOutputStream temp_oos = active_output_streams.get(i);
                        temp_oos.writeObject(message);
                        System.out.println("[nodo] Enviando mensaje: " + message + " a " + active_connections.get(i));
                        final int temp_i = i;
                        Platform.runLater(() -> {
                            logger.appendText("[nodo] Enviando mensaje: " + tempMessage + " a " + active_connections.get(temp_i) + "\n");
                        });
                    }
                }
                
            }

        }
        catch (IOException e) {
            System.out.println("*[nodo] Conexion finalizada con: " + node_socket.getRemoteSocketAddress());
            Platform.runLater(() -> {
                logger.appendText("*[nodo] Conexion finalizada con: " + node_socket.getRemoteSocketAddress() + "\n");
            });
            
            active_connections.remove(node_socket);
            active_output_streams.remove(oos);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(NodeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}