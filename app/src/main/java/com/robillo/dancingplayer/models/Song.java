package com.robillo.dancingplayer.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Song {

    private String data;
    private String title;
    private String titleKey;

    @SuppressWarnings("NullableProblems")
    @NonNull
    @PrimaryKey
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

    @Ignore
    public Song() {

    }

    public Song(String data, String title, String titleKey, @NonNull String id, String dateAdded, String dateModified, String duration, String composer, String album, String albumId, String albumKey, String artist, String artistId, String artistKey, String size, String year) {
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

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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