package database;

public interface BDConnection {
    void init();
    void dispose();
    void createDB();
    void clearDB();
}
