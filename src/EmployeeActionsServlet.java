import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "EmployeeActionsServlet", urlPatterns = "/api/employeeactions")
public class EmployeeActionsServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                System.out.println("conn is null.");
            }
            else {
                PrintWriter out = response.getWriter();


                String query = " SELECT tables.TABLE_NAME, columns.COLUMN_NAME, columns.DATA_TYPE FROM INFORMATION_SCHEMA.TABLES as tables,INFORMATION_SCHEMA.COLUMNS as columns WHERE tables.TABLE_SCHEMA = 'moviedb' and tables.TABLE_NAME = columns.TABLE_NAME;";
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();

                JsonObject responseJsonObject = new JsonObject();
                HashMap<String, ArrayList<HashMap<String, String>>> tables = new HashMap<String, ArrayList<HashMap<String, String>>>();

                while (rs.next()) {
                    if (tables.containsKey(rs.getString("TABLE_NAME"))) {
                        HashMap<String, String> column = new HashMap<String, String>();
                        column.put("column_name", rs.getString("COLUMN_NAME"));
                        column.put("data_type", rs.getString("DATA_TYPE"));

                        tables.get(rs.getString("TABLE_NAME")).add(column);
                    } else {
                        HashMap<String, String> column = new HashMap<String, String>();
                        column.put("column_name", rs.getString("COLUMN_NAME"));
                        column.put("data_type", rs.getString("DATA_TYPE"));

                        ArrayList<HashMap<String, String>> table_info = new ArrayList<HashMap<String, String>>();
                        table_info.add(column);

                        tables.put(rs.getString("TABLE_NAME"), table_info);
                    }
                }

                Gson gson = new Gson();
                String tablesJson = gson.toJson(tables);

                //JsonArray tablesJson = new Gson().toJsonTree(tables).getAsJsonArray();
                out.write(tablesJson);

                statement.close();
            }
        }
        catch (Exception e) {
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = dataSource.getConnection()) {
            if(request.getParameter("title")!=null){
                // Movie insert
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String starName = request.getParameter("star_name");
                String birthYear = request.getParameter("birth_year");
                String genre = request.getParameter("genre");

                JsonObject responseJsonObject = new JsonObject();

                if(title.equals("")||year.equals("")||director.equals("")){
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "Title, year, and director are required fields.");
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }

                String query = "CALL insert_movie(?,?,?,?,?,?)";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, title);
                statement.setInt(2, Integer.parseInt(year));
                statement.setString(3, director);
                statement.setString(4, starName);
                if(birthYear.equals("")) {
                    statement.setNull(5, Types.INTEGER);
                }
                else {
                    statement.setInt(5, Integer.parseInt(birthYear));
                }
                statement.setString(6, genre);
                System.out.println(statement.toString());

                ResultSet rs = statement.executeQuery();

                while(rs.next()){
                    if(rs.getString(1).equals("Movie already exists!")){
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Movie already exists!");
                    }
                    else{
                        String movieId = rs.getString("maxMovieId");
                        String genreId = rs.getString("maxGenreId");
                        String starId = rs.getString("maxStarId");
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "movieId: "+ movieId +", genreId: "+genreId+", starId: "+ starId);
                    }
                }
                response.getWriter().write(responseJsonObject.toString());
                statement.close();
            }
            else {
                // Star insert
                String name = request.getParameter("name");
                String year = request.getParameter("year");

                JsonObject responseJsonObject = new JsonObject();

                if (name.equals("")) {
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("message", "Star name is a required field.");
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }

                String query = "CALL insert_star(?,?)";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, name);

                if (year.equals("")) {
                    statement.setString(2, null);
                } else {
                    statement.setString(2, year);
                }

                ResultSet rs = statement.executeQuery();
                rs.next();

                responseJsonObject.addProperty("status","success");
                responseJsonObject.addProperty("message", "starId: "+rs.getString("maxString"));
                response.getWriter().write(responseJsonObject.toString());

                statement.close();
            }
        }
        catch (Exception e) {
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}