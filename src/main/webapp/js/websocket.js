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
                case 13: // ENTER
                    FIRE = false;
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
            case "char":
                renderChar(toProc);
                break;
            case "bomb":
                renderBombs(toProc);
                break;
        }
    };
    socket.onclose   = function(msg){
        log("Disconnected - status "+this.readyState);
        $("#chatUsers").html("");
        clearInterval(timer);
    };
  }
  catch(ex){ log(ex); }
  $("#msg").focus();
}

function renderBombs(toProc){
    //console.log(toProc);
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
        catch(ex){ log(ex); }
    } 
}

function renderMap(toProc){
    //console.log(toProc);
    $(".brick").remove();
    bricks = toProc.split("[#brickSep#]");
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
        catch(ex){ log(ex); }
    }
}

function renderChar(toProc){
    try{
        var x = JSON.parse(toProc);
        //console.log(x);
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
//                console.log("change image");
            }
        }
        else{
            str = "<div id='char_"+x.name+"' style='position:absolute; top:" + x.posY + "px; left:" + x.posX + "px;' alt='" + x.name + "' title='" + x.name + "'><img src='images/characters/" + x.crtTexture + ".gif' width='" + x.width + "' height='" + x.height + "' /></div>";
            $("#world").append(str);
        }
    }
    catch(ex){ log(ex); }
}

function boundNumber(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

function updateStatus(){
    
    if (MOVE_UP){
        try{ socket.send("up"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    else if (MOVE_DOWN){
        try{ socket.send("down"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    else if (MOVE_LEFT){
        try{ socket.send("left"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    else if (MOVE_RIGHT){
        try{ socket.send("right"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    if (FIRE){
        try{ socket.send("bomb"); } catch(ex){ log(ex); } // request info about the other users
        FIRE = !FIRE;
    }

    try{ socket.send("STATUS"); } catch(ex){ log(ex); } // request info about the other users

}

function canFire(){
    IS_FIRING = false;
}

function send(){
  var txt,msg;
  txt = $("#msg");
  msg = txt.val();
  if(!msg){ alert("Message can not be empty"); return; }
  txt.val("");
  txt.focus();
  try{ socket.send("TEXT " + msg); /*log('Sent: '+msg);*/ } catch(ex){ log(ex); }
}
function quit(){
  log("Goodbye!");
  try{
	socket.send("QUIT");
  	socket.close();
  	socket=null;
  	init();
  	$("#text").attr("disabled", "disabled");
	$("#quit").attr("disabled", "disabled");
	$("#msg").attr("disabled", "disabled");
        $("#upload").attr("disabled", "disabled");
	$("#join").removeAttr("disabled");
	$("#name").removeAttr("disabled");
  }
  catch(ex){ log(ex); }
}

init();