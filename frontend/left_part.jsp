<%@page contentType="text/html"%>
<%@page import="java.util.*"%>

<jsp:useBean id="extractor" scope="application" class="gilbert.extractor.jsp.ExtractorBean">
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
    <h2>Other sites</h2>
    <%
        Vector curSites = extractor.getUrls();
        int count = 25;
        if (curSites.size() < 25) count = curSites.size();
        for (int i=0 ; i < count ; i++) {
            String curSite = ((Properties) curSites.get(i)).getProperty("url.name");
            out.print("<a href=\"" + curSite + "\" target=\"right\">");
            out.print(curSite);
            out.println("</a><br>");
        }    
    %>
  </body>
</html>