package edu.northeastern.stage.model.music;

public class PopularityTrack {
    private String trackName;
    private String artistName;
    private String albumImage;
    private Integer ranking;

    public PopularityTrack(String trackName, String artistName, String albumImage, Integer ranking) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumImage = albumImage;
        this.ranking = ranking;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }
}
