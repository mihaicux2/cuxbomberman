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
}

BombermanClient.get_random_color = function() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.round(Math.random() * 15)];
    }
    return color;
}

BombermanClient.bindKeyDown = function(){
    jQuery(window).keydown(function(e) {
//        console.log(e.keyCode);
        switch (e.keyCode) {
            case 38:  // KEY_UP
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_UP = true;
                BombermanClient.MOVE_DOWN = false;
                break;
            case 40:  // KEY_DOWN
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_DOWN = true;
                BombermanClient.MOVE_UP = false;
                break;
            case 37:  // KEY_LEFT
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_LEFT = true;
                BombermanClient.MOVE_RIGHT = false;
                break;
            case 39:  // KEY_RIGHT
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_RIGHT = true;
                BombermanClient.MOVE_LEFT = false;
                break;
            case 16: // SHIFT
                if (BombermanClient.chatBoxOpen)
                    break;
                //INC_SPEED = true;
                BombermanClient.DETONATE = true;
                break;
            case 13: // ENTER
                if (jQuery("#nameBox").css("display") != "none"){
                    BombermanClient.changingName = true;
                    break;
                }
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.showChatBox();
                break;
            case 32: // SPACE
                if (BombermanClient.chatBoxOpen)
                    break;
                //DETONATE = true;
                BombermanClient.FIRE = true;
                break;
            case 27: // ESC
                if (jQuery("#nameBox").css("display") != "none"){
                    BombermanClient.hideNameBox();
                    break;
                }
                BombermanClient.closeChatBox();
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

BombermanClient.bindKeyUp = function(){
    jQuery(window).keyup(function(e) {
        switch (e.keyCode) {
            case 38:  // KEY_UP
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_UP = false;
                break;
            case 40:  // KEY_DOWN
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_DOWN = false;
                break;
            case 37:  // KEY_LEFT
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_LEFT = false;
                break;
            case 39:  // KEY_RIGHT
                if (BombermanClient.chatBoxOpen)
                    break;
                BombermanClient.MOVE_RIGHT = false;
                break;
            case 16: // SHIFT
                if (BombermanClient.chatBoxOpen)
                    break;
                //INC_SPEED = false;
                BombermanClient.DETONATE = false;
                break;
            case 13: // ENTER
                if (BombermanClient.changingName == true){
                    BombermanClient.changingName = false;
                    if (BombermanClient.changeName()){
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
                if (BombermanClient.chatBoxOpen)
                    break;
                //DETONATE = false;
                BombermanClient.FIRE = false;
                break;
            case 27: // ESC
                if (jQuery("#nameBox").css("display") != "none"){
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

BombermanClient.init = function(){
    try{
        BombermanClient.socket = new WebSocket(BombermanClient.host);
        BombermanClient.socket.onopen = function(msg){
            BombermanClient.log("Welcome - status "+this.readyState);
            $.blockUI({message:"<p>Please wait for the map to load</p>"});
            setTimeout("BombermanClient.getMap()", 100);
            BombermanClient.bindKeyDown();
            BombermanClient.bindKeyUp();
            BombermanClient.timer = setInterval("BombermanClient.updateStatus()", 10); // send requests every 10 miliseconds => limit to 100 FPS (at most)
        }
        BombermanClient.socket.onmessage = function(msg){
            var op = msg.data.substr(0, msg.data.indexOf(":["));
            var toProc = msg.data.substr(msg.data.indexOf(":[") + 2);
            //console.log(msg);
            //socket.close();
            //var x = JSON.parse(toProc);
            switch (op) {
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
            }
        }
        BombermanClient.socket.onclose = function(msg){
            BombermanClient.log("Disconnected - status "+this.readyState);
            alert("Connection closed");
            jQuery("#chatUsers").html("");
            clearInterval(BombermanClient.timer);
        }
        BombermanClient.showNameBox();
        jQuery(window).onclose(function(){
            BombermanClient.socket.send("QUIT");
        });
    }
    catch(ex){ this.log(ex); }
}

BombermanClient.log = function(msg){
    console.log(msg);
}

BombermanClient.playSound = function(file){
    var audio = document.getElementById("sound_"+file);
    if (audio == undefined){
            jQuery('<audio id="sound_'+file+'" src="'+file+'" />').appendTo('body');
            audio = document.getElementById("sound_"+file);
    }
    audio = document.getElementById("sound_"+file);
    if (audio.paused == false) return;
    //audio.volume = .3;
    audio.play();
}

BombermanClient.stopSound = function(file){
    var audio = document.getElementById("sound_"+file);
    if (audio != undefined) audio.pause();
}

BombermanClient.showNameBox = function(){
    if (jQuery("#nameBox").css("display") == "none"){
        jQuery("#nameBox").css("display", "block");
        jQuery("#name").focus();
        jQuery("#name").val("");
    }
    else{
        BombermanClient.hideNameBox();
    }
}

BombermanClient.hideNameBox = function(){
    jQuery("#nameBox").css("display", "none");
}

BombermanClient.removeWalls = function(toProc){
    items = toProc.split("[#brickSep#]");
    var last = items.length;
    var idx = 0;
    for (i in items){
        idx++;
        if (idx == last) break;
        var item = items[i];
        jQuery("#brick_"+item).remove();
    }
}

BombermanClient.renderItems = function(toProc){
    jQuery(".item").remove();
    //console.log(toProc);
    items = toProc.split("[#itemSep#]");
    var last = items.length;
    var idx = 0;
    for (i in items){
        idx++;
        if (idx == last) break;
        try{
           item = JSON.parse(items[i]);
           str = "<div class='item' style='position:absolute; top:" + item.posY + "px; left:" + item.posX + "px;'><img src='images/items/"+item.texture+"' width='" + item.width + "' height='" + item.height + "' /></div>";
           jQuery("#world").append(str);
           //console.log(item);
        }
        catch(ex){ BombermanClient.log(ex); }
    } 
}

BombermanClient.renderBombs = function(toProc){
    jQuery(".bomb").remove();
    bombs = toProc.split("[#bombSep#]");
    var last = bombs.length;
    var idx = 0;
    for (i in bombs){
        idx++;
        if (idx == last) break;
        try{
           bomb = JSON.parse(bombs[i]);
           str = "<div class='bomb' style='position:absolute; top:" + bomb.posY + "px; left:" + bomb.posX + "px;'><img src='images/characters/19.gif' width='" + bomb.width + "' height='" + bomb.height + "' /></div>";
           jQuery("#world").append(str);
        }
        catch(ex){ BombermanClient.log(ex); }
    } 
}

BombermanClient.renderExplosions = function(toProc){
    //jQuery(".exp").remove();
    exps = toProc.split("[#explosionSep#]");
    var last = exps.length;
    var idx = 0;
    for (i in exps){
        idx++;
        if (idx == last) break;
        try{
           exp = JSON.parse(exps[i]);
           str = "<div class='exp' style='z-index:999; position:absolute; font-size:3px; background:#ff9900; top:" + exp.posY + "px; left:" + exp.posX + "px; width:"+exp.width+"px; height:"+exp.height+"px;'>&nbsp;</div>";
           var ii = 0;
           for (j in exp.directions){
               ii++;
               //console.log(exp.directions[j]);
               var posX = exp.posX;
               var posY = exp.posY;
               switch (exp.directions[j]){
                   case "up":
                       posY -= exp.height*exp.ranges.up;
                       break;
                   case "down":
                       posY += exp.height;
                       break;
                   case "left":
                       posX -= exp.width*exp.ranges.left;
                       break;
                   case "right":
                       posX += exp.width;
                       break;
                   default: continue;
               }
               str += "<div class='exp extra' direction='"+exp.directions[j]+"' style='border:1px solid #ff9900; z-index:999; position:absolute; font-size:3px; background:#ff9900; top:" + posY + "px; left:" + posX + "px; width:"+exp.width+"px; height:"+exp.height+"px;'>&nbsp;</div>";
           }
           jQuery("#world").append(str);
           //console.log(exp);
           jQuery(".extra").each(function(index){
               var op    = "";
               var sign  = ""; // retract
               var sign2 = ""; // expand
               var scale = "";
               switch (jQuery(this).attr("direction")){
                   case "up":
                       op = "top";
                       sign = "+="+exp.height*(exp.ranges.up)+"px";
                       sign2 = "-="+exp.height*(exp.ranges.up)+"px";
                       scale = "height";
                       break;
                   case "down":
                       op = "top";
                       sign = "-="+exp.height*(exp.ranges.down-1)+"px";
                       sign2 = "+="+exp.height*(exp.ranges.down-1)+"px";
                       scale = "height";
                       break;
                   case "left":
                       op = "left";
                       sign = "+="+exp.width*(exp.ranges.left)+"px";
                       sign2 = "-="+exp.width*(exp.ranges.left)+"px";
                       scale = "width";
                       break;
                   case "right":
                       op = "left";
                       sign = "-="+exp.width*(exp.ranges.right-1)+"px";
                       sign2 = "+="+exp.width*(exp.ranges.right-1)+"px";;
                       scale = "width";
                       break;
               }
               var animation = "";
               if (scale == "height"){
                   if (jQuery(this).attr("direction") == "up"){
                       jQuery(this).css("border-top-left-radius", "10px");
                       jQuery(this).css("border-top-right-radius", "10px");
                       jQuery(this).css("height", exp.height*(exp.ranges.up)+"px");
                   }
                   else{ // down
                       jQuery(this).css("border-bottom-left-radius", "10px");
                       jQuery(this).css("border-bottom-right-radius", "10px");
                       jQuery(this).css("height", exp.height*(exp.ranges.down)+"px");
                   }
                   jQuery(this).css("width", exp.width+"px");
                   //animation  = 'jQuery(this).animate({"'+scale+'":"'+exp.height*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 200)';
               }
               else{
                   if (jQuery(this).attr("direction") == "left"){
                       jQuery(this).css("border-top-left-radius", "10px");
                       jQuery(this).css("border-bottom-left-radius", "10px");
                       jQuery(this).css("width", exp.width*(exp.ranges.left)+"px");
                   }
                   else{ // right
                       jQuery(this).css("border-top-right-radius", "10px");
                       jQuery(this).css("border-bottom-right-radius", "10px");
                       jQuery(this).css("width", exp.width*(exp.ranges.right)+"px");
                   }
                   jQuery(this).css("height", exp.height+"px");
                   //animation  = 'jQuery(this).animate({"'+scale+'":"'+exp.width*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 600)';
               }
               var animation2 = 'jQuery(this).animate({"'+scale+'":"0px", "'+op+'":"'+sign+'"}, 300)';
               //eval(animation);
               eval(animation2);
               setTimeout('jQuery(".exp").remove();', 300);
               //console.log(animation2);
           });
        }
        catch(ex){ BombermanClient.log(ex); }
    } 
}

BombermanClient.renderMap = function(toProc){
    //console.log(toProc);
    var dims = toProc.substr(0, toProc.indexOf("[#walls#]")).split("x");
    var worldWidth = parseInt(dims[0]);
    if (!worldWidth) worldWidth = 660;
    var worldHeight = parseInt(dims[1]);
    if (!worldWidth) worldHeight = 510;
    jQuery("#world").css("width", worldWidth+"px");
    jQuery("#world").css("height", worldHeight+"px");
    //console.log(dims);
    toProc = toProc.substr(toProc.indexOf("[#walls#]") + 9 /*"[#walls#]".length*/);
    bricks = toProc.split("[#wallSep#]");
    if (bricks.length && BombermanClient.brickLength == bricks.length) return;
    BombermanClient.brickLength = bricks.length;
    jQuery(".brick").remove();
    var last = bricks.length;
    var idx = 0;
    for (i in bricks){
        idx++;
        if (idx == last) break;
        try{
           brick = JSON.parse(bricks[i]);
           str = "<div class='brick' id='brick_"+brick.wallId+"' style='position:absolute; top:" + brick.posY + "px; left:" + brick.posX + "px;' alt='"+brick.name+"' title='"+brick.name+"'><img src='images/walls/" + brick.texture + "' width='" + brick.width + "' height='" + brick.height + "' /></div>";
           jQuery("#world").append(str);
        }
        catch(ex){ BombermanClient.log(ex); }
    }
    BombermanClient.log("gata");
    BombermanClient.sendReadyMessage();
    $.unblockUI();
}

BombermanClient.sendReadyMessage = function(){
    try{ BombermanClient.socket.send("ready"); } catch(ex){BombermanClient.log(ex); } // request info about the other users
}

BombermanClient.renderChars = function(toProc){
    
    BombermanClient.stats = {};
    BombermanClient.charID = toProc.substr(0, toProc.indexOf("[#chars#]"));
    toProc = toProc.substr(toProc.indexOf("[#chars#]") + 9 /*"[#chars#]".length*/);
    chars = toProc.split("[#charSep#]");
    var last = chars.length;
    var idx = 0;
    BombermanClient.charNames = new Array();
    for (i in chars){
        idx++;
        if (idx == last) break;
        try{
           var x = JSON.parse(chars[i]);
           //console.log(x);
           BombermanClient.stats[x.id] = {};
           BombermanClient.stats[x.id]["kills"] = x.kills;
           BombermanClient.stats[x.id]["deaths"] = x.deaths;
           BombermanClient.stats[x.id]["connectionTime"] = x.connectionTime;
           BombermanClient.stats[x.id]["name"] = x.name;
//           log(x);
           if (jQuery("#char_"+x.id).length > 0){
                var ob = jQuery("#char_"+x.id);

                if (ob.css("top") != (x.posY+"px")){
                    ob.css("top", x.posY+"px");
                }
                if (ob.css("left") != (x.posX+"px")){
                    ob.css("left", x.posX+"px");
                }

                var crtImg = ob.find("img").attr("src").substr(ob.find("img").attr("src").lastIndexOf("/")+1);
                if (crtImg != (x.crtTexture+".gif")){
                    ob.find("img").attr("src", "images/characters/"+x.crtTexture+".gif");
                    ob.find("canvas").hide();
                    ob.find("img").show();
                }
                else{
                    // "stop" image only if character is not walking and it's state is "Normal" or "Bomb"
                    if (!x.walking && (x.state == "Normal" || x.state == "Bomb")){
                        ob.find("canvas")[0].getContext("2d").clearRect(0, 0, x.width, x.height);
                        ob.find("canvas")[0].getContext("2d").drawImage(ob.find("img")[0], 0, 0, x.width, x.height);
                        ob.find("img").hide();
                        ob.find("canvas").show();
                    }
                    else{
                        if (ob.find("img").css("display") == "none"){
                            ob.find("canvas").hide();
                            ob.find("img").show();
                        }
                    }
                }
            }
            else{
//                console.log(x.id);
                str = "<div id='char_"+x.id+"' class='character' style='position:absolute; top:" + x.posY + "px; left:" + x.posX + "px;' alt='" + x.name + "' title='" + x.name + "'><img src='images/characters/" + x.crtTexture + ".gif' width='" + x.width + "' height='" + x.height + "' /><canvas style='display:none;' width='"+x.width+"' height='"+x.height+"'></canvas></div>";
                jQuery("#world").append(str);
            }
            
            BombermanClient.charNames.push("char_"+x.id);
            if (x.ready == false){
                if (!ob.hasClass("blinking")){
                    BombermanClient.blinking[x.id] = true;
                    ob.addClass("blinking");
                    BombermanClient.blinkChar(x.id, true);
                }
            }
            else{
                if (ob.hasClass("blinking")){
                    ob.removeClass("blinking");
                    BombermanClient.unblinkChar(x.id);
                }
            }
        }
        catch(ex){ BombermanClient.log(ex); }
    }
    //console.log(charNames);
    jQuery(".character").each(function(idx){
        if (jQuery.inArray(jQuery(this).attr("id"), BombermanClient.charNames) == -1){
            jQuery(this).remove();
        }
    });
    BombermanClient.renderStats(BombermanClient.charID);
}

BombermanClient.blinkChar = function(charID, hide){
    if (BombermanClient.blinking[charID]){
         var elem = jQuery("#char_"+charID);
         if (hide){
            elem.fadeOut(500);
        }
        else{
            elem.fadeIn(500);
        }
        setTimeout('BombermanClient.blinkChar("'+charID+'", '+!hide+')', 500);   
    }
}

BombermanClient.unblinkChar = function(charID){
    var elem = jQuery("#char_"+charID);
    BombermanClient.blinking[charID] = false;
    elem.fadeIn('fast');
}

BombermanClient.renderStats = function(charID){
    jQuery(".stat").remove();
    var str = "";
    for (i in this.stats){
        var style = "";
        if (i == charID){
            style = "background:#ffcccc";
        }
        var x = BombermanClient.stats[i];
        str += "<tr class='stat' style='"+style+"'>";
        str += "<td>"+x.name+"</td>";
        str += "<td>"+x.kills+"</td>";
        str += "<td>"+x.deaths+"</td>";
        str += "<td>"+x.connectionTime+"</td>";
        str += "</tr>";
    }
    jQuery("#stats").append(str);
}

BombermanClient.boundNumber = function(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

BombermanClient.centerMap = function(id){
    var viewportWidth = jQuery(window).width();
    viewportHeight = jQuery(window).height();
    $foo = jQuery('#char_'+id);
    if ($foo.length == 0) return;
    elWidth = $foo.width();
    elHeight = $foo.height();
    elOffset = $foo.offset();
    jQuery(window)
        .scrollTop(elOffset.top + (elHeight/2) - (viewportHeight/2))
        .scrollLeft(elOffset.left + (elWidth/2) - (viewportWidth/2));
}

BombermanClient.changeName = function(){
    var name = $.trim(jQuery("#name").val());
    if (!name){
        alert("Enter a valid name!");
        return false;
    }
    try{ BombermanClient.socket.send("name "+name); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
    return true;
}

BombermanClient.updateStatus = function(){
    BombermanClient.walking = false;
    if (BombermanClient.MOVE_UP){
        try{ BombermanClient.socket.send("up"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.walking = true;
    }
    
    else if (BombermanClient.MOVE_DOWN){
        try{ BombermanClient.socket.send("down"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.walking = true;
    }
    
    else if (BombermanClient.MOVE_LEFT){
        try{ BombermanClient.socket.send("left"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.walking = true;
    }
    
    else if (BombermanClient.MOVE_RIGHT){
        try{ BombermanClient.socket.send("right"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.walking = true;
    }
    
    if (BombermanClient.DETONATE){
        try{ BombermanClient.socket.send("detonate"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.DETONATE = !BombermanClient.DETONATE;
    }
    
    if (BombermanClient.FIRE){
        try{ BombermanClient.socket.send("bomb"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
        BombermanClient.FIRE = !BombermanClient.FIRE;
    }

    BombermanClient.centerMap(BombermanClient.charID);

}

BombermanClient.resetMap = function(){
    try{ BombermanClient.socket.send("reset"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
}

BombermanClient.canFire = function(){
    BombermanClient.IS_FIRING = false;
}

BombermanClient.getMap = function(){
    try{ BombermanClient.socket.send("getEnvironment"); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
}

BombermanClient.showChatBox = function(){
    jQuery("#chatBox").css("display", "inline");
    jQuery("#chatMessage").val("");
    jQuery("#chatMessage").focus();
    BombermanClient.chatBoxOpen = true;
}

BombermanClient.closeChatBox = function(){
    jQuery("#chatBox").css("display", "none");
    BombermanClient.chatBoxOpen = false;
}

BombermanClient.showStats = function(){
    jQuery("#stats").css("display", "table");
}

BombermanClient.closeStats = function(){
    jQuery("#stats").css("display", "none");
}

BombermanClient.sendMessage = function(){
    try{ BombermanClient.socket.send("msg "+ jQuery("#chatMessage").val()); } catch(ex){ BombermanClient.log(ex); } // request info about the other users
    jQuery("#chatMessage").val("");
    jQuery("#chatMessage").focus();
}

BombermanClient.showMessage = function(message){
    //log(message);
    var msgID = Math.random().toString(36).slice(2);
    jQuery(".messages").append("<div class='message' id='msg_"+msgID+"'>"+message+"</div>");
    setTimeout("BombermanClient.hideMessage('"+msgID+"')", 3000);
}

BombermanClient.hideMessage = function(msgID){
    jQuery("#msg_"+msgID).fadeOut('slow');
}

BombermanClient.quit = function(){
  BombermanClient.log("Goodbye!");
  try{
	BombermanClient.socket.send("QUIT");
  	BombermanClient.socket.close();
  	BombermanClient.socket=null;
        jQuery("#world").html("");
        BombermanClient.log("connection closed");
  }
  catch(ex){ BombermanClient.log(ex); }
}

var mouseX = 0;
var mouseY = 0;
var precMouseX = 0;
var precMouseY = 0;

jQuery(document).ready(function(){
    BombermanClient.init();
    hideMouse = setInterval("checkIdleMouse()", 1000);
    $('body').mousemove(function (event) {
        //console.log("move");
        mouseX = event.clientX || event.pageX;
        mouseY = event.clientY || event.pageY;
        if (precMouseX != mouseX || precMouseY != mouseY){
            $("body").css("cursor", "auto");
        }
        precMouseX = mouseX;
        precMouseY = mouseY;
    });
});

function checkIdleMouse(){
    if (mouseX == precMouseX && mouseY == precMouseY){
        $("body").css("cursor", "none");
    }
}