import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.lang.Math;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        int DEFAULT_NUM_RESULTS = 20;

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String star = request.getParameter("star");
                String genre = request.getParameter("genre");
                String titleSearch = request.getParameter("titleSearch");
                String selectOrder = request.getParameter("selectOrder");
                String numResults = request.getParameter("numResults");
                String page = request.getParameter("page");
                String fulltext = request.getParameter("fulltext");
                String logFilePath = (String) request.getAttribute("logFilePath");

                if (!title.equals("") && !titleSearch.equals("true") && !fulltext.equals("true"))
                    title = " and movies.title LIKE '%" + title + "%'";

                if (!director.equals(""))
                    director = " and movies.director LIKE '%" + director + "%'";

                if (!year.equals(""))
                    year = " and movies.year = " + year;

                if (page != null && !page.equals("")) {
                    if (numResults != null && !numResults.equals(""))
                        page = " OFFSET " + Integer.valueOf(page) * Integer.valueOf(numResults);
                    else
                        page = " OFFSET " + Integer.valueOf(page) * Integer.valueOf(DEFAULT_NUM_RESULTS); // Case where next page is called before number of results is specified
                } else {
                    page = " OFFSET 0";
                }

                if (numResults != null && !numResults.equals("")) {
                    numResults = " LIMIT " + numResults;
                } else {
                    numResults = " LIMIT " + DEFAULT_NUM_RESULTS;
                }

                if (selectOrder != null || !selectOrder.equals("")) {
                    if (selectOrder.equals("TURD")) {
                        selectOrder = " ORDER BY title ASC, ratings.rating DESC";
                    } else if (selectOrder.equals("TURU")) {
                        selectOrder = " ORDER BY title ASC, rating ASC";
                    } else if (selectOrder.equals("TDRD")) {
                        selectOrder = " ORDER BY title DESC, rating DESC";
                    } else if (selectOrder.equals("TDRU")) {
                        selectOrder = " ORDER BY title DESC, rating ASC";
                    } else if (selectOrder.equals("RUTD")) {
                        selectOrder = " ORDER BY rating ASC, title DESC";
                    } else if (selectOrder.equals("RUTU")) {
                        selectOrder = " ORDER BY rating ASC, title ASC";

                    } else if (selectOrder.equals("RDTD")) {
                        selectOrder = " ORDER BY rating DESC, title DESC";
                    } else if (selectOrder.equals("RDTU")) {
                        selectOrder = " ORDER BY rating DESC, title ASC";
                    }
                }
                String query;
                if (titleSearch.equals("true")) {
                    if (title.equals("*"))
                        query = "SELECT * from movies,ratings WHERE movies.id = ratings.movieId and movies.title REGEXP '^[^0-9A-Za-z]'" + selectOrder + numResults + page;
                    else
                        query = "SELECT * from movies,ratings WHERE movies.id = ratings.movieId and movies.title LIKE '" + title + "%'" + selectOrder + numResults + page;
                } else if (!genre.equals("")) {
                    query = "SELECT * FROM movies, genres_in_movies,genres,ratings WHERE movies.id = ratings.movieId and movies.id = genres_in_movies.movieId and genres.name ='" + genre + "' and genres.genreId = genres_in_movies.genreId" + selectOrder + numResults + page;
                } else if (fulltext.equals("true")) {
                    query = "SELECT * FROM movies,ratings WHERE movies.id = ratings.movieId AND MATCH (movies.title) AGAINST (";
                    String[] titleArr = title.split(" ");
                    for (String s : titleArr) {
                        query += "'+" + s + "*' ";
                    }
                    query += "IN BOOLEAN MODE)" + numResults + page;
                } else {
                    query = "SELECT * from movies,ratings WHERE movies.id = ratings.movieId" + title + year + director + " and movies.id IN (SELECT movieId FROM stars, stars_in_movies WHERE stars.id = stars_in_movies.starId and movies.id = stars_in_movies.movieId and stars.name LIKE '%" + star + "%')" + selectOrder + numResults + page;
                }
                System.out.println(query);
                // Perform the query
                long startTime = System.nanoTime();
                PreparedStatement statement = conn.prepareStatement(query);
                long endTime = System.nanoTime();
                long totalQueryTime = endTime - startTime;
                ResultSet rs = statement.executeQuery();

                JsonArray jsonArray = new JsonArray();

                // Iterate through each row of rs
                while (rs.next()) {
                    String movieId = rs.getString("id");

                    String movie_title = rs.getString("title");
                    String movie_year = rs.getString("year");
                    String movie_director = rs.getString("director");
                    ArrayList<String> movie_genres = new ArrayList<String>();
                    ArrayList<String> movie_stars = new ArrayList<String>();
                    float movie_rating = 0; // NO DATA FOR RATING MEANS RATING IS 0!

                    String genreQuery = "SELECT name FROM genres WHERE genreId IN (SELECT genreId FROM genres_in_movies WHERE movieId = '" + movieId + "') ORDER BY genres.name ASC";
                    PreparedStatement statementGenres = conn.prepareStatement(genreQuery);

                    startTime = System.nanoTime();
                    ResultSet rsGenre = statementGenres.executeQuery();
                    endTime = System.nanoTime();
                    totalQueryTime += endTime - startTime;

                    int genreNum = 0;
                    while (rsGenre.next() && genreNum < 3) {
                        movie_genres.add(rsGenre.getString("name"));
                        genreNum++;
                    }

                    JsonArray genreJson = new Gson().toJsonTree(movie_genres).getAsJsonArray();

                    String starsQuery = "SELECT name, id FROM stars, stars_in_movies WHERE stars.id = starId and stars.id IN (SELECT starId FROM stars_in_movies WHERE stars_in_movies.movieId = '" + movieId + "') GROUP BY starId ORDER BY COUNT(movieId) DESC, stars.name ASC";
                    PreparedStatement statementStars = conn.prepareStatement(starsQuery);

                    startTime = System.nanoTime();
                    ResultSet rsStars = statementStars.executeQuery();
                    endTime = System.nanoTime();
                    totalQueryTime += endTime - startTime;

                    int starsNum = 0;
                    while (rsStars.next() && starsNum < 3) {
                        movie_stars.add(rsStars.getString("id"));
                        movie_stars.add(rsStars.getString("name"));
                        starsNum++;
                    }

                    JsonArray starsJson = new Gson().toJsonTree(movie_stars).getAsJsonArray();

                    String ratingQuery = "SELECT rating FROM ratings WHERE movieId = '" + movieId + "'";
                    PreparedStatement statementRating = conn.prepareStatement(ratingQuery);

                    startTime = System.nanoTime();
                    ResultSet rsRating = statementRating.executeQuery();
                    endTime = System.nanoTime();
                    totalQueryTime += endTime - startTime;

                    if (rsRating.next()) {
                        movie_rating = rsRating.getFloat("rating");
                    }

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("movie_director", movie_director);
                    jsonObject.add("movie_genres", genreJson);
                    jsonObject.add("movie_stars", starsJson);
                    jsonObject.addProperty("movie_rating", movie_rating);

                    jsonArray.add(jsonObject);

                    statementGenres.close();
                    statementStars.close();
                    statementRating.close();

                    rsGenre.close();
                    rsStars.close();
                    rsRating.close();
                }
                rs.close();
                statement.close();
                if(logFilePath != null && !logFilePath.equals("")){
                    System.out.println(totalQueryTime);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath,true));
                    writer.write(String.valueOf(totalQueryTime)+" ");
                    writer.close();
                }

                // Log to localhost log
                request.getServletContext().log("getting " + jsonArray.size() + " results");

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
            System.out.println(e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        doGet(request, response);
    }
}
