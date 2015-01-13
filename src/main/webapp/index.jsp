<%@page import="com.cux.bomberman.util.BStringEncrypter"%>
<%@page import="com.cux.bomberman.BombermanWSEndpoint"%>
<!DOCTYPE html>
<html>
    <head>
        <title>AtomicBomberman look-a-like: Java websocket implementation</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="css/main.css">
    </head>
    <body>

        <div class="panel panel-primary" id="stats">
            <div class="panel-heading">
                <h3 class="panel-title">cuxBomberman - GAME STATS</h3>
            </div>
            <div class="panel-body" style="padding:0px;">
                <table style="width:100%;" id="statsTable">
                    <thead>
                        <tr style="font-weight:bold;" class="alert alert-info">
                            <td>Player</td>
                            <td>Kills</td>
                            <td>Deaths</td>
                            <td>Connected (seconds)</td>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="modal fade" id="optionsBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="myModalLabel">CuxBomberman</h4>
                    </div>
                    <div class="modal-body">
                        <div class='header subtitile'>A websocket implementation of the well known Atomic Bomberman</div>
                        <div class='header'>
                            You need to <a href='javascript:BombermanClient.showLoginOptions();'>login</a> or <a href='javascript:BombermanClient.showRegisterOptions();'>register</a> in order to play our fabulous game
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-success" onclick="BombermanClient.showLoginOptions();">Login here!</button>
                        <button type="button" class="btn btn-primary" onclick="BombermanClient.showRegisterOptions();">Register here!</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="loginBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="myModalLabel">LOGIN</h4>
                    </div>
                    <div class="modal-body">
                        <table>
                            <tbody>
                                <tr>
                                    <td width="40%">Username</td>
                                    <td width="60%"><input type="text" name="username" id="username" placeholder="username" /></td>
                                </tr>
                                <tr>
                                    <td width="40%">Password</td>
                                    <td width="60%">
                                        <input type="password" name="password" id="password" placeholder="password" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button type="button" id="loginBtn" class="btn btn-primary" onclick="BombermanClient.login();">Login</button>
                        <button type="button" class="btn btn-success" onclick="BombermanClient.showRegisterOptions();">Register here!</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="registerBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="myModalLabel">Register</h4>
                    </div>
                    <div class="modal-body">
                        <table>
                            <tbody>
                                <tr>
                                    <td width="40%">Email</td>
                                    <td width="60%"><input type="text" name="email2" id="email2" placeholder="you@yourdomain.com" /></td>
                                </tr>
                                <tr>
                                    <td width="40%">Username</td>
                                    <td width="60%"><input type="text" name="username2" id="username2" placeholder="username" /></td>
                                </tr>
                                <tr>
                                    <td width="40%">Password</td>
                                    <td width="60%">
                                        <input type="password" name="password2" id="password2" placeholder="password" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button type="button" id="registerBtn" class="btn btn-primary" onclick="BombermanClient.register();">REGISTER</button>
                        <button type="button" class="btn btn-success" onclick="BombermanClient.showLoginOptions();">Login here!</button>
                    </div>
                </div>
            </div>
        </div>

        <div id="options">
            <div style="position:relative;">
                <img src="images/utils/options.png" onclick="BombermanClient.showNameBox()" />
            </div>
        </div>

        <div id="chatBtn">
            <div style="position:relative;">
                <img src="images/utils/chatBtn.png" onclick="BombermanClient.showChatLogBox()" />
            </div>
        </div>
        
        <div class="modal fade" id="chatLogBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="myModalLabel">CHAT</h4>
                    </div>
                    <div class="modal-body" id="chatLogBoxContent">
                        
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-danger" data-dismiss="modal" aria-hidden="true">Close</button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="modal fade" id="nameBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="myModalLabel">OPTIONS</h4>
                    </div>
                    <div class="modal-body" id="nameBoxContent">
                        <ol>
                            <li>Press SPACE to drop bomb</li>
                            <li>Press SHIFT to detonate bomb - if you have the trigger item</li>
                            <li>Press ENTER to open the chat box</li>
                            <li>Press ENTER to send a message - if the chat box is opened</li>
                            <li>Press ESC to close the chat box - if the chat box is opened</li>
                            <li>Press ESC to close the help box - if the help box is opened</li>
                            <li>Press and hold TAB to see the game stats</li>
                            <li>Press CONSOLE KEY (` | ~) to see the help box ( this menu )</li>
                        </ol>
                    </div>
                    <div class="modal-footer">
                        <b class="panel-title">New name</b>
                        <input type="text" id="name" value="" placeholder="mihaicux" />
                        <input type="button" class="btn btn-primary"  value="Change Name" onclick="BombermanClient.changeName()" />
                        <button type="button" class="btn btn-danger" onclick="BombermanClient.quit();">QUIT</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="alertBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-body">
                        <div id="alertContent" class="alert alert-danger"></div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-danger" data-dismiss="modal" aria-hidden="true">OK</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="confirmBox" tabindex="-1" role="dialog" aria-labelledby="basicModal" aria-hidden="true" data-backdrop="static" data-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-body">
                        <p id="confirmMessage">Any confirmation message?</p>
                    </div>
                    <div class="modal-footer">
                        <button class="btn" id="confirmFalse">Cancel</button>
                        <button class="btn btn-primary" id="confirmTrue">OK</button>
                    </div>
                </div>
            </div>
        </div>

        <div style="position:relative;">
            <div id="world"></div>
            <div id="chatBox">
                <h3 style="margin-top:3px;">
                    <span class="label label-success">cuxBomberman - chat system</span>
                </h3>
                <textarea id="chatMessage" style="width:100%; height:55px;"></textarea>
                <input type="button" class="btn btn-sm btn-info" value="SEND" onclick="BombermanClient.sendMessage()" />
                <input type="button" class="btn btn-sm btn-danger" value="CLOSE BOX" onclick="BombermanClient.closeChatBox()" />
            </div>
        </div>
        <div class="messages"></div>
        <script type="text/javascript" src="js/jquery.min.js"></script>
        <script src="bootstrap/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="js/jquery.blockUI.js"></script>
        <script type="text/javascript" src="js/websocket.js"></script>
        
        <%
        String ipAddress  = request.getHeader("X-FORWARDED-FOR");  
        if(ipAddress == null)  
        {  
          ipAddress = request.getRemoteAddr();  
        }
        
        BStringEncrypter desEncrypter = new BStringEncrypter(BombermanWSEndpoint.passKey);
        ipAddress = desEncrypter.encrypt(ipAddress).replace("/", "__slash__");
        
        %>
        
        <script>
            var mouseX = 0;
            var mouseY = 0;
            var precMouseX = 0;
            var precMouseY = 0;

            jQuery(document).ready(function () {
                BombermanClient.host += "<% out.print(ipAddress); %>";
                BombermanClient.init();
                hideMouse = setInterval("checkIdleMouse()", 1000);
                jQuery('body').mousemove(function (event) {
                    //console.log("move");
                    mouseX = event.clientX || event.pageX;
                    mouseY = event.clientY || event.pageY;
                    if (precMouseX != mouseX || precMouseY != mouseY) {
                        jQuery("body").css("cursor", "auto");
                    }
                    precMouseX = mouseX;
                    precMouseY = mouseY;
                });

                jQuery("#username2, #password2, #email2").keydown(function (e) {
                    if (e.keyCode == 13) {
                        jQuery("#registerBtn").trigger("click");
                        e.preventDefault();
                    }
                });

                jQuery("#username, #password").keydown(function (e) {
                    if (e.keyCode == 13) {
                        jQuery("#loginBtn").trigger("click");
                        e.preventDefault();
                    }
                });

                jQuery("#chatLogBoxContent").scroll(function(e){
                    if (BombermanClient.firstMessage == false) return;
                    var target = e.currentTarget,
                        scrollTop = target.scrollTop || document.getElementById("chatLogBoxContent").pageYOffset,
                        scrollHeight = target.scrollHeight || document.getElementById("chatLogBoxContent").scrollHeight;
//                    if (scrollHeight - scrollTop === $(target).innerHeight()) {
//                      BombermanClient.log("End of scroll");
//                    }
                    if (scrollTop == undefined || scrollTop == 0){
                        try {
                            jQuery("#chatLogBoxContent").prepend("<div id='loadingImg'>Loading older messages</div>");
                            BombermanClient.socket.send("getChat "+BombermanClient.firstMessage);
//                            BombermanClient.log("getChat "+BombermanClient.firstMessage);
                        } catch (ex) {
                            BombermanClient.log(ex);
                        }                        
                    }
                });

            });

            function checkIdleMouse() {
                if (mouseX == precMouseX && mouseY == precMouseY) {
                    jQuery("body").css("cursor", "none");
                }
            }
        </script>
        
    </body>
</html>
