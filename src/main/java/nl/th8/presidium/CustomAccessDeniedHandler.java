package nl.th8.presidium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;


public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
//        logger.info(e.getMessage());
//        logger.info(String.valueOf(e.getCause()));
//        logger.info(Arrays.toString(e.getStackTrace()));
//        logger.info(httpServletRequest.getQueryString());

        httpServletResponse.sendRedirect("/?accessdenied");
    }
}
