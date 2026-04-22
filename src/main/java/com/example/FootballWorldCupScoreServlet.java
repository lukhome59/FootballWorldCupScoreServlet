package com.example;

import com.example.database.Database;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(asyncSupported = true, urlPatterns = {"/getSummary", "/startGame", "/finishGame", "/updateScore"})
public class FootballWorldCupScoreServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String KEY_RESULT = "result";
    public static final String VALUE_RESULT_SUCCESS = "success";
    public static final String VALUE_RESULT_FAILURE = "failure";
    public static final String KEY_ID = "id";
    public static final String PARAM_HOME_TEAM = "homeTeam";
    public static final String PARAM_AWAY_TEAM = "awayTeam";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LIST = "list";
    public static final String KEY_HOME_TEAM_NAME = "homeTeam";
    public static final String KEY_AWAY_TEAM_NAME = "awayTeam";
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
        			result = CompletableFuture.completedFuture(
        					new JSONObject().put(KEY_RESULT, VALUE_RESULT_SUCCESS));
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
			for (Score score : list) {
				JSONObject item = new JSONObject().put(KEY_HOME_TEAM_NAME, score.getHomeTeam())
						.put(KEY_HOME_TEAM_SCORE, score.getHomeScore())
						.put(KEY_AWAY_TEAM_NAME, score.getAwayTeam())
						.put(KEY_AWAY_TEAM_SCORE, score.getAwayScore());
				jsonArray.put(item);
			}
			json.put(KEY_LIST, jsonArray);
			return json;
		});
	}
	
	private CompletableFuture<JSONObject> handleStartGame(HttpServletRequest request) {
		String homeTeam = request.getParameter(PARAM_HOME_TEAM);
		String awayTeam = request.getParameter(PARAM_AWAY_TEAM);
		Score score = new Score(homeTeam, awayTeam);
		return mDatabase.insertAsync(score, mDatabaseExecutor).thenApply(id -> {
			JSONObject json = new JSONObject()
					.put(KEY_RESULT, VALUE_RESULT_SUCCESS)
					.put(KEY_ID, id);
			return json;
		});
	}

	private CompletableFuture<JSONObject> handleFinishGame(HttpServletRequest request) {
		String homeTeam = request.getParameter(PARAM_HOME_TEAM);
		String awayTeam = request.getParameter(PARAM_AWAY_TEAM);
		Score score = new Score(homeTeam, awayTeam);
		return mDatabase.deleteAsync(score, mDatabaseExecutor).thenApply(unused -> {
			JSONObject json = new JSONObject()
					.put(KEY_RESULT, VALUE_RESULT_SUCCESS);
			return json;
		});
	}

	public final ExecutorService mDatabaseExecutor = Executors.newSingleThreadExecutor();
    private final Database mDatabase = new Database();
}
