<%@page contentType="text/html"%>
<%@page import="java.util.*" %>
<%@page import="gilbert.extractor.*" %>
<%!
    // Global definitions
    /** String that contains the page which is showed on the left side. */
    String selUrl = "nothing.html";
%>

<jsp:useBean id="extractor" scope="session" class="gilbert.extractor.jsp.ExtractorBean">
<jsp:setProperty name="extractor"  property="dataSource" value="http://ubicomp.lancs.ac.uk/~dhahn/cgi-bin/weblog.pl" />
</jsp:useBean>
<jsp:useBean id="state" scope="application" class="gilbert.extractor.jsp.StateBean">
<jsp:setProperty name="state" property="autoReload" value="true" />
<jsp:setProperty name="state" property="reloadFrequency" value="10" />
</jsp:useBean>
    <%
        String turnPar = request.getParameter("reload");
        if (turnPar != null) {
            if (turnPar.equals("on")) {
                state.setAutoReload(true);
            } else if (turnPar.equals("off")) {
                state.setAutoReload(false);
            } else if (turnPar.equals("help")) {
                state.setAutoReload(false);
                selUrl = "help.html";
            } else if (turnPar.equals("admin")) {
                state.setAutoReload(false);
                selUrl = "admin.jsp";
            } else if (turnPar.equals("site")) {
                state.setAutoReload(false);
                selUrl = request.getParameter("url");
            }
        } else {
            state.setAutoReload(true);
        }
        if ((turnPar == null) || (!turnPar.equals("off"))) { // Should preserve state when switching reload off...
            // set the base URI for the state
            state.setBaseURI(request.getRequestURI());
            // select a new URL to show
            extractor.update();
            Vector extractedURLs = extractor.getUrls();
            int selection = (int) (Math.random() * (extractedURLs.size() - 1));
            selUrl = ((Properties) extractedURLs.get(selection)).getProperty("url.name");
            Util.logMessage("Update cycle selected: " + selUrl, Util.LOG_MESSAGE);
        }
    %>

<html>
<head>
<title>Guest Viewer</title>
<%
    // Insert the META header, if the state says the page should be refreshed...
    
    out.print("<meta http-equiv=\"refresh\" ");
    out.print("content=\"");
    if (state.getAutoReload()) {
        out.print(state.getReloadFrequency());
    } else { 
        out.print("1800");
    }
    out.print("; URL=\"" + state.getBaseURI());
    if (!state.getAutoReload()) {
        out.print("?reload=on");
    }
    out.println("\"/>");
%>
</head>

<%--
  <script language="JavaScript">
    var myTimer = setTimeout("reloadFrame()", 50000);

    function reloadFrame() {
        document.location.reload();
        myTimer = setTimeout("reloadFrame()", 50000);
    }
    
    function showAlert() {
        alert("Frame was clicked!");
    }
  </script>
--%>
  <frameset ROWS="80%, 20%">
    <frameset COLS="20%, 80%">
        <frame SRC="left_part.jsp" NAME="left" />
        <frame SRC="<%=selUrl%>" NAME="right" />
    </frameset>
    <frame SRC="bottom_part.jsp" NAME="bottom" />
    <noframes>Your browser doesn't support frames. Guess you already knew that?</noframes>
  </frameset>
</html>
