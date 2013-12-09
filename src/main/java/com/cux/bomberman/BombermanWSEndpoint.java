/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author mihaicux
 */
@ServerEndpoint("/bombermanendpoint")
public class BombermanWSEndpoint {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    
    private static boolean isFirst = false;
    
    @OnMessage
    public String onMessage(String message, Session peer) throws IOException {
        for (Session peer2 : peers){
            if (!peer.equals(peer2)){
                peer2.getBasicRemote().sendText(peer.getId()+" : "+message);
            } else {
                peer2.getBasicRemote().sendText("back : "+message);
            }
        }
        return null; // any string will be send to the requesting peer
    }

    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }

    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
        if (!isFirst){
            isFirst = true;
            watchPeers();
        }
    }

    @OnError
    public void onError(Throwable t) {
    }
    
    public synchronized void watchPeers(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                while (true){
                    try {
                        // monitor :-?? bombs, bonuses, events in general 
                        for (Session peer : peers){
                            //peer.getBasicRemote().sendText("asd");
                            //peer.getBasicRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu serverul
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }/* catch (IOException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                }
            }
        }).start();
    }
    
}
