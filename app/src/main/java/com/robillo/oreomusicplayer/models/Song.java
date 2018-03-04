package com.robillo.oreomusicplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by robinkamboj on 09/09/17.
 */

public class Song implements Parcelable {

    private String data;
    private String title;
    private String titleKey;
    private String id;
    private String dateAdded;
    private String dateModified;
    private String duration;
    private String composer;
    private String album;
    private String albumId;
    private String albumKey;
    private String artist;
    private String artistId;
    private String artistKey;
    private String size;
    private String year;

    public Song() {

    }

    public Song(String data, String title, String titleKey, String id, String dateAdded, String dateModified, String duration, String composer, String album, String albumId, String albumKey, String artist, String artistId, String artistKey, String size, String year) {
        this.data = data;
        this.title = title;
        this.titleKey = titleKey;
        this.id = id;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.duration = duration;
        this.composer = composer;
        this.album = album;
        this.albumId = albumId;
        this.albumKey = albumKey;
        this.artist = artist;
        this.artistId = artistId;
        this.artistKey = artistKey;
        this.size = size;
        this.year = year;
    }

    /**
     * Interface that must be implemented and provided as a public CREATOR field
     * that generates instances of your Parcelable class from a Parcel.
     */
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        /**
         * Creates a new USer object from the Parcel. This is the reason why
         * the constructor that takes a Parcel is needed.
         */
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        /**
         * Create a new array of the Parcelable class.
         * @return an array of the Parcelable class,
         * with every entry initialized to null.
         */
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    /**
     * Parcel overloaded constructor required for
     * Parcelable implementation used in the CREATOR
     */
    private Song(Parcel in) {
        data = in.readString();
        title = in.readString();
        titleKey = in.readString();
        id = in.readString();
        dateAdded = in.readString();
        dateModified = in.readString();
        duration = in.readString();
        composer = in.readString();
        album = in.readString();
        albumId = in.readString();
        albumKey = in.readString();
        artist = in.readString();
        artistId = in.readString();
        artistKey = in.readString();
        size = in.readString();
        year = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * This is where the parcel is performed.
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(data);
        parcel.writeString(title);
        parcel.writeString(titleKey);
        parcel.writeString(id);
        parcel.writeString(dateAdded);
        parcel.writeString(dateModified);
        parcel.writeString(duration);
        parcel.writeString(composer);
        parcel.writeString(album);
        parcel.writeString(albumId);
        parcel.writeString(albumKey);
        parcel.writeString(artist);
        parcel.writeString(artistId);
        parcel.writeString(artistKey);
        parcel.writeString(size);
        parcel.writeString(year);
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumKey() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistKey() {
        return artistKey;
    }

    public void setArtistKey(String artistKey) {
        this.artistKey = artistKey;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

}