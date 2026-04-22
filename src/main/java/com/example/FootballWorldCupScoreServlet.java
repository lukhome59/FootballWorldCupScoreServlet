package com.example;

import com.example.database.Database;
import com.example.model.DatabaseScore;
import com.example.model.Score;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet handling Football World Cup score operations via HTTP POST requests.
 * Consider to utilize whole REST API in the future.
 *
 * <p>Supported endpoints:
 * <ul>
 *   <li>{@code /getSummary}   – returns the list of all ongoing games sorted by timestamp (descending)</li>
 *   <li>{@code /startGame}    – starts a new game between two teams; requires {@code homeTeam} and {@code awayTeam} parameters</li>
 *   <li>{@code /finishGame}   – removes a game by its {@code id}</li>
 *   <li>{@code /updateScore}  – updates the score of a game; requires {@code id}, {@code homeScore} and {@code awayScore} parameters</li>
 * </ul>
 *
 * <p>All database operations are executed asynchronously on a dedicated single-thread executor.
 * Every response is a JSON object containing at minimum a {@code result} field with value
 * {@code "success"} or {@code "failure"}. On failure an additional {@code message} field
 * describes the error.
 */
@WebServlet(asyncSupported = true, urlPatterns = {"/getSummary", "/startGame", "/finishGame", "/updateScore"})
public class FootballWorldCupScoreServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TEAM_NAME_LENGTH = 10;
    public static final String KEY_RESULT = "result";
    public static final String VALUE_RESULT_SUCCESS = "success";
    public static final String VALUE_RESULT_FAILURE = "failure";
    public static final String PARAM_HOME_TEAM = "homeTeam";
    public static final String PARAM_AWAY_TEAM = "awayTeam";
    public static final String PARAM_ID = "id";
    public static final String PARAM_HOME_TEAM_SCORE = "homeScore";
    public static final String PARAM_AWAY_TEAM_SCORE = "awayScore";
    public static final String KEY_ID = "id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LIST = "list";
    public static final String KEY_HOME_TEAM = "homeTeam";
    public static final String KEY_AWAY_TEAM = "awayTeam";
    public static final String KEY_HOME_TEAM_SCORE = "homeScore";
    public static final String KEY_AWAY_TEAM_SCORE = "awayScore";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.start(() -> {
        	CompletableFuture<JSONObject> result;
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            switch (request.getServletPath()) {
        		case "/getSummary":
        			result = handleGetSummary(request);
        			break;
        		case "/startGame":
        			result = handleStartGame(request);
        			break;
        		case "/finishGame":
        			result = handleFinishGame(request);
        			break;
        		case "/updateScore":
        			result = handleUpdateScore(request);
        			break;
        		default:
        			result = CompletableFuture.completedFuture(
        					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
        					.put(KEY_MESSAGE, "Unknown action: " + request.getServletPath()));
        			break;
            	}
            result
        		.exceptionally(ex -> new JSONObject()
        				.put(KEY_RESULT, VALUE_RESULT_FAILURE)
        				.put(KEY_MESSAGE, ex.getMessage()))
        		.thenAccept(jsonResult -> {
        			try (PrintWriter out = response.getWriter()) {
        				out.print(jsonResult);
        			} catch (IOException e) {
        				e.printStackTrace();
        			} finally {
        				asyncContext.complete();
        			}
        		});
        });
    }

	private CompletableFuture<JSONObject> handleGetSummary(HttpServletRequest request) {
		return mDatabase.selectAsync(mDatabaseExecutor).thenApply(list -> {
			JSONObject json = new JSONObject().put(KEY_RESULT, VALUE_RESULT_SUCCESS);
			JSONArray jsonArray = new JSONArray();
			for (DatabaseScore score : list) {
				JSONObject item = new JSONObject().put(KEY_HOME_TEAM, score.getHomeTeam())
						.put(KEY_HOME_TEAM_SCORE, score.getHomeScore())
						.put(KEY_AWAY_TEAM, score.getAwayTeam())
						.put(KEY_AWAY_TEAM_SCORE, score.getAwayScore())
						.put(KEY_ID, score.getId());
				jsonArray.put(item);
			}
			json.put(KEY_LIST, jsonArray);
			return json;
		});
	}

	private CompletableFuture<JSONObject> handleStartGame(HttpServletRequest request) {
		String homeTeam = request.getParameter(PARAM_HOME_TEAM);
		String awayTeam = request.getParameter(PARAM_AWAY_TEAM);
		if (homeTeam != null && homeTeam.length() > MAX_TEAM_NAME_LENGTH) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Home team name too long (max " + MAX_TEAM_NAME_LENGTH + " characters)"));
		}
		if (awayTeam != null && awayTeam.length() > MAX_TEAM_NAME_LENGTH) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Away team name too long (max " + MAX_TEAM_NAME_LENGTH + " characters)"));
		}
		Score score = new Score(homeTeam, awayTeam);
		return mDatabase.insertAsync(score, mDatabaseExecutor).thenApply(id -> {
			JSONObject json = new JSONObject()
					.put(KEY_RESULT, VALUE_RESULT_SUCCESS)
					.put(KEY_ID, id);
			return json;
		});
	}

	private CompletableFuture<JSONObject> handleFinishGame(HttpServletRequest request) {
		int id;
		try {
			id = Integer.parseInt(request.getParameter(PARAM_ID));
		}
		catch (NumberFormatException e) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid ID format: "
								+ request.getParameter(PARAM_ID)));
		}
		return mDatabase.deleteAsync(id, mDatabaseExecutor).thenApply(unused -> {
			JSONObject json = new JSONObject()
					.put(KEY_RESULT, VALUE_RESULT_SUCCESS);
			return json;
		});
	}

	private CompletableFuture<JSONObject> handleUpdateScore(HttpServletRequest request) {
		int homeScore;
		try {
			homeScore = Integer.parseInt(request.getParameter(PARAM_HOME_TEAM_SCORE));
		}
		catch (NumberFormatException e) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid home team score format: "
								+ request.getParameter(PARAM_HOME_TEAM_SCORE)));
		}
		if (homeScore < 0) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid home team score: must not be negative"));
		}
		int awayScore;
		try {
			awayScore = Integer.parseInt(request.getParameter(PARAM_AWAY_TEAM_SCORE));
		}
		catch (NumberFormatException e) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid away team score format: "
								+ request.getParameter(PARAM_AWAY_TEAM_SCORE)));
		}
		if (awayScore < 0) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid away team score: must not be negative"));
		}
		int id;
		try {
			id = Integer.parseInt(request.getParameter(PARAM_ID));
		}
		catch (NumberFormatException e) {
			return CompletableFuture.completedFuture(
					new JSONObject().put(KEY_RESULT, VALUE_RESULT_FAILURE)
					.put(KEY_MESSAGE, "Invalid ID format: "
								+ request.getParameter(PARAM_ID)));
		}
		return mDatabase.updateAsync(id, homeScore, awayScore, mDatabaseExecutor).thenApply(unused -> {
			JSONObject json = new JSONObject()
					.put(KEY_RESULT, VALUE_RESULT_SUCCESS);
			return json;
		});
	}

    @Override
    public void destroy() {
        mDatabaseExecutor.shutdown();
        super.destroy();
    }

	/** Waits until all pending database tasks are complete. For testing only. */
	void awaitDatabase() throws Exception {
		mDatabaseExecutor.submit(() -> {}).get(5, java.util.concurrent.TimeUnit.SECONDS);
	}

	private final ExecutorService mDatabaseExecutor = Executors.newSingleThreadExecutor();
    private final Database mDatabase = new Database();
}
