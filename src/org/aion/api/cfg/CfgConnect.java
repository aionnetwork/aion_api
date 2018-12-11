package org.aion.api.cfg;

/** Created by Jay Tseng on 18/04/17. */
public class CfgConnect {

    private String ip;
    private int port;
    protected String user;
    protected String password;

    public CfgConnect() {
        this.ip = "tcp://127.0.0.1";
        this.port = 8547;
        this.user = "account";
        this.password = "account";
    }

    // getters
    public String getIp() {
        return this.ip;
    }

    // setters
    public void setIp(String _ip) {
        this.ip = _ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int _port) {
        this.port = _port;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String _user) {
        this.user = _user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String _pw) {
        this.password = _pw;
    }
}
