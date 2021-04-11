package db.dblogic.enums;

public enum DBConfiguration {
    instance;

    final String driverName = "jdbc:hsqldb:";
    final String username = "sa";
    final String password = "";
    String userDirectory = System.getProperty("user.dir");
    String fileSeparator = System.getProperty("file.separator");
    String dataDirectory = userDirectory + fileSeparator + "data" + fileSeparator;
    String databaseFile = dataDirectory + "datastore.db";
}
