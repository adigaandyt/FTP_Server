import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientGUI {
    private JPanel mainPanel;
    private JLabel clientNameLabel;
    private JTextArea msgArea;
    private JButton downloadButton;
    private JTextField downloadField;
    private JButton uploadButton;
    private JTextField uploadField;
    private JButton selectUploadButton;
    private JButton lockButton;
    private JButton unlockButton;
    private JTextField lockField;
    private JButton getVersionButton;
    private JTextField getVersionField;
    private JButton fileListButton;
    private JComboBox serversBox;
    private JButton connectButton;


    private static Client client;
    private static String clientName;
    private ServerObj currentServer;
    public static ArrayList<ServerObj> serverList = new ArrayList<>();
    public static String[] configInfo;


    public ClientGUI() {
        configInfo = UsefulFunctions.readConfig();
        UsefulFunctions.populateServerList(configInfo);
        clientNameLabel.setText("Name: "+configInfo[0]);
        for (ServerObj element : serverList) {
            serversBox.addItem(element.getName());
        }
        serversBox.addItem("All");


        //Upload
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = uploadField.getText();
                File tmpDir = new File(filePath);
                boolean exists = tmpDir.exists();
                if(exists == false){
                    msgArea.append("No file found\n");
                }else{
                Path p1 = Paths.get(filePath);
                Path filename = p1.getFileName();

                if(serversBox.getSelectedItem().equals("All")){
                    client.closeClient();
                    for (int i = 0; i<serverList.size();i++){
                        ServerObj serverObj = serverList.get(i);
                        new Thread(() ->{
                            try{
                                ServerObj tempServer = serverObj;
                                Socket socket = new Socket(tempServer.getIp(),tempServer.getPort());
                                Client allclient = new Client(socket,clientName);
                                UsefulFunctions.connectTo(allclient.getClientName(),socket,tempServer);
                                msgArea.append("Connected to : "+tempServer.getName()+ "\n");
                                tempServer.getDatOut().writeUTF("UPLOAD " + filename.toString());
                                tempServer.getDatOut().flush();
                                    msgArea.append("Upload "+  filename.toString() + " Request Sent To " + tempServer.getName() + "\n");
                                    String msgFromServer = tempServer.getDatIn().readUTF();
                                    msgArea.append("SERVER: "+msgFromServer+ "\n");
                                    if(UsefulFunctions.getCommand(msgFromServer).equals("ERROR")){
                                        msgArea.append(tempServer.getName()+": " + UsefulFunctions.getFilename(msgFromServer)+ "\n");
                                        allclient.closeClient();
                                    }else{
                                        try{
                                            allclient.uploadFile(filePath,tempServer);
                                            msgFromServer = tempServer.getDatIn().readUTF();
                                            msgArea.append(tempServer.getName()+": " + msgFromServer+ "\n");
                                            allclient.closeClient();
                                        }catch (Exception ex){
                                            allclient.closeClient();
                                        }
                                    }

                            }catch (Exception ex){
                                msgArea.append("Error during upload to " + serverObj.getName() +"\n");
                                Client.logger.info("Error during upload");
                            }
                        }).start();
                    }
                }else{
                    new Thread(() ->{
                        try{
                            if(currentServer==null){
                                msgArea.append("Not connected to server\n");
                            }else{
                                currentServer.getDatOut().writeUTF("UPLOAD " + filename.toString());
                                currentServer.getDatOut().flush();
                                msgArea.append("Upload "+  filename.toString() + " Request Sent To " + currentServer.getName() + "\n");
                                String msgFromServer = currentServer.getDatIn().readUTF();
                                msgArea.append(currentServer.getName() +" "+msgFromServer+ "\n");
                                if(UsefulFunctions.getCommand(msgFromServer).equals("ERROR")){
                                    msgArea.append(currentServer.getName()+": " + UsefulFunctions.getFilename(msgFromServer)+ "\n");
                                }else{
                                    try{
                                        client.uploadFile(filePath,currentServer);
                                        msgFromServer = currentServer.getDatIn().readUTF();
                                        msgArea.append(currentServer.getName()+": " + msgFromServer+ "\n");
                                    }catch (Exception ex){

                                    }
                                }
                            }
                        }catch (Exception ex){
                            msgArea.append("Error during upload to " +currentServer.getName() + "\n");
                            Client.logger.info("Error during upload");
                        }
                    }).start();
                }
                }
            }
        });
        //Connect
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String servername = serversBox.getSelectedItem().toString();
                if(!servername.equals("All")){
                    client.closeClient();
                    currentServer = UsefulFunctions.getServerObjByName(servername);
                    try{
                        Socket socket = new Socket(currentServer.getIp(),currentServer.getPort());
                        client.setClientSocket(socket);
                        UsefulFunctions.connectTo(client.getClientName(),socket,currentServer);
                        msgArea.append("Connected to : "+currentServer.getName()+ "\n");
                    }catch (Exception ex){
                        msgArea.append("Couldn't Connect To Server\n");
                        Client.logger.info("Error connecting");
                    }
                }else {
                    //do nothing
                }
            }
        });
        //Select
        selectUploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select a file for upload");
                fileChooser.setSelectedFile(new File(uploadField.getText()));

                if (fileChooser.showOpenDialog(selectUploadButton) == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().toString();
                    uploadField.setText(path);
                }
            }
        });
        //Lock
        lockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /*TODO: TRY TO CONNECT TO ANY SERVER IN THE SERVER LIST AND SEND THE REQUEST */

                if(serversBox.getSelectedItem().equals("All")) {
                    client.closeClient();
                    for (int i = 0; i < serverList.size(); i++) {

                        ServerObj serverObj = serverList.get(i);
                        try{

                            Socket socket = new Socket(serverObj.getIp(),serverObj.getPort());
                            Client allclient = new Client(socket,clientName);
                            UsefulFunctions.connectTo(allclient.getClientName(),socket,serverObj);
                            msgArea.append("Connected to : "+serverObj.getName()+ "\n");

                            serverObj.getDatOut().writeUTF("LOCK " + lockField.getText());
                            serverObj.getDatOut().flush();
                            msgArea.append("LOCK " + lockField.getText() + " Request Sent to "+ serverObj.getName() +"\n");
                            String msgFromServer = serverObj.getDatIn().readUTF();
                            msgArea.append(serverObj.getName()+": " + msgFromServer +"\n");
                            allclient.closeClient();
                        }catch (Exception ex){
                            Client.logger.info("Error Locking File");
                            ex.printStackTrace();
                        }
                    }

                }else{

                    if(currentServer==null) {
                        msgArea.append("Not connected to server\n");
                    }else{
                        try{
                            currentServer.getDatOut().writeUTF("LOCK " + lockField.getText());
                            currentServer.getDatOut().flush();
                            msgArea.append("LOCK " + lockField.getText() + " Request Sent to "+ currentServer.getName() +"\n");
                            String msgFromServer = currentServer.getDatIn().readUTF();
                            msgArea.append(currentServer.getName()+": " + msgFromServer +"\n");
                        }catch (Exception ex){
                            Client.logger.info("Error Locking File");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        //Unlock
        unlockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(serversBox.getSelectedItem().equals("All")) {
                    client.closeClient();
                    for (int i = 0; i < serverList.size(); i++) {
                        ServerObj serverObj = serverList.get(i);
                        try {

                            Socket socket = new Socket(serverObj.getIp(), serverObj.getPort());
                            Client allclient = new Client(socket, clientName);
                            UsefulFunctions.connectTo(allclient.getClientName(), socket, serverObj);
                            msgArea.append("Connected to : " + serverObj.getName() + "\n");
                            serverObj.getDatOut().writeUTF("UNLOCK " + lockField.getText());
                            serverObj.getDatOut().flush();
                            msgArea.append("UNLOCK " + lockField.getText() + " Request Sent to " + serverObj.getName() + "\n");
                            String msgFromServer = serverObj.getDatIn().readUTF();
                            msgArea.append(serverObj.getName() + ": " + msgFromServer + "\n");
                            allclient.closeClient();

                        } catch (Exception ex) {
                            Client.logger.info("Error Unlocking File");
                            ex.printStackTrace();
                        }
                    }
                    }else {
                    if(currentServer==null) {
                        msgArea.append("Not connected to server\n");
                    }else{
                        try{
                            currentServer.getDatOut().writeUTF("UNLOCK " + lockField.getText());
                            currentServer.getDatOut().flush();
                            msgArea.append("UNLOCK " + lockField.getText() + " Request Sent to "+ currentServer.getName() +"\n");
                            String msgFromServer = currentServer.getDatIn().readUTF();
                            msgArea.append(currentServer.getName()+": " + msgFromServer +"\n");
                        }catch (Exception ex){
                            Client.logger.info("Error Unlocking File");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        //Get version
        getVersionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(serversBox.getSelectedItem().equals("All")) {
                    client.closeClient();
                    for (int i = 0; i < serverList.size(); i++) {
                        ServerObj serverObj = serverList.get(i);
                        try {
                            Socket socket = new Socket(serverObj.getIp(), serverObj.getPort());
                            Client allclient = new Client(socket, clientName);
                            UsefulFunctions.connectTo(allclient.getClientName(), socket, serverObj);
                            msgArea.append("Connected to : " + serverObj.getName() + "\n");
                            serverObj.getDatOut().writeUTF("GETVERSION " + getVersionField.getText());
                            serverObj.getDatOut().flush();
                            msgArea.append("GETVERSION " + getVersionField.getText() + " Request Sent to "+ serverObj.getName() +"\n");
                            String msgFromServer = serverObj.getDatIn().readUTF();
                            msgArea.append(serverObj.getName()+": " + msgFromServer +"\n");
                            allclient.closeClient();
                        }catch (Exception ex){
                            Client.logger.info("Error Unlocking File");
                            ex.printStackTrace();
                        }
                    }
                }else{
                    if(currentServer==null) {
                        msgArea.append("Not connected to server\n");
                    }else{
                        try{
                            currentServer.getDatOut().writeUTF("GETVERSION " + getVersionField.getText());
                            currentServer.getDatOut().flush();
                            msgArea.append("GETVERSION " + getVersionField.getText() + " Request Sent to "+ currentServer.getName() +"\n");
                            String msgFromServer = currentServer.getDatIn().readUTF();
                            msgArea.append(currentServer.getName()+": " + msgFromServer +"\n");
                        }catch (Exception ex){
                            Client.logger.info("Error Unlocking File");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        //File list
        fileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentServer==null) {
                    msgArea.append("Not connected to server\n");
                }else{
                    try{
                        currentServer.getDatOut().writeUTF("GETLIST");
                        currentServer.getDatOut().flush();
                        msgArea.append("File List Requst Sent To " + currentServer.getName() +"\n");
                        String msgFromServer = currentServer.getDatIn().readUTF();
                        msgArea.append(currentServer.getName()+": " + msgFromServer +"\n");
                        if(msgFromServer.equals("OK")){
                            msgArea.append(currentServer.getName() + " File List");
                            msgFromServer = currentServer.getDatIn().readUTF();
                            String[] fileList = msgFromServer.split(":");
                            for (String filename : fileList) {
                                msgArea.append(filename + "\n");
                            }
                        }else{
                            msgArea.append("Couldn't get File List");
                        }
                    }catch (Exception ex){
                        Client.logger.info("Error Get File");
                        ex.printStackTrace();
                    }
                }
            }
        });
        //Download
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String filename = downloadField.getText();
                    if (filename.equals("")){
                        msgArea.append("No file selected\n");
                    }
                    else {
                        new Thread(() ->{
                            if(currentServer==null){
                                msgArea.append("Not connected to server\n");
                            }else {
                                try{
                                    currentServer.getDatOut().writeUTF("DOWNLOAD "+filename );
                                    currentServer.getDatOut().flush();
                                    msgArea.append("Download " + filename + " Request Sent To " + currentServer.getName() +"\n");
                                    String msgFromServer = currentServer.datIn.readUTF();
                                    if(UsefulFunctions.getCommand(msgFromServer).equals("ERROR")){
                                        msgArea.append(currentServer.getName()+": " + msgFromServer+ "\n");
                                    }else{
                                        client.downloadFile(filename,currentServer.getDatIn());
                                        msgArea.append(currentServer.getName()+": " + msgFromServer+ "\n");
                                    }
                                }catch (Exception ex){
                                    msgArea.append("Error during download\n");
                                    Client.logger.info("Error during download");
                                    ex.printStackTrace();
                                }
                            }






                        }).start();
                    }
                }catch (Exception ex){
                    msgArea.append("Error during download");
                    Client.logger.info("Error during download");
                    ex.printStackTrace();
                }
            }
        });


    }

    public static void main(String[] args) {
        //read config, get name, get server list, make a client instance with an empty socket


        JFrame frame = new JFrame("Client");
        frame.setContentPane(new ClientGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        clientName = configInfo[0];
        client = new Client(null,clientName);




    }
}
