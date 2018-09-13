package ch.heiafr.entertainme.beans;

/**
 * Created by Danny on 01.06.2017.
 */

public class Actor extends Person {
    private String character;
    private String profile_path;

    public Actor(String character, String name, String profil_path) {
        super(name, "Acteur");
        this.character = character;
        this.profile_path = profil_path;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getProfile_path() {
        return profile_path;
    }

    public void setProfile_path(String profile_path) {
        this.profile_path = profile_path;
    }
}
