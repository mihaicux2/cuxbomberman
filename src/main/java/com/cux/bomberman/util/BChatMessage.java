/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cux.bomberman.util;

import com.cux.bomberman.BombermanWSEndpoint;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class BChatMessage {
    
    protected String author;
    protected int author_id;
    protected String timestamp;
    protected String message;
    protected int room_nr;
    protected int id;

    public BChatMessage(String author, /*String timestamp, */String message, int room_nr){
        this.setAuthor(author);
//        this.setTimestamp(timestamp);
        this.setMessage(message);
//        this.setAuthorIDByName(author);
        this.setRoom_nr(room_nr);
    }
    
    public BChatMessage(int author_id, /*String timestamp, */String message, int room_nr){
        this.setAuthor_id(author_id);
//        this.setTimestamp(timestamp);
        this.setMessage(message);
//        this.setAuthorByID(author_id);
        this.setRoom_nr(room_nr);
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setAuthorByID(int author_id){
        try {
            String query = "SELECT username FROM `user` WHERE `id`=?";
            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, author_id);
            ResultSet rs = st.executeQuery();
            if(rs.next())
            {
                this.setAuthor(rs.getString("username"));
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
    public void setAuthorIDByName(String author){
        try {
            String query = "SELECT id FROM `user` WHERE `username`=?";
            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, author);
            ResultSet rs = st.executeQuery();
            if(rs.next())
            {
                this.setAuthor_id(rs.getInt("id"));
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
    public int saveToDB(){
        try {
            String query = "INSERT INTO `chat_message` SET "
                + "`user_id`=?,"
                + "`room_nr`=?,"
                + "`message_time`=NOW(),"
                + "`message`=?;";

            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, this.author_id);
            st.setInt(2, this.room_nr);
            st.setString(3, this.message);
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0){
                throw new SQLException("Cannot save message");
            }
            else{
                ResultSet rs = st.getGeneratedKeys();
                if(rs.next())
                {
                    this.setId(rs.getInt(1));
                }
            }
            return 1;
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            return 0;
        }
    }
    
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }
    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getRoom_nr() {
        return room_nr;
    }

    public void setRoom_nr(int room_nr) {
        this.room_nr = room_nr;
    }
    
    /**
     * Public method used to export the bomb for the client
     * @return The JSON representation of the bomb
     */
    @Override
    public String toString(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
            return ex.getMessage();
           // return "";
        }
    }
    
}
