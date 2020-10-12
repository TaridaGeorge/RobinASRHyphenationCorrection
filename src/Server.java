import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import cratima.CSVAssoc;
import cratima.CorrectionModel;
import cratima.NERList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

import java.security.SecureRandom;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class implements the server for LangResServer
 * 
 *
 */
public class Server {
    
    public static int PORT=8012;
    
    public static final  SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws Exception {
        String dbpath=null;
        
        if(args.length==2) {
        	PORT=Integer.parseInt(args[0]);
        	dbpath=args[1];
        }else {
        	System.out.println("Syntax:\n\n\tServer PORT DBPATH\n");
        	System.out.println("\t\tPORT=server port");
        	System.out.println("\t\tDBPATH=resource database path");
        	System.exit(-1);
        }
        
        System.out.println("Loading resources from ["+dbpath+"]");
        CorrectionModel model=new CorrectionModel();
        model.setCratima(CSVAssoc.loadResource(dbpath+"/cratima.csv.gz",'\t'));
        model.setCratima_n1(CSVAssoc.loadResource(dbpath+"/cratima_n_1.csv.gz",'\t'));
        model.setCratima_w(CSVAssoc.loadResource(dbpath+"/cratima_without.csv.gz",'\t'));
        model.setCratima_w_n1(CSVAssoc.loadResource(dbpath+"/cratima_without_n_1.csv.gz",'\t'));
        
        HashMap<String,Boolean> filter=new HashMap<>(2);
        filter.put("PER", true);
        filter.put("LOC", true);
        model.ne=NERList.loadGazetteer(dbpath+"/ne.1.gazetteer", ' ', null, true, filter,true);
        NERList.loadList(dbpath+"/locnames.txt", "LOC", model.ne, true,true);
        NERList.loadList(dbpath+"/orase.txt", "LOC", model.ne, true,true);
        
        model.prepare();
        System.out.println("All resources loaded");
        
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",PORT), 0);
        server.createContext("/test", new TESTHandler());
        //server.createContext("/", new FileHandler("html/index.html"));
        server.createContext("/correct", new CorrectionHandler("logs",model));
        
        // Add all files with known extenstion from html folder
        File folderFile = new File("html");
        File[] listOfFiles = folderFile.listFiles();
        if(listOfFiles!=null) {
	        for (int i = 0; i < listOfFiles.length; i++) {
	            if (listOfFiles[i].isFile()) {
	                String fname=listOfFiles[i].getName();
	                if(fname.endsWith(".html") || fname.endsWith(".js") || fname.endsWith(".gif") || fname.endsWith(".css") || fname.endsWith(".jpg") || fname.endsWith(".jpeg") || fname.endsWith(".png")){
	                    server.createContext("/"+fname, new FileHandler("html/"+fname));
	                }
	            }
	        }
        }
        
        System.out.println("Starting server on port "+PORT);
        server.start();
    }
    
    public static HashMap<String,String> getRequestParams(InputStream input) throws IOException {
        HashMap<String,String> params=new HashMap<String,String>(10);
        Integer state=0;
        String separator=""; 
        String currentName="";
        String currentContent="";

        BufferedReader in=new BufferedReader(new InputStreamReader(input));
        for(String line=in.readLine();line!=null && state!=100;line=in.readLine()){
            switch(state){
            case 0:
                if(line.length()>0){
                    separator=line;
                    state=1;
                }
                break;
            case 1:
                if(line.startsWith("Content-Disposition: form-data")){
                    int pos=line.indexOf("name=\"");
                    if(pos>0){
                        currentName=line.substring(pos+6);
                        pos=currentName.indexOf('"');
                        if(pos>0){
                            currentName=currentName.substring(0,pos);
                            state=2;
                        }
                    }
                }
                break;
            case 2:
                if(line.length()==0)state=3;
                break;
            case 3:
                if(line.startsWith(separator)){
                    params.put(currentName, currentContent);
                    currentName="";
                    currentContent="";
                    state=1;
                    if(line.equals(separator+"--"))state=100;
                }else{
                    if(currentContent.length()>0)currentContent+="\n";
                    currentContent+=line;
                }
                break;
            }
            //stringBuilder.append(line + "\n");
        };
        in.close();
        
        return params;
    }
    
    public static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }    

    static class TESTHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String response = "This is the received request:\n";
            
            HashMap<String,String> params=getRequestParams(ex.getRequestBody());

            for(Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                response+=key+"="+value+"\n";
            }
            
            Headers headers=ex.getResponseHeaders();
            headers.add("Content-type", "text/plain; charset=utf-8");
            
            ex.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
            
            ex.close();
        }
    }

    static class FileHandler implements HttpHandler {
        public byte[] content=null;
        public String fname=null;
        
        public FileHandler(String fname) throws FileNotFoundException, IOException {
            this.fname=fname;
            Path path = Paths.get(fname);
            content = Files.readAllBytes(path);
        }
        
        @Override
        public void handle(HttpExchange ex) throws IOException {
            //System.out.println("Request for: "+ex.getRequestURI().toString()+" Serving: "+fname);
            
            Headers headers=ex.getResponseHeaders();
            
            if(fname.endsWith(".js"))
                headers.add("Content-type", "application/javascript");
            else if(fname.endsWith(".gif"))
                headers.add("Content-type", "image/gif");
            else if(fname.endsWith(".css"))
                headers.add("Content-type", "text/css");
            else
                headers.add("Content-type", "text/html; charset=utf-8");
            
            ex.sendResponseHeaders(200, content.length);
            
            OutputStream os = ex.getResponseBody();
            os.write(content);
            os.close();
            
            ex.close();
        }
    }
    
    static class CorrectionHandler implements HttpHandler {
        public String base_log_path;
        private static final boolean FULL_LOG=false;
        private CorrectionModel model;
        
        public CorrectionHandler(String base_log_path, CorrectionModel model ){
            this.base_log_path=base_log_path;
            this.model=model;
        }
        
        public String escape(String s){
            s=s.replace("\n","\\n");
            s=s.replace("\"","'");
            return s;
        }
        
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try{
            	Map<String,String> params=splitQuery(ex.getRequestURI());
            	HashMap<String,String> paramsPost=getRequestParams(ex.getRequestBody());
            	if(paramsPost!=null) {
            		for(Map.Entry<String, String> entry:paramsPost.entrySet()) {
            			params.put(entry.getKey(), entry.getValue());
            		}
            	}
            	
                if(!params.containsKey("text"))return ;
                
                String text=params.get("text");
                ArrayList<String> comments=new ArrayList<String>(10);
                String result=model.correct(text, comments);
                result=model.correctCapital(result, comments);

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos,StandardCharsets.UTF_8)));

                JSONObject obj = new JSONObject();
                obj.put("status", "OK");
                obj.put("text", result);
                JSONArray arr=new JSONArray();
                for(String s:comments)arr.put(s);
                obj.put("comments", arr);

                out.print(obj.toString());
                out.close();

                String ip=ex.getRemoteAddress().getAddress().toString();
                ip=ip.substring(ip.indexOf('/')+1);
                
                DateFormat dateFormat = new SimpleDateFormat("yyyy");
                Date date = new Date();
                String year=dateFormat.format(date);
                dateFormat = new SimpleDateFormat("MM");
                String month=dateFormat.format(date);
                dateFormat = new SimpleDateFormat("dd");
                String day=dateFormat.format(date);
                dateFormat=new SimpleDateFormat("HHmmss");
                String time=dateFormat.format(date);
                String path=this.base_log_path+"/"+year+"/"+month;
                File log_path=new File(path);
                log_path.mkdirs();
                
                String response=new String(baos.toByteArray(),StandardCharsets.UTF_8);
    
                PrintWriter outLog = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"/"+day+".log",true),StandardCharsets.UTF_8)));
                
                if(FULL_LOG) {
                	outLog.println(String.format("%s-%s-%s %s %s", year,month,day,time,ip));
	                Headers reqh=ex.getRequestHeaders();
	                for(Map.Entry<String,List<String>> entry:reqh.entrySet()){
	                    for(String s:entry.getValue()){
	                        outLog.println(entry.getKey()+":"+s);
	                    }
	                }
	                outLog.println("\n--------------------------------------------------------------------------------\n");
	                outLog.println("Request params:");
	                for(Map.Entry<String,String> entry:params.entrySet()) {
	                	outLog.println(entry.getKey()+"="+entry.getValue());
	                }
	                outLog.println("\n--------------------------------------------------------------------------------\n");
	                outLog.println("Response:");
	                outLog.println(response);
                }else {
                	outLog.println(String.format("%s-%s-%s %s\t%s\t%s\t%s", year,month,day,time,ip,text,result));
                }
                outLog.close();
                
                Headers headers=ex.getResponseHeaders();
                headers.add("Content-type", "text/plain; charset=utf-8");
                
                ex.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                
                OutputStream os = ex.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                
                ex.close();
            }catch(Exception exception){
                System.out.println("Exception while handling request:");
                exception.printStackTrace();

                ex.sendResponseHeaders(500, 0);
                
                OutputStream os = ex.getResponseBody();
                os.close();

            }
                
        }
    }
    

}
