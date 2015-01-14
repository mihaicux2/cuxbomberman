# cuxbomberman

This application uses the Java implementation for the webSockets server to provide an endpoint for the classic ***bomberman*** game.
The whole code was written using **[Netbeans 8.0](https://netbeans.org/downloads/)**

The whole source code can be cloned and opened directly into NetBeans.
In order to run the game, you may need to adjust the source code, changing the line numbered 539 from the file ***src/main/java/com/cux/bomberman/BombermanWSEndpoint.java***, fixing the absolute location for the *firstmap.txt* file.

~~~~~~~
/* change this line to match your absolute path */
map.put(mapNumber, new World("/home/mihaicux/projects/bomberman/maps/firstmap.txt"));
~~~~~~~

You will also need a **[Glassfish](https://glassfish.java.net/)** server to deploy the application. I simply downloaded the FULL version of Netbeans, and it came with all the features needed to directly run this app.

Because the application needs to store login credentials, as well as other properties for the players, you will need a **[MySQL](http://dev.mysql.com/downloads/)** database server.
All the queries can be found in the file named **bomberman.sql**.
The default game admin credentials are as follows:
    Username : **bombermanadmin**
    Password : **bomberman**

All you have to do is to open the project in NetBeans, Clean&Build the project and press Play.
** *Note:* you will have to allow/unblock the default port for the WebServer - 8080**

You are now ready to play with your friends this simple, yet classy and entertaining game.
Just give them your IP address followed by *:8080/bomberman* and they will be ready to connect in no time.
