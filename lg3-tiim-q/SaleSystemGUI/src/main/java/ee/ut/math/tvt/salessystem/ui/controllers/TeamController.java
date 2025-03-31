package ee.ut.math.tvt.salessystem.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class TeamController implements Initializable {
    private static final Logger log = LogManager.getLogger(TeamController.class);

    @FXML
    Label teamName;
    @FXML
    Label teamLeader;
    @FXML
    Label teamLeaderMail;
    @FXML
    Label teamMembers;
    @FXML
    ImageView teamLogo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTeamInfo();
    }

    private void refreshTeamInfo(){
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream("../src/main/resources/application.properties")) {

            properties.load(fileInputStream);

            String name = properties.getProperty("teamtab.teamname");
            String leader = properties.getProperty("teamtab.leader");
            String mail = properties.getProperty("teamtab.leademail");
            String members = properties.getProperty("teamtab.members");
            Image logo = new Image(properties.getProperty("teamtab.logo"));

            teamName.setText(name);
            teamLeader.setText(leader);
            teamLeaderMail.setText(mail);
            teamMembers.setText(members);
            teamLogo.setImage(logo);
        }
        catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
