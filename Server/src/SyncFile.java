//Class to receive file info from servers during sync
public class SyncFile {
    private String name;
    private String lockedby;
    private String host;
    private String time;
    private  String hash;

    public SyncFile(String name, String lockedby, String host, String time,String hash) {
        this.name = name;
        this.lockedby = lockedby;
        this.host = host;
        this.time = time;
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLockedby() {
        return lockedby;
    }

    public void setLockedby(String lockedby) {
        this.lockedby = lockedby;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
