public class FileObj {
    //private static int fileId = 0;
    private String senderName;

    private String fileName;
    private String lockedBy;
    private String version;
    private String dateTime;

    public FileObj(String senderName, String fileName, String version, String dateTime) {
        //this.fileId = fileId;
        //fileId += 1;
        this.senderName = senderName;
        this.fileName = fileName;
        this.lockedBy = "null";
        this.version = version;
        this.dateTime = dateTime;
    }

    //constructor for existing file
    public FileObj(String senderName, String fileName, String version, String dateTime,String lockedBy) {
        //this.fileId = fileId;
        //fileId += 1;
        this.senderName = senderName;
        this.fileName = fileName;
        this.lockedBy = lockedBy;
        this.version = version;
        this.dateTime = dateTime;
    }


    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
