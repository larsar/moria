<%@ page import="no.feide.moria.controller.MoriaController,
                 java.util.Map,
                 java.util.HashMap"%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
  <head>
    <title>Moria Web Service - Status</title>
  </head>
  <%
     Map statusMap = MoriaController.getStatus();
     String statusMsg = "";

     if (statusMap != null) {

         String[] states = {"moria", "init", "am", "dm", "sm", "web"};
         Map moduleNames = new HashMap();
         moduleNames.put("init", "Controller");
         moduleNames.put("am", "Authorization manager");
         moduleNames.put("dm", "Directory manager");
         moduleNames.put("sm", "Store manager");
         moduleNames.put("web", "Web application");

         for (int i = 0; i < states.length; i++) {

             Object stateObject = statusMap.get(states[i]);
             Boolean isReady = new Boolean(false);

             if (stateObject instanceof Boolean) {
                 isReady = (Boolean) stateObject;
             }

             if (states[i].equals("moria") && isReady.booleanValue()) {
                 statusMsg = "All ready" + System.getProperty("line.separator");
                 break;
             } else {
                 statusMsg += moduleNames.get(states[i]) + " ready: " + isReady.toString().toUpperCase()
                         + System.getProperty("line.separator");
             }
         }
     }
%>
  <body>
    <p>
      <pre>
        <%= statusMsg %>
      </pre>
    </p>
  </body>
</html>
