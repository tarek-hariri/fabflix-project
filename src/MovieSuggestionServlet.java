import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = dataSource.getConnection()){
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                //JsonObject jsonObject = new JsonObject();
                JsonArray suggestions = new JsonArray();

                String query = request.getParameter("query");

                if (query == null || query.trim().isEmpty()) {
                    response.getWriter().write("empty");
                    return;
                }

                String fullQuery = "SELECT * FROM movies,ratings WHERE movies.id = ratings.movieId AND MATCH (movies.title) AGAINST (";
                String[] titleArr = query.split(" ");
                for (String s : titleArr) {
                    fullQuery += "'+" + s + "*' ";
                }

                fullQuery += "IN BOOLEAN MODE) LIMIT 10";
                System.out.println(fullQuery);

                PreparedStatement statement = conn.prepareStatement(fullQuery);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String title = rs.getString("title");
                    String movieId = rs.getString("id");
                    suggestions.add(generateJsonObject(title, movieId));
                }
                //jsonObject.add("suggestions",suggestions);

                response.getWriter().write(suggestions.toString());
            }
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }

    private static JsonObject generateJsonObject(String title, String movieId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        jsonObject.addProperty("data", movieId);
        return jsonObject;
    }


}