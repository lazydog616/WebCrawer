package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;

import org.w3c.dom.Document;

public class XPathEngineImpl implements XPathEngine {
	private ArrayList<String> xpaths;
	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
		xpaths = new ArrayList<String>();
	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		xpaths.clear();
		for(int i = 0; i < s.length; i++)
			xpaths.add(s[i]);
	}

	public boolean isValid(int i) {
		/* TODO: Check which of the XPath expressions are valid */
		String xpath  = xpaths.get(i);
		return isValid(xpath);
		
	}
	//check the string contains any operators or not
	boolean containNoOperator(String s){
		if(s.contains("[")||s.contains("]")||s.contains("(")||s.contains(")")||!s.contains("\"")&&!s.contains("=")||!s.contains("contains")||!s.contains("text()"))
			return false;
		else return true;
	}
	//check the interval is valid or not, the string between two axis '/'
	private boolean isValidInterval(String interval){
		if(containNoOperator(interval))
			return true;
		else{
			ArrayList<Integer> S_start_pos = new ArrayList<Integer>();//[__pos
			ArrayList<Integer> S_end_pos = new ArrayList<Integer>();//]_pos
			ArrayList<Integer> P_start_pos = new ArrayList<Integer>();//(_pos
			ArrayList<Integer> P_end_pos = new ArrayList<Integer>();//)_pos
			ArrayList<Integer> Q_pos = new ArrayList<Integer>();//"_pos
			ArrayList<Integer> E_pos = new ArrayList<Integer>();//=_pos
			ArrayList<Integer> a_pos = new ArrayList<Integer>();//@_pos
			int next = 0;
			//traversal through step string and record operator position
			while(next < interval.length()){
				switch(interval.charAt(next)){
				case '[':S_start_pos.add(next);break;
				case ']':S_end_pos.add(next);break;
				case '(':P_start_pos.add(next);break;
				case ')':P_end_pos.add(next);break;
				case '"':Q_pos.add(next);break;
				case '=':E_pos.add(next);break;
				case '@':a_pos.add(next);break;
				default:break;
				}
				next ++;
			}
			//should have []
			if(S_start_pos.isEmpty()) return false;
			else{
				if(S_end_pos.isEmpty()) return false;
				else{
					if(S_start_pos.get(0)>S_end_pos.get(0)) return false;//if [ is after ]
					//nodename should not contain any operator
					String nodename = interval.substring(0, S_start_pos.get(0));
					if(!containNoOperator(nodename)) return false;
					if(S_end_pos.get(S_end_pos.size()-1) != interval.length()-1) return false;//] should be at the end of this interval
					String test = interval.substring(S_start_pos.get(0)+1, interval.length()-1);
					
					//test :for case @attname = "..."
					if(a_pos.size()==1&&E_pos.size()==1&&Q_pos.size()==2&&P_start_pos.size()==0&&P_end_pos.size()==0&&S_start_pos.size()==1&&S_end_pos.size()==1){
						if(a_pos.get(0)<S_start_pos.get(0)||a_pos.get(0)>S_end_pos.get(0))return false;
						if(E_pos.get(0)<S_start_pos.get(0)||E_pos.get(0)>S_end_pos.get(0))return false;
						if(Q_pos.get(0)<S_start_pos.get(0)||Q_pos.get(0)>S_end_pos.get(0))return false;
						if(Q_pos.get(1)<S_start_pos.get(0)||Q_pos.get(1)>S_end_pos.get(0))return false;
						if(a_pos.get(0)>E_pos.get(0)) return false;
						if(E_pos.get(0)!=Q_pos.get(0)-1) return false;//=_pos should equal "_pos_0 - 1
						if(Q_pos.get(0) == Q_pos.get(1)-1) return false; //"..." is null
						//@ should at [_pos_0 +1 and the second " should be at ]_pos_0 -1
						if(a_pos.get(0)!=S_start_pos.get(0)+1||Q_pos.get(1)!=S_end_pos.get(0)-1) return false;
						String attname = interval.substring(a_pos.get(0)+1,E_pos.get(0));
						if(containNoOperator(attname)) return true;
						else return false;
					}
					//test :for case text()="..."
					if(interval.contains("text()")&&E_pos.size()==1&&P_start_pos.size()==1&&P_end_pos.size()==1&&Q_pos.size()==2&&S_start_pos.size()==1&&S_end_pos.size()==1){
						if(P_start_pos.get(0) != P_end_pos.get(1)-1) return false;
						String text_string = interval.substring(S_start_pos.get(0)+1, P_start_pos.get(0));
						if(text_string.compareTo("text")!=0) return false;
						if(Q_pos.get(0)!=E_pos.get(0)+1||Q_pos.get(1)!=S_end_pos.get(0)-1) return false;
						return true;
					}
					//test :for case contains(text(),"...")
					if(interval.contains("contains(text(),\"")&&P_end_pos.size()==2&&Q_pos.size()==2&&S_start_pos.size()==1&&S_end_pos.size()==1){
						String contains_string = interval.substring(S_start_pos.get(0)+1, P_start_pos.get(0));
						if(contains_string.compareTo("contains")!=0) return false;
						if(Q_pos.get(1)!=P_end_pos.get(1)-1) return false;
						return true;
					}
					//test :for case step
					if(test.contains("/")){
						return isValidStep(test);
					}
					return false;
				}
			}
			
			
		}
	}
	private boolean isValidStep(String step){
		int next = 0;
		while(next < step.length()){
			if(step.charAt(next) == '/'){
				if(!step.substring(1, next).contains("["))//make sure we are not stop at '/' in a []
					break;
			}
			next++;
		}
		String interval = step.substring(0, next);
	    if(!isValidInterval(interval))
	    	return false;
	    else{
	    	if(next == step.length())//if this is the last step in the xpath, there's no '/' in this step
	    		return true;
	    	if(next == step.length()-1)//if this step ends with '/'
	    		return false;
	    	return isValidStep(step.substring(next+1, step.length()));
	    }	
		
	}
	private boolean  isValid(String xpath){
		if(xpath.isEmpty()) return false;
		if(xpath.charAt(0) != '/')
			return false;
		else{
			return isValidStep(xpath.substring(1,xpath.length()));
		}
	}

	public boolean[] evaluate(Document d) {
		/* TODO: Check whether the document matches the XPath expressions */
		if(xpaths.isEmpty())
			return null;
		
	}

}
