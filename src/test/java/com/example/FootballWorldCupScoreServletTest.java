package com.example;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.model.DatabaseScore;
import com.example.model.Score;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class FootballWorldCupScoreServletTest {
    @BeforeEach
    void setUp() throws Exception {
    	mAsyncContext = Mockito.mock(AsyncContext.class);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(mAsyncContext).start(any(Runnable.class));
        mServlet = new FootballWorldCupScoreServlet();
    }

    @Test
    void testInvalidRequest() throws Exception {
    	HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    	HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    	StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.startAsync()).thenReturn(mAsyncContext);
    	when(request.getServletPath()).thenReturn("/invalid");

        mServlet.doPost(request, response);
        awaitExecutor();

        verifyResult(new JSONObject(responseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testInvalidUpdateScore() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

        JSONObject response2 = executeUpdateScore(id, "_", "_");
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }
    
    @Test
    void testUpdateNegativeScore() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

        JSONObject response2 = executeUpdateScore(id, "1", "-1");
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testMissingUpdateScore() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

    	JSONObject response2 = executeUpdateScore(id + 1, "12", "13");
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testUpdateScore() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

    	JSONObject response2 = executeUpdateScore(id, "10", "2");
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);

        List<DatabaseScore> list = new ArrayList<>();
        DatabaseScore dbScore = new DatabaseScore(score);
        dbScore.setHomeScore(10);
        dbScore.setAwayScore(2);
        list.add(dbScore);
        validateSummary(list);
    }

    @Test
    void testUpdateScoreTwice() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

    	executeUpdateScore(id, "10", "2");
    	JSONObject response2 = executeUpdateScore(id, "20", "4");
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);

        List<DatabaseScore> list = new ArrayList<>();
        DatabaseScore dbScore = new DatabaseScore(score);
        dbScore.setHomeScore(20);
        dbScore.setAwayScore(4);
        list.add(dbScore);
        validateSummary(list);
    }

    @Test
    void testOrderAfterUpdateScore() throws Exception {
    	Score score1 = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score1);
    	int id = verifyValidId(response1);

    	Score score2 = new Score("xxx", "zzz");
    	executeStartGame(score2);

    	executeUpdateScore(id, "5", "4");

        List<DatabaseScore> list = new ArrayList<>();
        DatabaseScore dbScore1 = new DatabaseScore(score1);
        dbScore1.setHomeScore(5);
        dbScore1.setAwayScore(4);
        DatabaseScore dbScore2 = new DatabaseScore(score2);
        list.add(dbScore1);
        list.add(dbScore2);
        validateSummary(list);
    }

    @Test
    void testFinishInvalidGame() throws Exception {
    	JSONObject response = executeFinishGame(100);
        verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartValidGame() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response = executeStartGame(score);
    	verifyValidId(response);
    }

    @Test
    void testStartTheSameTeam() throws Exception {
    	Score score = new Score("the", "the");
    	JSONObject response = executeStartGame(score);
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }
    
    @Test
    void testStartTeamWthiTooLongName() throws Exception {
    	Score score = new Score("1234567890A", "aaa");
    	JSONObject response = executeStartGame(score);
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartGameTwice() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	executeStartGame(score);
    	JSONObject response = executeStartGame(score);
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartGameTwiceWithMistakenlySwappedTeams() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	executeStartGame(score);
    	Score swappedScore = new Score("bbb", "aaa");
    	JSONObject response = executeStartGame(swappedScore);
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartAndFinishGame() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

    	JSONObject response2 = executeFinishGame(id);
        verifyResult(response2, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);

        List<DatabaseScore> list = new ArrayList<>();
        validateSummary(list);
    }

    @Test
    void testEmptySummary() throws Exception {
        List<DatabaseScore> list = new ArrayList<>();
        validateSummary(list);
    }

    @Test
    void testOneItemSummary() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	JSONObject response1 = executeStartGame(score);
    	int id = verifyValidId(response1);

        List<DatabaseScore> list = new ArrayList<>();
        DatabaseScore dbScore = new DatabaseScore(score);
        assertEquals(id, dbScore.getId());
        list.add(dbScore);
        validateSummary(list);
    }

    @Test
    void testTwoItemsSummary() throws Exception {
    	Score score1 = new Score("aaa", "bbb");
    	executeStartGame(score1);

    	Score score2 = new Score("www", "ttt");
    	executeStartGame(score2);

        List<DatabaseScore> list = new ArrayList<>();
        list.add(new DatabaseScore(score2));
        list.add(new DatabaseScore(score1));
        validateSummary(list);
    }

    private JSONObject executeStartGame(Score score) throws Exception {
    	HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    	StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.startAsync()).thenReturn(mAsyncContext);
    	when(request.getServletPath()).thenReturn("/startGame");
        when(request.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM))
        		.thenReturn(score.getHomeTeam());
        when(request.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM))
        		.thenReturn(score.getAwayTeam());

        mServlet.doPost(request, response);
        awaitExecutor();

        return new JSONObject(responseBody.toString());
    }

    private JSONObject executeFinishGame(int id) throws Exception {
    	HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    	StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.startAsync()).thenReturn(mAsyncContext);
    	when(request.getServletPath()).thenReturn("/finishGame");
    	when(request.getParameter(FootballWorldCupScoreServlet.PARAM_ID))
				.thenReturn(String.valueOf(id));

        mServlet.doPost(request, response);
        awaitExecutor();

        return new JSONObject(responseBody.toString());
    }

    private JSONObject executeUpdateScore(int id, String homeScore, String awayScore) throws Exception {
    	HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    	StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.startAsync()).thenReturn(mAsyncContext);
    	when(request.getServletPath()).thenReturn("/updateScore");
        when(request.getParameter(FootballWorldCupScoreServlet.PARAM_ID))
        		.thenReturn(String.valueOf(id));
        when(request.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM_SCORE))
        		.thenReturn(homeScore);
        when(request.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM_SCORE))
        		.thenReturn(awayScore);

        mServlet.doPost(request, response);
        awaitExecutor();

        return new JSONObject(responseBody.toString());
    }

    private void validateSummary(List<DatabaseScore> list) throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.startAsync()).thenReturn(mAsyncContext);
        when(request.getServletPath()).thenReturn("/getSummary");

        mServlet.doPost(request, response);
        awaitExecutor();

        verifyScoreList(new JSONObject(responseBody.toString()), list);
    }

    private void awaitExecutor() throws Exception {
        mServlet.awaitDatabase();
    }

    private static int verifyValidId(JSONObject response) {
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    	assertTrue(response.has(FootballWorldCupScoreServlet.KEY_ID));
    	return response.getInt(FootballWorldCupScoreServlet.KEY_ID);
    }

    private static void verifyScoreList(JSONObject response, List<DatabaseScore> expectedList) {
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    	assertTrue(response.has(FootballWorldCupScoreServlet.KEY_LIST));
    	JSONArray jsonList = response.getJSONArray(FootballWorldCupScoreServlet.KEY_LIST);
    	assertEquals(jsonList.length(), expectedList.size());
    	for (int i = 0; i < jsonList.length(); i++) {
    		assertEquals(jsonList.getJSONObject(i).getString(FootballWorldCupScoreServlet.KEY_HOME_TEAM),
    				expectedList.get(i).getHomeTeam());
    		assertEquals(jsonList.getJSONObject(i).getString(FootballWorldCupScoreServlet.KEY_AWAY_TEAM),
    				expectedList.get(i).getAwayTeam());
    		assertEquals(jsonList.getJSONObject(i).getInt(FootballWorldCupScoreServlet.KEY_HOME_TEAM_SCORE),
    				expectedList.get(i).getHomeScore());
    		assertEquals(jsonList.getJSONObject(i).getInt(FootballWorldCupScoreServlet.KEY_HOME_TEAM_SCORE),
    				expectedList.get(i).getHomeScore());
    		assertEquals(jsonList.getJSONObject(i).getInt(FootballWorldCupScoreServlet.KEY_ID),
    				expectedList.get(i).getId());
    	}
    }

    private static void verifyResult(JSONObject response, String expectedResult) {
    	assertEquals(response.get(FootballWorldCupScoreServlet.KEY_RESULT), expectedResult);
    }

    private AsyncContext mAsyncContext;
    private FootballWorldCupScoreServlet mServlet;
}
