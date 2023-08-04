import java.util.ArrayList;

public class Film {

    private String title;
    private int year;
    private String director;
    private String fid;

    private String genre;


    public Film(String title, int year, String director, String fid, String genre) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.fid = fid;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getGenre(){ return genre;}

    public String getFid(){ return fid;}

    public String toString() {

        return  "Title: " + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Director:" + getDirector() + ", " +
                "fid: " + fid + ", " +
                "genre: " + genre;
    }
}