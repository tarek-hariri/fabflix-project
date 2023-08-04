import com.google.gson.JsonObject;
import com.mysql.cj.x.protobuf.MysqlxPrepare;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employee")
public class EmployeeLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(password);


        // Verify reCAPTCHA

        // If session not verified already
        if(request.getSession().getAttribute("recaptchaVerified")==null || !request.getSession().getAttribute("recaptchaVerified").equals("true")) {
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                request.getSession().setAttribute("recaptchaVerified", "true");
            } catch (Exception e) {

                request.getServletContext().log("Recaptcha failed");
                request.getSession().setAttribute("recaptchaVerified", "false");
                return;
            }
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                JsonObject responseJsonObject = new JsonObject();

                String passwordQuery = "SELECT * FROM employees WHERE employees.email = ?";
                PreparedStatement passStatement = conn.prepareStatement(passwordQuery);
                passStatement.setString(1, username);
                ResultSet passRS = passStatement.executeQuery();
                boolean success = false;
                String dbPass = "";
                while (passRS.next()) {
                    dbPass = passRS.getString("password");
                    success = new StrongPasswordEncryptor().checkPassword(password, dbPass);
                }
                passStatement.close();
                if (!success) {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "Invalid credentials");
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }
                // Login success:

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));
                request.getSession().setAttribute("loggedIn", new String("true"));
                request.getSession().setAttribute("employee", new String("true"));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                response.getWriter().write(responseJsonObject.toString());
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