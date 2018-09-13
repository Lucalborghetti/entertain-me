package ch.heiafr.entertainme.beans;

import java.util.ArrayList;

/**
 * Created by Danny on 13.06.2017.
 */

public class Serie extends Media {
    private int nb_episodes;
    private int nb_seasons;

    public Serie(int id, String poster, String title, String date, String genre, String overview, double vote, int nb_episodes, int nb_seasons, String trailer, ArrayList<Actor> actors) {
        super(id, poster, title, date, genre, overview, vote, actors, trailer);
        this.nb_episodes = nb_episodes;
        this.nb_seasons = nb_seasons;
    }

    public int getNb_episodes() {
        return nb_episodes;
    }

    public int getNb_seasons() {
        return nb_seasons;
    }
}
