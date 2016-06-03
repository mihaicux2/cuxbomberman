// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
// console key (` | ~) : 192

var BombermanClient = {};

{
    BombermanClient.options = options;
    BombermanClient.posX = 0;
    BombermanClient.posY = 0;
    BombermanClient.MOVE_UP = false;
    BombermanClient.MOVE_DOWN = false;
    BombermanClient.MOVE_LEFT = false;
    BombermanClient.MOVE_RIGHT = false;
    BombermanClient.INC_SPEED = false;
    BombermanClient.FIRE = false;
    BombermanClient.DETONATE = false;
    BombermanClient.brickLength = 0;
    BombermanClient.walking = false;
    BombermanClient.charNames;
    BombermanClient.timers = {};
    BombermanClient.charID = "";
    BombermanClient.stats = {};
    BombermanClient.blinking = {};
    BombermanClient.chatBoxOpen = false;
    BombermanClient.host = "ws://" + document.location.host + document.location.pathname + "bombermanendpoint/";
    BombermanClient.socket = {};
    BombermanClient.timer = null;
    BombermanClient.charNames = null;
    BombermanClient.changingName = false;
    BombermanClient.ready = false;
    BombermanClient.currentPlayer = {};
    BombermanClient.typeCommand = false;
    BombermanClient.isAdmin = false;
    BombermanClient.isBanned = false;
    BombermanClient.commands = new Array();
    BombermanClient.stackInit = false;
    BombermanClient.stackHead = 0;
    BombermanClient.firstMessage =false;
    
}

BombermanClient.get_random_color = function () {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.round(Math.random() * 15)];
    }
    return color;
}

BombermanClient.unbindKeyDown = function () {
    jQuery(window).unbind("keydown");
}

BombermanClient.unbindKeyUp = function () {
    jQuery(window).unbind("keyup");
}

BombermanClient.bindKeyDown = function () {
    jQuery(window).keydown(function (e) {
//        console.log(e.keyCode);
        switch (e.keyCode) {
            case 38:  // KEY_UP
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_UP = true;
                BombermanClient.MOVE_DOWN = false;
                break;
            case 40:  // KEY_DOWN
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_DOWN = true;
                BombermanClient.MOVE_UP = false;
                break;
            case 37:  // KEY_LEFT
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_LEFT = true;
                BombermanClient.MOVE_RIGHT = false;
                break;
            case 39:  // KEY_RIGHT
               if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_RIGHT = true;
                BombermanClient.MOVE_LEFT = false;
                break;
            case 16: // SHIFT
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                //INC_SPEED = true;
                BombermanClient.DETONATE = true;
                break;
            case 13: // ENTER
                if (jQuery("#nameBox").css("display") != "none") {
                    BombermanClient.changingName = true;
                    break;
                }
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.showChatBox();
                break;
            case 32: // SPACE
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                //DETONATE = true;
                BombermanClient.FIRE = true;
                break;
            case 27: // ESC
                if (jQuery("#nameBox").css("display") != "none") {
                    BombermanClient.hideNameBox();
                    break;
                }
                BombermanClient.closeChatBox();
                BombermanClient.hideLogChatBox();
                break;
            case 9: // TAB
                BombermanClient.showStats();
                e.preventDefault();
                break;
//            case 192: // CONSOLE KEY (` | ~)
//                BombermanClient.showNameBox();
//                e.preventDefault();
//                break;
        }
    });
}

BombermanClient.bindKeyUp = function () {
    jQuery(window).keyup(function (e) {
        switch (e.keyCode) {
            case 38:  // KEY_UP
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_UP = false;
                break;
            case 40:  // KEY_DOWN
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_DOWN = false;
                break;
            case 37:  // KEY_LEFT
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_LEFT = false;
                break;
            case 39:  // KEY_RIGHT
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                BombermanClient.MOVE_RIGHT = false;
                break;
            case 16: // SHIFT
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                //INC_SPEED = false;
                BombermanClient.DETONATE = false;
                break;
            case 13: // ENTER
                if (BombermanClient.typeCommand == true) {
                    break;
                }
                if (BombermanClient.changingName == true) {
                    BombermanClient.changingName = false;
                    if (BombermanClient.changeName()) {
                        BombermanClient.showNameBox();
                    }
                    break;
                }
                if (BombermanClient.chatBoxOpen) {
                    BombermanClient.sendMessage();
                    break;
                }
                BombermanClient.showChatBox();
                break;
            case 32: // SPACE
                if (BombermanClient.chatBoxOpen || jQuery("#nameBox").css("display") != "none")
                    break;
                //DETONATE = false;
                BombermanClient.FIRE = false;
                break;
            case 27: // ESC
                if (jQuery("#nameBox").css("display") != "none") {
                    BombermanClient.hideNameBox();
                    break;
                }
                BombermanClient.closeChatBox();
                break;
            case 9: // TAB
                BombermanClient.closeStats();
                e.preventDefault();
                break;
            case 192: // CONSOLE KEY (` | ~)
                BombermanClient.showNameBox();
                e.preventDefault();
                break;
        }
    });
}

BombermanClient.init = function () {
    try {
        BombermanClient.socket = new WebSocket(BombermanClient.host);
        BombermanClient.socket.onopen = function (msg) {
            BombermanClient.log("Welcome - status " + this.readyState); // connected to the server
        }
        BombermanClient.socket.onmessage = function (msg) {
            var op = msg.data.substr(0, msg.data.indexOf(":["));
            var toProc = msg.data.substr(msg.data.indexOf(":[") + 2);
            //console.log(op);
            //socket.close();
            //var x = JSON.parse(toProc);
            switch (op) {
                case "banned":
                    BombermanClient.hideGameOptions();
                    BombermanClient.isBanned = true;
                    BombermanClient.socket.close();
                    BombermanClient.socket = null;
                    jQuery("#world").html("");
                    BombermanClient.log("connection closed");
                    clearInterval(BombermanClient.timer);
                    BombermanClient.alert("You are banned from this site!");
                    break;
                case "getip":
                    BombermanClient.sendIP();
                    break;
                case "ready":
                    BombermanClient.hideLoginOptions();
                    BombermanClient.initGame();
                    break;
                case "alreadyTaken":
                    BombermanClient.alreadyTaken();
                    break;
                case "invalidAddress":
                    BombermanClient.invalidEmailAddress();
                    break;
                case "registerFailed":
                    BombermanClient.showRegisterFailed();
                    break;
                case "registerSuccess":
                    BombermanClient.registrationSuccess();
                    break;
                case "loginFirst":
                    BombermanClient.showGameOptions();
                    break;
                case "loginFailed":
                    BombermanClient.showLogInFailed();
                    break;
                case "map":
                    BombermanClient.renderMap(toProc);
                    break;
                case "chars":
                    BombermanClient.renderChars(toProc);
                    break;
                case "bombs":
                    BombermanClient.renderBombs(toProc);
                    break;
                case "explosions":
                    BombermanClient.renderExplosions(toProc);
                    break;
                case "items":
                    BombermanClient.renderItems(toProc);
                    break;
                case "blownWalls":
                    BombermanClient.removeWalls(toProc);
                    break;
                case "sound":
                    BombermanClient.playSound(toProc);
                    break;
                case "msg":
                    BombermanClient.showMessage(toProc);
                    break;
                case "admin":
                    BombermanClient.makeAdmin();
                    break;
                case "notadmin":
                    BombermanClient.notAdmin();
                    break;
                case "fullmap":
                    BombermanClient.fullMap();
                    break;
                case "invalidmap":
                    BombermanClient.invalidMap();
                    break;
                case "status":
                    BombermanClient.writeStatus(toProc);
                    break;
                case "chatLog":
                    BombermanClient.writeChatLog(toProc);
                    break;
            }
        }
        BombermanClient.socket.onclose = function (msg) {
            BombermanClient.log("Disconnected - status " + this.readyState);
            if (!BombermanClient.isBanned){
                BombermanClient.alert("Connection closed");
            }
            jQuery("#chatUsers").html("");
            clearInterval(BombermanClient.timer);
            BombermanClient.unbindKeyDown();
            BombermanClient.unbindKeyUp();
            if (!BombermanClient.isBanned){
                BombermanClient.init();
            }
        }
        //BombermanClient.showNameBox();
        jQuery(window).onclose(function () {
            clearInterval(BombermanClient.timer)
            BombermanClient.socket.send("QUIT");
        });
    }
    catch (ex) {
        this.log(ex);
    }
}

BombermanClient.initGame = function () {
    setTimeout("BombermanClient.getMap()", 100);
    BombermanClient.bindKeyDown();
    BombermanClient.bindKeyUp();
    BombermanClient.timer = setInterval("BombermanClient.updateStatus()", 10); // send requests every 10 miliseconds => limit to 100 FPS (at most)
    jQuery("#options, #chatBtn").css("display", "");
}

BombermanClient.writeStatus = function (msg) {
    jQuery("#consoleStatus").html(msg.replace(/\n/g, "<br />"));
}

BombermanClient.writeChatLog = function (toProc){
//    BombermanClient.log(toProc);
    BombermanClient.firstMessage = false;
    jQuery("#loadingImg").remove();
    try{
        var chatMsgs = JSON.parse(toProc);
//        BombermanClient.log(chatMsgs);
        var str = "";
        for (i in chatMsgs){
            var chatMsg = chatMsgs[i];
            if (!BombermanClient.firstMessage) BombermanClient.firstMessage = chatMsg["id"];
            str += "<div class='chat_message'>";
            str += "<b>"+chatMsg["timestamp"].substr(0, 19)+", "+chatMsg["author"]+"</b> : "+chatMsg["message"];
            str += "</div>";
        }
        precStr = jQuery.trim(jQuery("#chatLogBoxContent").text());
        if (jQuery("#chatLogBoxContent").css("display") != "none"){
            jQuery("#chatLogBoxContent").prepend(str);
        }
        else{
            jQuery("#chatLogBoxContent").html(str);
        }
        if (precStr == ""){
            setTimeout("BombermanClient.scrollChatLog()", 200);
        }
//        BombermanClient.log(precStr);
    } catch (ex) {
        BombermanClient.log(ex);
    }
}

BombermanClient.scrollChatLog = function () {
//    BombermanClient.log("scrolling...");
    document.getElementById("chatLogBoxContent").scrollTop = document.getElementById("chatLogBoxContent").scrollHeight;
}

BombermanClient.sendAdminCommand = function (command) {
    var command = jQuery.trim(jQuery("#adminConsole").val());
    if (command == "") {
        BombermanClient.writeStatus("Enter a command before sending it to the server");
        return;
    }
    try {
        if (command != BombermanClient.commands[BombermanClient.commands.length-1]){
            BombermanClient.commands.push(command);
        }
        BombermanClient.socket.send(command);
    } catch (ex) {
        BombermanClient.log(ex);
    }
    jQuery("#adminConsole").val("");
}

BombermanClient.makeAdmin = function () {
    BombermanClient.log("you are now admin");
    BombermanClient.isAdmin = true;
    var str = "";
    str += "<div class='well' style='padding-top: 5px; font-family: verdana;'>";
    str += "<div class='consoleDiv'>";
    str += "<span id='adminConsoleSpan'></span>";
    str += "<input type='text' id='adminConsole' class='consoleInput' />";
    str += "<input type='button' class='btn btn-xs btn-success' id='sendAdminCommand' onClick='BombermanClient.sendAdminCommand()' value='Send' />";
    str += "</div>";
    str += "<div style='width:100%; height:100px; margin-top: 5px; word-wrap: break-word; overflow: auto;' id='consoleStatus'>TYPE `help` to see the available commands</div>";
    str += '</div>';
    jQuery("#nameBoxContent").append(str);

    jQuery("#adminConsole").keydown(function (e) {
        if (e.keyCode == 13) { // ENTER
            BombermanClient.typeCommand = true;
            e.preventDefault();
            return false;
        }
        
    });

    jQuery("#adminConsole").keyup(function (e) {
        if (e.keyCode == 13) { // ENTER
            BombermanClient.sendAdminCommand();
            BombermanClient.typeCommand = false;
            BombermanClient.stackInit = false;
            BombermanClient.stackHead = 0;
            e.preventDefault();
            return false;
        }
        
        switch (e.keyCode){
            case 13: // ENTER
                BombermanClient.sendAdminCommand();
                BombermanClient.typeCommand = false;
                BombermanClient.stackInit = false;
                BombermanClient.stackHead = 0;
                e.preventDefault();
                return false;
                break; // >:) troll
            case 38:  // KEY_UP
                if (BombermanClient.commands.length > 0){
                    if (!BombermanClient.stackInit){
                        BombermanClient.stackInit = true;
                        BombermanClient.stackHead = BombermanClient.commands.length-1;
                    }
                    jQuery("#adminConsole").val(BombermanClient.commands[BombermanClient.stackHead]);
                    if (BombermanClient.stackHead > 0){
                        BombermanClient.stackHead--;
                    }
                }
                break;
            case 40:  // KEY_DOWN
                if (BombermanClient.commands.length > 0){
                    if (!BombermanClient.stackInit){
                        BombermanClient.stackInit = true;
                        BombermanClient.stackHead = BombermanClient.commands.length-1;
                    }
                    jQuery("#adminConsole").val(BombermanClient.commands[BombermanClient.stackHead]);
                    if (BombermanClient.stackHead < BombermanClient.commands.length - 1){
                        BombermanClient.stackHead++;
                    }
                }
                break;
        }
        
    });

}

BombermanClient.notAdmin = function () {
    BombermanClient.alert("You are not admin!");
}

BombermanClient.fullMap = function () {
    BombermanClient.alert("The current map if full!");
}

BombermanClient.invalidMap = function () {
    BombermanClient.alert("The selected map does not exist");
}

BombermanClient.showLogInFailed = function () {
    BombermanClient.alert("Login Failed");
}

BombermanClient.showRegisterFailed = function () {
    BombermanClient.alert("Login Failed");
}

BombermanClient.alreadyTaken = function () {
    BombermanClient.alert("This email address/username is already in use");
}

BombermanClient.login = function () {
    var username = jQuery.trim(jQuery("#username").val());
    var password = jQuery.trim(jQuery("#password").val());
    if (username.length == 0 || password.length == 0) {
        BombermanClient.alert("Enter credentials!");
        jQuery("#username").focus();
        return;
    }
    var loginMsg = "login " + btoa(username) + "#" + btoa(password);
    BombermanClient.log(loginMsg);
    try {
        BombermanClient.socket.send(loginMsg);
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
}

BombermanClient.register = function () {
    var email = jQuery.trim(jQuery("#email2").val());
    var username = jQuery.trim(jQuery("#username2").val());
    var password = jQuery.trim(jQuery("#password2").val());
    if (email.length == 0 || username.length == 0 || password.length == 0) {
        BombermanClient.alert("Enter credentials!");
        jQuery("#email2").focus();
        return;
    }
    var pattern = /^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
    if (!pattern.test(email)) {
        BombermanClient.invalidEmailAddress();
        return;
    }
    var registerMsg = "register " + btoa(username) + "#" + btoa(password) + "#" + btoa(email);
    BombermanClient.log(registerMsg);
    try {
        BombermanClient.socket.send(registerMsg);
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
}

BombermanClient.invalidEmailAddress = function () {
    BombermanClient.alert("Enter a valid email address!");
    jQuery("#email2").focus();
}

BombermanClient.registrationSuccess = function () {
    BombermanClient.alert("Registration success! You can now login with your username and password");
    BombermanClient.showLoginOptions();
}

BombermanClient.showChatBox = function () {
    jQuery("#chatBox").css("display", "inline");
    jQuery("#chatMessage").val("");
    jQuery("#chatMessage").focus();
    BombermanClient.chatBoxOpen = true;
}

BombermanClient.closeChatBox = function () {
    jQuery("#chatBox").css("display", "none");
    BombermanClient.chatBoxOpen = false;
}

BombermanClient.showStats = function () {
    jQuery("#stats").css("display", "block");
}

BombermanClient.closeStats = function () {
    jQuery("#stats").css("display", "none");
}

BombermanClient.showNameBox = function () {
    BombermanClient.hideRegisterOptions();
    BombermanClient.hideLoginOptions();
    BombermanClient.hideGameOptions();
    jQuery('#nameBox').modal('toggle');
    BombermanClient.stackInit = false;
    BombermanClient.stackHead = 0;
}

/**
 * You first need to create a formatting function to pad numbers to two digits…
 **/
BombermanClient.twoDigits = function(d) {
    if(0 <= d && d < 10) return "0" + d.toString();
    if(-10 < d && d < 0) return "-0" + (-1*d).toString();
    return d.toString();
}

/**
 * …and then create the method to output the date string as desired.
 * Some people hate using prototypes this way, but if you are going
 * to apply this to more than one Date object, having it as a prototype
 * makes sense.
 **/
Date.prototype.toMysqlFormat = function() {
    return this.getUTCFullYear() + "-" + BombermanClient.twoDigits(1 + this.getUTCMonth()) + "-" + BombermanClient.twoDigits(this.getUTCDate()) + " " + BombermanClient.twoDigits(this.getUTCHours()) + ":" + BombermanClient.twoDigits(this.getUTCMinutes()) + ":" + BombermanClient.twoDigits(this.getUTCSeconds());
};

BombermanClient.showChatLogBox = function () {
    jQuery("#chatLogBoxContent").html("");
    if(jQuery('#chatLogBox').css("display") == "none"){
        try {
            BombermanClient.socket.send("getChat 0");
        } catch (ex) {
            BombermanClient.log(ex);
        }
    }
    jQuery('#chatLogBox').modal('toggle');
}

BombermanClient.hideNameBox = function () {
    jQuery("#nameBox").modal("hide");
}

BombermanClient.showGameOptions = function () {
    BombermanClient.hideRegisterOptions();
    BombermanClient.hideLoginOptions();
    BombermanClient.hideNameBox();
    BombermanClient.closeChatBox();
    BombermanClient.hideLogChatBox();
    jQuery("#options, #chatBtn").css("display", "none");
    jQuery('#optionsBox').modal('show');
}

BombermanClient.hideGameOptions = function () {
    jQuery('#optionsBox').modal('hide');
}

BombermanClient.hideLogChatBox = function () {
    jQuery('#chatLogBox').modal('hide');
}

BombermanClient.showLoginOptions = function () {
    BombermanClient.hideRegisterOptions();
    BombermanClient.hideGameOptions();
    jQuery('#loginBox').modal('show');
    jQuery("#username").val("");
    jQuery("#password").val("");
    jQuery("#username").focus();
}

BombermanClient.hideLoginOptions = function () {
    jQuery('#loginBox').modal('hide');
}

BombermanClient.showRegisterOptions = function () {
    BombermanClient.hideLoginOptions();
    BombermanClient.hideGameOptions();
    jQuery("#registerBox").modal('show');
    jQuery("#email2").val("");
    jQuery("#username2").val("");
    jQuery("#password2").val("");
    jQuery("#email2").focus();

}

BombermanClient.hideRegisterOptions = function () {
    jQuery("#registerBox").modal('hide');
}

BombermanClient.log = function (msg) {
    console.log(msg);
}

BombermanClient.playSound = function (file) {
    var audio = document.getElementById("sound_" + file);
    if (audio == undefined) {
        jQuery('<audio id="sound_' + file + '" src="' + file + '" />').appendTo('body');
        audio = document.getElementById("sound_" + file);
    }
    audio = document.getElementById("sound_" + file);
    if (audio.paused == false)
        return;
    //audio.volume = .3;
    audio.play();
}

BombermanClient.stopSound = function (file) {
    var audio = document.getElementById("sound_" + file);
    if (audio != undefined)
        audio.pause();
}

BombermanClient.removeWalls = function (toProc) {
    items = toProc.split("[#brickSep#]");
    var last = items.length;
    var idx = 0;
    for (i in items) {
        idx++;
        if (idx == last)
            break;
        var item = items[i];
        jQuery("#brick_" + item).remove();
    }
}

BombermanClient.renderItems = function (toProc) {
    jQuery(".item").remove();
    //console.log(toProc);
    items = toProc.split("[#itemSep#]");
    var last = items.length;
    var idx = 0;
    for (i in items) {
        idx++;
        if (idx == last)
            break;
        try {
            item = JSON.parse(items[i]);
            str = "<div class='item' style='position:absolute; top:" + item.posY + "px; left:" + item.posX + "px;'><img src='images/items/" + item.texture + "' width='" + item.width + "' height='" + item.height + "' /></div>";
            jQuery("#world").append(str);
            //console.log(item);
        }
        catch (ex) {
            BombermanClient.log(ex);
        }
    }
}

BombermanClient.renderBombs = function (toProc) {
    jQuery(".bomb").remove();
    bombs = toProc.split("[#bombSep#]");
    var last = bombs.length;
    var idx = 0;
    for (i in bombs) {
        idx++;
        if (idx == last)
            break;
        try {
            bomb = JSON.parse(bombs[i]);
            str = "<div class='bomb' style='position:absolute; top:" + bomb.posY + "px; left:" + bomb.posX + "px;'><img src='images/characters/19.gif' width='" + bomb.width + "' height='" + bomb.height + "' /></div>";
            jQuery("#world").append(str);
        }
        catch (ex) {
            BombermanClient.log(ex);
        }
    }
}

BombermanClient.renderExplosions = function (toProc) {
    //jQuery(".exp").remove();
    exps = toProc.split("[#explosionSep#]");
    var last = exps.length;
    var idx = 0;
    for (i in exps) {
        idx++;
        if (idx == last)
            break;
        try {
            exp = JSON.parse(exps[i]);
            str = "<div class='exp' style='z-index:999; position:absolute; font-size:3px; background:#ff9900; top:" + exp.posY + "px; left:" + exp.posX + "px; width:" + exp.width + "px; height:" + exp.height + "px;'>&nbsp;</div>";
            var ii = 0;
            for (j in exp.directions) {
                ii++;
                //console.log(exp.directions[j]);
                var posX = exp.posX;
                var posY = exp.posY;
                switch (exp.directions[j]) {
                    case "up":
                        posY -= exp.height * exp.ranges.up;
                        break;
                    case "down":
                        posY += exp.height;
                        break;
                    case "left":
                        posX -= exp.width * exp.ranges.left;
                        break;
                    case "right":
                        posX += exp.width;
                        break;
                    default:
                        continue;
                }
                str += "<div class='exp extra' direction='" + exp.directions[j] + "' style='border:1px solid #ff9900; z-index:999; position:absolute; font-size:3px; background:#ff9900; top:" + posY + "px; left:" + posX + "px; width:" + exp.width + "px; height:" + exp.height + "px;'>&nbsp;</div>";
            }
            jQuery("#world").append(str);
            //console.log(exp);
            jQuery(".extra").each(function (index) {
                var op = "";
                var sign = ""; // retract
                var sign2 = ""; // expand
                var scale = "";
                switch (jQuery(this).attr("direction")) {
                    case "up":
                        op = "top";
                        sign = "+=" + exp.height * (exp.ranges.up) + "px";
                        sign2 = "-=" + exp.height * (exp.ranges.up) + "px";
                        scale = "height";
                        break;
                    case "down":
                        op = "top";
                        sign = "-=" + exp.height * (exp.ranges.down - 1) + "px";
                        sign2 = "+=" + exp.height * (exp.ranges.down - 1) + "px";
                        scale = "height";
                        break;
                    case "left":
                        op = "left";
                        sign = "+=" + exp.width * (exp.ranges.left) + "px";
                        sign2 = "-=" + exp.width * (exp.ranges.left) + "px";
                        scale = "width";
                        break;
                    case "right":
                        op = "left";
                        sign = "-=" + exp.width * (exp.ranges.right - 1) + "px";
                        sign2 = "+=" + exp.width * (exp.ranges.right - 1) + "px";
                        ;
                        scale = "width";
                        break;
                }
                var animation = "";
                if (scale == "height") {
                    if (jQuery(this).attr("direction") == "up") {
                        jQuery(this).css("border-top-left-radius", "10px");
                        jQuery(this).css("border-top-right-radius", "10px");
                        jQuery(this).css("height", exp.height * (exp.ranges.up) + "px");
                    }
                    else { // down
                        jQuery(this).css("border-bottom-left-radius", "10px");
                        jQuery(this).css("border-bottom-right-radius", "10px");
                        jQuery(this).css("height", exp.height * (exp.ranges.down) + "px");
                    }
                    jQuery(this).css("width", exp.width + "px");
                    //animation  = 'jQuery(this).animate({"'+scale+'":"'+exp.height*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 200)';
                }
                else {
                    if (jQuery(this).attr("direction") == "left") {
                        jQuery(this).css("border-top-left-radius", "10px");
                        jQuery(this).css("border-bottom-left-radius", "10px");
                        jQuery(this).css("width", exp.width * (exp.ranges.left) + "px");
                    }
                    else { // right
                        jQuery(this).css("border-top-right-radius", "10px");
                        jQuery(this).css("border-bottom-right-radius", "10px");
                        jQuery(this).css("width", exp.width * (exp.ranges.right) + "px");
                    }
                    jQuery(this).css("height", exp.height + "px");
                    //animation  = 'jQuery(this).animate({"'+scale+'":"'+exp.width*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 600)';
                }
                var animation2 = 'jQuery(this).animate({"' + scale + '":"0px", "' + op + '":"' + sign + '"}, 300)';
                //eval(animation);
                eval(animation2);
                setTimeout('jQuery(".exp").remove();', 300);
                //console.log(animation2);
            });
        }
        catch (ex) {
            BombermanClient.log(ex);
        }
    }
}

BombermanClient.renderMap = function (toProc) {
    //console.log(toProc);
    jQuery.blockUI({message: "<p>Please wait for the map to load</p>"});
    var dims = toProc.substr(0, toProc.indexOf("[#walls#]")).split("x");
    var worldWidth = parseInt(dims[0]);
    if (!worldWidth)
        worldWidth = 660;
    var worldHeight = parseInt(dims[1]);
    if (!worldWidth)
        worldHeight = 510;
    jQuery("#world").css("width", worldWidth + "px");
    jQuery("#world").css("height", worldHeight + "px");
    //console.log(dims);
    toProc = toProc.substr(toProc.indexOf("[#walls#]") + 9 /*"[#walls#]".length*/);
    bricks = toProc.split("[#wallSep#]");
    //if (bricks.length && BombermanClient.brickLength == bricks.length){
    //    jQuery.unblockUI();
    //    setTimeout('jQuery.unblockUI()', 200);
    //    return;
    //}
    BombermanClient.brickLength = bricks.length;
    jQuery(".brick").remove();
    var last = bricks.length;
    var idx = 0;
    for (i in bricks) {
        idx++;
        if (idx == last)
            break;
        try {
            brick = JSON.parse(bricks[i]);
            str = "<div class='brick' id='brick_" + brick.wallId + "' style='position:absolute; top:" + brick.posY + "px; left:" + brick.posX + "px;' alt='" + brick.name + "'><img src='images/walls/" + brick.texture + "' width='" + brick.width + "' height='" + brick.height + "' /></div>";
            jQuery("#world").append(str);
        }
        catch (ex) {
            BombermanClient.log(ex);
        }
    }
    BombermanClient.log("ready");
    BombermanClient.sendReadyMessage();
    jQuery.unblockUI();
    setTimeout('jQuery.unblockUI()', 200);
}

BombermanClient.sendReadyMessage = function () {
    try {
        BombermanClient.socket.send("ready");
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
}

BombermanClient.renderChars = function (toProc) {

    BombermanClient.stats = {};
    BombermanClient.charID = toProc.substr(0, toProc.indexOf("[#chars#]"));
    toProc = toProc.substr(toProc.indexOf("[#chars#]") + 9 /*"[#chars#]".length*/);
    chars = toProc.split("[#charSep#]");
    var last = chars.length;
    var idx = 0;
    BombermanClient.charNames = new Array();
    for (i in chars) {
        idx++;
        if (idx == last)
            break;
        try {
            var x = JSON.parse(chars[i]);
            //console.log(x);
            BombermanClient.stats[x.id] = {};
            BombermanClient.stats[x.id]["kills"] = x.kills;
            BombermanClient.stats[x.id]["deaths"] = x.deaths;
            BombermanClient.stats[x.id]["connectionTime"] = x.connectionTime;
            BombermanClient.stats[x.id]["name"] = x.name;

            if (x.id == BombermanClient.charID) {
                BombermanClient.currentPlayer = x;
                jQuery("#adminConsoleSpan").html(BombermanClient.currentPlayer.name + "@cuxBomberman:~$ ");
            }

//           log(x);
            if (jQuery("#char_" + x.id).length > 0) {
                var ob = jQuery("#char_" + x.id);

                if (ob.css("top") != (x.posY + "px")) {
                    ob.css("top", x.posY + "px");
                }
                if (ob.css("left") != (x.posX + "px")) {
                    ob.css("left", x.posX + "px");
                }

                var crtImg = ob.find("img").attr("src").substr(ob.find("img").attr("src").lastIndexOf("/") + 1);
                if (crtImg != (x.crtTexture + ".gif")) {
                    ob.find("img").attr("src", "images/characters/" + x.crtTexture + ".gif");
                    ob.find("canvas").hide();
                    ob.find("img").show();
                }
                else {
                    // "stop" image only if character is not walking and it's state is "Normal" or "Bomb"
                    if (!x.walking && (x.state == "Normal" || x.state == "Bomb")) {
                        ob.find("canvas")[0].getContext("2d").clearRect(0, 0, x.width, x.height);
                        ob.find("canvas")[0].getContext("2d").drawImage(ob.find("img")[0], 0, 0, x.width, x.height);
                        ob.find("img").hide();
                        ob.find("canvas").show();
                    }
                    else {
                        if (ob.find("img").css("display") == "none") {
                            ob.find("canvas").hide();
                            ob.find("img").show();
                        }
                    }
                }
            }
            else {
//                console.log(x.id);
                str = "<div id='char_" + x.id + "' class='character' style='position:absolute; top:" + x.posY + "px; left:" + x.posX + "px;' alt='" + x.name + "' title='" + x.name + "'><img src='images/characters/" + x.crtTexture + ".gif' width='" + x.width + "' height='" + x.height + "' /><canvas style='display:none;' width='" + x.width + "' height='" + x.height + "'></canvas></div>";
                jQuery("#world").append(str);
            }

            BombermanClient.charNames.push("char_" + x.id);
            if (x.ready == false) {
                if (!ob.hasClass("blinking")) {
                    BombermanClient.blinking[x.id] = true;
                    ob.addClass("blinking");
                    BombermanClient.blinkChar(x.id, true);
                }
            }
            else {
                if (ob.hasClass("blinking")) {
                    ob.removeClass("blinking");
                    BombermanClient.unblinkChar(x.id);
                }
            }
        }
        catch (ex) {
            BombermanClient.log(ex);
        }
    }
    //console.log(charNames);
    jQuery(".character").each(function (idx) {
        if (jQuery.inArray(jQuery(this).attr("id"), BombermanClient.charNames) == -1) {
            jQuery(this).remove();
        }
    });
    BombermanClient.renderStats(BombermanClient.charID);
}

BombermanClient.blinkChar = function (charID, hide) {
    if (BombermanClient.blinking[charID]) {
        var elem = jQuery("#char_" + charID);
        if (hide) {
            elem.fadeOut(500);
        }
        else {
            elem.fadeIn(500);
        }
        setTimeout('BombermanClient.blinkChar("' + charID + '", ' + !hide + ')', 500);
    }
}

BombermanClient.unblinkChar = function (charID) {
    var elem = jQuery("#char_" + charID);
    BombermanClient.blinking[charID] = false;
    elem.fadeIn('fast');
}

BombermanClient.renderStats = function (charID) {
    jQuery(".stat").remove();
    var str = "";
    for (i in this.stats) {
        var cls = "";
        if (i == charID) {
            cls = "alert alert-danger";
        }
        var x = BombermanClient.stats[i];
        str += "<tr class='stat " + cls + "' style='padding:2px; margin:0px;'>";
        str += "<td>" + x.name + "</td>";
        str += "<td>" + x.kills + "</td>";
        str += "<td>" + x.deaths + "</td>";
        str += "<td>" + x.connectionTime + "</td>";
        str += "</tr>";
    }
    jQuery("#statsTable").append(str);
}

BombermanClient.boundNumber = function (nr, lo, hi) {
    if (nr < lo)
        nr = lo;
    if (nr > hi)
        nr = hi;
    return nr;
}

BombermanClient.centerMap = function (id) {
    var viewportWidth = jQuery(window).width();
    viewportHeight = jQuery(window).height();
    $foo = jQuery('#char_' + id);
    if ($foo.length == 0)
        return;
    elWidth = $foo.width();
    elHeight = $foo.height();
    elOffset = $foo.offset();
    jQuery(window)
            .scrollTop(elOffset.top + (elHeight / 2) - (viewportHeight / 2))
            .scrollLeft(elOffset.left + (elWidth / 2) - (viewportWidth / 2));
}

BombermanClient.alert = function (msg) {
    jQuery("#alertContent").html(msg);
    jQuery("#alertBox").modal('show');
}

BombermanClient.changeName = function () {
    var name = jQuery.trim(jQuery("#name").val());
    if (!name) {
        BombermanClient.alert("Enter a valid name!");
        return false;
    }
    try {
        BombermanClient.socket.send("name " + name);
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
    return true;
}

BombermanClient.updateStatus = function () {
    BombermanClient.walking = false;
    if (BombermanClient.MOVE_UP) {
        try {
            BombermanClient.socket.send("up");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.walking = true;
    }

    else if (BombermanClient.MOVE_DOWN) {
        try {
            BombermanClient.socket.send("down");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.walking = true;
    }

    else if (BombermanClient.MOVE_LEFT) {
        try {
            BombermanClient.socket.send("left");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.walking = true;
    }

    else if (BombermanClient.MOVE_RIGHT) {
        try {
            BombermanClient.socket.send("right");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.walking = true;
    }

    if (BombermanClient.DETONATE) {
        try {
            BombermanClient.socket.send("detonate");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.DETONATE = !BombermanClient.DETONATE;
    }

    if (BombermanClient.FIRE) {
        try {
            BombermanClient.socket.send("bomb");
        } catch (ex) {
            BombermanClient.log(ex);
        } // request info about the other users
        BombermanClient.FIRE = !BombermanClient.FIRE;
    }

    BombermanClient.centerMap(BombermanClient.charID);

}

BombermanClient.resetMap = function () {
    try {
        BombermanClient.socket.send("reset");
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
}

BombermanClient.canFire = function () {
    BombermanClient.IS_FIRING = false;
}

BombermanClient.getMap = function () {
    try {
        BombermanClient.socket.send("getEnvironment");
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
}

BombermanClient.sendMessage = function () {
    try {
        BombermanClient.socket.send("msg " + jQuery("#chatMessage").val());
    } catch (ex) {
        BombermanClient.log(ex);
    } // request info about the other users
    jQuery("#chatMessage").val("");
    jQuery("#chatMessage").focus();
}

BombermanClient.showMessage = function (message) {
    //log(message);
    var msgID = Math.random().toString(36).slice(2);
    jQuery(".messages").append("<div class='alert alert-info message' id='msg_" + msgID + "'>" + message + "</div>");
    if (jQuery("#chatLogBox").css("display") != "none"){
        jQuery("#chatLogBoxContent").append("<div id=''></div>");
        var str = "";
        str += "<div class='chat_message'>";
        str += "<b>"+new Date().toMysqlFormat()+", </b>"+message;
        str += "</div>";
        jQuery("#chatLogBoxContent").append(str);
    }
    setTimeout("BombermanClient.hideMessage('" + msgID + "')", 3000);
}

BombermanClient.hideMessage = function (msgID) {
    jQuery("#msg_" + msgID).fadeOut('slow');
}

BombermanClient.confirm = function (confirmMessage, callback) {
    confirmMessage = confirmMessage || '';

    jQuery('#confirmBox').modal('show');

    jQuery('#confirmMessage').html(confirmMessage);
    jQuery('#confirmFalse').click(function () {
        jQuery('#confirmBox').modal('hide');
        if (callback)
            callback(false);

    });
    jQuery('#confirmTrue').click(function () {
        jQuery('#confirmBox').modal('hide');
        if (callback)
            callback(true);
    });
}

BombermanClient.quit = function () {
    BombermanClient.confirm('Are you sure you want to exit?', function (result) {
        if (result) {
            BombermanClient.log("Goodbye!");
            try {
                BombermanClient.socket.send("QUIT");
                BombermanClient.socket.close();
                BombermanClient.socket = null;
                jQuery("#world").html("");
                BombermanClient.log("connection closed");
                clearInterval(BombermanClient.timer);
            }
            catch (ex) {
                BombermanClient.log(ex);
            }
        }
    });
}

BombermanClient.sendIP = function (){
    try {
        BombermanClient.socket.send("ip "+BombermanClient.getIP());
    } catch (ex) {
        BombermanClient.log(ex);
    }
}

BombermanClient.getIP = function () {
    var ip = "0.0.0.0";
    $.ajax({
        type: "GET",
        url: "http://api.hostip.info/get_json.php",
        datatype: "application/json",
        contentType: "text/plain",
        async: false,
        success: function (msg) {
//            BombermanClient.log(msg);
            ip = msg.ip;
        },
        error: function (msg) {
            BombermanClient.log(msg);
        }
    });
    return ip;
}
