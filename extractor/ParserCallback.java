package gilbert.extractor;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.*;

/**
 * Parser callback class. This will call the handler functions in the
 * WebSearch class.
 */
class ParserCallback extends HTMLEditorKit.ParserCallback {
    WebSearch myParent;
    
    public ParserCallback(WebSearch theSearch) {
        myParent = theSearch;
    }
    
    public void handleComment(char[] data, int pos) {
        myParent.handleHTMLComment(data, pos);
    }
    
    public void handleEndTag(HTML.Tag t, int pos) {
        myParent.handleHTMLEndTag(t, pos);
    }
    
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        myParent.handleHTMLSimpleTag(t, a, pos);
    }
    
    public void handleText(char[] data, int pos) {
        myParent.handleHTMLText(data, pos);
    }
    
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        myParent.handleHTMLStartTag(t, a, pos);
    }
}