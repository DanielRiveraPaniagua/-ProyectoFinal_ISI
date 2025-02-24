package urjc.isi.controladores;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;

import urjc.isi.entidades.*;

import urjc.isi.service.PeliculasService;

public class PeliculasController {

	private static PeliculasService ps;
	private static String adminkey = "1234";

	/**
	 * Constructor por defecto
	 */
	public PeliculasController() {
		ps = new PeliculasService();
	}

	/**
	 * Maneja las peticiones que llegan al endpoint /peliculas/uploadTable
	 * @param request
	 * @param response
	 * @return El formulario para subir el fichero con las pseudoqueries o una redireccion al endpoint /welcome
	 */
	public static String uploadTable(Request request, Response response) {
		if(!adminkey.equals(request.queryParams("key"))) {
			response.redirect("/welcome"); //Se necesita pasar un parametro (key) para poder subir la tabla
		}
		return "<form action='/peliculas/upload' method='post' enctype='multipart/form-data'>"
			    + "    <input type='file' name='uploaded_films_file' accept='.txt'>"
			    + "    <button>Upload file</button>" + "</form>";
	}

	/**
	 * Metodo que se encarga de manejar las peticiones a /peliculas/upload
	 * @param request
	 * @param response
	 * @return Mensaje de estado sobre la subida de los registros
	 */
	public static String upload(Request request, Response response) {
		return ps.uploadTable(request);
	}

	/**
	 * Metodo encargado de manejar las peticiones a /peliculas/selectAll
	 * @param request
	 * @param response
	 * @return Listado de peliculas que estan en la tabla Peliculas de la base de datos en formato HTML o JSON
	 * @throws SQLException
	 */
	public static String selectAllPeliculas(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		Dictionary<String,String> filter = new Hashtable<String,String>();
		if(request.queryParams("actor")!= null)
			filter.put("actor",request.queryParams("actor"));
		if(request.queryParams("director")!= null)
			filter.put("director",request.queryParams("director"));
		if(request.queryParams("guionista")!= null)
			filter.put("guionista",request.queryParams("guionista"));
		if(request.queryParams("duracion")!=null)
			filter.put("duracion", request.queryParams("duracion"));
		if(request.queryParams("adultos")!=null)
			if(request.queryParams("adultos").equals("si") || request.queryParams("adultos").equals("no"))
				filter.put("adultos", request.queryParams("adultos"));
		if(request.queryParams("titulo")!=null)
			filter.put("titulo", request.queryParams("titulo"));
		if(request.queryParams("year")!=null)
			filter.put("year", request.queryParams("year"));
		if(request.queryParams("idioma")!=null)
			filter.put("idioma", request.queryParams("idioma"));
		if(request.queryParams("order")!=null)
			filter.put("order", request.queryParams("order"));
		if(request.queryParams("genero")!=null) {
			String[] generos = request.queryParamsValues("genero");
			String entrada = "";
			if(generos.length < 2) {
				filter.put("genero", generos[0]);
			} else {
				for(int i=0; i< generos.length;i++) {
					if(i==generos.length-1)
						entrada += generos[i];
					else
						entrada += generos[i] + "%";
				}
				filter.put("genero", entrada);
			} 
		}
		if(request.queryParams("rating")!=null)
			filter.put("rating", request.queryParams("rating"));
		
		if(request.queryParams("nvotos")!=null)
			filter.put("nvotos", request.queryParams("nvotos"));

		output = ps.getAllPeliculas(filter);

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else if(request.queryParams("format")!= null && request.queryParams("format").equals("links")){
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toLinkedHTMLString() +"</br>";
			}
		}else {
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}

	public static String selectAllRanking(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		Dictionary<String,String> filter = new Hashtable<String,String>();

		String form = "Filtrar por: <br/><br/>"
					+ "<form action='/peliculas/ranking' method='get' enctype='multipart/form-data'>"
					+ "Actor: <input type=text name=actor size=30><br/><br/>"
					+ "Director: <input type=text name=director size=30><br/><br/>"
					+ "Guionista: <input type=text name=guionista size=30><br/><br/>"
					+ "Género: <input type=text name=genero size=30><br/><br/>"
					+ "<button type=submit>Enviar </button>"
					+ "</form>";

		if(request.queryParams("actor")!= null && !request.queryParams("actor").equals("")) {
			filter.put("actor",request.queryParams("actor"));
		}
		if(request.queryParams("director")!= null && !request.queryParams("director").equals("")) {
			filter.put("director",request.queryParams("director"));
		}
		if(request.queryParams("guionista")!= null && !request.queryParams("guionista").equals("")) {
			filter.put("guionista",request.queryParams("guionista"));
		}
		if(request.queryParams("genero")!=null && !request.queryParams("genero").equals("")) {
			filter.put("genero", request.queryParams("genero"));
		}
		output = ps.getAllRanking(filter);

		if(filter.isEmpty()) {
			result = result + form;
		}

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else {
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}

	public static String calificacion(Request request, Response response) throws SQLException {

		String output;
		String result =	"<form action='/peliculas/calificacion' method='get' enctype='multipart/form-data'>"
						+ "Pelicula: <input type=text name=pelicula size=30>"
						+ "<button type=submit value=Pelicula>Buscar </button><br/></form>";

		if(request.queryParams("pelicula") != null) {
			output = ps.getCalificacionForPelicula(request.queryParams("pelicula"));
			result = "";
		} else {
			output = "";
		}
		if (!output.equals("")) {
			if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
				response.type("application/json");
				JsonObject json = new JsonObject();
				json.addProperty("status", "SUCCESS");
				json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
				json.addProperty("Titulo", request.queryParams("pelicula"));
				json.addProperty("Calificacion", output);
				json.add("output", json);
				result = json.toString();
			} else {
				result = result + request.queryParams("pelicula") + ": " +  output +"</br>";
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static String infoPeliculas(Request request, Response response) throws SQLException {
		String result = "";
		Dictionary<String,Object> output;

		if(request.queryParams("titulo")== null & request.queryParams("id")==null){
			return "Por favor introduce un título para buscar la película que deseas"+
					"<form action='/peliculas/info' method='get' enctype='multipart/form-data'>"
					+ "Título Pelicula: <input type=text name=titulo size=30>"
					+ "<button type=submit value=Pelicula>Buscar </button><br/></form>";
		}
		if(request.queryParams("id")!=null) {
			output = ps.fullPeliculasInfo(request.queryParams("id"),true);
		}else {
			output = ps.fullPeliculasInfo(request.queryParams("titulo"),false);
		}

		if(output.isEmpty()) {
			response.redirect("/peliculas/info");
			return "La pelicula no se encuentra en la base de datos";
		}

		Peliculas pelicula = (Peliculas)output.get("pelicula");
		List<Personas> actores = (List<Personas>)output.get("actores");
		List<Personas> guionistas = (List<Personas>)output.get("guionistas");
		List<Personas> directores = (List<Personas>)output.get("directores");
		List<Generos> generos = (List<Generos>)output.get("generos");

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			json.add("filmdata", pelicula.toJSONObject());
			JsonArray jarray = new JsonArray();
			for(int i = 0; i < directores.size(); i++) {
				jarray.add(directores.get(i).toJSONObject());;
			}
			json.add("directores", jarray);
			jarray = new JsonArray();
			for(int i = 0; i < guionistas.size(); i++) {
				jarray.add(guionistas.get(i).toJSONObject());;
			}
			json.add("guionistas",jarray);
			jarray = new JsonArray();
			for(int i = 0; i < actores.size(); i++) {
				jarray.add(actores.get(i).toJSONObject());;
			}
			json.add("actores",jarray);
			jarray = new JsonArray();
			for(int i = 0; i < generos.size(); i++) {
				jarray.add(generos.get(i).toJSONObject());;
			}
			json.add("generos",jarray);
			result = json.toString();
		}else{
			result = "<b>Información de: " + pelicula.getTitulo() + " (" + pelicula.getAño()+")</b> </br>";
			result += "<b>PeliculaID: </b>"+ pelicula.getIdPelicula() + "&emsp;<b>Calificacion: </b>";
			result+=(pelicula.getCalificacion()==1)?"Adulta":"No adulta";
			result += "&emsp;<b>Duración: </b>"+pelicula.getDuracion() + "</br>";
			result += "<b>Rating: </b>"+ pelicula.getRating() + "&emsp;<b>Numero de votos: </b>" + pelicula.getNVotos() +"</br>";
			result += "<b>Generos: </b>";
			for(int i = 0; i < generos.size(); i++) {
				result +=  generos.get(i).getNombre();
				result+= i<generos.size()-1?", ":"</br>";
			}
			result += "<b>Dirigida por:</b></br>";
			for(int i = 0; i < directores.size(); i++) {
				result += "&emsp;" + directores.get(i).toHTMLString() +"</br>";
			}
			result += "<b>Escrita por:</b></br>";
			for(int i = 0; i < guionistas.size(); i++) {
				result += "&emsp;" + guionistas.get(i).toHTMLString() +"</br>";
			}
			result += "<b>Lista de actores:</b></br>";
			for(int i = 0; i < actores.size(); i++) {
				result += "&emsp;" + actores.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}

	/**
	 * Maneja las peticiones al endpoint /peliculas/filmsByGenero
	 * @param request
	 * @param response
	 * @return Muestra el listado de las peliculas dado un genero elegido por el usuario.
	 * @throws SQLException
	 */

	public static String filmsByGenero(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		String generos =request.queryString();


		output = ps.getAllPeliculasByGenero(generos);

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else {
			for(int i = 0; i < output.size(); i++) {
				result = result + output.get(i).toHTMLString() +"</br>";
			}
		}

		return result;
	}


	public static String WorstorBestFilmsByYear(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		Dictionary<String,String> filter = new Hashtable<String,String>();

		if(request.queryParams("year")!= null)
			filter.put("year",request.queryParams("year"));
		if(request.queryParams("score")!= null)
			filter.put("score",request.queryParams("score"));

		output = ps.getWorstORBestFilmBy(filter);

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else {
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}

	public static String SelectFilsbyMood(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		Dictionary<String,String> filter = new Hashtable<String,String>();

		if(request.queryParams("mood")!= null)
			filter.put("mood",request.queryParams("mood"));

		output = ps.getfilmsbymood(filter);

		if(filter.isEmpty()) {
			String base = "<h1> <em>Listado de moods posibles </em></h1> <br>";
			String result2 = base + "<form action='/peliculas/filmsbymood' method='get' enctype='multipart/form-data'>" + "  <select name=\"mood\" size=\"5\"  multiple>\n";
			result2 = result2 + "<option value='feliz'>Feliz</option>\n";
			result2 = result2 + "<option value='triste'>Triste</option>\n";
			result2 = result2 + "<option value='indiferente'>Indiferente</option>\n";
			result2 = result2 + "<option value='chill'>Chill</option>\n";
			result2 = result2 + "<option value='atrevido'>Atrevido</option>\n";
			result2 = result2 + "  </select>\n" +
					"  <br/><br/> <input type=\"submit\" value=\"Filtrar\">"
					+ "</form>";
			return result2;
		}

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else {
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}

	
	public static String SelectFilmsbyWeather(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		Dictionary<String,String> filter = new Hashtable<String,String>();

		if(request.queryParams("weather")!= null)
			filter.put("weather",request.queryParams("weather"));

		output = ps.getfilmsbyweather(filter);

		if(filter.isEmpty()) {
			String base = "<h1> <em>Listado de weathers posibles </em></h1> <br>";
			String result2 = base + "<form action='/peliculas/filmsbyweather' method='get' enctype='multipart/form-data'>" + "  <select name=\"weather\" size=\"5\"  multiple>\n";
			result2 = result2 + "<option value='soleado'>Soleado</option>\n";
			result2 = result2 + "<option value='lluvioso'>Lluvioso</option>\n";
			result2 = result2 + "<option value='despejado'>Despejado</option>\n";
			result2 = result2 + "<option value='aire'>Aire</option>\n";
			result2 = result2 + "  </select>\n" +
					"  <br/><br/> <input type=\"submit\" value=\"Filtrar\">"
					+ "</form>";
			return result2;
		}

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			JsonArray array = new JsonArray();
			for(int i = 0; i < output.size(); i++) {
				array.add(output.get(i).toJSONObject());;
			}
			json.add("output", array);
			result = json.toString();
		}else {
			for(int i = 0; i < output.size(); i++) {
			    result = result + output.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}


	/**
	 * Metodo que se encarga de manejar todos los endpoints que cuelgan de /peliculasactores
	 */
	public void peliculasHandler() {
		get("/selectAll", PeliculasController::selectAllPeliculas);
		get("/uploadTable", PeliculasController::uploadTable);
		post("/upload", PeliculasController::upload);
		get("/ranking", PeliculasController::selectAllRanking);
		get("/calificacion", PeliculasController::calificacion);
		get("/filmoftheyear", PeliculasController::WorstorBestFilmsByYear);
		get("/info", PeliculasController::infoPeliculas);
		get("/filmsbymood", PeliculasController::SelectFilsbyMood);
		get("/filmsbyweather", PeliculasController::SelectFilmsbyWeather);

	}

}
