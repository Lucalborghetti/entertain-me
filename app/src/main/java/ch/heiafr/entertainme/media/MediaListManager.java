package ch.heiafr.entertainme.media;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.heiafr.entertainme.R;
import ch.heiafr.entertainme.beans.Actor;
import ch.heiafr.entertainme.beans.Media;
import ch.heiafr.entertainme.beans.Movie;
import ch.heiafr.entertainme.beans.Serie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Danny on 25.04.2017.
 */

public class MediaListManager {
    public static final String API_KEY = "a8be2cd15716907db196ca5c5e45e31f";

    public static final String LIST_REQUEST = "LIST";
    private final String GENRE_REQUEST = "GENRES";
    private final String MOVIE_REQUEST = "MOVIE";
    private final String SERIE_REQUEST = "SERIE";
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public static final String POPULAR_URL = "popular?page=1&language=fr-CH&";
    public static final String UPCOMING_URL = "upcoming?page=1&language=fr-CH&";
    public static final String NOWPLAYING_URL = "now_playing?page=1&language=fr-CH&";
    public static final String ONTHEAIR_URL = "on_the_air?page=1&language=fr-CH&";
    public static final String AIRINGTODAY = "airing_today?page=1&language=fr-CH&";

    public static final String MOVIE_URL = "movie/";
    public static final String SERIE_URL = "tv/";

    private final String MEDIA_APPEND = "&language=fr-CH&append_to_response=credits%2Cvideos";
    private final String MULTIPLE = "MULTIPLE";
    private final String LAST = "LAST";

    private ArrayList<Media> mediasList;
    private SparseArray<String> genres;
    private OnRequestCompletedListener cListener;
    private Context context;
    private Movie movie;
    private Serie serie;


    public MediaListManager(Activity activity) {
        context = activity.getApplicationContext();
        cListener = (OnRequestCompletedListener) activity;
        mediasList = new ArrayList<Media>();
        genres = new SparseArray<String>();
    }

    public void updateMediasList(String requestType, String mediaURL) {
        mediasList.clear();
        new RequestTask().execute("genre/" + mediaURL + "list?language=fr-CH&", GENRE_REQUEST, null);
        new RequestTask().execute(mediaURL + requestType, LIST_REQUEST);
    }

    public void updateMediasList(ArrayList<Integer> mediasId, String mediaURL) {
        mediasList.clear();
        if (mediasId.size() == 0) {
            cListener.onRequestCompleted();
            return;
        }
        for (int i = 0; i < mediasId.size(); i++) {
            if (i != mediasId.size() - 1)
                new RequestTask().execute(mediaURL + mediasId.get(i) + "?", mediaURL == MOVIE_URL ? MOVIE_REQUEST : SERIE_REQUEST, MEDIA_APPEND, MULTIPLE);
            else
                new RequestTask().execute(mediaURL + mediasId.get(i) + "?", mediaURL == MOVIE_URL ? MOVIE_REQUEST : SERIE_REQUEST, MEDIA_APPEND, MULTIPLE, LAST);
        }

    }

    public int getNbMovies() {
        return mediasList.size();
    }

    public ArrayList<Media> getMediasList() {
        return mediasList;
    }

    public void getMovie(int id) {
        new RequestTask().execute(MOVIE_URL + id + "?", MOVIE_REQUEST, MEDIA_APPEND);
    }

    public void getSerie(int id) {
        new RequestTask().execute(SERIE_URL + id + "?", SERIE_REQUEST, MEDIA_APPEND);
    }

    public Movie getMovie() {

        return movie;
    }

    public Serie getSerie() {
        return serie;
    }


    /******************
     * Get the poster if available for the current movie and
     * load it asynchronously using a SingleThreadPool.
     *
     * @param movieIndex
     * @param imageView
     *
     ******************/
    public void getPosterPool(final int movieIndex, final ImageView imageView, final int tag) {
        if (!imageView.getTag(R.string.tag_imageview).equals(tag))
            return;
        String imageUrl = IMAGE_BASE_URL + mediasList.get(movieIndex).getPoster();
        Picasso.with(context).load(imageUrl).into(imageView);
        imageView.setBackgroundDrawable(null);
    }

    private class RequestTask extends AsyncTask<String, String, String> {

        private String requestType;
        private boolean isMultiple;
        private boolean isLast;

        @Override
        protected String doInBackground(String... params) {
            requestType = params[1];
            isMultiple = false;
            isLast = false;
            String append = "";
            if (params.length >= 3 && params[2] != null) {
                append = params[2];
            }
            if (params.length >= 4 && params[3] != null) {
                if (params[3].equals(MULTIPLE)) {
                    isMultiple = true;
                }
            }
            if (params.length >= 5 && params[4] != null) {
                if (params[4].equals(LAST)) {
                    isLast = true;
                }
            }
            OkHttpClient client = new OkHttpClient();
            String requestURL = "https://api.themoviedb.org/3/" + params[0] + "api_key=" + API_KEY + append;
            Request request = new Request.Builder()
                    .url(requestURL)
                    .get()
                    .build();
            String jsonData = null;
            Response response = null;
            try {
                response = client.newCall(request).execute();
                jsonData = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonData;
        }


        @Override
        protected void onPostExecute(String jsonData) {
            try {
                JSONObject jsonResponse = new JSONObject(jsonData);
                switch (requestType) {
                    case LIST_REQUEST:
                        JSONArray moviesArray = jsonResponse.getJSONArray("results");
                        for (int i = 0; i < moviesArray.length(); i++) {
                            JSONObject movieJson = moviesArray.getJSONObject(i);
                            JSONArray genresIdArray = movieJson.getJSONArray("genre_ids");
                            String genre = "";
                            for (int j = 0; j < genresIdArray.length(); j++) {
                                int index = genresIdArray.getInt(j);
                                genre += genres.get(index) + " ";
                            }
                            String title = "";
                            String date = "";
                            try {
                                title = movieJson.getString("title");
                                date = movieJson.getString("release_date");
                            } catch (Exception e) {
                                title = movieJson.getString("name");
                                date = movieJson.getString("first_air_date");
                            }
                            Media m = new Media(movieJson.getInt("id"), movieJson.getString("poster_path"),
                                    title,
                                    date,
                                    genre,
                                    movieJson.getString("overview"),
                                    movieJson.getDouble("vote_average"));
                            mediasList.add(m);
                        }
                        cListener.onRequestCompleted();
                        break;
                    case GENRE_REQUEST:
                        JSONArray genresArray = jsonResponse.getJSONArray("genres");
                        for (int i = 0; i < genresArray.length(); i++) {
                            JSONObject genreJson = genresArray.getJSONObject(i);
                            genres.put(genreJson.getInt("id"), genreJson.getString("name"));
                        }
                        break;
                    case MOVIE_REQUEST:
                        JSONObject credits = jsonResponse.getJSONObject("credits");
                        JSONArray castArray = credits.getJSONArray("cast");
                        ArrayList<Actor> actors = new ArrayList<>();
                        for (int i = 0; i < castArray.length(); i++) {
                            JSONObject actorJson = castArray.getJSONObject(i);
                            actors.add(new Actor(actorJson.getString("character"), actorJson.getString("name"), actorJson.getString("profile_path")));
                        }
                        JSONArray genreArray = jsonResponse.getJSONArray("genres");
                        String genre = "";
                        for (int i = 0; i < genreArray.length(); i++) {
                            JSONObject genreJson = genreArray.getJSONObject(i);
                            genre += genreJson.get("name") + " ";
                        }
                        String videoKey = "";
                        JSONArray videosResults = jsonResponse.getJSONObject("videos").getJSONArray("results");
                        for (int i = 0; i < videosResults.length(); i++) {
                            JSONObject video = videosResults.getJSONObject(i);
                            if (video.getString("type").equals("Trailer")) {
                                videoKey = video.getString("key");
                                break;
                            }
                        }
                        Movie m = new Movie(jsonResponse.getInt("id"), jsonResponse.getString("poster_path"),
                                jsonResponse.getString("title"),
                                jsonResponse.getString("release_date"),
                                genre,
                                jsonResponse.getString("overview"),
                                jsonResponse.getDouble("vote_average"),
                                jsonResponse.getInt("runtime"),
                                videoKey,
                                actors);
                        if (isMultiple) {
                            mediasList.add(m);
                            if (isLast) {
                                cListener.onRequestCompleted();
                            }
                        } else {
                            movie = m;
                            cListener.onRequestCompleted();
                        }
                        break;
                    case SERIE_REQUEST:
                        JSONObject serieCredits = jsonResponse.getJSONObject("credits");
                        JSONArray serieCastArray = serieCredits.getJSONArray("cast");
                        ArrayList<Actor> serieActors = new ArrayList<>();
                        for (int i = 0; i < serieCastArray.length(); i++) {
                            JSONObject actorJson = serieCastArray.getJSONObject(i);
                            serieActors.add(new Actor(actorJson.getString("character"), actorJson.getString("name"), actorJson.getString("profile_path")));
                        }
                        JSONArray serieGenreArray = jsonResponse.getJSONArray("genres");
                        String serieGenre = "";
                        for (int i = 0; i < serieGenreArray.length(); i++) {
                            JSONObject genreJson = serieGenreArray.getJSONObject(i);
                            serieGenre += genreJson.get("name") + " ";
                        }
                        String serieVideoKey = "";
                        JSONArray serieVideosSuggestions = jsonResponse.getJSONObject("videos").getJSONArray("results");
                        for (int i = 0; i < serieVideosSuggestions.length(); i++) {
                            JSONObject video = serieVideosSuggestions.getJSONObject(i);
                            if (video.getString("type").equals("Trailer")) {
                                serieVideoKey = video.getString("key");
                                break;
                            }
                        }
                        Serie s = new Serie(jsonResponse.getInt("id"), jsonResponse.getString("poster_path"),
                                jsonResponse.getString("name"),
                                jsonResponse.getString("first_air_date"),
                                serieGenre,
                                jsonResponse.getString("overview"),
                                jsonResponse.getDouble("vote_average"),
                                jsonResponse.getInt("number_of_episodes"),
                                jsonResponse.getInt("number_of_seasons"),
                                serieVideoKey,
                                serieActors);
                        if (isMultiple) {
                            mediasList.add(s);
                            if (isLast) {
                                cListener.onRequestCompleted();
                            }
                        } else {
                            serie = s;
                            cListener.onRequestCompleted();
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnRequestCompletedListener {
        public void onRequestCompleted();
    }
}
