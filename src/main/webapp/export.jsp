<%@page import="java.text.SimpleDateFormat"%><%@page import="java.util.Date"%><%@page import="org.json.JSONArray"%><%@page import="org.json.JSONObject"%><%@page import="java.io.*"  %><%
    SimpleDateFormat sm = new SimpleDateFormat("d-MMM-m H-m-s");
    JSONObject jsn = new JSONObject((String)request.getParameter("randMap"));
    JSONArray size = jsn.getJSONArray("size");
    JSONArray walls = jsn.getJSONArray("bricks");
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition","attachment;filename="+sm.format(new Date())+"_map.txt");
    out.print(size.get(0)+"x"+size.get(1));
    for (int i = 0; i < walls.length(); i++){
        String brick = walls.getString(i);
        if (i % size.getInt(0) == 0) out.println();
        out.print(brick);
    }
%>