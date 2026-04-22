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

	public void setHomeScore(int score) {
		mHomeScore = score;
	}

	public void setAwayScore(int score) {
		mAwayScore = score;
	}

	public Score swap() {
		return new Score(mAwayTeam, mHomeTeam);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Score)) return false;
		Score other = (Score) o;
		return mHomeTeam.equals(other.mHomeTeam) && mAwayTeam.equals(other.mAwayTeam)
				&& mHomeScore == other.mHomeScore && mAwayScore == other.mAwayScore;
	}

	@Override
	public int hashCode() {
		return 31 * (31 * mHomeTeam.hashCode() + mAwayTeam.hashCode()) + 31 * mHomeScore + mAwayScore;
	}

	private final String mHomeTeam;
	private final String mAwayTeam;
	private int mHomeScore;
	private int mAwayScore;
}
