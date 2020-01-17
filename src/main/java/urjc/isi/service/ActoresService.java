package urjc.isi.service;
package urjc.isi.grafos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import spark.Request;
import urjc.isi.dao.implementaciones.*;
import urjc.isi.entidades.*;

public class ActoresService {

	final static Integer DIST_MAX = 10; /* 
					     * considering this number as the maximum 
					     * distance of relation between actors
					     */
	final static Integer FACTOR = 5;  /*
					   * the part of the actors that will be 
					   * chosen for each percentage based on 
					   * their popularity
					   */
	final static String DELIMITER = "/";
	final static String EOL = ";";
	
	/**
	 * Constructor por defecto
	 */
	public ActoresService() {}

	/**
	 * Metodo encargado de procesar un selectAll de la tabla actores
	 * @return Lista de actores de la tabla Actores
	 * @throws SQLException
	 */
	public List<Personas> getAllActores() throws SQLException{
		ActoresDAOImpl actores = new ActoresDAOImpl();
		List<Personas> result = actores.selectAll();
		actores.close();
		return result;
	}

	/**
	 * Metodo encargado de procesar la subida de los registros de la tabla Actores
	 * @param req
	 * @return Estado de la subida
	 */
	public String uploadTable(Request req){
		ActoresDAOImpl actores = new ActoresDAOImpl();
		req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
		String result = "File uploaded!";
		try (InputStream input = req.raw().getPart("uploaded_actores_file").getInputStream()) {
		    actores.dropTable();
		    actores.createTable();
			InputStreamReader isr = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(isr);
			actores.uploadTable(br);
		} catch (IOException | ServletException | SQLException e) {
			System.out.println(e.getMessage());
		}
		actores.close();
		return result;
	}

	public List<Personas> getActoresByFechaNac (String fecha) throws SQLException {
		ActoresDAOImpl actores = new ActoresDAOImpl ();
		List<Personas> result = actores.selectPerByFechaNac (fecha);
		actores.close();
		return result;
	}

	public List<Personas> getActoresMuertos () throws SQLException {
		ActoresDAOImpl actores = new ActoresDAOImpl ();
		List<Personas> result = actores.selectPerMuertas ();
		actores.close();
		return result;
	}

	public List<Personas> getActoresByIntervaloNac (String fechaIn, String fechaFin) throws SQLException {
		ActoresDAOImpl actores = new ActoresDAOImpl ();
		List<Personas> result = actores.selectPerByIntervaloNac (fechaIn, fechaFin);
		actores.close();
		return result;
	}

	public 	Dictionary<String,Object> fullActoresInfo(String name) throws SQLException{
		ActoresDAOImpl actoresDAO = new ActoresDAOImpl();
		PeliculasDAOImpl peliDAO = new PeliculasDAOImpl();
		Personas persona = new Personas();
		persona = actoresDAO.selectByName(name);
		String id = persona.getId();

		Dictionary<String,Object> result = new Hashtable<String,Object>();
		if(id.length()>0){
			result.put("actor", (Object)actoresDAO.selectByID(id));
			result.put("peliculas", (Object)peliDAO.selectByActorID(id));
		}
		actoresDAO.close();
		peliDAO.close();
		return result;
	}
	
	public List<Personas> getActoresByCercania (String actor_p, String dist_max_p, String factor_p) throws SQLException {
		PeliculasDAOImpl pelisDAO = new PeliculasDAOImpl();
		ActoresDAOImpl actoresDAO = new ActoresDAOImpl();
		List<Peliculas> peliculas = pelisDAO.selectAll();
		String str_graph = "";
		
		// Create Graph
		for (int i = 0; i < peliculas.length; i++) {
			str_graph = str_graph + peliculas[i] + DELIMITER;
			String id_peli = pelisDAO.selectIDByTitle(peliculas[i]);
			List<Personas> actores = actoresDAO.selectByPeliculaID(id_peli);
			for (int j = 0; j < actores.length; j++) {
				str_graph = str_graph + actores[j];
				if (j == actores.length - 1) {
					str_graph = str_graph + EOL;
				}else {
					str_graph = str_graph + DELIMITER;
				}
			}
		}
		Graph G = new Graph(str_graph, DELIMITER, EOL);
		
        // create popularity data structure
        ST<String, Double> act_popularity = new ST<String, Double>();
        // create distances data structure
        ST<Double, SET<String>> act_distances = new ST<Double, SET<String>>();
        
        // run breadth first search
        PathFinder finder = new PathFinder(G, actor_p);
        
        // calculate the popularity and distance of each actor
        for (String actor : G.vertices()) {
        	Double dist = (double)finder.distanceTo(actor);
        	Double popularity = G.popularity(actor);
            if (dist % 2 != 0) continue;  // it's a movie vertex
            
            act_popularity.put(actor, popularity);
            
            if (actor.equals(actor_p)) continue;  // it's the same actor
            if (!act_distances.contains(dist/2)) {
            	act_distances.put(dist/2, new SET<String>());
            }
            act_distances.get(dist/2).add(actor);
        }

        // convert distances to percent
        Integer max = act_distances.max().intValue();
        for (Integer d=1; d<=max; d++) {
        	Double percent;
        	if (d < DIST_MAX) {
        		percent = (1- (double)d/(double)DIST_MAX) * 100;
        	}else {
        		percent = 0.0;
        	}
        	
        	act_distances.put(percent, act_distances.get((double)d));
        	act_distances.remove((double)d);
        }
		
        // Calculate result and return
        SET<String> result = new SET<String>();
        for (Double p : act_distances) {
            int numb_act = (int) Math.ceil((double)act_distances.get(p).size()/FACTOR);
            for (int i=1; i<=numb_act; i++) {
            	double pop = 0.0;
            	String actor = "";
            	for (String act : act_distances.get(p)) {
	                if (act_popularity.get(act) > pop) {
	                	pop = act_popularity.get(act);
	                	actor = act;
	                }
	            }
            	result.add(actor);
            	act_distances.get(p).delete(actor);
            }
        }
        actoresDAO.close();
		pelisDAO.close();
		return result;
	}
}
