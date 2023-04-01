import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Class to hold the servers in config file as objects
 * For easier management of data streams and sockets
 * Only has constructors, getters and setters
 * @author Andy Thaok
 * @version 1.0
 */
public class ServerObj {


    String name;
    InetAddress ip;
    int port;
    Socket clientSocket;
    DataInputStream datIn;
    DataOutputStream datOut;

    public ServerObj(String name, InetAddress ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.clientSocket = null;
        this.datIn = null;
        this.datOut = null;
    }

    public ServerObj(String name, InetAddress ip, int port, Socket socket) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.clientSocket = socket;
        try{
            this.datIn = null;
            this.datOut = null;
        }catch (Exception e){
            Client.logger.info("Error making Serverobj");
            e.printStackTrace();
        }
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public DataInputStream getDatIn() {
        return datIn;
    }

    public void setDatIn(DataInputStream datIn) {
        this.datIn = datIn;
    }

    public DataOutputStream getDatOut() {
        return datOut;
    }

    public void setDatOut(DataOutputStream datOut) {
        this.datOut = datOut;
    }
}
