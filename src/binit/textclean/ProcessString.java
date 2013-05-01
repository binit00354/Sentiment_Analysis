package binit.textclean;


public class ProcessString {

	public String processString(String string) {
		
		string = string.toLowerCase();
		string = string.replaceAll("[-]{1}|\"", "");
		string = removeNewLine(string);
		string = string.replaceAll("[.]{2,}",".");
	
		string = removeSlash(string);
		string = string.replaceAll("\\(", "");
		string = string.replaceAll("\\)", "");
		string = string.trim();
		
		return string;
	}
	
	private String removeNewLine(String str) {
		
		String string = str;
		int indx = 1;
		
		while(indx != -1) {
		
			indx = string.indexOf("\\n");
			
			String t = "";
			if(indx != -1) {
				
				t = string.substring(0, indx);
				t += string.substring(indx + 2);
				string = t;
			}	
		}
		return string;
	}
	
	private String removeSlash(String str) {
		
		String string = str;
		int indx = 1;
		
		while(indx != -1) {
		
			indx = string.indexOf("\\");
			
			String t = "";
			if(indx != -1) {
				
				t = string.substring(0, indx);
				t += string.substring(indx + 1);
				string = t;
			}	
		}
		return string.trim();
	}
}
