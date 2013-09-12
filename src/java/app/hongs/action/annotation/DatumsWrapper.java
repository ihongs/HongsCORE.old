/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.hongs.action.annotation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 返回数据包裹
 * @author Hong
 */
public class DatumsWrapper extends HttpServletResponseWrapper {

    private StringWriter output;
    private  PrintWriter writer;

    public DatumsWrapper(HttpServletResponse response) {
        super( response );
        output = new StringWriter();
        writer = new  PrintWriter(output);
    }

    @Override
    public PrintWriter getWriter()
    throws IOException {
        return writer;
    }

    @Override
    public String toString() {
        writer.flush();
        return output.toString();
    }

    @Override
    public void finalize()
    throws Throwable {
         super.finalize();
         output.close();
         writer.close();
    }

}
