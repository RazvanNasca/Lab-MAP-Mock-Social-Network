package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.vm.ci.meta.Local;
import socialnetwork.MyException;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.EventValidator;
import socialnetwork.event.EventEvent;
import socialnetwork.observer.Observer;
import socialnetwork.paging.PageableImplementation;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.EventService;
import socialnetwork.service.SuperService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventController implements Observer<EventEvent> {

    public Button buttonBack;
    public Button buttonCreate;

    public Label labelForTable;
    public Label messageToUser;
    public Label eventsUserTitle;
    public Label eventsTitle;

    public TableView <Event> eventsUserTable;
    public TableColumn <Event, String> eventColumnName;
    public TableColumn <Event, String> eventColumnDescription;
    public TableColumn <Event, LocalDateTime> eventColumnDate;
    public TableColumn <Event, Void> buttonColumnUser = new TableColumn<>("Unsubscribe");
    public TableColumn <Event, Void> buttonNotification = new TableColumn<>("Notification");

    public TableView <Event> eventsTable;
    public TableColumn <Event, String> allEventsColumnName;
    public TableColumn <Event, String> allEventsColumnDescription;
    public TableColumn <Event, LocalDateTime> allEventsColumnDate;
    public TableColumn <Event, Void> buttonColumnEvent = new TableColumn<>("Subscribe");

    public Pagination paginatorUsersEvents;
    public Pagination paginatorEvents;


    PageableImplementation curentPageableEvenimente = new PageableImplementation(0,2);

    ObservableList<Event> modelUserEvent = FXCollections.observableArrayList();
    ObservableList<Event> modelEvent = FXCollections.observableArrayList();

    private EventService eventService;
    private PageDTO pageDTO;
    private SuperService superService;

    private Stage stage = new Stage();

    public void setStage(Stage stage1) { this.stage = stage1; }


    private void initUsersEvent()
    {
       modelUserEvent.setAll(((EventDatabaseRepository) superService.eventDatabaseRepository).findAllForUser(HomeController.activeUser.getId(), curentPageableEvenimente).getContent().collect(Collectors.toList()));
    }

    private void initEvent()
    {
        modelEvent.setAll(superService.eventDatabaseRepository.findAll(new PageableImplementation(0,2)).getContent().collect(Collectors.toList()));
    }

    public void initialize()
    {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");


        PagingRepository<Long, User> userDatabaseRepository = new UserDatabaseRepository(url, username, password);
        PagingRepository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
        PagingRepository<Tuple<Long, Long>, FriendRequest> friendRequestDatabaseRepository = new FriendRequestDatabaseRepository(url, username, password);
        PagingRepository<Long, Event> eventDatabaseRepository = new EventDatabaseRepository(url, username, password);


        this.superService = new SuperService(userDatabaseRepository, friendshipDatabaseRepository, friendRequestDatabaseRepository, eventDatabaseRepository);
        pageDTO = superService.getAll(HomeController.activeUser.getId());

        this.eventService = new EventService(eventDatabaseRepository, new EventValidator());
        eventService.addObserver(this);

        eventColumnName.setCellValueFactory(new PropertyValueFactory<Event, String>("name"));
        eventColumnDescription.setCellValueFactory(new PropertyValueFactory<Event, String>("descriere"));
        eventColumnDate.setCellValueFactory(new PropertyValueFactory<Event, LocalDateTime>("data"));

        allEventsColumnName.setCellValueFactory(new PropertyValueFactory<Event, String>("name"));
        allEventsColumnDescription.setCellValueFactory(new PropertyValueFactory<Event, String>("descriere"));
        allEventsColumnDate.setCellValueFactory(new PropertyValueFactory<Event, LocalDateTime>("data"));

        buttonBack.setTooltip(new Tooltip("Home page"));
        buttonCreate.setTooltip(new Tooltip("Create an event"));

        paginatorEvents.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                modelEvent.setAll(eventService.findAll(new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                return eventsTable;
            }
        });

        paginatorUsersEvents.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
               curentPageableEvenimente = new PageableImplementation(param, curentPageableEvenimente.getPageSize());
               modelUserEvent.setAll(eventService.findAllForUser(HomeController.activeUser.getId(),curentPageableEvenimente).getContent().collect(Collectors.toList()));
                return eventsUserTable;
            }
        });

        InputStream inputCreate = getClass().getResourceAsStream("/images/newEvent.png");
        Image imageCreate = new Image(inputCreate, 45,45, true, true);
        buttonCreate.setBackground(Background.EMPTY);
        buttonCreate.setGraphic(new ImageView(imageCreate));

        InputStream inputBack = getClass().getResourceAsStream("/images/home2.png");
        Image imageBack = new Image(inputBack, 40,40, true, true);
        buttonBack.setBackground(Background.EMPTY);
        buttonBack.setGraphic(new ImageView(imageBack));

        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactoryJoin = new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
            @Override
            public TableCell<Event, Void> call(TableColumn<Event, Void> param) {

                TableCell<Event, Void> cell = new TableCell<Event, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Event selected = (Event) eventsTable.getSelectionModel().getSelectedItem();
                            if (selected != null)
                            {
                                Tuple<Long, Long> tuplu = new Tuple<>(HomeController.activeUser.getId(), selected.getId());
                                try
                                {
                                    eventService.addSub(tuplu);
                                    //initialize();
                                    messageToUser.setText("Sub successful!");
                                    messageToUser.setTextFill(Color.DARKGREEN);
                                }catch (MyException e)
                                {
                                    messageToUser.setText(e.getMessage());
                                    messageToUser.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUser.setText("No item selected!");
                                messageToUser.setTextFill(Color.DARKRED);
                            }
                        });

                        InputStream input = getClass().getResourceAsStream("/images/plus.png");
                        Image image = new Image(input, 17,17, true, true);
                        btn.setBackground(Background.EMPTY);
                        btn.setGraphic(new ImageView(image));

                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactoryRemove = new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
            @Override
            public TableCell<Event, Void> call(TableColumn<Event, Void> param) {

                TableCell<Event, Void> cell = new TableCell<Event, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Event selected = (Event) eventsUserTable.getSelectionModel().getSelectedItem();
                            if (selected != null)
                            {
                                Tuple<Long, Long> tuplu = new Tuple<>(HomeController.activeUser.getId(), selected.getId());
                                try
                                {
                                    eventService.deleteSub(tuplu);
                                    //initialize();
                                    messageToUser.setText("UnSub successful!");
                                    messageToUser.setTextFill(Color.DARKGREEN);
                                }catch (MyException e)
                                {
                                    messageToUser.setText(e.getMessage());
                                    messageToUser.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUser.setText("No item selected!");
                                messageToUser.setTextFill(Color.DARKRED);
                            }
                        });
                        InputStream input = getClass().getResourceAsStream("/images/minus.png");
                        Image image = new Image(input, 15,15, true, true);
                        btn.setBackground(Background.EMPTY);
                        btn.setGraphic(new ImageView(image));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactoryNotification = new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
            @Override
            public TableCell<Event, Void> call(TableColumn<Event, Void> param) {

                TableCell<Event, Void> cell = new TableCell<Event, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Event selected = (Event) eventsUserTable.getSelectionModel().getSelectedItem();
                            if (selected != null)
                            {
                                Tuple<Long, Long> tuplu = new Tuple<>(HomeController.activeUser.getId(), selected.getId());
                                try
                                {
                                    String not = eventService.findNotification(tuplu);

                                    if(not.equals("ON"))
                                    {
                                        eventService.updateEvent(tuplu, "OFF");
                                        InputStream input = getClass().getResourceAsStream("/images/on.png");
                                        Image image = new Image(input, 35,35, true, true);
                                        btn.setBackground(Background.EMPTY);
                                        btn.setGraphic(new ImageView(image));
                                        messageToUser.setText("Notifications are off now!");
                                        messageToUser.setTextFill(Color.DARKGREEN);
                                    }
                                    else
                                    {
                                        eventService.updateEvent(tuplu, "ON");
                                        InputStream input = getClass().getResourceAsStream("/images/off.png");
                                        Image image = new Image(input, 35,35, true, true);
                                        btn.setBackground(Background.EMPTY);
                                        btn.setGraphic(new ImageView(image));
                                        messageToUser.setText("Notifications are on now!");
                                        messageToUser.setTextFill(Color.DARKGREEN);
                                    }

                                }catch (MyException e)
                                {
                                    messageToUser.setText(e.getMessage());
                                    messageToUser.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUser.setText("No item selected!");
                                messageToUser.setTextFill(Color.DARKRED);
                            }

                        });
                        InputStream input = getClass().getResourceAsStream("/images/off.png");
                        Image image = new Image(input, 35,35, true, true);
                        btn.setBackground(Background.EMPTY);
                        btn.setGraphic(new ImageView(image));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        buttonColumnUser.setCellFactory(cellFactoryRemove);
        eventsUserTable.getColumns().add(buttonColumnUser);

        buttonNotification.setCellFactory(cellFactoryNotification);
        eventsUserTable.getColumns().add(buttonNotification);

        buttonColumnEvent.setCellFactory(cellFactoryJoin);
        eventsTable.getColumns().add(buttonColumnEvent);

        eventsTable.setItems(modelEvent);
        initEvent();

        eventsUserTable.setItems(modelUserEvent);
        initUsersEvent();


    }


    public void handleBack(ActionEvent actionEvent)
    {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Home.fxml"));
            GridPane root = loader.load();
            HomeController controller = loader.getController();
            Scene scene = new Scene(root, 600, 300);
            stage.setTitle("Home");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/home.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        }
        catch (MyException e) {
            messageToUser.setText(e.getMessage());
            messageToUser.setTextFill(Color.DARKRED);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void update(EventEvent eventEvent) { }

    public void handleCreate(ActionEvent actionEvent) {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/NewEvent.fxml"));
            GridPane root = loader.load();
            CreateEventController controller = loader.getController();
            Scene scene = new Scene(root, 400, 300);
            stage.setTitle("New Event");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/event.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        }
        catch (MyException e) {
            messageToUser.setText(e.getMessage());
            messageToUser.setTextFill(Color.DARKRED);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
