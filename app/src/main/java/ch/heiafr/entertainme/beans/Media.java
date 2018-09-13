package ch.heiafr.entertainme.beans;

import android.content.ContentValues;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static ch.heiafr.entertainme.database.MediaSQLiteHelper.KEY_ID_COLUMN;

/**
 * Created by Danny on 25.04.2017.
 */

public class Media {
    private String poster;
    private String title;
    private String date;
    private String genre;
    private String overview;
    private double vote;
    private int id;
    private ArrayList<Actor> actors;
    private String trailer;

    public Media(int id, String poster, String title, String date, String genre, String overview, double vote) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            this.date = new SimpleDateFormat("dd/MM/yyyy").format(d);
        } catch (ParseException e) {
            this.date = date;
        }
        this.genre = genre;
        this.overview = overview;
        this.vote = vote;
    }

    public Media(int id, String poster, String title, String date, String genre, String overview, double vote, ArrayList<Actor> actors, String trailer) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            this.date = new SimpleDateFormat("dd/MM/yyyy").format(d);
        } catch (ParseException e) {
            this.date = date;
        }
        this.genre = genre;
        this.overview = overview;
        this.vote = vote;
        this.actors = actors;
        this.trailer = trailer;
    }

    public ArrayList<Actor> getActors() {
        return actors;
    }

    public String getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getGenre() {
        return genre;
    }

    public String getOverview() {
        return overview;
    }

    public String getTrailer() {
        return trailer;
    }

    public double getVote() {
        return vote;
    }

    public int getId() {
        return id;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_ID_COLUMN, id);
        return values;
    }
}