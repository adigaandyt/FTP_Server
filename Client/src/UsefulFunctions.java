import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * A couple of useful functions
 * @author Andy Thaok
 * @version 1.0
 */

public class UsefulFunctions {

    /**
     * Function for reading config.txt in working directory and return the content in a string array
     *
     * @return  String[] String array with info from config.txt
     */
    public static String[] readConfig(){
        try{
            List<String> listOfStrings = new ArrayList<String>();
            BufferedReader bf = new BufferedReader(new FileReader("config.txt"));
            String line = bf.readLine();
            while (line != null) {
                listOfStrings.add(line);
                line = bf.readLine();
            }
            bf.close();
            String[] array = listOfStrings.toArray(new String[0]);
            return array;
        }catch(Exception e){
            Client.logger.info("Error reading config file");
        }
        return null;
    }

    /**
     * Function for reading the first word of a string
     *
     * @param msg Input string, usually a message from a server
     * @return  String first word from msg
     */
    public static String getCommand(String msg){
        String arr[] = msg.split(" ", 2);
        return arr[0];
    }

    /**
     * Function for everything but the first word of a string, usually a filename or a reply
     *
     * @param msg Input string, usually a message from a server
     * @return String everything except the first word from msg
     */
    public static String getFilename(String msg){
        String arr[] = msg.split(" ", 2);
        return arr[1];
    }

    /**
     * Creates Server Objects and puts them in the ServerList in ClientGUI
     *
     * @param configInfo String array containing all the info from config.exe
     * @return void
     */
    public static void populateServerList(String[] configInfo){
        try{
            for(int i=1;i<configInfo.length;i++){
                InetAddress ip = InetAddress.getByName(configInfo[i].split(" ")[1]);
                int port = Integer.parseInt(configInfo[i].split(" ")[2]);
                String serverName = configInfo[i].split(" ")[0];
                ServerObj newServer = new ServerObj(serverName,ip,port);
                ClientGUI.serverList.add(newServer);
            }
        }catch (Exception e){
            Client.logger.info("Error while making server list");
        }
    }


    /**
     * Takes a clients name and socket and then connects it to a given server
     * and sends the server the clients name
     * @param clientName Name of the client
     * @param socket clients socket
     * @param serverObj Server to connect to
     * @return void
     */
    public static void connectTo(String clientName,Socket socket,ServerObj serverObj){
        serverObj.setClientSocket(socket);
        try{
            serverObj.setDatIn(new DataInputStream(socket.getInputStream()));
            serverObj.setDatOut(new DataOutputStream(socket.getOutputStream()));
            serverObj.getDatOut().writeUTF("MYNAMEIS "+ clientName);
            serverObj.getDatOut().flush();
        }catch (Exception e){
            Client.logger.info("Error connecting");
            e.printStackTrace();
        }
    }

    /**
     * Returns a ServerObj from ServerList using the servers name
     * @param servername Server name whose object you want
     * @return ServerObj The ServerObject with the given server name
     */
    public static ServerObj getServerObjByName(String servername) {
        return ClientGUI.serverList.stream().filter(server -> servername.equals(server.getName())).findFirst().orElse(null);
    }


}
