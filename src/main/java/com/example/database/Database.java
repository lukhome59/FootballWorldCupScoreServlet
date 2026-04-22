package com.example.database;

import com.example.model.DatabaseScore;
import com.example.model.Score;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class Database {
	public CompletableFuture<Integer> insertAsync(Score score, Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> {
                	if (score.getHomeTeam().isEmpty()) {
                		throw new IllegalStateException("Insertion failed: missing home team");
                	}
                	if (score.getAwayTeam().isEmpty()) {
                		throw new IllegalStateException("Insertion failed: missing away team");
                	}
                	if (find(score) != null) {
                		throw new IllegalStateException("Insertion failed. The item already exists");
                	}
                	if (find(score.swap()) != null) {
                		throw new IllegalStateException("Insertion failed. The swapped item already exists");
                	}
                	DatabaseScore databaseScore = new DatabaseScore(score);
                	mScores.put(databaseScore.getId(), databaseScore);
                    return databaseScore.getId();
                },
                executor);
	}

	public CompletableFuture<Void> deleteAsync(Score score, Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> {
                	if (mScores.remove(DatabaseScore.getHash(score)) == null) {
                		if (mScores.remove(DatabaseScore.getHash(score.swap())) == null) {
                			throw new IllegalStateException("Removal failed: no item");
                		}
                	}
                    return null;
                },
                executor);
	}

	public CompletableFuture<List<Score>> selectAsync(Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> mScores.values().stream()
                	.sorted(Comparator.comparing(DatabaseScore::getTimestamp))
                	.map(s -> (Score) s)
                	.collect(Collectors.toList()),
                executor);
	}

	private DatabaseScore find(Score score) {
		return mScores.get(DatabaseScore.getHash(score));
	}

	private final HashMap<Integer, DatabaseScore> mScores = new HashMap<>();
}
