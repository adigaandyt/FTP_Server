import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UsefulFunctions {

    //get first word of message
    public static String getCommand(String msg){
        String arr[] = msg.split(" ", 2);
        return arr[0];
    }
    public static String getFilename(String msg){
        String arr[] = msg.split(" ", 2);
        return arr[1];
    }

    public static String getDatetime(){
        DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
        ZonedDateTime now = ZonedDateTime.now().plusHours(2);
        return dtf.format(now.truncatedTo(ChronoUnit.SECONDS));
    }

    /**
    // return file object based on file name
    public static FileObj findFileByName(String filename) {
        return Server.fileList.stream().filter(file -> filename.equals(file.getFileName())).findFirst().orElse(null);
    }
    */

    public static String[] readConfig(String filename){
        try{
            List<String> listOfStrings = new ArrayList<String>();
            BufferedReader bf = new BufferedReader(new FileReader(filename));
            String line = bf.readLine();
            while (line != null) {
                listOfStrings.add(line);
                line = bf.readLine();
            }
            bf.close();
            String[] array = listOfStrings.toArray(new String[0]);
            return array;
        }catch(Exception e){
            log("Error reading config file");
        }
        return null;
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void populateServerList(String[] configInfo){
        try{
            for(int i=0;i<configInfo.length;i++){
                InetAddress ip = InetAddress.getByName(configInfo[i].split(" ")[1]);
                int port = Integer.parseInt(configInfo[i].split(" ")[2]);
                String serverName = configInfo[i].split(" ")[0];
                ServerObj newServer = new ServerObj(serverName,ip,port);
                Server.serverList.add(newServer);
            }
        }catch (Exception e){
            log("Error while making server list");
        }
    }

    public static ServerObj getServerObjByName(String servername) {
        return Server.serverList.stream().filter(server -> servername.equals(server.getName())).findFirst().orElse(null);
    }
    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }

    public static boolean replaceFile(String old, String current) {
        try{
            String[] datetime1 = old.split("T");
            String[] datetime2 = current.split("T");
            datetime1[1] = datetime1[1].replace("Z", "");
            datetime2[1] = datetime2[1].replace("Z", "");
            old = datetime1[0] + " " + datetime1[1];
            current = datetime2[0] + " " + datetime2[1];
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(old);
            Date oldDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(current);
            if (newDate.compareTo(oldDate) > 0) {
                return false;
            } else
                return true;
        }catch (Exception e){
            log("Error while comparing times");
            return false;
        }
    }




}
