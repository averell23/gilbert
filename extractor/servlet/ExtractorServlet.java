
/*
 * ExtractorServlet.java
 *
 * Created on 15 January 2002, 16:27
 */
 

import javax.servlet.*;
import javax.servlet.http.*;
import gilbert.extractor.*;
import gilbert.extractor.extractors.*;
import gilbert.extractor.refiners.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;

/** 
 * This will return an HTML/XML page with extracted URL information.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class ExtractorServlet extends HttpServlet {
    /// Default URL for original data
    public final String DATA_HOME = "http://127.0.0.1/marco/url.xml";
    /// The Extractor Chain
    ExtractingChain extractorChain;
   
    /** Initializes the servlet.
    */  
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        extractorChain = new ExtractingChain(DATA_HOME);
        extractorChain.setExtractor(new StraightExtractor());
        SearchingRefiner sRef = new SearchingRefiner();
        sRef.setKeywords("ubicom,handheld,context");
        sRef.setPassing(true);
        extractorChain.addRefiner(sRef);
        extractorChain.addRefiner(new HtmlRefiner());
        Util.setLogLevel(Util.LOG_DEBUG);
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
        ServletOutputStream out = response.getOutputStream();
        extractorChain.setOutputStream(out);
        extractorChain.extract();
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
