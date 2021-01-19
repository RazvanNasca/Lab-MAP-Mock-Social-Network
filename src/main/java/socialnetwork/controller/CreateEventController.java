package socialnetwork.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.AccountValidator;
import socialnetwork.domain.validators.EventValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.AccountDatabaseRepository;
import socialnetwork.repository.database.EventDatabaseRepository;
import socialnetwork.repository.database.FriendshipDatabaseRepository;
import socialnetwork.repository.database.UserDatabaseRepository;
import socialnetwork.service.EventService;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateEventController {

    public Label title;
    public Label titleName;
    public Label titleDescription;
    public Label titleDate;
    public Label titleHour;
    public Label messageToUser;

    public TextField textName;
    public TextField textDescription;
    public TextField textHour;

    public DatePicker calendarDate;
    public Button createButton;

    private EventService eventService;

    private Stage stage = new Stage();

    public void setStage(Stage stage1) { this.stage = stage1; }


    @FXML
    public void initialize()
    {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

        PagingRepository<Long, Event> eventDatabaseRepository = new EventDatabaseRepository(url, username, password);
        eventService = new EventService(eventDatabaseRepository, new EventValidator());

        createButton.setTooltip(new Tooltip("Create event"));

        InputStream inputCreate = getClass().getResourceAsStream("/images/createEvent.png");
        Image imageCreate = new Image(inputCreate, 45,45, true, true);
        createButton.setBackground(Background.EMPTY);
        createButton.setGraphic(new ImageView(imageCreate));

        textName.setPromptText("Title of event");
        textDescription.setPromptText("Description");
        textHour.setPromptText("Ex: 13:40");
        calendarDate.setPromptText("Date of event");

        textName.setText("");
        textDescription.setText("");
        textHour.setText("");
    }


    public void handleBack(ActionEvent actionEvent) {

        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Event.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 600, 300);
            EventController controller = loader.getController();
            stage.setTitle("Events");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/event.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void handleCreateEvent(ActionEvent actionEvent) {

        try {
            String name = textName.getText();
            String description = textDescription.getText();

            String hour = textHour.getText(); // 13:45
            String date = calendarDate.getValue().toString();
            String str = date + " " + hour;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
            ArrayList<Long> list = new ArrayList<>();

            Event event = new Event(name, description, dateTime, list);
            event.setId(eventService.generateEventID());

            eventService.addEvent(event);
            this.messageToUser.setText("Event created successfully!");
            this.messageToUser.setTextFill(Color.DARKGREEN);
            textName.setText("");
            textDescription.setText("");
            textHour.setText("");
        }
        catch (Exception e)
        {
            this.messageToUser.setText(e.getMessage());
            this.messageToUser.setTextFill(Color.DARKRED);
        }

    }
}
