package cratima;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class NERList {

	public static HashMap<String,String> loadGazetteer(String file, char separator,HashMap<String,String> map, boolean enh, HashMap<String,Boolean>filter, boolean lower){
		HashMap<String,String> ret=map;
		if(map==null)ret=new HashMap<>(100000);
		
		BufferedReader in=null;
		try{
			if(file.endsWith(".gz")) {
				in=new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),Charset.forName("UTF-8")));
			}else {
				in=new BufferedReader(new InputStreamReader(new FileInputStream(file),Charset.forName("UTF-8")));
			}
			
			for(String line=in.readLine();line!=null;line=in.readLine()) {
				line=line.trim();
				if(line.isEmpty())continue;
				
				String[] data=line.split("["+separator+"]");
				if(data.length!=2)continue;
				
				if(filter!=null && !filter.containsKey(data[0]))continue;
				
				String tput=data[0];
				if(lower) {tput=data[1];data[1]=data[1].toLowerCase();}
				
				ret.put(data[1], tput);
				if(enh) {
					for(String s:enhance(line,data[0]))ret.put(s, data[0]);
				}

			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}finally {
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return ret;
	}
	
	public static List<String> enhance(String ent,String type) {
		ArrayList<String> ret=new ArrayList<>(10);
		if(type.equalsIgnoreCase("ORG")) {
			ret.add(ent+"ul");
			ret.add(ent+"ului");
		}
		return ret;
	}
	

	public static HashMap<String,String> loadList(String file, String type,HashMap<String,String> map, boolean enh, boolean lower){
		HashMap<String,String> ret=map;
		if(map==null)ret=new HashMap<>(100000);
		
		BufferedReader in=null;
		try{
			if(file.endsWith(".gz")) {
				in=new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),Charset.forName("UTF-8")));
			}else {
				in=new BufferedReader(new InputStreamReader(new FileInputStream(file),Charset.forName("UTF-8")));
			}
			
			for(String line=in.readLine();line!=null;line=in.readLine()) {
				line=line.trim();
				if(line.isEmpty())continue;
				
				String tput=type;
				if(lower) {tput=line;line=line.toLowerCase();}
				
				ret.put(line, tput);
				if(enh) {
					for(String s:enhance(line,type))ret.put(s, type);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}finally {
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return ret;
	}

}
