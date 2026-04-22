package com.example.model;

public class Score {
	public Score(String homeTeam, String awayTeam) {
		mHomeTeam = homeTeam;
		mAwayTeam = awayTeam;
	}

	public String getHomeTeam() {
		return mHomeTeam;
	}

	public String getAwayTeam() {
		return mAwayTeam;
	}

	public int getHomeScore() {
		return mHomeScore;
	}
	
	public int getAwayScore() {
		return mAwayScore;
	}
	
	public Score swap() {
		return new Score(mAwayTeam, mHomeTeam);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Score)) return false;
		Score other = (Score) o;
		return mHomeTeam.equals(other.mHomeTeam) && mAwayTeam.equals(other.mAwayTeam);
	}

	@Override
	public int hashCode() {
		return 31 * mHomeTeam.hashCode() + mAwayTeam.hashCode();
	}

	private final String mHomeTeam;
	private final String mAwayTeam;
	private int mHomeScore;
	private int mAwayScore;
}
