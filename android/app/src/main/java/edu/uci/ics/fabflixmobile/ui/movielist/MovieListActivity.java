package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {

    private EditText movie;

    private Button searchButton;

    private Button prevButton;

    private Button nextButton;

    private EditText pageNum;

    private int page = 1;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "13.52.215.120";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        searchButton = (Button) findViewById(R.id.search);
        movie = (EditText) findViewById(R.id.movie);
        prevButton = (Button) findViewById(R.id.prev);
        nextButton = (Button) findViewById(R.id.next);
        pageNum = (EditText) findViewById(R.id.pageNum);

        pageNum.setText(String.valueOf(page));


        searchButton.setOnClickListener(view -> search());
        prevButton.setOnClickListener(view -> prev());
        nextButton.setOnClickListener(view -> next());
    }

    @SuppressLint("SetTextI18n")
    public void prev(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        if(page > 1) {
            page--;
            pageNum.setText(String.valueOf(page));
        }
        else{
            return;
        }
        final StringRequest prevRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title="+movie.getText().toString().replaceAll(" ", "%20")+"&fulltext=true&numResults=10&selectOrder=&titleSearch=&year=&director=&star=&genre=&page="+ this.page,
                response -> {
                    try {
                        JSONArray json = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();

                        for (int i = 0; i < json.length (); i++) {
                            JSONObject value = json.getJSONObject(i);

                            String title = value.getString("movie_title");
                            short year = (short) Integer.parseInt(value.getString("movie_year"));
                            String director = value.getString("movie_director");
                            ArrayList<String> genres = new ArrayList();
                            ArrayList<String> stars = new ArrayList();

                            JSONArray genresArray = new JSONArray(value.getString("movie_genres"));

                            for (int j = 0; j < genresArray.length(); j++) {
                                genres.add(genresArray.getString(j));
                            }

                            JSONArray starsArray = new JSONArray(value.getString("movie_stars"));
                            for (int j = 0; j < starsArray.length(); j++) {
                                stars.add(starsArray.getString(j));
                            }


                            movies.add(new Movie(title, year, director, genres, stars));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            finish();
                            // initialize the activity(page)/destination
                            Intent MovieListPage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            MovieListPage.putExtra("title",movie.getName());
                            MovieListPage.putExtra("subtitle",movie.getYear() + "\nDirector:" + movie.getDirector() + "\nGenres:" + movie.getGenres(false) + "\nStars:" + movie.getStars(false));
                            // activate the list page.
                            startActivity(MovieListPage);
                        });

                    } catch (Exception e) {
                        Log.d("JSON Parse failure", e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("Prev error", error.toString());
                });
        queue.add(prevRequest);
    }
    @SuppressLint("SetTextI18n")
    public void next(){
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        page++;
        pageNum.setText(String.valueOf(page));
        final StringRequest nextRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title="+movie.getText().toString()+"&fulltext=true&numResults=10&selectOrder=&titleSearch=&year=&director=&star=&genre=&page="+ this.page,
                response -> {
                    try {
                        JSONArray json = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();

                        for (int i = 0; i < json.length (); i++) {
                            JSONObject value = json.getJSONObject(i);

                            String title = value.getString("movie_title");
                            short year = (short) Integer.parseInt(value.getString("movie_year"));
                            String director = value.getString("movie_director");
                            ArrayList<String> genres = new ArrayList();
                            ArrayList<String> stars = new ArrayList();

                            JSONArray genresArray = new JSONArray(value.getString("movie_genres"));

                            for (int j = 0; j < genresArray.length(); j++) {
                                genres.add(genresArray.getString(j));
                            }

                            JSONArray starsArray = new JSONArray(value.getString("movie_stars"));
                            for (int j = 0; j < starsArray.length(); j++) {
                                stars.add(starsArray.getString(j));
                            }


                            movies.add(new Movie(title, year, director, genres, stars));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            finish();
                            // initialize the activity(page)/destination
                            Intent MovieListPage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            MovieListPage.putExtra("title",movie.getName());
                            MovieListPage.putExtra("subtitle",movie.getYear() + "\nDirector:" + movie.getDirector() + "\nGenres:" + movie.getGenres(false) + "\nStars:" + movie.getStars(false));
                            // activate the list page.
                            startActivity(MovieListPage);
                        });

                    } catch (Exception e) {
                        Log.d("JSON Parse failure", e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("Prev error", error.toString());
                });
        queue.add(nextRequest);
    }
    @SuppressLint("SetTextI18n")
    public void search() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        page = 1;
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title="+movie.getText().toString().replaceAll(" ", "%20")+"&fulltext=true&numResults=10&selectOrder=&titleSearch=&year=&director=&star=&genre=&page="+ this.page,
                response -> {
                    try {
                        JSONArray json = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();

                        for (int i = 0; i < json.length (); i++) {
                            JSONObject value = json.getJSONObject(i);

                            String title = value.getString("movie_title");
                            short year = (short) Integer.parseInt(value.getString("movie_year"));
                            String director = value.getString("movie_director");
                            ArrayList<String> genres = new ArrayList();
                            ArrayList<String> stars = new ArrayList();

                            JSONArray genresArray = new JSONArray(value.getString("movie_genres"));

                            for (int j = 0; j < genresArray.length(); j++) {
                                genres.add(genresArray.getString(j));
                            }

                            JSONArray starsArray = new JSONArray(value.getString("movie_stars"));
                            for (int j = 0; j < starsArray.length(); j++) {
                                stars.add(starsArray.getString(j));
                            }


                            movies.add(new Movie(title, year, director, genres, stars));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            finish();
                            // initialize the activity(page)/destination
                            Intent MovieListPage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            MovieListPage.putExtra("title",movie.getName());
                            MovieListPage.putExtra("subtitle",movie.getYear() + "\nDirector:" + movie.getDirector() + "\nGenres:" + movie.getGenres(false) + "\nStars:" + movie.getStars(false));
                            // activate the list page.
                            startActivity(MovieListPage);
                        });
                    } catch (Exception e) {
                        Log.d("JSON Parse failure", e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("Search error", error.toString());
                });
        queue.add(searchRequest);
    }
}