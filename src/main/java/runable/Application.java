package runable;

import config.Config;
import db.dblogic.enums.DBService;
import db.dblogic.enums.HSQLDB;
import gui.GUI;

import java.sql.SQLException;

public class Application {

    DBService dbService = DBService.instance;

    public static void main(String[] args) {

        Application app = new Application();
        app.setup();
        javafx.application.Application.launch(GUI.class);
    }

    public void setup(){
        Config.instance.textArea.setUseParentHandlers(false);
        dbService.setupConnection();
        try {
            HSQLDB.instance.setupDatabase();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        if (!DBService.instance.userExists("msa")) DBService.instance.init();
    }

    private void startupGUI(){}

    private void close(){
        dbService.shutdown();
    }

    private void initNetworks(){}
}
