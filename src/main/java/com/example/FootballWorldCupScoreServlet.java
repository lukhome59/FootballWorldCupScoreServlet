package com.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/getSummary", "/startGame", "/finishGame", "/updateScore"})
public class FootballWorldCupScoreServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
                response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String result = "invalid";
        switch (request.getServletPath()) {
        	case "/getSummary":
        	case "/startGame":
        	case "/finishGame":
        	case "/updateScore":
        		result = "ok";
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.print(result);
        }
    }
}
