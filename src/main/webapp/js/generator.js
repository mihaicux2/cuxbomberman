var isMouseDown = false,
        isHighlighted,
        actionsStack = [],
        clipBoard = false;

$(document).mouseup(function () {
    isMouseDown = false;
});

function isRightClick(e) {
    if (e.which) {
        return (e.which == 3);
    } else if (e.button) {
        return (e.button == 2);
    }
    return false;
}

function pasteAction() {
    if (!clipBoard)
        return;
    $(".selected").each(function (idx, el) {
        var copy = clipBoard.clone();
//                copy.attr("id", Math.random().toString(36).slice(2));
        $(this).html(copy);
        $(this).removeClass("selected");
    });
}

function deleteAction() {
    $(".selected").each(function (idx, el) {
        $(this).empty();
        $(this).removeClass("selected");
    });
}

function undoAction() {
    if (actionsStack.length > 0){
        $(".our_table").html(actionsStack.pop());
        initDnD();
        initContextMenus();        
    }
}

function appendStack(){
    actionsStack.push($(".our_table").html());
}

function initContextMenus() {
    $('.our_table td').contextMenu({
        menuSelector: "#contextMenu2",
        menuSelected: function (invokedOn, selectedMenu) {
//                                    var msg = "You selected the menu item '" + selectedMenu.text() +
//                                        "' on the value '" + invokedOn.text() + "'";
//                                    alert(msg);
            if (selectedMenu.text() == "Paste") {
                appendStack();
                pasteAction();
                if (!clipBoard)
                    return;
                var clone = clipBoard.clone();
//                        clone.attr("id", Math.random().toString(36).slice(2));
                invokedOn.html(clone);
            }
            if (selectedMenu.text() == "Undo") {
                undoAction();
            }
            initContextMenus();
            initDnD();
            initDnD();
        }
    });
    $(".event").contextMenu({
        menuSelector: "#contextMenu",
        menuSelected: function (invokedOn, selectedMenu) {
//                                    var msg = "You selected the menu item '" + selectedMenu.text() +
//                                        "' on the value '" + invokedOn.text() + "'";
//                                    alert(msg);
            if (selectedMenu.text() == "Delete") {
                appendStack();
                deleteAction();
                invokedOn.remove();
            }
            if (selectedMenu.text() == "Copy") {
                clipBoard = invokedOn.clone();
//                        clipBoard.attr("id", Math.random().toString(36).slice(2));
            }
            if (selectedMenu.text() == "Paste") {
                //appendStack();
                pasteAction();

                var clone = clipBoard.clone();
//                        clone.attr("id", Math.random().toString(36).slice(2));
                invokedOn.html(clone);
            }
            if (selectedMenu.text() == "Undo") {
                undoAction();
            }
            initContextMenus();
            initDnD();
            initDnD();
        }
    });
}

function initDnD() {
//    $('.event').on("dragstart", function (event) {
//        var dt = event.originalEvent.dataTransfer;
//        dt.setData('Text', $(this).attr('id'));
//    });
//    $('.our_table td').on("dragenter dragover drop", function (event) {
//        event.preventDefault();
//        if (event.type === 'drop') {
//            var data = event.originalEvent.dataTransfer.getData('Text', $(this).attr('id'));
//            if ($(this).find('span').length === 0) {
//                de = $('#' + data).detach();
//                de.appendTo($(this));
//            }
//        }
//    });

    $(".our_table td").mousedown(function (e) {
        if (isRightClick(e))
            return false;
        if (!isMouseDown){
            isMouseDown = true;
            appendStack();
        }
        $(this).toggleClass("selected");
        isHighlighted = $(this).hasClass("selected");
        return false; // prevent text selection
    }).mouseover(function () {
        if (isMouseDown) {
            $(this).toggleClass("selected", isHighlighted);
        }
    }).bind("selectstart", function () {
        return false;
    });

}

function addWall(owner, img) {
    clipBoard = $("#" + img + "_event").clone();
    clipBoard.css("display", "");
//            initDnD();
//            initContextMenus();
    $(".btn-crt").removeClass("btn-crt");
    $(owner).addClass("btn-crt");
//            console.log($(owner));

}

function exportMap() {
    var ret = {};
    ret["size"] = [$("#w").val(), $("#h").val()];
    ret["bricks"] = [];
    $(".our_table td").each(function(idx){
        var chr='x';
        var child = $(this).find('.event');
        if (child.length > 0){
            switch (child.attr("wall")){
                case "brick":
                    chr = 'b';
                    break;
                case "grass":
                    chr = 'g';
                    break;
                case "steel":
                    chr = 's';
                    break;
                case "stone":
                    chr = 'r';
                    break;
                case "water":
                    chr = 'w';
                    break;
            }
        }
        ret["bricks"].push(chr);
    });
    
//    jQuery.blockUI({message: "<p>Please wait for the map to be exported</p>"});
//    $.ajax({
//        type: "POST",
//        url: "export.jsp",
////        datatype: "json",
////        contentType: "text/html",
//        data: {
//            map: JSON.stringify(ret)
//        },
////        async: false,
//        success: function (msg) {
//            jQuery.unblockUI();
////            BombermanClient.alert(msg);
//        },
//        error: function (msg) {
//            BombermanClient.log(msg);
//            BombermanClient.alert("Cannot export map");
//            jQuery.unblockUI();
//        }
//    });
    $("#randMap").val(JSON.stringify(ret));
    $("#randMapForm").submit();
}

function resetMap(){
    if (actionsStack.length > 0){
        $(".our_table").html(actionsStack[0]);
        initDnD();
        initContextMenus();  
        actionsStack = null;
        actionsStack = [];
        appendStack();
    }
}