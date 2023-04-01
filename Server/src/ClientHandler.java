import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ClientHandler extends Thread{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private String clientName = "null";
    private  DataOutputStream datOut = null;
    private  DataInputStream datIn = null;
    //Server.serverClient, this server as a client
    private static boolean accepted;



    public ClientHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
        } catch (Exception e) {
            closeClient();
            log("Error creating client socket");
        }
    }
    public void removeClientHandler(){
        clientHandlers.remove(this);
        System.out.printf("SERVER: " +clientName + " Disconnected\n");
    }
    public void closeClient(){
        removeClientHandler();
        try{

            if(this.clientSocket != null){
                this.clientSocket.close();
            }
            if(this.datOut != null){
                this.datOut.close();
            }
            if(this.datIn != null){
                this.datIn.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }
    @Override
    public void run(){
        while(clientSocket.isConnected()){
            String clientMsg = null;
            Server.clientsMap.putIfAbsent(clientSocket,clientName);
            try {
                datIn = new DataInputStream(clientSocket.getInputStream());
                datOut = new DataOutputStream(clientSocket.getOutputStream());
                clientMsg = datIn.readUTF();
                if (UsefulFunctions.getCommand(clientMsg).equals("MYNAMEIS")) {
                    clientName = UsefulFunctions.getFilename(clientMsg);
                    log(clientName + " Has Connected...");
                }
                //Log commands here instead of each if statement
                if (!clientMsg.equals("") && !clientName.equals("null") && !UsefulFunctions.getCommand(clientMsg).equals("MYNAMEIS") && !isInterrupted()) {
                    log(clientMsg + " Request from " + clientName + " [" + clientSocket.getInetAddress() + "]");
                }
                //UPLOAD
                if (UsefulFunctions.getCommand(clientMsg).equals("UPLOAD") && !clientName.equals("null") && !isInterrupted()){
                    String filename = UsefulFunctions.getFilename(clientMsg);
                    try{
                        //if file already exists then it must  be locked by the client uploading
                        if (Server.fileList.containsKey(filename)){
                            FileObj checkfile = Server.fileList.get(filename);
                            if(checkfile.getLockedBy().equals(clientName)){
                                datOut.writeUTF("OK");
                                String hash = ClientActions.UPLOAD(filename+"_temp",datIn);
                                String time = UsefulFunctions.getDatetime();
                                FileObj newFile = new FileObj(clientName,filename,hash,time,clientName); // FileObj to add to file list
                                File file = new File(Server.downloadPath + filename + "_temp"); //File object to handle rename/delete
                                if(ServerClient.Offer("OFFER " + hash + " " + time + " " + filename + " " + clientName,filename,datOut)){
                                    //if everyone accepts, rename the file without _temp and add it to the file list
                                    Server.fileList.replace(filename,newFile);
                                    new File(Server.downloadPath + filename).delete();
                                    file.renameTo(new File(Server.downloadPath + filename));
                                    datOut.writeUTF("Uploaded successfully");
                                    log("Uploaded successfully");
                                }else {
                                    //someone rejected, delete
                                    file.delete();
                                    datOut.writeUTF("ERROR One of the servers rejected the file");
                                    log("Sent ERROR to %s when one of the servers rejected the file ".formatted(clientName));
                                }
                            }else {
                                if(checkfile.getLockedBy().equals("null")){
                                    datOut.writeUTF("ERROR Lock file first");
                                    log("Sent ERROR Lock file first to "+clientName);
                                }else{
                                    datOut.writeUTF("ERROR File is locked");
                                    log("Sent 'ERROR File is locked' to "+clientName);
                                }
                            }
                        }else{
                            //if it's a new file just accept the upload
                            datOut.writeUTF("OK");
                            String hash = ClientActions.UPLOAD(filename + "_temp",datIn); //receive file and save with _temp for now
                            String time = UsefulFunctions.getDatetime();
                            FileObj newFile = new FileObj(clientName,filename,hash,time); // FileObj to add to file list (without a LockedBy)
                            File file = new File(Server.downloadPath + filename + "_temp"); //File object to handle rename/delete

                            // Sending the offer and checking if everyone accepted
                            if(ServerClient.Offer("OFFER " + hash + " " + time + " " + filename + " " + clientName,filename,datOut)){
                                //if everyone accepts, rename the file without _temp and add it to the file list
                                Server.fileList.put(filename,newFile);
                                file.renameTo(new File(Server.downloadPath + filename));
                                datOut.writeUTF("Uploaded successfully");
                                log("Uploaded successfully");
                            }else{
                                file.delete();
                                datOut.writeUTF("ERROR One of the servers rejected the file");
                                log("Sent ERROR to %s when one of the servers rejected the file ".formatted(clientName));
                            }
                        }
                    }catch (Exception e){
                        datOut.writeUTF("ERROR Error During upload");
                        log("Sent 'ERROR Error During upload' to "+clientName);
                    }
                }
                //LOCK
                if ((UsefulFunctions.getCommand(clientMsg).equals("LOCK") || UsefulFunctions.getCommand(clientMsg).equals("OFFERLOCK")) && !clientName.equals("null") && !isInterrupted()) {
                    boolean isAnOffer = false;
                    String filename;
                    String lockrequester = null;
                    if(UsefulFunctions.getCommand(clientMsg).equals("OFFERLOCK")){isAnOffer = true;
                        String[] inf = UsefulFunctions.getFilename(clientMsg).split(" ",2);
                        filename = inf[0];
                        lockrequester = inf[1];
                    }else {filename = UsefulFunctions.getFilename(clientMsg);}
                    try {
                        if (!Server.fileList.containsKey(filename)) {
                            datOut.writeUTF("ERROR File doesn't exist");
                            log("Sent 'ERROR File doesn't exist' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                        } else {
                            FileObj fileobj = Server.fileList.get(filename);
                            String lockedy = fileobj.getLockedBy();
                            if (lockedy.equals("null")) {
                                if(!isAnOffer){
                                    if(ServerClient.lock(filename,clientName)){
                                        fileobj.setLockedBy(clientName);
                                        Server.fileList.replace(filename,fileobj);
                                        datOut.writeUTF("File locked successfully");
                                        log("File locked successfully");
                                    }else {
                                        datOut.writeUTF("ERROR one of the servers rejected lock");
                                        log("ERROR one of the servers rejected lock");
                                    }
                                }else {
                                    fileobj.setLockedBy(lockrequester);
                                    Server.fileList.replace(filename,fileobj);
                                    datOut.writeUTF("File locked successfully");
                                    log("File locked successfully");
                                }

                            } else {
                                datOut.writeUTF("ERROR File is already locked");
                                log("Sent 'ERROR File is already locked' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                            }
                        }
                    } catch (Exception e) {
                        datOut.writeUTF("ERROR Error During lock");
                        log("Sent 'ERROR Error During lock' to " + clientName);
                    }
                }
                //UNLOCK
                if ((UsefulFunctions.getCommand(clientMsg).equals("UNLOCK") || UsefulFunctions.getCommand(clientMsg).equals("OFFERUNLOCK"))&& !clientName.equals("null") && !isInterrupted()) {
                    boolean isAnOffer = false;
                    String filename;
                    String unlockrequester = null;

                    if(UsefulFunctions.getCommand(clientMsg).equals("OFFERUNLOCK")){isAnOffer = true;
                        String[] inf = UsefulFunctions.getFilename(clientMsg).split(" ",2);
                        filename = inf[0];
                        unlockrequester = inf[1];
                    }else {filename = UsefulFunctions.getFilename(clientMsg);}

                    if (!Server.fileList.containsKey(filename)) {
                        datOut.writeUTF("File doesn't exist");
                        log("Sent 'File doesn't exist' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                    } else {
                        FileObj fileobj = Server.fileList.get(filename);
                        if(!isAnOffer){
                            if (fileobj.getLockedBy().equals(clientName)) {
                                if (ServerClient.unlock(filename, clientName)) {
                                    fileobj.setLockedBy("null");
                                    Server.fileList.replace(filename, fileobj);
                                    // There is no already unlocked checked because as far as the user is concerned the file is unlocked
                                    datOut.writeUTF("File unlocked successfully");
                                    log("Sent 'File unlocked successfully' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                                } else {
                                    datOut.writeUTF("ERROR one of the servers rejected unlock");
                                    log("ERROR one of the servers rejected unlock");
                                }
                            } else {
                                datOut.writeUTF("ERROR File is locked");
                                log("Sent 'ERROR File is locked' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                            }
                        }else{
                            if (fileobj.getLockedBy().equals(unlockrequester)){
                                fileobj.setLockedBy("null");
                                Server.fileList.replace(filename, fileobj);
                                // There is no already unlocked checked because as far as the user is concerned the file is unlocked
                                datOut.writeUTF("File unlocked successfully");
                                log("Sent 'File unlocked successfully' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                            }else{
                                datOut.writeUTF("ERROR File is locked");
                                log("Sent 'ERROR File is locked' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                            }
                        }
                    }
                }
                //FILELIST
                if (UsefulFunctions.getCommand(clientMsg).equals("GETLIST") && !clientName.equals("null") && !isInterrupted()) {
                    try {
                        datOut.writeUTF("OK");
                        //Check on later
                        AtomicReference<String> returnList = new AtomicReference<>("");
                        Server.fileList.forEach((key, value) -> {
                            returnList.set(returnList + ":" + key);
                        });
                        datOut.writeUTF(returnList.get());
                        log("Sent File list to " + clientName);
                    } catch (Exception ex) {
                        log("Error duirng FILELIST request");
                    }
                }
                //GETVERSION
                if (UsefulFunctions.getCommand(clientMsg).equals("GETVERSION") && !clientName.equals("null") && !isInterrupted()) {
                    String filename = UsefulFunctions.getFilename(clientMsg);
                    if (!Server.fileList.containsKey(filename)) {
                        datOut.writeUTF("ERROR File doesn't exist");
                        log("Sent 'ERROR File doesn't exist' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                    } else {
                        FileObj fileobj = Server.fileList.get(filename);
                        datOut.writeUTF(fileobj.getFileName() + ": " + fileobj.getVersion() + " [" + fileobj.getDateTime() + "]");
                        log("Sent " + fileobj.getFileName() + " Version to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                        log("Version: " + fileobj.getVersion());
                    }
                }
                //DOWNLOAD
                if (UsefulFunctions.getCommand(clientMsg).equals("DOWNLOAD") && !clientName.equals("null") && !isInterrupted()) {
                    String filename = UsefulFunctions.getFilename(clientMsg);
                    try {
                        if (Server.fileList.containsKey(filename)) {
                            datOut.writeUTF("OK");
                            log("Sent 'OK' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                            ClientActions.DOWNLOAD(Server.downloadPath + filename, datOut);
                        } else {
                            datOut.writeUTF("ERROR File doesn't exist");
                            log("Sent 'ERROR File doesn't exis' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                        }
                    } catch (Exception e) {
                        datOut.writeUTF("ERROR Error During download");
                        log("Sent 'ERROR Error During download' to " + clientName + " [" + clientSocket.getInetAddress() + "]");
                    }
                }
                //OFFER
                if (UsefulFunctions.getCommand(clientMsg).equals("OFFER") && !clientName.equals("null") && !isInterrupted()) {
                    //element.getDatOut().writeUTF("OFFER " + hash + " " + time + " " + " " + filename + " " + clientName);
                    String arr[] = clientMsg.split(" ", 5);
                    String hash = arr[1];
                    String time = arr[2];
                    String filename = arr[3];
                    String fromClient = arr[4];
                    //New file
                    if (Server.fileList.containsKey(filename)) {
                        //file exists check locker
                        FileObj checkfile = Server.fileList.get(filename);
                        if (checkfile.getLockedBy().equals(fromClient)) {
                            //Locked by client, answer YES to the offer
                            FileObj newFile = new FileObj(clientName,filename,hash,time,fromClient); // FileObj to add to file list
                            File file = new File(Server.downloadPath + filename + "_temp");  //File object to handle rename/delete
                            datOut.writeUTF("YES");
                            log("Offer accepted");
                            ClientActions.UPLOAD(filename,datIn);
                            String reply = datIn.readUTF();
                            Server.fileList.replace(filename,newFile);
                        }else{
                            if(checkfile.getLockedBy().equals("null")){
                                datOut.writeUTF("ERROR Lock file first");
                                log("Sent ERROR Lock file first to "+fromClient);
                            }else{
                                datOut.writeUTF("ERROR File is locked");
                                log("Sent 'ERROR File is locked' to "+fromClient);
                            }
                        }
                    } else {
                        //New file, request download
                        datOut.writeUTF("YES");
                        log("Offer accepted");
                        String tempName = filename+"_temp";
                        FileObj newFile = new FileObj(clientName,filename,hash,time); // FileObj to add to file list
                        File file = new File(Server.downloadPath + filename + "_temp");  //File object to handle rename/delete
                        ClientActions.UPLOAD(filename+"_temp",datIn);
                        String reply = datIn.readUTF();
                        if(reply.equals("ACCEPTED")){
                            //Everyone accepted save file and rename
                            Server.fileList.put(filename,newFile);
                            file.renameTo(new File(Server.downloadPath + filename));
                            log("Downloaded %s from %s".formatted(filename,clientName));
                        }else if (reply.equals("REJECTED")){
                            //One of the servers rejected, delete file
                            file.delete();
                            log("One of the servers rejected");
                        }else{
                            log("Unexpected reply : " + reply); //debugging
                        }
                    }
                }
                //SYNCLIST
                if (UsefulFunctions.getCommand(clientMsg).equals("SYNCLIST") && !clientName.equals("null") && !isInterrupted()) {
                    try {
                        datOut.writeUTF("OK");
                        datOut.writeUTF(String.valueOf(Server.fileList.size()));
                        //Check on later
                        Server.fileList.forEach((key, value) -> {
                            System.out.println("Sending "+ Server.fileList.get(key) );
                            try {
                                datOut.writeUTF("%s/%s/%s/%s/%s".formatted(value.getFileName(),value.getLockedBy(),Server.name,value.getDateTime(),value.getVersion()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        log("Sent File list to " + clientName);
                    } catch (Exception ex) {
                        log("Error duirng FILELIST request");
                    }
                }


            }catch (Exception e){
                //e.printStackTrace();
                closeClient();
                break;
            }
        }
    }
}

