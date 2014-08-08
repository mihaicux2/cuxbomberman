(function($){

  $.extend({
    playSound: function(){
	var audio = document.getElementById("sound_"+arguments[0]);
  	if (audio == undefined){
      		$('<audio id="sound_'+arguments[0]+'" src="'+arguments[0]+'" />').appendTo('body');
		audio = document.getElementById("sound_"+arguments[0]);
	}
	audio = document.getElementById("sound_"+arguments[0]);
	if (audio.paused == false) return;
	//audio.volume = .3;
	audio.play();
    },

    stopSound: function(){
        var audio = document.getElementById("sound_"+arguments[0]);
  	if (audio != undefined) audio.pause();
    }

  });

})(jQuery);

function get_random_color() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.round(Math.random() * 15)];
    }
    return color;
}

function log(msg){
    console.log(msg);
    //$("#output").append(msg+"<hr />");
}

// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
//var keys = [37, 38, 39, 40];
//
//function keydown(e) {
//    for (var i = keys.length; i--;) {
//        if (e.keyCode === keys[i]) {
//            preventDefault(e);
//            return;
//        }
//    }
//}
//
//function preventDefault(e) {
//    e = e || window.event;
//    if (e.preventDefault)
//        e.preventDefault();
//    e.returnValue = false;
//}
//
//function wheel(e) {
//    preventDefault(e);
//}
//
//function disable_scroll() {
//    if (window.addEventListener) {
//        window.addEventListener('DOMMouseScroll', wheel, false);
//    }
//    window.onmousewheel = document.onmousewheel = wheel;
//    document.onkeydown = keydown;
//}
//
//function enable_scroll() {
//    if (window.removeEventListener) {
//        window.removeEventListener('DOMMouseScroll', wheel, false);
//    }
//    window.onmousewheel = document.onmousewheel = document.onkeydown = null;
//}

//$("body").scrollLeft("40px");

function BombermanClient(options){
    this.options = options;
    this.posX = 0;
    this.posY = 0;
    this.MOVE_UP = false;
    this.MOVE_DOWN = false;
    this.MOVE_LEFT = false;
    this.MOVE_RIGHT = false;
    this.INC_SPEED = false;
    this.FIRE = false;
    this.DETONATE = false;
    this.brickLength = 0;
    this.walking = false;
    this.charNames;
    this.timers = {};
    this.charID = "";
    this.stats = {};
    this.blinking = {};
    this.chatBoxOpen = false;
    this.host = "ws://" + document.location.host + document.location.pathname + "bombermanendpoint/";
    this.socket = {};
}

BombermanClient.prototype.init = function(){
    try{
        this.socket = new WebSocket(host);
    }
    catch(ex){ this.log(ex); }
}

BombermanClient.prototype.log = function(msg){
    console.log(msg);
}

var posX = 0;
var posY = 0;
var MOVE_UP = false;
var MOVE_DOWN = false;
var MOVE_LEFT = false;
var MOVE_RIGHT = false;
var INC_SPEED  = false;
var FIRE = false;
var DETONATE = false;

var brickLength = 0;
var walking = false;
var charNames;
var timers = {};
var charID = "";
var stats = {};
var blinking = {};

var chatBoxOpen = false;

function init(){
  var host = "ws://" + document.location.host + document.location.pathname + "bombermanendpoint/";
  try{
    socket = new WebSocket(host);
    log('WebSocket - status '+socket.readyState);
    socket.onopen    = function(msg){
        log("Welcome - status "+this.readyState);
        $.blockUI({message:"<p>Please wait for the map to load</p>"})
        setTimeout("getMap()", 100);
        $(window).keydown(function(e){
            
            switch (e.keyCode){
                case 38:  // KEY_UP
                    if (chatBoxOpen) break;
                    MOVE_UP = true;
                    MOVE_DOWN = false;
                    break;
                case 40:  // KEY_DOWN
                    if (chatBoxOpen) break;
                    MOVE_DOWN = true;
                    MOVE_UP = false;
                    break;
                case 37:  // KEY_LEFT
                    if (chatBoxOpen) break;
                    MOVE_LEFT = true;
                    MOVE_RIGHT = false;
                    break;
                case 39:  // KEY_RIGHT
                    if (chatBoxOpen) break;
                    MOVE_RIGHT = true;
                    MOVE_LEFT = false;
                    break;
                case 16: // SHIFT
                    if (chatBoxOpen) break;
                    //INC_SPEED = true;
                    DETONATE = true;
                    break;
                case 13: // ENTER
                    if (chatBoxOpen) break;
                    showChatBox();
                    break;
                case 32: // SPACE
                    if (chatBoxOpen) break;
                    //DETONATE = true;
                    FIRE = true;
                    break;
                case 27: // ESC
                    closeChatBox();
                    break;
                case 9: // TAB
                    showStats();
                    e.preventDefault();
                    break;
            }
        });
        $(window).keyup(function(e){
            switch (e.keyCode){
                case 38:  // KEY_UP
                    if (chatBoxOpen) break;
                    MOVE_UP = false;
                    break;
                case 40:  // KEY_DOWN
                    if (chatBoxOpen) break;
                    MOVE_DOWN = false;
                    break;
                case 37:  // KEY_LEFT
                    if (chatBoxOpen) break;
                    MOVE_LEFT = false;
                    break;
                case 39:  // KEY_RIGHT
                    if (chatBoxOpen) break;
                    MOVE_RIGHT = false;
                    break;
                case 16: // SHIFT
                    if (chatBoxOpen) break;
                    //INC_SPEED = false;
                    DETONATE = false;
                    break;
                case 13: // ENTER
                    if (chatBoxOpen){
                        sendMessage();
                        break;
                    }
                    showChatBox();
                    break;
                case 32: // SPACE
                    if (chatBoxOpen) break;
                    //DETONATE = false;
                    FIRE = false;
                    break;
                case 27: // ESC
                    closeChatBox();
                    break;
                case 9: // TAB
                    closeStats();
                    e.preventDefault();
                    break;
            }
        });
        timer = setInterval("updateStatus()", 10); // send requests every 10 miliseconds => limit to 100 FPS (at most)
        
        //disable_scroll();
        
    };
    socket.onmessage = function(msg){
        
        var op = msg.data.substr(0, msg.data.indexOf(":["));
        var toProc = msg.data.substr(msg.data.indexOf(":[")+2);
        //console.log(msg);
        //socket.close();
        //var x = JSON.parse(toProc);
        switch(op){
            case "map":
                renderMap(toProc);
                break;
            case "chars":
                renderChars(toProc);
                break;
            case "bombs":
                renderBombs(toProc);
                break;
            case "explosions":
                renderExplosions(toProc);
                break;
            case "items":
                renderItems(toProc);
                break;
            case "blownWalls":
                console.log("blown...");
                removeWalls(toProc);
                break;
            case "sound":
                $.playSound(toProc);
                break;
            case "msg":
                showMessage(toProc);
                break;
        }
    };
    socket.onclose   = function(msg){
        log("Disconnected - status "+this.readyState);
        alert("Connection closed");
        $("#chatUsers").html("");
        clearInterval(timer);
    };
    
    $(window).onclose(function(){
        socket.send("QUIT");
    });
    
  }
  catch(ex){ console.log(ex); }
  //$("#msg").focus();
  showNameBox();
}

function showNameBox(){
    if ($("#nameBox").css("display") == "none"){
        $("#nameBox").css("display", "block");
    }
    else{
        hideNameBox();
    }
}

function hideNameBox(){
    $("#nameBox").css("display", "none");
}

function removeWalls(toProc){
    items = toProc.split("[#brickSep#]");
    var last = items.length;
    var idx = 0;
    for (i in items){
        idx++;
        if (idx == last) break;
        var item = items[i];
        $("#brick_"+item).remove();
    }
}

function renderItems(toProc){
    $(".item").remove();
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
           $("#world").append(str);
           //console.log(item);
        }
        catch(ex){ console.log(ex); }
    } 
}

function renderBombs(toProc){
    $(".bomb").remove();
    bombs = toProc.split("[#bombSep#]");
    var last = bombs.length;
    var idx = 0;
    for (i in bombs){
        idx++;
        if (idx == last) break;
        try{
           bomb = JSON.parse(bombs[i]);
           str = "<div class='bomb' style='position:absolute; top:" + bomb.posY + "px; left:" + bomb.posX + "px;'><img src='images/characters/19.gif' width='" + bomb.width + "' height='" + bomb.height + "' /></div>";
           $("#world").append(str);
        }
        catch(ex){ console.log(ex); }
    } 
}

function renderExplosions(toProc){
    //$(".exp").remove();
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
           $("#world").append(str);
           //console.log(exp);
           $(".extra").each(function(index){
               var op    = "";
               var sign  = ""; // retract
               var sign2 = ""; // expand
               var scale = "";
               switch ($(this).attr("direction")){
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
                   if ($(this).attr("direction") == "up"){
                       $(this).css("border-top-left-radius", "10px");
                       $(this).css("border-top-right-radius", "10px");
                       $(this).css("height", exp.height*(exp.ranges.up)+"px");
                   }
                   else{ // down
                       $(this).css("border-bottom-left-radius", "10px");
                       $(this).css("border-bottom-right-radius", "10px");
                       $(this).css("height", exp.height*(exp.ranges.down)+"px");
                   }
                   $(this).css("width", exp.width+"px");
                   //animation  = '$(this).animate({"'+scale+'":"'+exp.height*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 200)';
               }
               else{
                   if ($(this).attr("direction") == "left"){
                       $(this).css("border-top-left-radius", "10px");
                       $(this).css("border-bottom-left-radius", "10px");
                       $(this).css("width", exp.width*(exp.ranges.left)+"px");
                   }
                   else{ // right
                       $(this).css("border-top-right-radius", "10px");
                       $(this).css("border-bottom-right-radius", "10px");
                       $(this).css("width", exp.width*(exp.ranges.right)+"px");
                   }
                   $(this).css("height", exp.height+"px");
                   //animation  = '$(this).animate({"'+scale+'":"'+exp.width*exp.owner.bombRange+'px", "'+op+'":"'+sign2+'"}, 600)';
               }
               var animation2 = '$(this).animate({"'+scale+'":"0px", "'+op+'":"'+sign+'"}, 300)';
               //eval(animation);
               eval(animation2);
               setTimeout('$(".exp").remove();', 300);
               //console.log(animation2);
           });
        }
        catch(ex){ console.log(ex); }
    } 
}

function renderMap(toProc){
    //console.log(toProc);
    var dims = toProc.substr(0, toProc.indexOf("[#walls#]")).split("x");
    var worldWidth = parseInt(dims[0]);
    if (!worldWidth) worldWidth = 660;
    var worldHeight = parseInt(dims[1]);
    if (!worldWidth) worldHeight = 510;
    $("#world").css("width", worldWidth+"px");
    $("#world").css("height", worldHeight+"px");
    //console.log(dims);
    toProc = toProc.substr(toProc.indexOf("[#walls#]") + 9 /*"[#walls#]".length*/);
    bricks = toProc.split("[#wallSep#]");
    if (bricks.length && brickLength == bricks.length) return;
    brickLength = bricks.length;
    //console.log(brickLength);
    $(".brick").remove();
    var last = bricks.length;
    var idx = 0;
    for (i in bricks){
        idx++;
        if (idx == last) break;
        try{
           brick = JSON.parse(bricks[i]);
           str = "<div class='brick' id='brick_"+brick.wallId+"' style='position:absolute; top:" + brick.posY + "px; left:" + brick.posX + "px;' alt='"+brick.name+"' title='"+brick.name+"'><img src='images/walls/" + brick.texture + "' width='" + brick.width + "' height='" + brick.height + "' /></div>";
           $("#world").append(str);
        }
        catch(ex){ console.log(ex); }
    }
    log("gata");
    sendReadyMessage();
    $.unblockUI();
}

function sendReadyMessage(){
    try{ socket.send("ready"); } catch(ex){ log(ex); } // request info about the other users
}

function renderChars(toProc){
    
    stats = {};
    
    charID = toProc.substr(0, toProc.indexOf("[#chars#]"));
    //console.log(charID);
    toProc = toProc.substr(toProc.indexOf("[#chars#]") + 9 /*"[#chars#]".length*/);
    chars = toProc.split("[#charSep#]");
    var last = chars.length;
    var idx = 0;
    charNames = new Array();
    for (i in chars){
        idx++;
        if (idx == last) break;
        try{
           var x = JSON.parse(chars[i]);
           //console.log(x);
           stats[x.id] = {};
           stats[x.id]["kills"] = x.kills;
           stats[x.id]["deaths"] = x.deaths;
           stats[x.id]["connectionTime"] = x.connectionTime;
           stats[x.id]["name"] = x.name;
//           log(x);
           if ($("#char_"+x.id).length > 0){
                var ob = $("#char_"+x.id);

                if (ob.css("top") != (x.posY+"px")){
                    ob.css("top", x.posY+"px");
    //                console.log("change top");
                }
                if (ob.css("left") != (x.posX+"px")){
                    ob.css("left", x.posX+"px");
    //                console.log("change left");
                }

                crtImg = ob.find("img").attr("src").substr(ob.find("img").attr("src").lastIndexOf("/")+1);
                //console.log(crtImg+" | "+x.crtTexture+".gif");
                if (crtImg != (x.crtTexture+".gif")){
                    ob.find("img").attr("src", "images/characters/"+x.crtTexture+".gif");
                    ob.find("canvas").hide();
                    ob.find("img").show();
                }
                else{
                    // "stop" image only if character is not walking and it's state is "Normal" or "Bomb"
                    if (!x.walking && !walking && x.state != "Blow" && x.state != "Trapped"){
                        ob.find("canvas")[0].getContext("2d").clearRect(0, 0, x.width, x.height);
                        ob.find("canvas")[0].getContext("2d").drawImage(ob.find("img")[0], 0, 0, x.width, x.height);
                        //console.log(ob.find("canvas")[0].getContext("2d"));
                        //socket.close();
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
                str = "<div id='char_"+x.id+"' class='character' style='position:absolute; top:" + x.posY + "px; left:" + x.posX + "px;' alt='" + x.name + "' title='" + x.name + "'><img src='images/characters/" + x.crtTexture + ".gif' width='" + x.width + "' height='" + x.height + "' /><canvas style='display:none;' width='"+x.width+"' height='"+x.height+"'></canvas></div>";
                $("#world").append(str);
            }
            charNames.push("char_"+x.id);
            var elem = $("#char_"+x.id);
            if (x.ready == false){
                if (!elem.hasClass("blinking")){
                    blinking[charID] = true;
                    elem.addClass("blinking");
                    blinkChar(x.id, true);
                }
            }
            else{
                if (elem.hasClass("blinking")){
                    elem.removeClass("blinking");
                    unblinkChar(x.id);
                }
            }
        }
        catch(ex){ console.log(ex); }
    }
    //console.log(charNames);
    $(".character").each(function(idx){
        if ($.inArray($(this).attr("id"), charNames) == -1){
            $(this).remove();
            //console.log($(this).attr("id"));
        }
    });
    renderStats(charID);
}

function blinkChar(charID, hide){
    if (blinking[charID]){
         var elem = $("#char_"+charID);
         if (hide){
            elem.fadeOut(500);
        }
        else{
            elem.fadeIn(500);
        }
        setTimeout('blinkChar("'+charID+'", '+!hide+')', 500);   
    }
}

function unblinkChar(charID){
    var elem = $("#char_"+charID);
    blinking[charID] = false;
    elem.fadeIn('fast');
}

function renderStats(charID){
    $(".stat").remove();
    var str = "";
    for (i in stats){
        var style = "";
        if (i == charID){
            style = "background:#ffcccc";
        }
        var x = stats[i];
        str += "<tr class='stat' style='"+style+"'>";
        str += "<td>"+x.name+"</td>";
        str += "<td>"+x.kills+"</td>";
        str += "<td>"+x.deaths+"</td>";
        str += "<td>"+x.connectionTime+"</td>";
        str += "</tr>";
    }
    $("#stats").append(str);
}

function boundNumber(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

function centerMap(id){
    var viewportWidth = jQuery(window).width(),
    viewportHeight = jQuery(window).height(),
    $foo = jQuery('#char_'+id),
    elWidth = $foo.width(),
    elHeight = $foo.height(),
    elOffset = $foo.offset();
    jQuery(window)
        .scrollTop(elOffset.top + (elHeight/2) - (viewportHeight/2))
        .scrollLeft(elOffset.left + (elWidth/2) - (viewportWidth/2));
}

function changeName(){
    var name = $.trim($("#name").val());
    if (!name) alert("Enter a valid name!");
    try{ socket.send("name "+name); } catch(ex){ log(ex); } // request info about the other users
}

function updateStatus(){
    walking = false;
    if (MOVE_UP){
        try{ socket.send("up"); } catch(ex){ log(ex); } // request info about the other users
        walking = true;
    }
    
    else if (MOVE_DOWN){
        try{ socket.send("down"); } catch(ex){ log(ex); } // request info about the other users
        walking = true;
    }
    
    else if (MOVE_LEFT){
        try{ socket.send("left"); } catch(ex){ log(ex); } // request info about the other users
        walking = true;
    }
    
    else if (MOVE_RIGHT){
        try{ socket.send("right"); } catch(ex){ log(ex); } // request info about the other users
        walking = true;
    }
    
    if (DETONATE){
        try{ socket.send("detonate"); } catch(ex){ log(ex); } // request info about the other users
        DETONATE = !DETONATE;
    }
    
    if (FIRE){
        try{ socket.send("bomb"); } catch(ex){ log(ex); } // request info about the other users
        FIRE = !FIRE;
    }

    //try{ socket.send("STATUS"); } catch(ex){ log(ex); } // request info about the other users

    centerMap(charID);

}

function resetMap(){
    try{ socket.send("reset"); } catch(ex){ log(ex); } // request info about the other users
}

function canFire(){
    IS_FIRING = false;
}

function getMap(){
    try{ socket.send("getEnvironment"); } catch(ex){ log(ex); } // request info about the other users
}

function showChatBox(){
    $("#chatBox").css("display", "inline");
    $("#chatMessage").val("");
    $("#chatMessage").focus();
    chatBoxOpen = true;
}

function closeChatBox(){
    $("#chatBox").css("display", "none");
    chatBoxOpen = false;
}

function showStats(){
    $("#stats").css("display", "table");
}

function closeStats(){
    $("#stats").css("display", "none");
}

function sendMessage(){
    try{ socket.send("msg "+ $("#chatMessage").val()); } catch(ex){ log(ex); } // request info about the other users
    $("#chatMessage").val("");
    $("#chatMessage").focus();
}

function showMessage(message){
    //log(message);
    var msgID = Math.random().toString(36).slice(2);
    $(".messages").append("<div class='message' id='msg_"+msgID+"'>"+message+"</div>");
    setTimeout("hideMessage('"+msgID+"')", 3000);
}

function hideMessage(msgID){
    $("#msg_"+msgID).fadeOut('slow');
}

function quit(){
  log("Goodbye!");
  try{
	socket.send("QUIT");
  	socket.close();
  	socket=null;
        $("#world").html("");
        log("connection closed");
  }
  catch(ex){ log(ex); }
}

init();