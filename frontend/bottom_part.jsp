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
<p>
This page has been automatically created at <%=currentTime%>. 
</p>
<p>
<p>
<b>Log Analyzer:</b> The last run of the log Analyzer was at <%=lastTimestamp%>
and it is set to run every <%=(extractor.getTimeout() / 1000)%> seconds.
<%=(extractor.getUrls().size())%> URLs have been found.
</p>
<p>
<b>Page Reload:</b> Automatic page reload is now <b>
<%
    if (state.getAutoReload()) {
        out.print("on");
    } else {
        out.print("off");
    }
%>
</b>. The frequency of the automatic page reload is <%=state.getReloadFrequency()%>
seconds. 
<a href="<%
    out.print(state.getBaseURI() + "?reload=");
    if (state.getAutoReload()) {
        out.print("off");
    } else {
        out.print("on");
    }
%>" target="_parent">Turn <%
    if (state.getAutoReload()) {
        out.print("off");
    } else {
        out.print("on");
    }
%></a>
</p>
<p>
<b>Session ID:</b> <%=request.getSession(false).getId()%>
</p>
</body>
</html>