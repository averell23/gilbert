<%@page contentType="text/html"%>
<%@page import="java.util.*" %>
<%@page import="gilbert.extractor.*" %>

<jsp:useBean id="extractor" scope="session" class="gilbert.extractor.jsp.ExtractorBean">
<jsp:setProperty name="extractor"  property="dataSource" value="http://ubicomp.lancs.ac.uk/~dhahn/cgi-bin/weblog.pl" />
</jsp:useBean>
<jsp:useBean id="state" scope="application" class="gilbert.extractor.jsp.StateBean">
<jsp:setProperty name="state" property="autoReload" value="true" />
<jsp:setProperty name="state" property="reloadFrequency" value="10" />
</jsp:useBean>
    <%
        // set the base URI for the state
        state.setBaseURI(request.getRequestURI());
        // select a new URL to show
        extractor.update();
        Vector extractedURLs = extractor.getUrls();
        int selection = (int) (Math.random() * (extractedURLs.size() - 1));
        String selUrl = ((Properties) extractedURLs.get(selection)).getProperty("url.name");
    %>

<html>
<head>
<title>Guest Viewer</title>
<%
    String turnPar = request.getParameter("reload");
    if ((turnPar != null) && turnPar.equals("off")) {
        state.setAutoReload(false);
    }
    if ((turnPar != null) && turnPar.equals("on")) {
        state.setAutoReload(true);
    }
    // Insert the META header, if the state says the page should be refreshed...
    if (state.getAutoReload()) {
        out.print("<meta http-equiv=\"refresh\" ");
        out.print("content=\"" + state.getReloadFrequency());
        out.println("; URL=\"" + state.getBaseURI() + "\"/>");
    }
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
