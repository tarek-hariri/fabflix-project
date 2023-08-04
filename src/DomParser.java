import com.mysql.cj.x.protobuf.MysqlxPrepare;
import jakarta.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;

public class DomParser {

    List<DirectorFilm> directorFilms = new ArrayList<>();
    //List<Actor> actors = new ArrayList<>();
    HashMap<String, Actor> actors = new HashMap<>();
    HashMap<String, ArrayList<Actor>> actorsInMovie = new HashMap<>();
    Document dom;

    int genreInconsistency = 0;
    int movieInconsistency = 0;
    public void runExample() throws Exception{

        parseXmlFile("stanford-movies/mains243.xml");
        parseMainsDocument();
        //printFilmsData();
        System.out.println("Done with films");

        parseXmlFile("stanford-movies/actors63.xml");
        parseActorsDocument();
        //printActorData();
        System.out.println("Done with actors");

        parseXmlFile("stanford-movies/casts124.xml");
        parseCastsDocument();
        //printCastsData();
        System.out.println("Done with casts");

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        int movieId = 0;
        int starId = 0;
        String moviePrefix = "iM"; // insert Movie
        String starPrefix = "iS"; // insert Star

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection movieConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Connection starConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Connection batchConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Connection gimConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Connection genreConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Connection ratingsConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        // CACHE GENRES
        HashMap<String, String> genreMap = new HashMap<>();
        PreparedStatement genres = movieConnection.prepareStatement("SELECT * FROM genres");
        ResultSet genresRs = genres.executeQuery();

        int maxGenreId = -1; // Next available genreId
        while(genresRs.next()){
            String genreId = genresRs.getString("genreId");
            String genreName = genresRs.getString("name");
            if(Integer.valueOf(genreId)>maxGenreId){
                maxGenreId = Integer.valueOf(genreId);
            }

            genreMap.put(genreName, genreId);
        }
        maxGenreId++;


        movieConnection.setAutoCommit(false);
        int movieBatchSize = 0;
        PreparedStatement insertMovie = movieConnection.prepareStatement("INSERT INTO movies(id,title,year,director) VALUES (?,?,?,?)"); // Just need to initialize this

        starConnection.setAutoCommit(false);
        int starBatchSize = 0;
        PreparedStatement insertStar = starConnection.prepareStatement("INSERT INTO stars(id,name,birthYear) VALUES (?,?,?)"); // Just need to initialize this

        batchConnection.setAutoCommit(false);
        int batchSize = 0;
        PreparedStatement insertSIM = batchConnection.prepareStatement("INSERT INTO stars_in_movies(starId,movieId) VALUES (?,?)"); // Just need to initialize this

        gimConnection.setAutoCommit(false);
        int gimBatchSize = 0;
        PreparedStatement insertGIM = gimConnection.prepareStatement("INSERT INTO genres_in_movies(genreId,movieId) VALUES(?,?)");

        ratingsConnection.setAutoCommit(false);
        int ratingsBatchSize = 0;
        PreparedStatement insertRating = ratingsConnection.prepareStatement("INSERT INTO ratings(movieId,rating,numVotes) VALUES(?,-1,-1)");



        for(int i = 0; i < directorFilms.size(); i++){
            for(int j = 0; j < directorFilms.get(i).getFilms().size(); j++){
                Film movie = directorFilms.get(i).getFilms().get(j);

                insertMovie.setString(1, moviePrefix+String.valueOf(movieId));
                insertMovie.setString(2, movie.getTitle());
                insertMovie.setInt(3, movie.getYear());
                insertMovie.setString(4, movie.getDirector());
                int thisMovieId = movieId;
                movieId++;

                if(movie.getTitle() == null || movie.getDirector() == null || movie.getYear() == -1){ //Deals with inconsistent data
                    movieInconsistency++;
                    continue;
                }

                insertMovie.addBatch();
                movieBatchSize++;


                insertRating.setString(1,moviePrefix+String.valueOf(thisMovieId));
                insertRating.addBatch();
                ratingsBatchSize++;

                String movieGenre = movie.getGenre();
                if(genreMap.containsKey(movieGenre)){

                    insertGIM.setInt(1, Integer.valueOf(genreMap.get(movie.getGenre())));
                    insertGIM.setString(2, moviePrefix+String.valueOf(thisMovieId));
                    insertGIM.addBatch();
                    gimBatchSize++;
                }
                else{ // Create new genre, store in hashMap, insert into
                    PreparedStatement newGenre = genreConnection.prepareStatement("INSERT INTO genres(genreId,name) VALUES(?,?)");
                    newGenre.setInt(1,maxGenreId);
                    newGenre.setString(2,movieGenre);
                    newGenre.executeUpdate();
                    genreMap.put(movieGenre,String.valueOf(maxGenreId));

                    insertGIM.setInt(1, maxGenreId);
                    insertGIM.setString(2, moviePrefix+String.valueOf(thisMovieId));
                    insertGIM.addBatch();
                    gimBatchSize++;

                    maxGenreId++;
                }


                ArrayList<Actor> actorsList = actorsInMovie.get(movie.getFid());
                // For each actor in the movie
                if(actorsList==null){
                    continue;
                }
                for(Actor actor : actorsList){
                    if(!actors.containsKey(actor.getName())){
                        // Insert with null bday

                        insertStar.setString(1, starPrefix+String.valueOf(starId));
                        insertStar.setString(2, actor.getName());
                        insertStar.setNull(3, Types.INTEGER);
                        insertStar.addBatch();
                        int thisStarId = starId;
                        starId++;
                        starBatchSize++;

                        insertSIM.setString(1, starPrefix+String.valueOf(thisStarId));
                        insertSIM.setString(2, moviePrefix+String.valueOf(thisMovieId));
                        insertSIM.addBatch();
                        batchSize++;
                    }
                    else {
                        int birthYear = actors.get(actor.getName()).getBirthYear();


                        insertStar.setString(1, starPrefix+String.valueOf(starId));
                        insertStar.setString(2, actor.getName());
                        if(birthYear == -1) {
                            insertStar.setNull(3, Types.INTEGER);
                        }
                        else{
                            insertStar.setInt(3, birthYear);
                        }
                        insertStar.addBatch();
                        int thisStarId = starId;
                        starId++;
                        starBatchSize++;

                        insertSIM.setString(1, starPrefix+String.valueOf(thisStarId));
                        insertSIM.setString(2, moviePrefix+String.valueOf(thisMovieId));
                        //insertSIM.executeUpdate();
                        insertSIM.addBatch();
                        batchSize++;
                    }
                }
                System.out.println(batchSize + " " + movieBatchSize + " " +  starBatchSize + " " +  gimBatchSize + " " + ratingsBatchSize);
                if(batchSize + movieBatchSize + starBatchSize + gimBatchSize > 10000){
                    insertMovie.executeBatch();
                    movieConnection.commit();

                    insertStar.executeBatch();
                    starConnection.commit();

                    insertGIM.executeBatch();
                    gimConnection.commit();

                    insertSIM.executeBatch();
                    batchConnection.commit();

                    insertRating.executeBatch();
                    ratingsConnection.commit();


                    batchSize = 0;
                    movieBatchSize = 0;
                    starBatchSize = 0;
                    gimBatchSize = 0;
                    ratingsBatchSize = 0;
                }
            }

        }
        insertMovie.executeBatch();
        movieConnection.commit();

        insertStar.executeBatch();
        starConnection.commit();

        insertGIM.executeBatch();
        gimConnection.commit();

        insertSIM.executeBatch();
        batchConnection.commit();

        insertRating.executeBatch();
        ratingsConnection.commit();

        batchConnection.close();
        movieConnection.close();
        starConnection.close();
        gimConnection.close();
        genreConnection.close();
        ratingsConnection.close();

        System.out.println("INCONSISTENCY REPORT:");
        System.out.println("\tGenre inconsistencies: " + genreInconsistency);
        System.out.println("\tMovie inconsistencies: " + movieInconsistency);

    }

    private void parseXmlFile(String file) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(file);

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseCastsDocument(){
        Element documentElement = dom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("dirfilms");
        for(int i = 0; i < nodeList.getLength(); i++){
            Element filmcElement = (Element) nodeList.item(i); //each filmc

            NodeList movieNodeList = filmcElement.getElementsByTagName("m");
            for(int j = 0; j < movieNodeList.getLength(); j++){
                Element movieNode = (Element) movieNodeList.item(j);
                String fid = getTextValue(movieNode, "f");
                String actor = getTextValue(movieNode, "a");
                Actor newActor = new Actor(actor,-1);

                if(actorsInMovie.containsKey(fid)){
                    actorsInMovie.get(fid).add(newActor);
                }
                else{
                    actorsInMovie.put(fid, new ArrayList<Actor>());
                    actorsInMovie.get(fid).add(newActor);
                }
            }
        }
    }

    private void parseActorsDocument(){
        Element documentElement = dom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {

            Element element = (Element) nodeList.item(i);

            Actor actor = parseActor(element);

            // add it to list
            actors.put(actor.getName(),actor);
        }
    }

    private void parseMainsDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the directorfilm element
            Element element = (Element) nodeList.item(i);

            // get the DirectorFilm object
            DirectorFilm directorFilm = parseDirectorFilm(element);

            // add it to list
            directorFilms.add(directorFilm);
        }
    }

    private Actor parseActor(Element element){
        String stagename = getTextValue(element, "stagename");
        int birthYear = getIntValue(element, "dob");

        return new Actor(stagename, birthYear);
    }
    private DirectorFilm parseDirectorFilm(Element element) {

        // for each <directorfilm> element get info
        Element director = (Element) element.getElementsByTagName("director").item(0);
        String directorName = getTextValue(director, "dirname");
        Element filmsElement = (Element) element.getElementsByTagName("films").item(0);
        ArrayList<Film> films = new ArrayList<>();

        NodeList nodeList = filmsElement.getElementsByTagName("film");
        for(int i = 0; i < nodeList.getLength(); i++){
            Element node = (Element) nodeList.item(i);
            String title = getTextValue(node, "t");
            int year = getIntValue(node, "year");
            String fid = getTextValue(node, "fid");

            // This only gets one of the potentially multiple categories, but that's fine for demo purposes
            Element cats = (Element) node.getElementsByTagName("cats").item(0);
            if(cats==null){
                // Inconsistency, cats element is null
                genreInconsistency++;
            }
            else {
                String genre = getTextValue(cats, "cat");
                if(genre == null){
                    // Inconsistency, cat element is null
                    genreInconsistency++;
                }
                else {
                    films.add(new Film(title, year, directorName, fid, genre));
                }
            }
        }


        // create a new Employee with the value read from the xml nodes
        DirectorFilm returnDF = new DirectorFilm(directorName, films);
        return returnDF;
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        try {
            if (nodeList.getLength() > 0) {
                // here we expect only one <Name> would present in the <Employee>
                textVal = nodeList.item(0).getFirstChild().getNodeValue();

            }
        }
        catch(Exception E){
            // Catches bad input and just substitutes it for nothing.
            textVal = "";
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        try {
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        catch(Exception E){
            return -1;
        }
    }

    private void printCastsData(){
        System.out.println("Total parsed " + actorsInMovie.size() + " movies");
        for(String key : actorsInMovie.keySet()){
            System.out.println("\t"+key+ ", " + actorsInMovie.get(key));
        }
    }
    private void printFilmsData() {
         System.out.println("Total parsed " + directorFilms.size() + " directorfilms");

         for (DirectorFilm directorFilm : directorFilms) {
             System.out.println("\t" + directorFilm.toString());
         }
    }

    private void printActorData() {
        System.out.println("Total parsed " + actors.size() + " actors");


        for (String key : actors.keySet()){
            System.out.println("\t" + actors.get(key).toString());
        }
    }


    public static void main(String[] args) {
        // create an instance
        DomParser domParser = new DomParser();

        // call run example
        try{
            domParser.runExample();
        }
        catch(Exception E){
            System.out.println(E.getMessage());
            System.out.println(E.getStackTrace());
        }
    }

}