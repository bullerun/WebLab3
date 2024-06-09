package db;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named("manager")
@SessionScoped
public class ManagerDB implements Serializable {
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs", "s368738", "password");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS results (\n" +
                    "  id SERIAL PRIMARY KEY,\n" +
                    "  x FLOAT NOT NULL,\n" +
                    "  y FLOAT NOT NULL,\n" +
                    "  r INTEGER NOT NULL,\n" +
                    "  result BOOLEAN NOT NULL);");
        } catch (SQLException e) {
            System.out.println("Ошибка подлючения к базе данных");
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public void addPointToTable(Point point) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO results (x, y, r, result)VALUES (?, ?,?, ?)");
            preparedStatement.setFloat(1, point.getX());
            preparedStatement.setFloat(2, point.getY());
            preparedStatement.setInt(3, point.getR());
            preparedStatement.setBoolean(4, point.isHit());
            preparedStatement.execute();
        } catch (SQLException e) {
            System.out.println("траблы");
        }
    }
    @Transactional
    public void addPointToTableFromJSON() {
        final Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        try {
            var x = Float.parseFloat(params.get("X"));
            var y = Float.parseFloat(params.get("Y"));
            var graphR = Integer.parseInt(params.get("R"));

            final Point point = new Point(
                    x,
                    y,
                    graphR
            );
            addPointToTable(point);
            PrimeFaces.current().ajax().addCallbackParam("isHit", point.isHit());

        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public List<Point> getPoints() {
        List<Point> pointsList = new ArrayList<>();
        try {

            ResultSet rs = connection.createStatement().executeQuery("select * from results");
            while (rs.next()) {
                pointsList.add(0, new Point(rs.getFloat("x"), rs.getFloat("y"), rs.getInt("r"), rs.getBoolean("result")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pointsList;
    }

    public int getPointsCount() {
        try {
           ResultSet rs = connection.createStatement().executeQuery("select count(*) from results");
           rs.next();
           return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
