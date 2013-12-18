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
    $("#output").append(msg+"<hr />");
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

function init(){
  var host = "ws://" + document.location.host + document.location.pathname + "bombermanendpoint";
  try{
    socket = new WebSocket(host);
    log('WebSocket - status '+socket.readyState);
    socket.onopen    = function(msg){
        log("Welcome - status "+this.readyState);
        $(window).keydown(function(e){
            switch (e.keyCode){
                case 38:  // KEY_UP
                    MOVE_UP = true;
                    MOVE_DOWN = false;
                    break;
                case 40:  // KEY_DOWN
                    MOVE_DOWN = true;
                    MOVE_UP = false;
                    break;
                case 37:  // KEY_LEFT
                    MOVE_LEFT = true;
                    MOVE_RIGHT = false;
                    break;
                case 39:  // KEY_RIGHT
                    MOVE_RIGHT = true;
                    MOVE_LEFT = false;
                    break;
                case 16: // SHIFT
                    INC_SPEED = true;
                    break;
                case 13: // ENTER
                    FIRE = true;
                    break;
                case 32: // SPACE
                    DETONATE = true;
                    break;
            }
        });
        $(window).keyup(function(e){
            switch (e.keyCode){
                case 38:  // KEY_UP
                    MOVE_UP = false;
                    break;
                case 40:  // KEY_DOWN
                    MOVE_DOWN = false;
                    break;
                case 37:  // KEY_LEFT
                    MOVE_LEFT = false;
                    break;
                case 39:  // KEY_RIGHT
                    MOVE_RIGHT = false;
                    break;
                case 16: // SHIFT
                    INC_SPEED = false;
                    break;
                case 13: // ENTER
                    FIRE = false;
                    break;
                case 32: // SPACE
                    DETONATE = false;
                    break;
            }
        });
        timer = setInterval("updateStatus()", 10); // send requests every 10 miliseconds => limit to 100 FPS (at most)
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
        }
    };
    socket.onclose   = function(msg){
        log("Disconnected - status "+this.readyState);
        $("#chatUsers").html("");
        clearInterval(timer);
    };
    
    $(window).onclose(function(){
        socket.send("QUIT");
    });
    
  }
  catch(ex){ console.log(ex); }
  $("#msg").focus();
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
                       posY -= exp.height*exp.owner.bombRange;
                       break;
                   case "down":
                       posY += exp.height;
                       break;
                   case "left":
                       posX -= exp.width*exp.owner.bombRange;
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

var brickLength = 0;

function renderMap(toProc){
    //console.log(toProc);
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
           str = "<div class='brick' style='position:absolute; top:" + brick.posY + "px; left:" + brick.posX + "px;' alt='"+brick.name+"' title='"+brick.name+"'><img src='images/walls/" + brick.texture + "' width='" + brick.width + "' height='" + brick.height + "' /></div>";
           $("#world").append(str);
        }
        catch(ex){ console.log(ex); }
    }
}

var charNames;
var timers = {};
function renderChars(toProc){
    
    chars = toProc.split("[#charSep#]");
    var last = chars.length;
    var idx = 0;
    charNames = new Array();
    for (i in chars){
        idx++;
        if (idx == last) break;
        try{
           var x = JSON.parse(chars[i]);
           if ($("#char_"+x.name).length > 0){
                var ob = $("#char_"+x.name);

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
                str = "<div id='char_"+x.name+"' class='character' style='position:absolute; top:" + x.posY + "px; left:" + x.posX + "px;' alt='" + x.name + "' title='" + x.name + "'><img src='images/characters/" + x.crtTexture + ".gif' width='" + x.width + "' height='" + x.height + "' /><canvas style='display:none;' width='"+x.width+"' height='"+x.height+"'></canvas></div>";
                $("#world").append(str);
            }
            charNames.push("char_"+x.name);
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
}

function boundNumber(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

var walking = false;
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

}

function canFire(){
    IS_FIRING = false;
}

function quit(){
  log("Goodbye!");
  try{
	socket.send("QUIT");
  	socket.close();
  	socket=null;
  }
  catch(ex){ log(ex); }
}

init();