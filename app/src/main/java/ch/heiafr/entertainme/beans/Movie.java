package ch.heiafr.entertainme.beans;

import java.util.ArrayList;

/**
 * Created by Danny on 25.04.2017.
 */

public class Movie extends Media {
    private int length;

    public Movie(int id, String poster, String title, String date, String genre, String overview, double vote, int runtime, String key, ArrayList<Actor> actors) {
        super(id, poster, title, date, genre, overview, vote, actors, key);
        this.length = runtime;
    }

    public int getLength() {
        return length;
    }

}
