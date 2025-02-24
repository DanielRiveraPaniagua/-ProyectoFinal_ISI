package urjc.isi.controladores;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;
import urjc.isi.entidades.Personas;
import urjc.isi.service.GuionistasService;
import urjc.isi.entidades.Peliculas;

public class GuionistasController {
	private static GuionistasService gs;
	private static String adminkey = "1234";

	/**
	 * Constructor por defecto
	 */
	public GuionistasController() {
		gs = new GuionistasService();
	}

	/**
	 * Maneja las peticiones que llegan al endpoint /guionistas/uploadTable
	 * @param request
	 * @param response
	 * @return El formulario para subir el fichero con las pseudoqueries o una redireccion al endpoint /welcome
	 */
	public static String uploadTable(Request request, Response response) {
		if(!adminkey.equals(request.queryParams("key"))) {
			response.redirect("/welcome"); //Se necesita pasar un parametro (key) para poder subir la tabla
		}
		return "<form action='/guionistas/upload' method='post' enctype='multipart/form-data'>"
			    + "    <input type='file' name='uploaded_guionistas_file' accept='.txt'>"
			    + "    <button>Upload file</button>" + "</form>";
	}

	/**
	 * Metodo que se encarga de manejar las peticiones a /guionistas/upload
	 * @param request
	 * @param response
	 * @return Mensaje de estado sobre la subida de los registros
	 */
	public static String upload(Request request, Response response) {
		return gs.uploadTable(request);
	}

	/**
	 * Maneja las peticiones al endpoint /guionistas/selectAll
	 * @param request
	 * @param response
	 * @return La lista de actores que hay en la tabla Guioistas de la base de datos en formato HTML o JSON
	 * @throws SQLException
	 */
	public static String selectAllGuionistas(Request request, Response response) throws SQLException {
		List<Personas> output;
		String result = "";

		Dictionary<String,String> filter = new Hashtable<String,String>();
		if(request.queryParams("director")!=null)
			filter.put("director", request.queryParams("director"));
		if(request.queryParams("actor")!=null)
			filter.put("actor", request.queryParams("actor"));
		if(request.queryParams("titulo")!=null)
			filter.put("titulo", request.queryParams("titulo"));
		if(request.queryParams("id_guio")!= null)
			filter.put("id_guio",request.queryParams("id_act"));
		if(request.queryParams("name")!= null)
			filter.put("name",request.queryParams("name"));
		if(request.queryParams("fecha_nac")!= null)
			filter.put("fecha_nac",request.queryParams("fecha_nac"));
		if(request.queryParams("fecha_muer")!= null)
			filter.put("fecha_muer",request.queryParams("fecha_muer"));

		output = gs.getAllGuionistas(filter);


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

	@SuppressWarnings("unchecked")
	public static String infoGuionistas(Request request, Response response) throws SQLException {
		Dictionary<String,Object> output;
		String result = "";
		if(request.queryParams("nombre")== null & request.queryParams("id")==null){
			return "Por favor introduce un nombre para buscar el guionista que deseas"+
					"<form action='/guionistas/info' method='get' enctype='multipart/form-data'>"
					+ "Nombre de Guionista: <input type=text name=nombre size=30>"
					+ "<button type=submit value=Guionista>Buscar </button><br/></form>";
		}
		if(request.queryParams("id")!=null) {
			output = gs.fullGuionistasInfo(request.queryParams("id"),true);
		}else {
			output = gs.fullGuionistasInfo(request.queryParams("nombre"),false);
		}
		if(output.isEmpty()) {
			response.redirect("/guionistas/info");
			return "El guionista no se encuentra en la base de datos";
		}

		Personas guionista = (Personas)output.get("guionista");
		List<Peliculas> pelis = (List<Peliculas>)output.get("peliculas");

		if(request.queryParams("format")!= null && request.queryParams("format").equals("json")) {
			response.type("application/json");
			JsonObject json = new JsonObject();
			json.addProperty("status", "SUCCESS");
			json.addProperty("serviceMessage", "La peticion se manejo adecuadamente");
			json.add("guionistadata", guionista.toJSONObject());
			JsonArray jpelis = new JsonArray();
			for(int i = 0; i < pelis.size(); i++) {
				jpelis.add(pelis.get(i).toJSONObject());;
			}
			json.add("peliculas",jpelis);
			result = json.toString();
		}else {
			result = "<b>Información de: " + guionista.getFullNombre() + " (" + guionista.getNacimiento() +"-" + guionista.getMuerte() +")</b>";
			result +="<b>&emsp;IDGuionista: </b>"+guionista.getId()+"</br>";
			result = result + "<b>Guionista en las películas:</b></br>";
			for(int i = 0; i < pelis.size(); i++) {
				result = result + "&emsp;" + pelis.get(i).toHTMLString() +"</br>";
			}
		}
		return result;
	}
	/**
	 * Metodo que se encarga de manejar todos los endpoints que cuelgan de /guionistas
	 */
	public void guionistasHandler() {
		get("/selectAll", GuionistasController::selectAllGuionistas);
		get("/uploadTable", GuionistasController::uploadTable);
		post("/upload", GuionistasController::upload);
		get("/info", GuionistasController::infoGuionistas);
	}

}
