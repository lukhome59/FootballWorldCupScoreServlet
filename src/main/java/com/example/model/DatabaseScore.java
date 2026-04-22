package com.example.model;

import java.sql.Timestamp;

public class DatabaseScore extends Score {
	public static int getHash(Score score) {
		return 31 * score.getAwayTeam().hashCode() + score.getHomeTeam().hashCode();
	}
	
	public DatabaseScore(Score score) {
		super(score.getHomeTeam(), score.getAwayTeam());
		setHomeScore(score.getHomeScore());
		setAwayScore(score.getAwayScore());
		mId = getHash(score);
		updateTimestamp();
	}
	
	@Override
	public void setHomeScore(int score) {
		super.setHomeScore(score);
		updateTimestamp();
	}

	@Override
	public void setAwayScore(int score) {
		super.setAwayScore(score);
		updateTimestamp();
	}
	
	public int getId() {
		return mId;
	}
	
	public Timestamp getTimestamp() {
		return mTimestamp;
	}
	
	private void updateTimestamp() {
		mTimestamp = new Timestamp(System.currentTimeMillis());
	}
	
	private final int mId;
	private Timestamp mTimestamp;
}
