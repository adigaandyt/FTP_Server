import java.io.*;

public class ClientActions {

    //Client wants to upload, receive file
    public static String UPLOAD(String filename, DataInputStream datIn){
        try{
            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream( Server.downloadPath + filename);
            long size = datIn.readLong();     // read file size
            byte[] buffer = new byte[4*1024];
            byte[] toHash = new byte[(int) size];//done after file is complete
            while (size > 0 && (bytes = datIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();
            // Re-read the created file data and create a hash
            String hash = ServerActions.createHash(Server.downloadPath + filename);
            return hash;
        }catch (Exception e){
            log("Error receiving file");
            e.printStackTrace();
            return null;
        }
    }

    //Client wants to download, send file
    public static void DOWNLOAD(String path, DataOutputStream datOut){
        try{
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            // send file size
            datOut.writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[4*1024];
            while ((bytes=fileInputStream.read(buffer))!=-1){
                datOut.write(buffer,0,bytes);
                datOut.flush();
            }
            fileInputStream.close();
        }catch (Exception ex){
            log("Error during download");
        }
    }
    public static void log(String msg){
        System.out.println(msg);
        Server.logger.info(msg);
    }



}
