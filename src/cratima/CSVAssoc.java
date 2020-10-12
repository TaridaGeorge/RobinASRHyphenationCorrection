package cratima;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class CSVAssoc {

	public static HashMap<String,Integer> loadResource(String file, char separator){
		HashMap<String,Integer> ret=new HashMap<>(100000);
		
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
				
				Integer n=Integer.parseInt(data[0]);
				ret.put(data[1], n);
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
