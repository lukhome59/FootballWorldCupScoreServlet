package com.example.model;

import java.sql.Timestamp;

public class DatabaseScore extends Score {
	public static int getHash(Score score) {
		return score.hashCode();
	}
	
	public DatabaseScore(Score score) {
		super(score.getHomeTeam(), score.getAwayTeam());
		mId = getHash(score);
		mTimestamp = new Timestamp(System.currentTimeMillis());
	}
	
	public int getId() {
		return mId;
	}
	
	public Timestamp getTimestamp() {
		return mTimestamp;
	}
	
	private final int mId;
	private Timestamp mTimestamp;
}
