import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class ServerActions {

    /**
    // Check if a file exists with the same name
    public static boolean fileNameExistCheck(String filename){
        Boolean result = Server.fileList.stream().anyMatch(o -> filename.equals(o.getFileName()));
        return result;
    }
    */

    /**
    // Check if name is the one who locked the file
    public static boolean isLockeyBy(String name){
        Boolean result = Server.fileList.stream().anyMatch(o -> name.equals(o.getLockedBy()));
        return result;
    }
     */

    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }
    //create hash for the data of the given file
    public static String createHash(String filepath){
        try{
            Path path = Paths.get(filepath);
            byte[] arr = Files.readAllBytes(path);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(arr);
            String returnHash = UsefulFunctions.bytesToHex(encodedhash);
            return returnHash;
        }catch (Exception e){
            log("Error while creating HASH");
            return null;
        }
    }

    /*TODO: ADD SERVER TO SERVER ACTIONS
    *  SEND FILE
    *  RECEIVE FILE
    *  RECEIVE FILE LIST
    *  COMPARE FILE LISTS, PICK LATEST FILES
    *  SEND OFFER AND HEAR REPLY, LOCK UNLOCK UPLOAD
    *  WHEN A CLIENT UPLOADS, SAVE THE FILE WITH _TEMP AND CHECK IF THE OTHER SERVERS WANT IT, IF THEY DON'T DELETE IT
    *  OR IF POSSIBLE CHECK REPLIES FIRST AND THEN ACCEPT THE UPLOAD
    *  SYNC START
    *  on start
    *  each server should hold a list of servers and a tag if they're synced/not synced
    *  1) START SYNC
    *  2) REQUEST FILE LIST
    *  3) CREATE A LIST "FILES I WANT"
    *  3.a) U GET A LIST AND SWAP OUT FILES IF U GET ANOTHER FILE WITH SAME NAME BUT A LATER TIME
    *  4) EACH FILE SOURCE AND INFO IS SAVED
    *  5) AFTER COMPARISON IS DONE REQUEST FROM EACH SERVER THE FILES IN THE "FILES I WANT LIST"
    *   */



}
