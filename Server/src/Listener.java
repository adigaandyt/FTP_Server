import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Listener extends Thread{
    private ServerSocket serverSocket;
    public Listener(ServerSocket socket) {
        serverSocket = socket;
    }

    //List of all client threads
    public static List<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();


    @Override
    public void run() {


        log("Listening to: " + serverSocket.getInetAddress().toString() +":"+serverSocket.getLocalPort() + "...");
        while (!Thread.currentThread().isInterrupted()){
            try{
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }catch (Exception e){
                log("Error while starting client handler thread \n" + e.getMessage());
            }
        }

        //interrupted, stop threads
        for (int i = 0; i < clientHandlers.size(); i++) {
            clientHandlers.get(i).interrupt();
        }

        //close socket
        try {
            serverSocket.close();
            System.out.println("Closing sockets");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }
}
