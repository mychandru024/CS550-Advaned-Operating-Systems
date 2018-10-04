package cs550.pa2.helpers;

/**
 * Created by Ajay on 2/26/17.
 */
public class Host {
    private String url;
    private int port;

    public Host(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String address() {
        return url+":"+port;
    }
}
