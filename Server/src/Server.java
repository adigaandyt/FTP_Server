import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    public static Logger logger = Logger.getLogger("MyLog");
    //public static String[] configInfo;
    public static String downloadPath;
    //File List, Key = FileName, Value = FileObj
    public static ConcurrentHashMap<String, FileObj> fileList = new ConcurrentHashMap<>();
    public static ArrayList<ServerObj> serverList = new ArrayList<>();
    public static ConcurrentHashMap<Socket, String> clientsMap = new ConcurrentHashMap<>();
    public static boolean synced;
    private static String[] configInfo;
    public static int port;
    public static InetAddress ip;
    public static String name;





    public static void main(String[] args) {

        //Setup Log File
        {
            FileHandler fh;
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %5$s %n");
            try {
                fh = new FileHandler("./MyLogFile.log");
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
                logger.setUseParentHandlers(false);
            } catch (SecurityException | IOException e) {
                e.printStackTrace();
                System.out.println("couldn't set up log file");
            }
        }
        //Read config files
        {
            try{
                //list of server objects
                // Store servers from serverlist.txt in serverList
                String[] serverlistConfig = UsefulFunctions.readConfig("serverlist.txt");
                UsefulFunctions.populateServerList(serverlistConfig);
                String[] configConfig = UsefulFunctions.readConfig("config.txt");
                downloadPath = configConfig[0]; // get download path from config.txt
                name = configConfig[1].split(" ")[0].toString();
                ip = InetAddress.getByName(configConfig[1].split(" ")[1]);
                port = Integer.parseInt(configConfig[1].split(" ")[2]);
            }catch (Exception e){
                log("Error while reading config");
            }

        }


        log("Server Starting...");


        String msg = ""; //String to stop and resume listening
        BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        Listener listener = null;
        ServerClient.sync();
        //sync up
        while(!synced){
            Thread.onSpinWait();
        }


        log("Starting client side");
        //ServerClient serverClient = new ServerClient();
        //serverClient.run();


        log("Starting server side");
        //Start server as a server
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port,50,ip);
        } catch (IOException e) {
            System.out.println("Error making server socket");
        }
        while (true) {
            try{
                listener = new Listener(serverSocket);
                listener.start();
                System.out.println("Enter Stop/Resume to Stop/Resume listening\n");
            }catch (Exception e){
                log("error starting server");
                e.printStackTrace();
            }
            try{
                while(!msg.equalsIgnoreCase("stop")){
                    msg = brIn.readLine();
                }
                while(!msg.equalsIgnoreCase("resume")){
                    listener.interrupt();
                    //ServerSocket.close();
                    log("Stopped listening...");
                    msg = brIn.readLine();
                    log("Resuming...");
                }
            }catch (Exception ex){
                ex.printStackTrace();
                log("server crashed");
                listener.interrupt();
            }
        }
    }
    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }
}
