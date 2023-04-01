public class ServerReply {
    String servername;
    String reply;

    public ServerReply(String servername, String reply) {
        this.servername = servername;
        this.reply = reply;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
