/*******************************************************************************
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 *
 ******************************************************************************/

package org.aion.api.cfg;


/**
 * Created by Jay Tseng on 18/04/17.
 */ public class CfgConnect {

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
