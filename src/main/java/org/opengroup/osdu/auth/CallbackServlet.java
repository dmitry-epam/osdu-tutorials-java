package org.opengroup.osdu.auth;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class CallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=utf-8");

        PrintWriter writer = resp.getWriter();
        writer.print("JWT token: ");
        writer.println(req.getSession().getAttribute("id_token"));
        writer.flush();
    }
}
