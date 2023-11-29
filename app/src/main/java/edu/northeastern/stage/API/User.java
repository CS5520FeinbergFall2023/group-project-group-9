package edu.northeastern.stage.API;

public class User {

    private Long lastLoggedInTimeStamp;
    private Name name;
    private Location lastLocation;

    public User(Long lastLoggedInTimeStamp, Name name, Location lastLocation) {
        this.lastLoggedInTimeStamp = lastLoggedInTimeStamp;
        this.name = name;
        this.lastLocation = lastLocation;
    }

    public Long getLastLoggedInTimeStamp() {
        return lastLoggedInTimeStamp;
    }

    public void setLastLoggedInTimeStamp(Long lastLoggedInTimeStamp) {
        this.lastLoggedInTimeStamp = lastLoggedInTimeStamp;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}
