<%@page contentType="text/html"%>
<%@page import="java.util.*"%>
<%@page import="java.net.*"%>
<%@page import="gilbert.extractor.*"%>

<jsp:useBean id="extractor" scope="session" class="gilbert.extractor.jsp.ExtractorBean">
<jsp:setProperty name="extractor"  property="dataSource" value="http://127.0.0.1/marco/url.xml" />
</jsp:useBean>
<jsp:useBean id="state" scope="application" class="gilbert.extractor.jsp.StateBean">
<jsp:setProperty name="state" property="autoReload" value="true" />
<jsp:setProperty name="state" property="baseURI" value="<%request.getRequestURI()%>" />
</jsp:useBean>

<html>
  <head>
    <title></title>
    <link rel="stylesheet" type="text/css" href="standard.css">
  </head>
  <body>
    <a href="<%
        out.print(state.getBaseURI() + "?reload=");
        if (state.getAutoReload()) {
            out.print("off");
        } else {
            out.print("on");
        }
    %>" target="_parent">
    <img
    <%if (state.getAutoReload()) {%>
        src="pics/pause.gif" alt="pause"
    <%} else {%>
        src="pics/start.gif" alt="start"
    <%}%></a>
    <h2>Internal</h2>
    <a href="<%=state.getBaseURI()%>?reload=help" target="_parent">Help page</a><br/>
    <a href="<%=state.getBaseURI()%>?reload=admin" target="_parent">Admin Information</a><br/>
    <h2>Other sites</h2>
    <%
        Vector curSites = extractor.getUrls();
        int count = 25;
        if (curSites.size() < 25) count = curSites.size();
        for (int i=0 ; i < count ; i++) {
            String curSite = ((Properties) curSites.get(i)).getProperty("url.name");
            String siteName = "***Unresolved?***";
            try {
                URL tmpU = new URL(curSite);
                siteName = tmpU.getHost();
            } catch (MalformedURLException e) {
                Util.logMessage("Encountered malformed URL: " + curSite, Util.LOG_MESSAGE);
            }
            out.print("<a href=\"" + state.getBaseURI() + "?reload=site&url=" + curSite + "\" target=\"_parent\">");
            out.print(siteName);
            out.println("</a><br>");
        }    
    %>
  </body>
</html>