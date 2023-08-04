import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                // Get a connection from dataSource

                // Construct a query with parameter represented by "?"
                String query = "SELECT * FROM movies, ratings WHERE movies.id = ratings.movieId and movies.id = ?";
                // Declare our statement
                PreparedStatement statement = conn.prepareStatement(query);

                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                statement.setString(1, id);

                // Perform the query
                ResultSet rs = statement.executeQuery();

                JsonArray jsonArray = new JsonArray();

                // Iterate through each row of rs
                while (rs.next()) {
                    String movieTitle = rs.getString("title");
                    String movieYear = rs.getString("year");
                    String movieDirector = rs.getString("director");
                    float rating = rs.getFloat("rating");

                    ArrayList<String> movie_genres = new ArrayList<String>();
                    ArrayList<String> movie_stars = new ArrayList<String>();

                    String genreQuery = "SELECT name FROM genres WHERE genreId IN (SELECT genreId FROM genres_in_movies WHERE movieId = '" + id + "') ORDER BY name ASC";
                    PreparedStatement statementGenres = conn.prepareStatement(genreQuery);
                    ResultSet rsGenre = statementGenres.executeQuery();
                    while (rsGenre.next()) {
                        movie_genres.add(rsGenre.getString("name"));
                    }

                    JsonArray genreJson = new Gson().toJsonTree(movie_genres).getAsJsonArray();

                    String starsQuery = "SELECT name, id FROM stars, stars_in_movies WHERE stars.id = starId and stars.id IN (SELECT starId FROM stars_in_movies WHERE stars_in_movies.movieId = '" + id + "') GROUP BY starId ORDER BY COUNT(movieId) DESC, stars.name ASC";
                    PreparedStatement statementStars = conn.prepareStatement(starsQuery);
                    ResultSet rsStars = statementStars.executeQuery();
                    while (rsStars.next()) {
                        movie_stars.add(rsStars.getString("id"));
                        movie_stars.add(rsStars.getString("name"));
                    }

                    JsonArray starsJson = new Gson().toJsonTree(movie_stars).getAsJsonArray();


                    // Create a JsonObject based on the data we retrieve from rs

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", id);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.add("movie_genres", genreJson);
                    jsonObject.add("movie_stars", starsJson);
                    jsonObject.addProperty("movie_rating", rating);

                    jsonArray.add(jsonObject);

                    statementGenres.close();
                    statementStars.close();
                    rsGenre.close();
                    rsStars.close();
                }
                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
