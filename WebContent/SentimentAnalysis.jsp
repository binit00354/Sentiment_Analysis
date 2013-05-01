<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Sentiment Analysis</title>
<link rel="stylesheet" href="css/bootstrap.css" type="text/css">
<link rel="stylesheet" href="css/bootstrap-responsive.css" type="text/css">
<%@ page import="java.util.List" %>
<script language="JavaScript" type="text/JavaScript" src="js/bootstrap.js"></script>
<style type="text/css">
.div
{
   display: table-cell;
   vertical-align:middle;
}
</style>
</head>
<body>
<br>
<table align="center" cellpadding="10" style="background-color:lightgreen;" border="2">
<tr><td align="center"><font size="6" color="green">239 PROJECT</font></td></tr>
<tr><td><font size="4">Classification of Reviews based on Sentiment Anaylsis</font></td></tr>
</table>
<br><br><br>

<!-- <table align="center" border="2" cellpadding="5"> -->
<!-- 	<tr><td>Business Id: &nbsp <input type="text"></input></td><td><button type="submit">Search</button></td></tr> -->
<!-- </table> -->
<br><br>
<table align="center" border="2" cellpadding="10">

	<%
		List<String> res = (List<String>)request.getAttribute("result");
	%>
	<tr><td colspan="2" align="center"><b>OUR RESULTS</b></td></tr>
	<tr><td>Name of Business Entity: &nbsp&nbsp&nbsp</td><td> <%= res.get(0) %> </td></tr> <!-- La Victoria, San Jose</td></tr> -->
	<tr><td>Yelp's rating: &nbsp&nbsp&nbsp</td><td> <%= res.get(1) %> </td></tr> <!-- * * * * *</td></tr> -->
	<tr><td>Our rating: &nbsp&nbsp&nbsp</td><td> <%= res.get(2) %> </td></tr>
	<tr><td>Positive/Negative comments: &nbsp&nbsp&nbsp</td>
		<td>
			<div class="progress" class="align_center">
			  <div class="bar bar-success" style="width: 70%;"><%= res.get(3) %></div>
			  <div class="bar bar-danger" style="width: 30%;"><%= res.get(4) %></div>
			</div>
		</td>
	</tr>
<!-- 	<tr><td>Distinctive Features: &nbsp&nbsp&nbsp</td><td>Clean, Tasty Food</td></tr> -->
</table>
<br><br><br>
<center>
<font size="2"><b>By - Binit Topiwala, Hitesh Patwari, Sagar Vikani</b></font>	
</center>
</body>
</html>