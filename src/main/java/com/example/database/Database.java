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
                		throw new RuntimeException("Insertion failed: missing home team");
                	}
                	if (score.getAwayTeam().isEmpty()) {
                		throw new RuntimeException("Insertion failed: missing away team");
                	}
                	if (score.getAwayTeam().equals(score.getHomeTeam())) {
                		throw new RuntimeException("Insertion failed: two same teams");
                	}
                	if (find(score) != null) {
                		throw new RuntimeException("Insertion failed. The item already exists");
                	}
                	if (find(score.swap()) != null) {
                		throw new RuntimeException("Insertion failed. The swapped item already exists");
                	}
                	DatabaseScore databaseScore = new DatabaseScore(score);
                	mScores.put(databaseScore.getId(), databaseScore);
                    return databaseScore.getId();
                },
                executor);
	}

	public CompletableFuture<Void> deleteAsync(int id, Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> {
                	if (mScores.remove(id) == null) {
                		throw new RuntimeException("Removal failed: no item");
                	}
                    return null;
                },
                executor);
	}

	public CompletableFuture<List<DatabaseScore>> selectAsync(Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> mScores.values().stream()
                	.sorted(Comparator.comparing(DatabaseScore::getTimestamp).reversed())
                	.collect(Collectors.toList()),
                executor);
	}

	public CompletableFuture<Void> updateAsync(int id, int homeScore, int awayScore, Executor executor) {
		return CompletableFuture.supplyAsync(
                () -> {
                	Score found = find(id);
                	if (found == null) {
                		throw new RuntimeException("Update failed: no item");
                	}
                	found.setHomeScore(homeScore);
                	found.setAwayScore(awayScore);
                    return null;
                },
                executor);
	}

	private DatabaseScore find(Score score) {
		return mScores.get(DatabaseScore.getHash(score));
	}

	private DatabaseScore find(int id) {
		return mScores.get(id);
	}

	private final HashMap<Integer, DatabaseScore> mScores = new HashMap<>();
}
