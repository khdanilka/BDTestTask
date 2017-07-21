package database;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        database.SQLDataBase sqlDataBase = new database.SQLDataBase();

        sqlDataBase.init();
        sqlDataBase.createDB();
        sqlDataBase.clearDB();
        try {
            sqlDataBase.addDataToDB("работник");
        } catch (SQLException e) {
            throw new RuntimeException("создание не удалось");
        }
        sqlDataBase.dispose();



    }


}
