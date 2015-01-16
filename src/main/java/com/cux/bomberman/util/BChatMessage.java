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
 * The following class is used for "mapping" the chat messages to a visible
 * JAVA object (simple and effective ORM mechanism)
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class BChatMessage {
    
    protected String author;
    protected int author_id;
    protected String timestamp;
    protected String message;
    protected int room_nr;
    protected int id;

    /**
     * Public constructor of the current class
     * @param author The username of the player that sent the message
     * @param message The message
     * @param room_nr The room number of the username
     */
    public BChatMessage(String author, /*String timestamp, */String message, int room_nr){
        this.setAuthor(author);
//        this.setTimestamp(timestamp);
        this.setMessage(message);
//        this.setAuthorIDByName(author);
        this.setRoom_nr(room_nr);
    }
    
    /**
     * Public constructor of the current class
     * @param author_id The user id of the player that sent the message
     * @param message The message
     * @param room_nr The room number of the username
     */
    public BChatMessage(int author_id, /*String timestamp, */String message, int room_nr){
        this.setAuthor_id(author_id);
//        this.setTimestamp(timestamp);
        this.setMessage(message);
//        this.setAuthorByID(author_id);
        this.setRoom_nr(room_nr);
    }
    
    /**
     * Public getter for the id property
     * @return The value of the id property
     */
    public int getId() {
        return id;
    }

    /**
     * Public setter for the id property
     * @param id The new value
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Public setter for the author property
     * @param author_id The id of the author for which we set the name
     */
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
    
    /**
     * Public setter for the author_id property
     * @param author The name of the author for which we set the id
     */
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
    
    /**
     * Public method used to store a message to the game database
     * @return 1 if the message was successfully saved. 0 in case of failure
     */
    public int saveToDB(){
        try {
            String query = "INSERT INTO `chat_message` SET "
                + "`user_id`=?,"
                + "`room_nr`=?,"
                + "`message_time`='"+BombermanWSEndpoint.getInstance().getMySQLDateTime()+"',"
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
    
    /**
     * Public getter for the author property
     * @return The value of the author property
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Public setter for the author property
     * @param author The new value
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Public getter for the author_id property
     * @return The value of the author_id property
     */
    public int getAuthor_id() {
        return author_id;
    }

    /**
     * Public setter for the author_id property
     * @param author_id The new value
     */
    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }
    
    /**
     * Public getter for the timestamp property
     * @return The value of the timestamp property
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Public setter for the timestamp property
     * @param timestamp The new value
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Public getter for the message property
     * @return The value of the message property
     */
    public String getMessage() {
        return message;
    }

    /**
     * Public setter for the message property
     * @param message The new value
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Public getter for the room_nr property
     * @return The value of the room_nr property
     */
    public int getRoom_nr() {
        return room_nr;
    }

    /**
     * Public setter for the room_nr property
     * @param room_nr The new value
     */
    public void setRoom_nr(int room_nr) {
        this.room_nr = room_nr;
    }
    
    /**
     * Public method used to export the message for the client
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
