import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ccId = request.getParameter("ccId");

        HttpSession session = request.getSession();
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                JsonObject responseJsonObject = new JsonObject();
                for (int i = 0; i < previousItems.size(); i++) {
                    String query = "INSERT INTO sales()";
                    PreparedStatement statement = conn.prepareStatement(query);

                    System.out.println(statement.toString());

                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        if (rs.getInt(1) > 0) {
                            // Payment success:

                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", "success");
                        } else {
                            // Login fail
                            responseJsonObject.addProperty("status", "fail");
                            // Log to localhost log
                            request.getServletContext().log("Payment failed");
                            responseJsonObject.addProperty("message", "Payment failed. Please re-enter payment information.");
                        }
                    }
                    response.getWriter().write(responseJsonObject.toString());
                    rs.close();
                    statement.close();
                }
            }
        }
        catch (Exception e) {
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {

        }
    }
}