/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.hongs.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 返回数据包裹
 * @author Hong
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private StringWriter output;
    private  PrintWriter writer;

    public ResponseWrapper(HttpServletResponse response) {
        super( response );
        output = new StringWriter();
        writer = new  PrintWriter(output);
    }

    @Override
    public void finalize()
    throws Throwable {
         super.finalize();
         output.close();
         writer.close();
    }

    @Override
    public PrintWriter getWriter()
    throws IOException {
        return writer;
    }

    @Override
    public String toString() {
               writer.  flush ();
        return output.toString();
    }

}
