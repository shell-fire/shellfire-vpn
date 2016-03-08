/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import de.shellfire.www.webservice.sf_soap_php.WsServer;

/**
 * 
 * @author bettmenn
 */
public class ServerList {

    private LinkedList<Server> servers = new LinkedList<Server>();

    ServerList(WsServer[] list) {
        this.loadServersFromSOAP(list);

    }

    private void loadServersFromSOAP(WsServer[] list) {
        for (int i = 0; i < list.length; i++) {
            WsServer wss = list[i];
            Server server = new Server(wss);
            this.servers.add(server);
        }
    }

    public int getNumberOfServers() {
        if (this.servers == null) {
            return 0;
        } else {
            return this.servers.size();
        }
    }

    public Server getServer(int num) {
        return this.servers.get(num);
    }

    public Server getServerByServerId(int serverId) {
        for (Server server : this.servers) {
            if (server.getServerId() == serverId) {
                return server;
            }
        }

        return null;
    }

    public int getServerNumberByServer(Server server) {
        return this.servers.indexOf(server);
    }

    public LinkedList<Server> getAll() {
        return this.servers;
    }

    public Server getRandomFreeServer() {
        Server[] arrServer = new Server[this.getNumberOfServers()];
        int i = 0;
        for (Server server : this.servers) {
            if (server.getServerType() == ServerType.Free) {
                arrServer[i++] = server;
            }
        }

        Random generator = new Random((new Date()).getTime());
        int num = generator.nextInt(i);

        return arrServer[num];

    }
    public Server getRandomPremiumServer() {
      Server[] arrServer = new Server[this.getNumberOfServers()];
      int i = 0;
      for (Server server : this.servers) {
          if (server.getServerType() == ServerType.Premium) {
              arrServer[i++] = server;
          }
      }

      Random generator = new Random((new Date()).getTime());
      int num = generator.nextInt(i);

      return arrServer[num];

  }
}
