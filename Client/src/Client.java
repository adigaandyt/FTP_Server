import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * The Client class that holds client info and does some client functions
 * @author Andy Thaok
 * @version 1.0
 */

public class Client {
    public static Logger logger = Logger.getLogger("MyLog");
    private String clientName;
    private Socket clientSocket;
    private static DataOutputStream datOut = null;
    private static DataInputStream datIn = null;

    public Client(Socket clientSocket, String name) {
        try {
            this.clientSocket = clientSocket;
            this.clientName = name;
        } catch (Exception e) {
            logger.info("Error creating client socket\n");
        }
    }

    public String getClientName() {
        return clientName;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    //Shuts down the client
    public void closeClient(){
        try{
            if(this.clientSocket != null){
                this.clientSocket.close();
            }
            if(this.datIn != null){
                this.datIn.close();
            }
            if(this.datOut != null){
                this.datOut.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Sends a file to a server
     * @param path File path of the file to send
     * @param server Server to send the file to
     * @return void
     */
    public static void uploadFile(String path, ServerObj server){
        try{
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            // send file size
            server.getDatOut().writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[4*1024];
            while ((bytes=fileInputStream.read(buffer))!=-1){
                server.getDatOut().write(buffer,0,bytes);
                server.getDatOut().flush();
            }
            fileInputStream.close();
        }catch (Exception e){
            logger.info("Error sending file");
            e.printStackTrace();
        }
    }

    /**
     * Download a file from a server
     * @param filename Name of the file to be downloaded
     * @param datIn DataInputStream of the server the file is being downloaded from
     * @return void
     */
    public static void downloadFile(String filename,DataInputStream datIn){
        try{
            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            long size = datIn.readLong();
            byte[] buffer = new byte[4*1024];
            while (size > 0 && (bytes = datIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();
        }catch (Exception e){
            Client.logger.info("Couldn't download");
            e.printStackTrace();
        }
    }



    }
