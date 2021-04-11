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
        app.init();
        javafx.application.Application.launch(GUI.class);
    }

    public void init(){
        Config.instance.textArea.setUseParentHandlers(false);
        dbService.setupConnection();
        try {
            HSQLDB.instance.setupDatabase();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (!DBService.instance.participantExists("msa")) DBService.instance.createInitialValues();
    }

    private void startupGUI(){

    }

    private void close(){
        dbService.shutdown();
    }

    private void initNetworks(){

    }
}
