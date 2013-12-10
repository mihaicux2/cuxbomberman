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
        try{
            var x = JSON.parse(msg.data);
            //console.log(x);
            // process walls
            //str = "<div style='position:absolute; top:"+x.posY+"px; left:"+x.posX+"px;' alt='"+x.name+"' title='"+x.name+"'><img src='images/walls/"+x.texture+"' width='"+x.width+"' height='"+x.height+"' /></div>";
            str = "<div style='position:absolute; top:"+x.posY+"px; left:"+x.posX+"px;' alt='"+x.name+"' title='"+x.name+"'><img src='images/characters/"+x.crtTexture+".gif' width='"+x.width+"' height='"+x.height+"' /></div>";
            $("#world").html(str);
        }
        catch (e){
            log(e.message);
            //log(msg);
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

function boundNumber(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

function updateStatus(){
    
    if (MOVE_UP){
        try{ socket.send("up"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    if (MOVE_DOWN){
        try{ socket.send("down"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    if (MOVE_LEFT){
        try{ socket.send("left"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    if (MOVE_RIGHT){
        try{ socket.send("right"); } catch(ex){ log(ex); } // request info about the other users
    }
    
    if (FIRE){
        try{ socket.send("bomb"); } catch(ex){ log(ex); } // request info about the other users
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