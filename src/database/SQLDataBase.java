package database;

import domFiles.Position;

import java.sql.*;

public class SQLDataBase implements BDConnection {

    private Connection connection;
    private Statement statement;


    @Override
    public void init() {
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:persons.sqlite");
            statement = connection.createStatement();
        }catch (ClassNotFoundException | SQLException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void createDB() {
        try {
            statement.execute("CREATE TABLE if not exists 'persons' " +
                    "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'DepCode' STRING(20), " +
                    "'DepJob' STRING(100), " +
                    "'Description' STRING(255));");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Таблица создана или уже существует.");
    }


    @Override
    public void clearDB() {

        try {
            statement.execute("DELETE FROM 'persons' ");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void dispose()  {
        try{
            connection.close();
        } catch (SQLException e){
            throw  new RuntimeException(e);
        }

    }

    public void connectionAutoCommit(boolean b) throws SQLException{
        connection.setAutoCommit(b);
    }

    public void executeCommit() throws SQLException{
        connection.commit();
    }

    public Savepoint setSavePoint() throws SQLException{
        return connection.setSavepoint();
    }

    public void rollBack(Savepoint sp) throws SQLException{
        connection.rollback(sp);
    }

    public void updateDataBase(Position position, int primaryKey) throws SQLException{
        PreparedStatement ps = null;
        ps = connection.prepareStatement
                ("UPDATE Persons SET description = ? WHERE id = ?");
        ps.setString(1, position.getDescription());
        ps.setInt(2, primaryKey);
        ps.executeUpdate();
    }

    public void deleteFromDataBase(int primaryKey) throws SQLException{
        PreparedStatement ps = null;
        ps = connection.prepareStatement
                ("DELETE FROM Persons WHERE id = ?");
        ps.setInt(1, primaryKey);
        ps.executeUpdate();
    }

    public void addToDataBase(Position position) throws SQLException{
        PreparedStatement ps = null;
        ps = connection.prepareStatement
                ("INSERT INTO persons (DepCode, DepJob, Description) VALUES(?, ?, ?);");
        ps.setString(1, position.getDepCode());
        ps.setString(2, position.getDepJob());
        ps.setString(3, position.getDescription());
        ps.execute();
    }


    public void addDataToDB(String title) throws SQLException {

        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement
                ("INSERT INTO persons (DepCode, DepJob, Description) VALUES(?, ?, ?);");
        for (int i = 1; i < 10001; i ++){
                ps.setString(1,"12" + i);
                ps.setString(2, title + i);
                ps.setString(3,"всем одинаковое описание");
                //ps.setInt(4, i);
                ps.addBatch();
        }
        ps.executeBatch();
        connection.commit();
        connection.setAutoCommit(true);

    }

    public ResultSet getDataBase() throws SQLException{
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = connection.prepareStatement ("SELECT * FROM Persons");
        rs = ps.executeQuery();
        return rs;
    }

}
