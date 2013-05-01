package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import binit.tree.CoreNLP_Tree;

/**
 * Servlet implementation class getdata
 */
@WebServlet("/getdata")
public class getdata extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public getdata() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String bid = request.getParameter("bid");
		System.out.println("-->:" + bid);
		
		CoreNLP_Tree cnt = new CoreNLP_Tree();
		List<String> reviews = cnt.getReviewsForBusiness(bid);
		
		List<String>result = cnt.analyze(reviews);
		
		request.setAttribute("result", result);
		
		RequestDispatcher rd = request.getRequestDispatcher("SentimentAnalysis.jsp");
		rd.forward(request, response);
	}

}
