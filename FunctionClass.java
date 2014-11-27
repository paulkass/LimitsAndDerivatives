
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.sql.*;

public class FunctionClass {
	
	FunctionClass[] func_array; // array of child functions
	char variable; // independent variable being used in the function
	char[] basic_operators = {'+', '-', '/', '*', '%'}; // basic operators
	String string_value; // formula of the function
	boolean is_operator; // true if the function is just an operator
	boolean is_power_function; // true if the function is raised to a power
	FunctionClass power; // the function that the current function is being raised to
	String special_function; // the string value of the special function is there is one
	boolean is_special_function; // true if the function is special as shown in the database
	BadFormatError bad_format_error = new BadFormatError("Function Formatted Incorrectly");
	// general exception if the function is formatted improperly
	
	public FunctionClass(String value, char v) throws BadFormatError {
		variable = v;
		func_array = new FunctionClass[0];
		string_value = value;
		if (power_function(value)) { // is the function is a power function, analyze it in a special way
			is_power_function = true;
          	power_analyze(value);
		}
		else if (special_function(value)) {
			special_analyze(value);
		}
		else if (check_for_operator(value)) {
			is_operator = true;
		}
		else if ((value.contains("(") || value.contains("["))) { // if there are still elements to parse, analyze
			is_operator = false;
			analyze(value);
		}
		
	}

	private void power_analyze(String s) {
		int index = -1;
		for (int i=1; i<s.length(); i++) {
			if (brackets_satisfied(s.substring(0, i)) && s.substring(i-1, i).equals("^")) {
				index = i;
				break;
			}
		}
		boolean are_we_done = false;
		int counter = index+1;
		while (!are_we_done) {
			if (brackets_satisfied(s.substring(index, counter)) && counter!=index+1) {
				are_we_done = true;
			} else {
				counter++;
			}
		}
		String power_s = s.substring(index, counter);
		try {
			power = new FunctionClass(power_s, variable);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		String regular_exp = s.substring(0, index-1);
		try {
			analyze(regular_exp);
		} catch (Exception e) {System.out.println(e.getMessage());}
	}

	private boolean power_function(String s) {
      boolean return_value = false;
      for (int i=1; i<s.length(); i++) {
		  if (brackets_satisfied(s.substring(0, i)) && s.substring(i-1, i).equals("^")) {
			  return_value = true;
			  break;
		  }
	  }
	  return return_value;
	}

	private void special_analyze(String value) {

		String[] results = connectToDatabase("special_functions", "Name");

		is_special_function = true;
		// Set string special_function to the function value
		special_function = value.substring(0, value.indexOf("("));
		// assign s_to_analyze the argument of that special function
		String s_to_analyze = value.substring(value.indexOf("("));

		// analyze the argument of the special function
		// catch an error if there is one
		try {
			analyze(s_to_analyze);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}

	}

	private boolean ends_with(String valueOf, String string) {
		boolean return_value = false;
		if (valueOf.length()>=string.length()) {
		  if (valueOf.substring(valueOf.length()-string.length()).equals(string)) {
			return_value = true;
		  }
		}
		return return_value;
	}

	public String evaluate(double value) {
		String return_string = "";
		ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptEngine engine = manager.getEngineByName("js");
	    if (is_operator) {
	    	return_string = string_value;
	    }
		else if (is_power_function) {
			String s = "Math.pow(";
			for (FunctionClass aFunc_array : func_array) {
				s = s.concat(aFunc_array.evaluate(value));
			}
			s = s.concat(", "+power.evaluate(value)).concat(");");
			try {
				return_string = engine.eval(s).toString();
			}
			catch (Exception e) {
				System.out.println(s);
				System.out.println(e.getMessage());
			}
		}
	    else if (is_special_function) {
	    	String[] names = connectToDatabase("special_functions", "Name");
	    	String[] replacements = connectToDatabase("special_functions", "Replacement");
	    	String s = "";
			for (FunctionClass aFunc_array : func_array) {
				s = s.concat(aFunc_array.evaluate(value));
			}
	    	int index = 0;
	    	for (int i=0; i<names.length; i++) {
	    		if (special_function.equals(names[i])) {
	    			index = i;
	    			break;
	    		}
	    	}
	    	String s2 = replacements[index].replace("#", "("+s+")");
	    	s2 = s2.concat(";");
	    	try {
	    		return_string = engine.eval(s2).toString();
	    	}
	    	catch (Exception e) { 
	    		System.out.println(s);
	    		System.out.println(e.getMessage());
	    	}
	    }
	    else if(func_array.length==0 && string_value.contains("^") && string_value.length()>=3) {
	    	String[] arr = string_value.split("\\^");
	    	FunctionClass f = null;
	    	try {
	    		f = new FunctionClass(arr[0], variable);
	    	} catch (Exception e) {}
	    	String func_eval = f.evaluate(value);
	    	String s = "Math.pow("+func_eval+","+arr[1]+");";
	    	try {
	    		return_string = engine.eval(s).toString();
	    	}
	    	catch (Exception e) { 
	    		System.out.println(s);
	    		System.out.println(e.getMessage());
	    	}
	    }
	    else if(func_array.length==0 && string_value.contains("^") && string_value.charAt(0)=='^') {
	    	return_string = string_value;
	    }
	    else if(func_array.length==0) {
	    	String s = string_value.replace(String.valueOf(variable), String.valueOf(value));
	    	s = s.concat(";");
	    	try {
	    		return_string = engine.eval(s).toString();
	    	}
	    	catch (Exception e) { 
	    		System.out.println(s);
	    		System.out.println(e.getMessage());
	    	}
	    }
	    else {
	    	String s = "";
			for (FunctionClass aFunc_array : func_array) {
				s = s.concat(aFunc_array.evaluate(value));
			}
	    	s = s.concat(";");
	    	try {
	    		return_string = engine.eval(s).toString();
	    	}
	    	catch (Exception e) { 
	    		System.out.println(s);
    			System.out.println(e.getMessage());
	    	}
	    }
		return return_string;
	}
	
	private String[] connectToDatabase(String table_name, String property) {
		String[] results = new String[0];
		try {
			 Class.forName("com.mysql.jdbc.Driver");
			 String database_name = "algebra_program";
			 String username = "root";
			 String password = "*********";
			 Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/".concat(database_name),
					 username, password);
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT "+property+" FROM "+table_name+";");
			 while (rs.next()) {
				 String[] temp_arr = new String[results.length+1];
				 for (int i=0; i<results.length; i++) {
					 temp_arr[i] = results[i];
				 }
				 temp_arr[results.length] = rs.getString(1);
				 results = temp_arr;
			 }
			 
			 conn.close();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return results;
	}
	
	private boolean special_function(String value) {
		boolean return_value = false;
		String[] results = new String[0];
		results = connectToDatabase("special_functions", "Name");
		for (String result : results) {
			if (starts_with(value, result)) {
				return_value = true;
			}
		}
		return return_value;
	}

	private boolean starts_with(String value, String string) {
		boolean return_value = false;
		if (value.length()>=string.length()) {
			if (value.substring(0, string.length()).equals(string))
				return_value = true;
		}
		return return_value;
	}

	private boolean check_for_operator(String value) {
		boolean return_value = false;
		for (char basic_operator : basic_operators) {
			if (value.equals(String.valueOf(basic_operator)))
				return_value = true;
		}
		return return_value;
	}

	public void analyze(String value) throws BadFormatError {
		if (!brackets_satisfied(value)) throw bad_format_error;
		String[] expression_array = split_array(value);
		String[] reformated_exp_arr = new String[0];
		boolean are_we_done = false;
		int i = 0;
		// make sure all brackets are satisfied
		while(!are_we_done) {
			if (i==expression_array.length) {
				break;
			}
			String examine = expression_array[i];
			if (brackets_satisfied(examine)) {
				reformated_exp_arr = push(reformated_exp_arr, examine);
				i++;
			}
			else {
				expression_array = join_in_array(expression_array, i, i+1);
			}
			
		}
		i=0;
		// clean up the array
		while(!are_we_done) {
			if (i>=reformated_exp_arr.length) {
				break;
			}
			if (reformated_exp_arr[i].equals("")) {
				String[] temp_arr = new String[reformated_exp_arr.length-1];
				for (int j=0; j<i; j++) {
					temp_arr[j] = reformated_exp_arr[j];
				}
				for (int k=i; k<reformated_exp_arr.length-1; k++){
					temp_arr[k] = reformated_exp_arr[k+1];
				}
				reformated_exp_arr = temp_arr;
				i--;
			}
			else if(is_opening_bracket(reformated_exp_arr[i].charAt(0))) {
				reformated_exp_arr[i] = reformated_exp_arr[i].substring(1, reformated_exp_arr[i].length()-1);
			}
			i++;
		}
		func_array = new FunctionClass[reformated_exp_arr.length];
		for (int j=0; j<reformated_exp_arr.length; j++) {
			func_array[j] = new FunctionClass(reformated_exp_arr[j], variable);
		}
		
	}
	
	private String[] split_array(String value) {
		String[] return_array = new String[0];
		char[] char_array = value.toCharArray();
		char[] buffer = new char[0];
		for (int i=0; i<char_array.length; i++) {
			if (check_for_operator(String.valueOf(char_array[i]))) {
				String s = String.copyValueOf(buffer);
				return_array = push(return_array, s);
				return_array = push(return_array, String.valueOf(char_array[i]));
				buffer = new char[0];
			}
			else if ((is_opening_bracket(char_array[i])) && i!=char_array.length-1) {
				String s = String.copyValueOf(buffer);
				return_array = push(return_array, s);
				buffer = new char[1];
				buffer[0] = char_array[i];
			}
			else if ((is_closing_bracket(char_array[i])) && i!=char_array.length-1) {
				char[] temp_arr = new char[buffer.length+1];
				for (int j=0; j<buffer.length; j++) {
					temp_arr[j] = buffer[j];
				}
				temp_arr[buffer.length] = char_array[i];
				buffer = temp_arr;
				String s = String.copyValueOf(buffer);
				return_array = push(return_array, s);
				buffer = new char[0];
			}
			else if (i!=char_array.length-1){
				char[] temp_arr = new char[buffer.length+1];
				for (int j=0; j<buffer.length; j++) {
					temp_arr[j] = buffer[j];
				}
				temp_arr[buffer.length] = char_array[i];
				buffer = temp_arr;
			}
			else {
				char[] temp_arr = new char[buffer.length+1];
				for (int j=0; j<buffer.length; j++) {
					temp_arr[j] = buffer[j];
				}
				temp_arr[buffer.length] = char_array[i];
				buffer = temp_arr;
				String s = String.copyValueOf(buffer);
				return_array = push(return_array, s);
				buffer = new char[0];
			}
		}
		return return_array;
	}

	private String[] join_in_array(String[] expression_array, int x, int y) {
		String[] return_array = new String[expression_array.length-1];
		for (int j=0; j<x; j++) {
			return_array[j] = expression_array[j];
		}
		return_array[x] = expression_array[x].concat(expression_array[y]);
		for (int i=y; i<expression_array.length-1; i++){
			return_array[i] = expression_array[i+1];
		}
		return return_array;
		
	}

	private boolean brackets_satisfied(String s) {
		boolean return_value = true;
		int o_b_c = 0; int c_b_c = 0;
		for (int i=0; i<s.length(); i++){
			if (is_opening_bracket(s.charAt(i)))
				o_b_c++;
			if (is_closing_bracket(s.charAt(i)))
				c_b_c++;
		}
		if (o_b_c!=c_b_c)
			return_value = false;
		return return_value;
	}
	
	private String[] push(String[] arr, String v) {
		String[] temp_arr = new String[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			temp_arr[i] = arr[i];
		}
		temp_arr[arr.length] = v;
		return temp_arr;
	}
	
	public static double convertToUsableForm(String s) {
		ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptEngine engine = manager.getEngineByName("js");
	    double return_value = 0.0;
	    try {
    		return_value = Double.parseDouble(engine.eval(s).toString());
    	}
    	catch (Exception e) { 
    		System.out.println(s);
    		System.out.println(e.getMessage());
    	}
	    return return_value;
	}
	
	private boolean is_opening_bracket(char c) {
		boolean return_value = false;
		if (c=='[' || c=='(') 
			return_value = true;
		return return_value;
	}
	
	private boolean is_closing_bracket(char c) {
		boolean return_value = false;
		if (c==']' || c==')') 
			return_value = true;
		return return_value;
	}

	public String getFormula() {
		String return_s;
		if (!is_operator) {
			return_s = "(";
			if (is_special_function) {
				return_s = return_s.concat(special_function).concat("(");
			}
			if (func_array.length == 0) {
				return_s = return_s.concat(string_value);
			} else {
				for (FunctionClass aFunc_array : func_array) {
					return_s = return_s.concat(aFunc_array.getFormula());
				}
			}
			if (is_special_function) {
				return_s = return_s.concat(")");
			}
			if (is_power_function) {
				return_s = return_s.concat("^"+power.getFormula());
			}
			return_s = return_s.concat(")");
		} else {
			return_s = string_value;
		}
		return return_s;
	}

	public String derivative(double z) {
		FunctionClass temp_func = null;
		try {
			FunctionClass temp = new FunctionClass(this.getFormula().replace('x', 'z'), 'z');
			String formula_string = "((" + this.getFormula() + ")-(" + temp.getFormula() + "))/(x-z)";
			temp_func = new FunctionClass(formula_string.replace("z", String.valueOf(z)), 'x');
		} catch (Exception e) {}
		Limit l = new Limit(temp_func);
		return l.evalLimit(z).replace("NaN", "Does Not Exist");
	}

	/**
	 * @return the string_value
	 */
	public String getString_value() {
		return string_value;
	}

	/**
	 * @return the is_operator
	 */
	public boolean isIs_operator() {
		return is_operator;
	}
}
