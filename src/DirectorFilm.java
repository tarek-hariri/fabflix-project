import java.util.ArrayList;

public class DirectorFilm {

    private String director;

    private ArrayList<Film> films;

    public DirectorFilm(String director, ArrayList<Film> films) {
        this.director = director;
        this.films = films;
    }

    public String getDirector() {
        return director;
    }

    public ArrayList<Film> getFilms() {
        return films;
    }

    public String toString() {

        return "Director:" + getDirector() + ", " +
                "Films:" + getFilms();
    }
}