package urjc.isi.interfacesdao;

import java.sql.*;
import urjc.isi.interfacesdao.GenericDAOInterface;

//Cosasa muy genericas, alguno de los métodos
//se pueden quedar sin implementar, esos son los
//que pueden venir de la interfaz para darles
//caracter de obligatoriedad
public abstract class GenericDAO<T> implements GenericDAOInterface<T>{

	private Connection c;

	public GenericDAO() {
		this.c = connect();
	}
	public Connection connect() {
		try {
			return DriverManager.getConnection("jdbc:sqlite:sample.db");
		} catch (SQLException e){
			throw new RuntimeException(e);
		}
	}
  public void close() {
    try {
        c.close();
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
  }
}
