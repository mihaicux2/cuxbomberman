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

function init(){
  var host = "ws://" + document.location.host + document.location.pathname + "bombermanendpoint";
  try{
    socket = new WebSocket(host);
    log('WebSocket - status '+socket.readyState);
    socket.onopen    = function(msg){ log("Welcome - status "+this.readyState); };
    socket.onmessage = function(msg){
        try{
            var x = JSON.parse(msg.data);
            console.log(x);
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

function join(){
	var txt = $("#name");
	var msg = txt.val();
	if (!msg){ alert("Username can not be empty"); return; }
	txt.val("");
  	txt.focus();
  	try{
            socket.send("JOIN " + msg);
            log(/*'@[sent : ] '+*/msg);
            $("#join").attr("disabled", "disabled");
            $("#name").attr("disabled", "disabled");
            $("#text").removeAttr("disabled");
            $("#quit").removeAttr("disabled");
            $("#msg").removeAttr("disabled");
            $("#upload").removeAttr("disabled");
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
                        if (!$("#autoRun").is(":checked"))
                            INC_SPEED = false;
		    case 13: // ENTER
                        if (!$("#autoFire").is(":checked"))
                            FIRE = false;
                }
            });
            timer = setInterval("updateStatus()", 10); // send requests every 10 miliseconds => limit to 100 FPS (at most)
            document.title = msg;
  	}
  	catch(ex){ log(ex); }
}

function boundNumber(nr, lo, hi){
    if (nr < lo) nr = lo;
    if (nr > hi) nr = hi;
    return nr;
}

function updateStatus(){
    var factor = 1;
    if (INC_SPEED == true) factor = 2;
    var step = 1 * factor;
    
    if (MOVE_UP == true)    posY -= step;
    if (MOVE_DOWN == true)  posY += step;
    if (MOVE_LEFT == true)  posX -= step;
    if (MOVE_RIGHT == true) posX += step;
    posX = boundNumber(posX, 0, 370);
    posY = boundNumber(posY, 0, 370);
    if (MOVE_UP || MOVE_DOWN || MOVE_LEFT || MOVE_RIGHT){ // client is moving
        try{
		socket.send("UPDATE_POS " + posX + " " + posY + " "+ CRT_LOOK_AT); 
		CRT_LOOK_AT = 0;
		if (MOVE_UP)    CRT_LOOK_AT += LOOK.UP;
		if (MOVE_DOWN)  CRT_LOOK_AT += LOOK.DOWN;
		if (MOVE_LEFT)  CRT_LOOK_AT += LOOK.LEFT;
		if (MOVE_RIGHT) CRT_LOOK_AT += LOOK.RIGHT;
	} catch(ex){ log(ex); }
    }
    else $.stopSound("steps.mp3");

//    if (FIRE){ // client is firing
//	if (IS_FIRING == false){
//	    try{ socket.send("FIRE " + CRT_LOOK_AT); IS_FIRING = true; setTimeout("canFire()", FIRE_RATE) } catch(ex){ log(ex); }
//	}
//    }

    if (FIRE){
        try{ socket.send("FIRE " + CRT_LOOK_AT); } catch(ex){ log(ex); }
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