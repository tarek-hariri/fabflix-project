package edu.uci.ics.fabflixmobile.data.model;
import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;
    private final short year;

    private final String director;

    private ArrayList<String> genres;

    private ArrayList<String> stars;

    public Movie(String name, short year, String director, ArrayList<String> genres, ArrayList<String> stars) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector(){ return director;}

    public String getGenres(boolean limitThree){
        int max = Math.min(3,genres.size());
        if(!limitThree){
            max = genres.size();
        }
        String output = "";
        for(int i = 0; i < max; i++){
            output += genres.get(i) + ", ";
        }
        return output;
    }

    public String getStars(boolean limitThree){
        int max = Math.min(6,stars.size()*2); // Each star takes up two indexes in the array, so 3 * 2 = 6
        if(!limitThree){
            max = stars.size()/2-1;
        }
        String output = "";
        for(int i = 0; i < stars.size()/2-1; i++){
            output += stars.get(2*i+1) + ", ";
        }
        return output;
    }
}