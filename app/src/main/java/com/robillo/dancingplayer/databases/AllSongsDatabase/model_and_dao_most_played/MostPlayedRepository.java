package com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played;

public class MostPlayedRepository {

    private MostPlayedDao mostPlayedDao;

    public MostPlayedRepository(MostPlayedDao mostPlayedDao) {
        this.mostPlayedDao = mostPlayedDao;
    }
}
