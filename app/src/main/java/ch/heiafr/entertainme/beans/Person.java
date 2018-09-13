package ch.heiafr.entertainme.beans;

/**
 * Created by Danny on 01.06.2017.
 */

public class Person {
    private String name;
    private String job;

    public Person(String name, String job) {
        this.name = name;
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
