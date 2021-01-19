package socialnetwork.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import socialnetwork.MyException;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.*;
import socialnetwork.event.UserEvent;
import socialnetwork.observer.Observer;
import socialnetwork.paging.Pageable;
import socialnetwork.paging.PageableImplementation;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HomeController implements Observer<UserEvent> {

    static User activeUser;

    public Button buttonRequests;
    public Button buttonMessage;
    public Button buttonEvent;
    public Button buttonLogOut;
    public Button buttonActivity;

    public TableView <User> friendsTable;
    public TableColumn <User, String> friendColumnFirstName;
    public TableColumn <User, String> friendColumnLastName;
    public TableColumn <User, Void> buttonColumnFriends= new TableColumn<>("Remove");

    public TableView <User> usersTable;
    public TableColumn <User, String> userColumnFirstName;
    public TableColumn <User, String> userColumnLastName;
    public TableColumn <User, Void> buttonColumnUsers = new TableColumn<>("Add");

    public Label messageToUser;
    public Label welcomeMessage;
    public Label friendsTableTitle;
    public Label usersTableTitle;

    public Pagination paginatopUsers;
    public Pagination paginatopFriends;

    public TextField searchField;
    public TextField searchFieldFriends;

    private Stage stage = new Stage();
    private DatePicker datePicker = new DatePicker();

    private PageDTO pageDTO;
    private SuperService superService;
    final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
    final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");
    PagingRepository<Long, Event> eventDatabaseRepository = new EventDatabaseRepository(url, username, password);
    private EventService eventService = new EventService(eventDatabaseRepository, new EventValidator());
    EventDatabaseRepository evenRepo = new EventDatabaseRepository(url, username, password);

    private UserService userService;
    private FriendRequestService friendRequestService;
    private FriendshipService friendshipService;
    ObservableList<User> modelUsers = FXCollections.observableArrayList();
    ObservableList<User> modelFriends = FXCollections.observableArrayList();


    static void setActiveUser(User activeUser) {
        HomeController.activeUser = activeUser;
    }

    private void initUsers()
    {
        modelUsers.setAll(superService.getAll(HomeController.activeUser.getId()).getUsers());
    }

    public void initFriends()
    {
        modelFriends.setAll(superService.getAll(HomeController.activeUser.getId()).getFriendships());
    }

    public void setStage(Stage stage1)
    {
        this.stage = stage1;
    }


    @FXML
    public void initialize()
    {
            PagingRepository<Long, User> userDatabaseRepository = new UserDatabaseRepository(url, username, password);
            PagingRepository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
            Repository<String, Account> accountDatabaseRepository = new AccountDatabaseRepository(url, username, password);
            PagingRepository<Tuple<Long, Long>, FriendRequest> friendRequestDatabaseRepository = new FriendRequestDatabaseRepository(url, username, password);

        this.userService = new UserService(userDatabaseRepository, friendshipDatabaseRepository, accountDatabaseRepository, new UserValidator(), new AccountValidator());
        this.friendRequestService = new FriendRequestService(friendRequestDatabaseRepository, userDatabaseRepository, friendshipDatabaseRepository, new FriendRequestValidator());
        this.friendshipService = new FriendshipService(userDatabaseRepository, friendRequestDatabaseRepository,friendshipDatabaseRepository, new FriendshipValidator());
        userService.addObserver(this);

        this.superService = new SuperService(userDatabaseRepository, friendshipDatabaseRepository, friendRequestDatabaseRepository, eventDatabaseRepository);
        pageDTO = superService.getAll(HomeController.activeUser.getId());

        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        userColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        userColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));

        paginatopUsers.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                modelUsers.setAll(userService.findALLPotentialFriends(HomeController.activeUser.getId(), new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                return usersTable;
            }
        });

        paginatopFriends.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                modelFriends.setAll(superService.getFriends(HomeController.activeUser.getId(), new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                return friendsTable;
            }
        });

        buttonRequests.setTooltip(new Tooltip("Friend requests"));
        buttonMessage.setTooltip(new Tooltip("Messages"));
        buttonEvent.setTooltip(new Tooltip("Events"));
        buttonLogOut.setTooltip(new Tooltip("Logout"));
        buttonActivity.setTooltip(new Tooltip("Activity"));

        searchField.setPromptText("Enter an user name");
        searchFieldFriends.setPromptText("Enter a friend name");

        InputStream inputRequest = getClass().getResourceAsStream("/images/friends_request.png");
        Image imageReguest = new Image(inputRequest, 35,36, true, true);
        buttonRequests.setBackground(Background.EMPTY);
        buttonRequests.setGraphic(new ImageView(imageReguest));

        InputStream inputMessage = getClass().getResourceAsStream("/images/message.png");
        Image imageMessage = new Image(inputMessage, 30,30, true, true);
        buttonMessage.setBackground(Background.EMPTY);
        buttonMessage.setGraphic(new ImageView(imageMessage));

        InputStream inputEvent = getClass().getResourceAsStream("/images/event.png");
        Image imageEvent = new Image(inputEvent, 35,35, true, true);
        buttonEvent.setBackground(Background.EMPTY);
        buttonEvent.setGraphic(new ImageView(imageEvent));

        InputStream inputLogOut = getClass().getResourceAsStream("/images/logout.png");
        Image imageLogOut = new Image(inputLogOut, 35,35, true, true);
        buttonLogOut.setBackground(Background.EMPTY);
        buttonLogOut.setGraphic(new ImageView(imageLogOut));

        InputStream inputActivity = getClass().getResourceAsStream("/images/activity.png");
        Image imageActivity = new Image(inputActivity, 35,35, true, true);
        buttonActivity.setBackground(Background.EMPTY);
        buttonActivity.setGraphic(new ImageView(imageActivity));


        usersTable.setItems(modelUsers);
        initUsers();

        friendsTable.setItems(modelFriends);
        initFriends();



        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {

                TableCell<User, Void> cell = new TableCell<User, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            User user = usersTable.getItems().get(usersTable.getSelectionModel().getSelectedIndex());
                            if (user != null) {
                                FriendRequest friendRequest = new FriendRequest(activeUser.getId(), user.getId());
                                try {
                                    friendRequestService.addFriendRequest(friendRequest);
                                    messageToUser.setText("Friend request sent successfully!");
                                    messageToUser.setTextFill(Color.DARKGREEN);
                                } catch (MyException e) {
                                    messageToUser.setText(e.getMessage());
                                    messageToUser.setTextFill(Color.DARKRED);
                                }
                            } else {
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

        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactoryFriends = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {

                TableCell<User, Void> cell = new TableCell<User, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            User user = friendsTable.getItems().get(friendsTable.getSelectionModel().getSelectedIndex());
                            if (user != null)
                            {
                                Friendship friendship = new Friendship();
                                friendship.setId(new Tuple<>(activeUser.getId(), user.getId()));
                                try
                                {
                                    Friendship deleted = friendshipService.removeFriendship(friendship);
                                    initFriends();
                                    messageToUser.setText("Friend removed successfully!");
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

        this.searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER))
                {
                    String search = searchField.getText();

                    paginatopUsers.setPageFactory(new Callback<Integer, Node>() {
                        @Override
                        public Node call(Integer param) {
                            modelUsers.setAll(userService.findUsersMatch( search, new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                            return usersTable;
                        }
                    });

                }
            }
        });

        this.searchFieldFriends.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER))
                {
                    String search = searchFieldFriends.getText();

                    paginatopFriends.setPageFactory(new Callback<Integer, Node>() {
                        @Override
                        public Node call(Integer param) {
                            modelFriends.setAll(userService.findFriendsMatch(search, HomeController.activeUser.getId(), new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                            return friendsTable;
                        }
                    });

                }
            }
        });

        buttonColumnUsers.setCellFactory(cellFactory);
        usersTable.getColumns().add(buttonColumnUsers);

        buttonColumnFriends.setCellFactory(cellFactoryFriends);
        friendsTable.getColumns().add(buttonColumnFriends);

        datePicker.setOnAction(e -> {
            System.err.println("Selected date: " + datePicker.getValue());
        });
        datePicker.show();

        welcomeMessage.setText("Welcome, " + activeUser.getFirstName() + " " + activeUser.getLastName());
        welcomeMessage.setWrapText(true);
    }

    public void handleSendFriendRequest(ActionEvent actionEvent)
    {
        User selected = (User) usersTable.getSelectionModel().getSelectedItem();
        if (selected != null)
        {
            FriendRequest friendRequest = new FriendRequest(activeUser.getId(), selected.getId());
            try
            {
                this.friendRequestService.addFriendRequest(friendRequest);
                this.messageToUser.setText("Friend request sent successfully!");
                this.messageToUser.setTextFill(Color.DARKGREEN);
            }catch (MyException e)
            {
                this.messageToUser.setText(e.getMessage());
                this.messageToUser.setTextFill(Color.DARKRED);
            }
        }
        else
        {
            this.messageToUser.setText("No item selected!");
            this.messageToUser.setTextFill(Color.DARKRED);
        }
    }

    public void handleRemoveFriend(ActionEvent actionEvent)
    {
        User selected = (User) friendsTable.getSelectionModel().getSelectedItem();
        if (selected != null)
        {
            Friendship friendship = new Friendship();
            friendship.setId(new Tuple<>(activeUser.getId(), selected.getId()));
            try
            {
                Friendship deleted = friendshipService.removeFriendship(friendship);
                initFriends();
                this.messageToUser.setText("Friend removed successfully!");
                this.messageToUser.setTextFill(Color.DARKGREEN);
            }catch (MyException e)
            {
                this.messageToUser.setText(e.getMessage());
                this.messageToUser.setTextFill(Color.DARKRED);
            }
        }
        else
        {
            this.messageToUser.setText("No item selected!");
            this.messageToUser.setTextFill(Color.DARKRED);
        }
    }

    public void handleRequests(ActionEvent actionEvent)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/FriendRequests.fxml"));
            GridPane root = loader.load();
            FriendRequestsController controller = loader.getController();
            this.friendRequestService.addObserver(controller);
            Scene scene = new Scene(root, 700, 300);
            stage.setTitle("Friend requests");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/request.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void update(UserEvent userEvent)
    {
        initUsers();
        initFriends();
    }

    public void handleMessage(ActionEvent actionEvent)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Messages.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 800, 500);
            MessageController controller = loader.getController();
            stage.setTitle("Messages");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/message.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void handleEvent(ActionEvent actionEvent) {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Event.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 600, 300);
            EventController controller = loader.getController();
            stage.setTitle("Event");
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

    public void handleLogOut(ActionEvent actionEvent) {

        try
        {
            FXMLLoader loader= new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Login.fxml")); //URL
            GridPane root = loader.load();
            LoginController controller = loader.getController();
            Scene scene = new Scene(root, 400, 500);
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/login.png"));
            controller.setStage(stage);
            stage.show();
            endEvents();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    Runnable eventsThread;
    ScheduledExecutorService executor;

    public void startEvents(){
        eventsThread = new Runnable() {
            @Override
            public void run() {
                Platform.runLater(()->{
                     eventService.getAllForUser(HomeController.activeUser.getId()).forEach(x->{
                        if(x.getData().minusMinutes(30).isBefore(LocalDateTime.now()) && x.getData().minusMinutes(29).isAfter(LocalDateTime.now())){
                            Stage t = new Stage();
                            Label l = new Label();
                            l.setText(x.getName() + " " + x.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm")));
                            Label l2 = new Label();
                            l2.setText("Event starts in less than 30 min");
                            VBox v = new VBox();
                            v.getChildren().addAll(l, l2);
                            t.setScene(new Scene(v));
                            t.getIcons().add(new Image("images/notification.png"));
                            t.show();
                        }else if(x.getData().minusMinutes(10).isBefore(LocalDateTime.now()) && x.getData().minusMinutes(9).isAfter(LocalDateTime.now())){
                            Stage t = new Stage();
                            Label l = new Label();
                            l.setText(x.getName() + " " + x.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm")));
                            Label l2 = new Label();
                            l2.setText("Event starts in less than 10 min");
                            VBox v = new VBox();
                            v.getChildren().addAll(l, l2);
                            t.setScene(new Scene(v));
                            t.getIcons().add(new Image("images/notification.png"));
                            t.show();
                        }
                    });
                });
            }
        };
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(eventsThread,0, 1, TimeUnit.MINUTES);
    }

    public void endEvents()
    {
        executor.shutdownNow();
    }

    public void handleActivity(ActionEvent actionEvent) {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/ActivityView.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 400, 500);
            ActivityController controller = loader.getController();
            stage.setTitle("Activity View");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/message.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
