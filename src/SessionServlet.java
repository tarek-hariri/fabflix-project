import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@WebServlet(name = "SessionServlet", urlPatterns = "/api/session")
public class SessionServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get a instance of current session on the request
        HttpSession session = request.getSession(true);

        String loggedIn = (String) session.getAttribute("loggedIn");

        JsonObject jsonObject = new JsonObject();
        if(loggedIn == null || loggedIn.equals("false")){
            jsonObject.addProperty("loggedIn", false);
        }
        else{
            jsonObject.addProperty("loggedIn", true);
        }
        out.write(jsonObject.toString());
        // Set response status to 200 (OK)
        response.setStatus(200);
    }
}