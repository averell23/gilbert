/*
 * RedirectingServlet.java
 *
 * Created on 18 January 2002, 11:44
 */


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;
import gilbert.extractor.*;
import gilbert.extractor.extractors.*;
import gilbert.extractor.refiners.*;

/**
 * Servlet that redirects the client to a found page
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class RedirectingServlet extends HttpServlet {
    /// Vector with pages that have been found previously
    protected Vector oldSites;
    /// URL for Fallback page
    protected final String FALLBACK_URL = "http://127.0.0.1/";
    /// URL for the data source.
    protected final String DATA_SOURCE = "http://127.0.0.1/marco/url.xml";
    /// Timeout for reloading the data
    protected final long TIMEOUT = 5 * 60 * 1000; // 5 Minutes for the start
    /// Timestamp of the last reload
    protected long timestamp;
    /// Extraction chain for the extraction
    protected ExtractingChain extractor;
    /// VectorRefiner for the results
    protected VectorRefiner endRef;
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        oldSites = new Vector();
        oldSites.add(FALLBACK_URL);
        extractor = new ExtractingChain(DATA_SOURCE);
        extractor.setExtractor(new StraightExtractor());
        extractor.addRefiner(new SearchingRefiner(true, "ubicomp,handheld,context"));
        endRef = new VectorRefiner();
        extractor.addRefiner(endRef);
        timestamp = 0;
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        response.setContentType("text/html");
        java.io.PrintWriter out = response.getWriter();
        if ((timestamp + TIMEOUT) < System.currentTimeMillis()) {
            timestamp = System.currentTimeMillis();
            endRef.reset();
            extractor.extract();
            Vector result = endRef.getUrlList();
            if (result.size() != 0) {
                oldSites = result;
            }
        }
        int idx = (int) (Math.random() * (oldSites.size()-1));
        String target = oldSites.get(idx).toString();
        /*
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Redirecting...</title>");  
        out.println("<meta http-equiv=\"refresh\" content=\"1; " + target + "\">");
        out.println("</head>");
        out.println("<body>");
        out.println("Please wait while the data is processed...");
        out.println("</body>");
        out.println("</html>");
        */
        response.sendRedirect(target);
        out.close();
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    
}
