import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "ShoppingCartUpdateServlet", urlPatterns = "/api/shoppingcartupdate")
public class ShoppingCartUpdateServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        if(request.getParameter("action").equals("update")) {
            synchronized (previousItems) {
                previousItems.set(3 * Integer.valueOf(request.getParameter("elementIndex")) + 2, request.getParameter("quantity"));
            }
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("total", Integer.parseInt(previousItems.get(3 * Integer.valueOf(request.getParameter("elementIndex")) + 2))*Integer.parseInt(previousItems.get(3 * Integer.valueOf(request.getParameter("elementIndex")) + 1)));
            response.getWriter().write(responseJsonObject.toString());
        }
        else if(request.getParameter("action").equals("delete")){
            synchronized (previousItems) {
                previousItems.remove(3 * Integer.valueOf(request.getParameter("elementIndex")));
                previousItems.remove(3 * Integer.valueOf(request.getParameter("elementIndex")));
                previousItems.remove(3 * Integer.valueOf(request.getParameter("elementIndex")));
            }
        }
    }
}
