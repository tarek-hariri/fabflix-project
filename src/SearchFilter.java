import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.*;


import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "SearchFilter", urlPatterns = "/api/movies")
public class SearchFilter implements Filter {
    private String logFilePath = null;
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Generate file name
        if(logFilePath == null){
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            //String contextPath = httpRequest.getServletContext().getRealPath("/");
            String xmlFilePath = "/var/log/tomcat10/log";
            //String xmlFilePath=contextPath+"\\log" + String.valueOf(System.nanoTime()); // Unique file name
            File myfile = new File(xmlFilePath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFilePath));
            writer.append("");
            writer.close();
            logFilePath = xmlFilePath;
        }

        request.setAttribute("logFilePath",logFilePath);
        long startTime = System.nanoTime();
        chain.doFilter(request, response);
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime; // elapsed time in nano seconds. Note: print the values in nanoseconds

        // Add TS to the log file
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath,true));
        writer.write(String.valueOf(elapsedTime)+"\n");
        writer.close();

    }

    public void init(FilterConfig fConfig) {
        // ignored ...
    }

    public void destroy() {
        // ignored.
    }

}