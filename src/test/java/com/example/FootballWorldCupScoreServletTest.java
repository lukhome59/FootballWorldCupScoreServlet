package com.example;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.model.Score;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        mRequest = Mockito.mock(HttpServletRequest.class);
        mResponse = Mockito.mock(HttpServletResponse.class);
        mResponseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(mResponseBody));
        when(mRequest.startAsync()).thenReturn(mAsyncContext);
    }

    @Test
    void testInvalidRequest() throws Exception {
    	when(mRequest.getServletPath()).thenReturn("/invalid");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyResult(new JSONObject(mResponseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testEmptyUpdateScore() throws Exception {
        when(mRequest.getServletPath()).thenReturn("/updateScore");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyResult(new JSONObject(mResponseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    }

    @Test
    void testFinishInvalidGame() throws Exception {
        when(mRequest.getServletPath()).thenReturn("/finishGame");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyResult(new JSONObject(mResponseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartValidGame() throws Exception {
        when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn("aaa");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn("bbb");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyValidStartGame(new JSONObject(mResponseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    }

    @Test
    void testStartGameTwice() throws Exception {	
        when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn("aaa");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn("bbb");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        StringWriter responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));
        
        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        verifyResult(new JSONObject(responseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }
    
    @Test
    void testStartGameTwiceWithMistakenlySwappedTeams() throws Exception {	
        when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn("aaa");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn("bbb");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn("bbb");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn("aaa");
        StringWriter responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));
        
        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        verifyResult(new JSONObject(responseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_FAILURE);
    }

    @Test
    void testStartAndFinishGame() throws Exception {	
        when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn("aaa");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn("bbb");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        when(mRequest.getServletPath()).thenReturn("/finishGame");
        StringWriter responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));
        
        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        verifyResult(new JSONObject(responseBody.toString()),
        		FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
        
        when(mRequest.getServletPath()).thenReturn("/getSummary");
        responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyScoreList(new JSONObject(responseBody.toString()), new ArrayList<Score>());
    }

    @Test
    void testEmptySummary() throws Exception {
        when(mRequest.getServletPath()).thenReturn("/getSummary");

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        verifyScoreList(new JSONObject(mResponseBody.toString()), new ArrayList<Score>());
    }
    
    @Test
    void testOneItemSummary() throws Exception {
    	Score score = new Score("aaa", "bbb");
    	when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn(score.getHomeTeam());
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn(score.getAwayTeam());

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        when(mRequest.getServletPath()).thenReturn("/getSummary");
        StringWriter responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        List<Score> list = new ArrayList<>();
        list.add(score);
        verifyScoreList(new JSONObject(responseBody.toString()), list);
    }
    
    @Test
    void testTwoItemsSummary() throws Exception {
    	Score score1 = new Score("aaa", "bbb");
    	when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn(score1.getHomeTeam());
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn(score1.getAwayTeam());
        
        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        Score score2 = new Score("www", "ttt");
    	when(mRequest.getServletPath()).thenReturn("/startGame");
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_HOME_TEAM)).thenReturn(score2.getHomeTeam());
        when(mRequest.getParameter(FootballWorldCupScoreServlet.PARAM_AWAY_TEAM)).thenReturn(score2.getAwayTeam());
        StringWriter responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();
        
        when(mRequest.getServletPath()).thenReturn("/getSummary");
        responseBody = new StringWriter();
        when(mResponse.getWriter()).thenReturn(new PrintWriter(responseBody));

        mServlet.doPost(mRequest, mResponse);
        awaitExecutor();

        List<Score> list = new ArrayList<>();
        list.add(score2);
        list.add(score1);
        verifyScoreList(new JSONObject(responseBody.toString()), list);
    }

    private void awaitExecutor() throws Exception {
        mServlet.mDatabaseExecutor.submit(() -> {}).get(5, TimeUnit.SECONDS);
    }

    private static void verifyValidStartGame(JSONObject response, String expectedResult) {
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    	assertTrue(response.has(FootballWorldCupScoreServlet.KEY_ID));
    }

    private static void verifyScoreList(JSONObject response, List<Score> expectedList) {
    	verifyResult(response, FootballWorldCupScoreServlet.VALUE_RESULT_SUCCESS);
    	assertTrue(response.has(FootballWorldCupScoreServlet.KEY_LIST));
    	JSONArray jsonList = response.getJSONArray(FootballWorldCupScoreServlet.KEY_LIST);
    	assertEquals(jsonList.length(), expectedList.size());
    }
    
    private static void verifyResult(JSONObject response, String expectedResult) {
    	assertEquals(response.get(FootballWorldCupScoreServlet.KEY_RESULT), expectedResult);
    }

    private AsyncContext mAsyncContext;
    private FootballWorldCupScoreServlet mServlet;
    private HttpServletRequest mRequest;
    private HttpServletResponse mResponse;
    private StringWriter mResponseBody;
}
