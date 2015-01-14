<!DOCTYPE html>
<html>
    <head>
        <title>AtomicBomberman look-a-like: Java websocket implementation</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="css/generator.css">
    </head>
    <body>

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

        <%
            int w = 50;
            int h = 22;
            if (request.getParameter("w") != null) {
                w = Integer.parseInt(request.getParameter("w"));
                if (w < 1) {
                    w = 80;
                }
            }
            if (request.getParameter("h") != null) {
                h = Integer.parseInt(request.getParameter("h"));
                if (h < 1) {
                    h = 22;
                }
            }
        %>

        <div class="title">AtomicBomberman look-a-like: Java websocket implementation</div>
        <div class="subtitile">AtomicBomberman look-a-like: Map generator</div>

        <div class="tools">
            <form id='toolsForm' class="form-inline" role="form" method="get" action="">
                <label>Map size (cols x rows) : </label>
                <div class="form-group">
                    <input type="text" class="form-control" id="w" name="w" value="<%=w%>" style="width:50px;">
                </div>
                x
                <div class="form-group">
                    <input type="text" class="form-control" id="h" name="h" value="<%=h%>" style="width:50px;">
                </div>
                <button type="submit" class="btn btn-primary" title='Set size'><img src='images/utils/resize.png' />Set size</button>
                <div class="form-group">
                    <label>Choose wall : </label>
                    <input type='button' class='btn btn-default tool-btn brick' onclick='addWall(this, "brick");' />
                    &nbsp;
                    <input type='button' class='btn btn-default tool-btn grass' onclick='addWall(this, "grass");' />
                    &nbsp;
                    <input type='button' class='btn btn-default tool-btn steel' onclick='addWall(this, "steel");' />
                    &nbsp;
                    <input type='button' class='btn btn-default tool-btn stone' onclick='addWall(this, "stone");' />
                    &nbsp;
                    <input type='button' class='btn btn-default tool-btn water' onclick='addWall(this, "water");' />
                    &nbsp;
                </div>
                <div class="form-group">
                    <button type="button" class="btn btn-success" title="Export map" onclick="exportMap()"><img src='images/utils/export.gif' />Export map</button>
                    <button type="button" class="btn btn-warning" title='Reset map' onclick="resetMap()"><img src='images/utils/refresh.png' />Reset map</button>
                </div>
            </form>
        </div>

        <div style="position:relative; margin-top:10px;">
            <div id="world">
                <table class="our_table">
                    <% for (int i = 0; i < h; i++) { %>
                    <tr>
                        <% for (int j = 0; j < w; j++) { %>
                        <td></td>
                        <% } %>
                    </tr>
                    <% }%>
                </table>
            </div>
        </div>

        <ul id="contextMenu" class="dropdown-menu" role="menu" style="display:none" >
            <li><a tabindex="-1" href="#"><img src='images/utils/delete.png' />Delete</a></li>
            <li><a tabindex="-1" href="#"><img src='images/utils/copy.png' />Copy</a></li>
            <li><a tabindex="-1" href="#"><img src='images/utils/paste.png' />Paste</a></li>
            <li><a tabindex="-1" href="#"><img src='images/utils/undo.png' />Undo</a></li>
        </ul>

        <ul id="contextMenu2" class="dropdown-menu" role="menu" style="display:none" >
            <li><a tabindex="-1" href="#"><img src='images/utils/paste.png' />Paste</a></li>
            <li><a tabindex="-1" href="#"><img src='images/utils/undo.png' />Undo</a></li>
        </ul>

        <div class='event event-btn brick' wall="brick" style='display:none;' draggable='true' id='brick_event'></div>
        <div class='event event-btn grass' wall="grass" style='display:none;' draggable='true' id='grass_event'></div>
        <div class='event event-btn steel' wall="steel" style='display:none;' draggable='true' id='steel_event'></div>
        <div class='event event-btn stone' wall="stone" style='display:none;' draggable='true' id='stone_event'></div>
        <div class='event event-btn water' wall="water" style='display:none;' draggable='true' id='water_event'></div>
                
        <script type="text/javascript" src="js/jquery.min.js"></script>
        <script src="bootstrap/js/bootstrap.min.js"></script>
        <script src="js/contextMenu.js"></script>
        <script type="text/javascript" src="js/jquery.blockUI.js"></script>
        
        <form method='post' action='export.jsp' id='randMapForm'>
            <input type='hidden' name='randMap' id='randMap' />
        </form>
        
        <script>
            
        $(document).ready(function () {
            $("#world").css("width", <%=30 * w%>);
            $("#world").css("height", <%=30 * h%>);
            initContextMenus();
            initDnD();
            appendStack();
        });
        
        var options = {};
        
        </script>
        <script src="js/generator.js"></script>
        <script type="text/javascript" src="js/websocket.js"></script>
    </body>
</html>
