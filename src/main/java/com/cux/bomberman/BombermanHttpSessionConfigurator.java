package com.cux.bomberman;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This class is used as a session configurator for the servlet connection 
 * protocol with the clients
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class BombermanHttpSessionConfigurator  extends ServerEndpointConfig.Configurator {
    
    /**
     * Public method used to store the session information about a given connection
     * @param config The server endpoint configuration
     * @param request The request made by a client to the server
     * @param response The response to be sent back to the client
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig config, 
                                HandshakeRequest request, 
                                HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(),httpSession);
    }
    
}
