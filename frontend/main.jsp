<%@page contentType="text/html"%>
<%@page import="java.util.*" %>
<%@page import="gilbert.extractor.*" %>
<%@page import="org.apache.log4j.*" %>
<%!
    // Global definitions
    /** String that contains the page which is showed on the left side. */
    String selUrl = "nothing.html";
    /// Logger for this JSP
    Logger logger = Logger.getLogger("main.jsp");
%>

<jsp:useBean id="extractor" scope="session" class="gilbert.extractor.jsp.ExtractorBean">
<jsp:setProperty name="extractor"  property="dataSource" value="http://ubicomp.lancs.ac.uk/~dhahn/cgi-bin/weblog.pl" />
</jsp:useBean>
<jsp:useBean id="state" scope="application" class="gilbert.extractor.jsp.StateBean">
<jsp:setProperty name="state" property="autoReload" value="true" />
<jsp:setProperty name="state" property="reloadFrequency" value="10" />
</jsp:useBean>
    <%
        String sid = request.getSession(false).getId();
        NDC.push(sid);
        String turnPar = request.getParameter("reload");
        if (turnPar != null) {
            if (turnPar.equals("on")) {
                state.setAutoReload(true);
                // Util.logMessage("[User event, sid: " + sid + "] User turned reload on.", Util.LOG_ERROR);
            } else if (turnPar.equals("off")) {
                state.setAutoReload(false);
                logger.info("User turned reload off.");
            } else if (turnPar.equals("help")) {
                state.setAutoReload(false);
                selUrl = "help.html";
                logger.info("User called help page.");
            } else if (turnPar.equals("admin")) {
                state.setAutoReload(false);
                selUrl = "admin.jsp";
                logger.info("User called admin page.");
            } else if (turnPar.equals("site")) {
                state.setAutoReload(false);
                selUrl = request.getParameter("url");
                logger.info("User called site: " + request.getParameter("url"));
            }
        } else {
            state.setAutoReload(true);
        }
        if ((turnPar == null) || (turnPar.equals("on"))) { // Should preserve state when switching reload off...
            // set the base URI for the state
            state.setBaseURI(request.getRequestURI());
            logger.debug("Updating URL selection now.");
            // select a new URL to show
            extractor.update();
            logger.debug("Update call returned.");
            Collection extractedURLs = extractor.getUrls();
            if (extractedURLs.size() > 0) {
                int selection = (int) ((Math.random() * extractedURLs.size()) + 1);
                if (selection > extractedURLs.size()) selection--;
                if (logger.isDebugEnabled()) logger.debug("Going to select URL no. " + selection + " of " + extractedURLs.size());
                Iterator extractedIterator = extractedURLs.iterator();
                Object selO = null;
                for (int i = 0 ; i < selection ; i++) {
                    selO = extractedIterator.next();
                }
                if (logger.isDebugEnabled()) logger.debug("Select object is an " + selO.getClass().getName());
                selUrl = ((Properties) selO).getProperty("url.name");
                if (logger.isDebugEnabled()) logger.debug("Update cycle selected: " + selUrl);
            } else {
                logger.warn("Could not update: No URL found.");
            }
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
    NDC.pop();
%>
  <meta http-equiv="cache-control" content="no-cache" />
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
  <frameset COLS="20%, 80%">
    <frame SRC="left_part.jsp" NAME="left" />
    <frame SRC="<%=selUrl%>" NAME="right" />
    <noframes>Your browser doesn't support frames. Guess you already knew that?</noframes>
  </frameset>
</html>
