<%@page contentType="text/html"%>
<%@page import="java.util.*"%>
<%@page import="gilbert.extractor.*"%>

<jsp:useBean id="extractor" scope="session" class="gilbert.extractor.jsp.ExtractorBean">
<jsp:setProperty name="extractor"  property="dataSource" value="http://127.0.0.1/marco/url.xml" />
</jsp:useBean>
<jsp:useBean id="state" scope="application" class="gilbert.extractor.jsp.StateBean">
<jsp:setProperty name="state" property="autoReload" value="true" />
<jsp:setProperty name="state" property="baseURI" value="<%request.getRequestURI()%>" />
</jsp:useBean>


<%
    //set up the different time strings
    String lastTimestamp = Util.dateToString(extractor.getTimestamp());
    String currentTime = Util.dateToString(System.currentTimeMillis());
%>
<html>
<head>
<title></title>
<link rel="stylesheet" type="text/css" href="standard.css">
</head>
<body>
    <h1>Administrational Information</h1>
    <p>
    <b>Page Creation Time:</b> <%=currentTime%><br/>
    <b>Last Extractor Run:</b> <%=lastTimestamp%><br/>
    <b>Extractor Timeout:</b> <%=(extractor.getTimeout()/1000)%> seconds</br>
    <b>Page Reload Inteval:</b> <%=state.getReloadFrequency()%> seconds</br>
    </p>
    <p>
    <b>Servlet Session ID:</b> <%=request.getSession(false).getId()%></br>
    <b>Extractor Data Source:</b> <%=extractor.getDataSource()%>
    </p>
    <p>
    List of extracted URLS:
    <ul>
        <%
            Enumeration urlList = extractor.getUrls().elements();
            while (urlList.hasMoreElements()) {
                VisitorURL thisURL = (VisitorURL) urlList.nextElement();
        %>
        <li>Host: <%=thisURL.getProperty("url.name")%>
        <ul>
            <%
                if (thisURL.getProperty("url.timestamp") != null)
                    out.println("<li><b>Timestamp:</b> " + thisURL.getProperty("url.timestamp") + "</li>");
                if (thisURL.getProperty("url.location_code") != null)
                    out.println("<li><b>Location Code:</b> " + thisURL.getProperty("url.location_code") + "</li>");
                if (thisURL.getProperty("url.title") != null)
                    out.println("<li><b>Title:</b> " + thisURL.getProperty("url.title") + "</li>");
                if (thisURL.getProperty("url.description") != null) 
                    out.println("<li><b>Description:</b> " + thisURL.getProperty("url.description") + "</li>");
                Enumeration keywordL = thisURL.getKeywords().elements();
                while (keywordL.hasMoreElements()) {
                    out.println("<li><b>Keyword:</b> " + keywordL.nextElement() + "</li>");
                }
            %>
        </ul>
        </li>
        <%} // close while loop %>
    </ul>
    </p>
</body>
</html>
