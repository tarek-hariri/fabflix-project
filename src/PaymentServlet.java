import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String first_name = request.getParameter("first_name");
        String last_name = request.getParameter("last_name");
        String card_number = request.getParameter("card_number");
        String expiration = request.getParameter("expiration");

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                JsonObject responseJsonObject = new JsonObject();

                String query = "SELECT COUNT(*), id FROM creditcards as cc WHERE cc.firstName = ? and cc.lastName = ? and cc.id = ? and cc.expiration = ?";
                PreparedStatement statement = conn.prepareStatement(query);

                statement.setString(1, first_name);
                statement.setString(2, last_name);
                statement.setString(3, card_number);
                statement.setString(4, expiration);

                System.out.println(statement.toString());

                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    if (rs.getInt(1) > 0) {
                        // Payment success:

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                        responseJsonObject.addProperty("ccId", rs.getString("id"));
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
        catch (Exception e) {
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {

        }
    }
}