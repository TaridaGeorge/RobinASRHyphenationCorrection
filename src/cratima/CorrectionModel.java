package cratima;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CorrectionModel {

	public HashMap<String,Integer> cratima;
	public HashMap<String,Integer> cratima_n1;
	public HashMap<String,Integer> cratima_w;
	public HashMap<String,Integer> cratima_w_n1;
	public HashMap<String,List<String>> search;
	public HashMap<String,String> ne;
	
	/**
	 * @return the cratima
	 */
	public HashMap<String, Integer> getCratima() {
		return cratima;
	}
	/**
	 * @param cratima the cratima to set
	 */
	public void setCratima(HashMap<String, Integer> cratima) {
		this.cratima = cratima;
	}
	/**
	 * @return the cratima_n1
	 */
	public HashMap<String, Integer> getCratima_n1() {
		return cratima_n1;
	}
	/**
	 * @param cratima_n1 the cratima_n1 to set
	 */
	public void setCratima_n1(HashMap<String, Integer> cratima_n1) {
		this.cratima_n1 = cratima_n1;
	}
	/**
	 * @return the cratima_w
	 */
	public HashMap<String, Integer> getCratima_w() {
		return cratima_w;
	}
	/**
	 * @param cratima_w the cratima_w to set
	 */
	public void setCratima_w(HashMap<String, Integer> cratima_w) {
		this.cratima_w = cratima_w;
	}
	/**
	 * @return the cratima_w_n1
	 */
	public HashMap<String, Integer> getCratima_w_n1() {
		return cratima_w_n1;
	}
	/**
	 * @param cratima_w_n1 the cratima_w_n1 to set
	 */
	public void setCratima_w_n1(HashMap<String, Integer> cratima_w_n1) {
		this.cratima_w_n1 = cratima_w_n1;
	}
	
	public void prepare() {
		search=new HashMap<String,List<String>>(100000);
		
		for(String w:this.cratima.keySet()) {
			String wn=w.replaceAll("[-]", "");
			if(!search.containsKey(wn))search.put(wn, new ArrayList<String>(3));
			search.get(wn).add(w);
		}
	}
	
	private WF getPCn1(String w, String w1) {
		WF wf=new WF();
		
		for(String wc:search.get(w)) {
			if(cratima_n1.containsKey(wc+" "+w1)) {
				int f=cratima_n1.get(wc+" "+w1);
				if(wf.word==null || f>wf.freq) {
					wf.word=wc;
					wf.freq=f;
				}
			}
			
		}
		
		if(wf.freq<2)wf.freq=0;
		
		return wf;
	}
	
	private int getPWn1(String w, String w1) {
		String wc=w+" "+w1;
		if(cratima_w_n1.containsKey(wc)) {
			int n=cratima_w_n1.get(wc);
			if(n>0)return n;
		}
		
		return 0;
	}
	
	private WF getPC1(String w) {
		WF wf=new WF();
		
		for(String wc:search.get(w)) {
			if(cratima.containsKey(wc)) {
				int f=cratima.get(wc);
				if(wf.word==null || f>wf.freq) {
					wf.word=wc;
					wf.freq=f;
				}
			}
			
		}
		
		if(wf.freq<2)wf.freq=0;
		
		return wf;
	}
	
	private int getPW1(String w) {
		if(cratima_w.containsKey(w)) {
			int n=cratima_w.get(w);
			if(n>1)return n;
		}
		
		return 0;
	}
	
	public String correct(String text,List<String> comments) {
		StringBuilder sb=new StringBuilder();
		
		String []data=text.split("[ \n\t\r]");
		for(int i=0;i<data.length;i++) {
			String w=data[i];
			if(w.isEmpty())continue;
			
			w=StringUtils.strip(w, ".,/?><;':\"[]\\{}|\n\t\r ");
			if(w.isEmpty())continue;
			
			w=w.toLowerCase();
			
			if(search.containsKey(w)) {
				
				String w1="";
				for(int j=i+1;j<data.length;j++) {
					w1=StringUtils.strip(data[j], ".,/?><;':\"[]\\{}|\n\t\r ");
					if(!w1.isEmpty())break;
				}
				
				WF pcn1=getPCn1(w,w1);
				int pwn1=getPWn1(w,w1);
				WF pc=new WF();
				int pw=0;
				if(pcn1.freq>pwn1) {
					w=pcn1.word;
				}else if(pcn1.freq==pwn1) {
					pc=getPC1(w);
					pw=getPW1(w);
					if(pc.freq>pw)w=pc.word;
				}
				
				if(comments!=null)comments.add(String.format("%s [%d] [%d] [%d] [%d]",w,pcn1.freq,pwn1,pc.freq,pw));
				
			}
			
			if(sb.length()>0)sb.append(" ");
			sb.append(w);
			
		}

		return sb.toString();
	}
	
	public String correctCapital(String text,List<String> comments) {
		StringBuilder sb=new StringBuilder();

		String []data=text.split("[ \n\t\r]");
		for(int i=0;i<data.length;i++) {
			String w=data[i];
			if(w.isEmpty())continue;

			if(ne.containsKey(w))w=ne.get(w);
			
			if(sb.length()>0)sb.append(" ");
			sb.append(w);
		}
		return sb.toString();

	}	
	
}
