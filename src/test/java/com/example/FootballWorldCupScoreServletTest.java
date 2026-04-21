package com.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FootballWorldCupScoreServletTest {

    private FootballWorldCupScoreServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new FootballWorldCupScoreServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    void testInvalidRequest() throws Exception {
    	when(request.getServletPath()).thenReturn("/invalid");

        servlet.doGet(request, response);

        assertEquals("invalid", responseBody.toString());
    }

    @Test
    void testUpdateScore() throws Exception {
        when(request.getServletPath()).thenReturn("/updateScore");

        servlet.doGet(request, response);

        assertEquals("ok", responseBody.toString());
    }
    
    @Test
    void testFinishGame() throws Exception {
        when(request.getServletPath()).thenReturn("/finishGame");

        servlet.doGet(request, response);

        assertEquals("ok", responseBody.toString());
    }
    
    @Test
    void testStartGame() throws Exception {
        when(request.getServletPath()).thenReturn("/startGame");

        servlet.doGet(request, response);

        assertEquals("ok", responseBody.toString());
    }
    
    @Test
    void testGetSummary() throws Exception {
        when(request.getServletPath()).thenReturn("/getSummary");

        servlet.doGet(request, response);

        assertEquals("ok", responseBody.toString());
    }
}
