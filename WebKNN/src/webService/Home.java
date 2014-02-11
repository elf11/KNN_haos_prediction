package webService;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Home extends HttpServlet {

	static final String ATTRIBUTE_STATUS = "status";
	  
	public String getParameter(HttpServletRequest req, String parameter) throws ServletException {
	    String value = req.getParameter(parameter);
	    if (isEmptyOrNull(value)) {
	      throw new ServletException("Parameter " + parameter + " not found");
	    }
	    return value.trim();
	}

	public String getParameter(HttpServletRequest req, String parameter, String defaultValue) {
	    String value = req.getParameter(parameter);
	    if (isEmptyOrNull(value)) {
	      value = defaultValue;
	    }
	    return value.trim();
	}

	public boolean isEmptyOrNull(String value) {
	    return value == null || value.trim().length() == 0;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String word = req.getParameter("word");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.print("<html><body>");
		out.print("<head>");
		out.print("  <title>Demo</title>");
		out.print("</head>");
		String status = (String) req.getAttribute(ATTRIBUTE_STATUS);
		if (status != null) {
			out.print(status);
		}
		out.print("<h2>Home page for the knn demo<br>" + word + " <br></h2>");
		
		out.print("</body></html>");
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}

}
