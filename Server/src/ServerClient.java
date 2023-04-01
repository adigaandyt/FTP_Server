import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


//Class for a server to be a client
public class ServerClient extends Thread {


    //Key-Filename, value-SyncFile
    public static ConcurrentHashMap<String, SyncFile> syncFiles = new ConcurrentHashMap<>();
    private String serverClientName;
    private Socket serverClientSocket;
    //private static DataOutputStream datOut = null;
    //private static DataInputStream datIn = null;
    public static ArrayList<ServerObj> myServers = new ArrayList<>();

    public void run(){
        DataOutputStream datOut = null;
        DataInputStream datIn = null;

        Socket socket = null;
        //Try to connect to servers in the serverlist
        for (ServerObj element : Server.serverList){
            log("Trying to connect to " + element.getName() + " [" + element.getIp() + ":"+element.getPort()+"]");
            try{
                socket = new Socket(element.getIp(), element.getPort());
                element.setClientSocket(socket);
                element.setDatOut(new DataOutputStream(socket.getOutputStream()));
                element.setDatIn(new DataInputStream(socket.getInputStream()));
                element.getDatOut().writeUTF("MYNAMEIS "+ Server.name);
                element.getDatOut().flush();
                log("Connected to " + element.getName() + " [" + element.getIp() + ":"+element.getPort()+"]");
            }catch (Exception e){
                log(element.getName() + " [" + element.getIp() + ":"+element.getPort()+"]" + " is offline");
            }
        }
        //Try to make a thread here that constantly tries to reconnect to the other servers
    }




    public String getServerClientName() {
        return serverClientName;
    }
    public void setServerClientName(String serverClientName) {
        this.serverClientName = serverClientName;
    }
    public Socket serverClientSocket() {
        return serverClientSocket;
    }
    public void setServerClientSocket(Socket serverClientSocket) {
        this.serverClientSocket = serverClientSocket;
    }
    public void closeServerClient(){
        try{
            if(this.serverClientSocket != null){
                this.serverClientSocket.close();
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
    public static void uploadFile(String filename, ServerObj server){
        try{
            int bytes = 0;
            File file = new File(Server.downloadPath+filename);
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
            log("Error sending file");
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
            FileOutputStream fileOutputStream = new FileOutputStream(Server.downloadPath+filename);
            long size = datIn.readLong();
            byte[] buffer = new byte[4*1024];
            while (size > 0 && (bytes = datIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();
        }catch (Exception e){
            log("Couldn't download");
            e.printStackTrace();
        }
    }
    public static void connectTo(String clientName,Socket socket,ServerObj serverObj){
        serverObj.setClientSocket(socket);
        try{
            serverObj.setDatIn(new DataInputStream(socket.getInputStream()));
            serverObj.setDatOut(new DataOutputStream(socket.getOutputStream()));
            serverObj.getDatOut().writeUTF("MYNAMEIS "+ clientName);
            serverObj.getDatOut().flush();
        }catch (Exception e){
            log("Error connecting");
            e.printStackTrace();
        }
    }
    public static boolean connectToEveryone() {
        Socket socket = null;
        for (ServerObj element : Server.serverList){
            try{
                socket = new Socket(element.getIp(), element.getPort());
                element.setClientSocket(socket);
                element.setDatOut(new DataOutputStream(socket.getOutputStream()));
                element.setDatIn(new DataInputStream(socket.getInputStream()));
                element.getDatOut().writeUTF("MYNAMEIS "+ Server.name);
                element.getDatOut().flush();
                log("Connected to " + element.getName() + " [" + element.getIp() + ":"+element.getPort()+"]");
            }catch (Exception e){
                log(element.getName() + " [" + element.getIp() + ":"+element.getPort()+"]" + " is Offline");
            }
        }
        return true;
    }
    public static boolean Offer(String offer, String filename,DataOutputStream datOut){
        log("Sending Offers...");
        connectToEveryone();
        try{
            ArrayList<ServerObj> offeredSoFar = new ArrayList<>();
            for (ServerObj element : Server.serverList) {
                try {
                    log("Trying to send OFFER to %s [%s:%s]".formatted(element.getName(), element.getIp(), element.getPort()));
                    if (element.getClientSocket() != null) {
                        String msgFromServer;
                        element.getDatOut().writeUTF(offer);
                        element.getDatOut().flush();
                        log("Offer sent");
                        msgFromServer = element.getDatIn().readUTF();
                        log("%s [%s:%s] replied: ".formatted(element.getName(), element.getIp(), element.getPort()));
                        if (msgFromServer.equals("YES")) {
                            uploadFile(filename+"_temp" , element);
                            offeredSoFar.add(element); //save a list of servers you've sent the file to so far
                        } else {
                            //One of the servers rejected the file, let the servers who got the file know to delete
                            for (ServerObj element2 : offeredSoFar){
                                if(Server.clientsMap.containsValue(element2.getName())){
                                    element.getDatOut().writeUTF("REJECTED");
                                }
                            }
                            closeConnections();
                            return false;
                        }
                    } else {
                        log("%s [%s:%s] is Offline".formatted(element.getName(), element.getIp(), element.getPort()));
                    }
                }catch (Exception e){
                    log("Error while sending offer to %s [%s:%s]".formatted(element.getName(), element.getIp(), element.getPort()));
                }
            }
            //All the servers wanted the file, let everyone know
            for (ServerObj element : offeredSoFar){
                element.getDatOut().writeUTF("ACCEPTED");
            }
        }catch (Exception e){
            log("Error while making offer");
        }
        closeConnections();
        return  true;
    }
    public static boolean IsOnline(ServerObj server) {
        try (Socket s = new Socket(server.getIp(), server.getPort())) {
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }
    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }
    public static void sync(){
        log("Starting sync...");
        connectToEveryone();
        //Get a file list from all the servers
        for (ServerObj element : Server.serverList) {
            if (element.clientSocket != null) {
                try{
                    element.getDatOut().writeUTF("SYNCLIST ");
                    log("Requested File List from %s [%s:%s]".formatted(element.getName(), element.getIp(), element.getPort()));
                    String msgFromServer;
                    msgFromServer = element.getDatIn().readUTF();
                    log("%s reply: " + msgFromServer); //debug
                    if (msgFromServer.equals("null")) {
                        log("%s [%s:%s] Has no files".formatted(element.getName(), element.getIp(), element.getPort()));
                    } else {//"Name/LockedBy/Host/Time/Hash"
                        msgFromServer = element.getDatIn().readUTF();
                        int filecount = Integer.valueOf(msgFromServer);
                        for (int i =0 ; i<filecount;i++) {
                            msgFromServer = element.getDatIn().readUTF();
                            String[] info;
                            info = msgFromServer.split("/", 5);
                            SyncFile newFile = new SyncFile(info[0], info[1], info[2], info[3], info[4]);
                            if (syncFiles.containsKey(info[0])) {
                                SyncFile oldFile = syncFiles.get(info[0]);
                                //Compares times
                                if (UsefulFunctions.replaceFile(oldFile.getTime(), newFile.getTime())) {
                                    syncFiles.remove(oldFile.getName());
                                    syncFiles.put(newFile.getName(), newFile);
                                }//no need to replace
                            } else {
                                syncFiles.put(newFile.getName(), newFile);
                            }
                        }
                    }
                }catch(Exception e){
                    log("Sync failed");
                    Server.synced = false;
                }
            }
        }
        //Now download the correct file from the correct server
        syncFiles.entrySet().forEach((entry)->{
            ServerObj server = UsefulFunctions.getServerObjByName(entry.getValue().getHost());
            try{
                server.getDatOut().writeUTF("DOWNLOAD "+ entry.getKey());
                String msgFromServer = server.getDatIn().readUTF();
                if(msgFromServer.equals("OK")){
                    ServerClient.downloadFile(entry.getKey(),server.getDatIn());
                }
            }catch (Exception e){
                log("Couldnt get %s from %s [%s:%s]".formatted(entry.getKey(),server.getName(),server.getIp(),server.getPort()));
            }
        });
        //Copy sync file info we got to the File list
        syncFiles.forEach((key,value)->{
            FileObj fileObj = new FileObj(value.getHost(), value.getName(), value.getHash(), value.getTime(), value.getLockedby());
            Server.fileList.put(key,fileObj);
        });
        log("Sync completed");
        closeConnections();
        Server.synced = true;
    }
    public static boolean lock(String filename,String clientname){
        connectToEveryone();
        for(ServerObj element : Server.serverList){
            if(element.getClientSocket() != null){
                try{
                    log("Sending lock to %s [%s:%s]".formatted(element.getName(), element.getIp(), element.getPort()));
                    element.getDatOut().writeUTF("OFFERLOCK "+filename +" " + clientname);
                    String msgFromServer = element.getDatIn().readUTF();
                    if(UsefulFunctions.getCommand(msgFromServer).equals("ERROR")){
                        closeConnections();
                        return false;
                    }
                }catch (Exception e){
                    log("Error sending lock");
                }
            }
        }
        closeConnections();
        return true;
    }
    public static boolean unlock(String filename,String clientname){
        connectToEveryone();
        for(ServerObj element : Server.serverList){
            if(element.getClientSocket() != null){
                try{
                    log("Sending lock to %s [%s:%s]".formatted(element.getName(), element.getIp(), element.getPort()));
                    element.getDatOut().writeUTF("OFFERUNLOCK "+filename + " "+clientname);
                    String msgFromServer = element.getDatIn().readUTF();
                    if(UsefulFunctions.getCommand(msgFromServer).equals("ERROR")){
                        closeConnections();
                        return false;
                    }
                }catch (Exception e){
                    log("Error sending lock");
                }
            }
        }
        closeConnections();
        return true;
    }
    public static void closeConnections() {
        for(ServerObj element : Server.serverList) {
            try{
                if (element.getClientSocket() != null) {
                    element.getDatOut().close();
                    element.getDatIn().close();
                    element.getClientSocket().close();
                    element.getClientSocket().shutdownOutput();
                    element.getClientSocket().shutdownInput();
                }
            }catch (Exception e){
            }
        }
    }

}
