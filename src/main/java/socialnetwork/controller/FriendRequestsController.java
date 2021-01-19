package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import socialnetwork.MyException;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.FriendRequestValidator;
import socialnetwork.event.ChangeEventType;
import socialnetwork.event.FriendRequestEvent;
import socialnetwork.observer.Observer;
import socialnetwork.paging.PageableImplementation;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.FriendRequestService;
import socialnetwork.service.SuperService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendRequestsController implements Observer<FriendRequestEvent>
{
    public TableView<FriendRequest> requestsSentTable;
    public TableColumn<FriendRequest, String> toColumnSent;
    public TableColumn<FriendRequest, String> statusColumnSent;
    public TableColumn<FriendRequest, LocalDateTime> dateColumnSent;
    public TableColumn <FriendRequest, Void> buttonColumnSent = new TableColumn<>("Remove");

    public TableView<FriendRequest> requestsReceivedTable;
    public TableColumn<FriendRequest, String> fromColumnReceived;
    public TableColumn<FriendRequest, String> statusColumnReceived;
    public TableColumn<FriendRequest, LocalDateTime> dateColumnReceived;
    public TableColumn <FriendRequest, Void> buttonColumnReceivedAccept = new TableColumn<>("Accept");
    public TableColumn <FriendRequest, Void> buttonColumnReceivedReject = new TableColumn<>("Reject");

    public Button buttonAccept;
    public Button buttonReject;
    public Label messageToUserForRequest;
    public Button buttonRemove;

    public Pagination btnRecevieve;
    public Pagination btnSent;
    public Button buttonBack;

    private FriendRequestService friendRequestService;

    private PageDTO pageDTO;
    private SuperService superService;

    ObservableList<FriendRequest> modelSent;
    ObservableList<FriendRequest> modelReceived;

    private Stage stage = new Stage();

    public void setStage(Stage stage1)
    {
        this.stage = stage1;
    }

    @FXML
    public void initialize()
    {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

        PagingRepository<Long, User> userDatabaseRepository = new UserDatabaseRepository(url, username, password);
        PagingRepository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
        PagingRepository<Tuple<Long, Long>, FriendRequest> friendRequestDatabaseRepository = new FriendRequestDatabaseRepository(url, username, password);
        PagingRepository<Long, Event> eventDatabaseRepository = new EventDatabaseRepository(url, username, password);

        this.friendRequestService = new FriendRequestService(friendRequestDatabaseRepository, userDatabaseRepository, friendshipDatabaseRepository, new FriendRequestValidator());

        this.superService = new SuperService(userDatabaseRepository, friendshipDatabaseRepository, friendRequestDatabaseRepository, eventDatabaseRepository);
        pageDTO = superService.getAll(HomeController.activeUser.getId());

        this.modelSent = FXCollections.observableArrayList();
        this.modelReceived = FXCollections.observableArrayList();

        this.toColumnSent.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("to"));
        this.statusColumnSent.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("status"));
        this.dateColumnSent.setCellValueFactory(new PropertyValueFactory<FriendRequest, LocalDateTime>("date"));

        this.fromColumnReceived.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("from"));
        this.statusColumnReceived.setCellValueFactory(new PropertyValueFactory<FriendRequest, String>("status"));
        this.dateColumnReceived.setCellValueFactory(new PropertyValueFactory<FriendRequest, LocalDateTime>("date"));

        buttonBack.setTooltip(new Tooltip("Home page"));

        btnSent.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                modelSent.setAll(((FriendRequestDatabaseRepository) friendRequestDatabaseRepository).findAllReguestSend(HomeController.activeUser.getId(),new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                return requestsSentTable;
            }
        });

        btnRecevieve.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                modelReceived.setAll(((FriendRequestDatabaseRepository) friendRequestDatabaseRepository).findAllReguestReceive(HomeController.activeUser.getId(),new PageableImplementation(param,2)).getContent().collect(Collectors.toList()));
                return requestsReceivedTable;
            }
        });


        Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>> cellFactoryRemove = new Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>>() {
            @Override
            public TableCell<FriendRequest, Void> call(TableColumn<FriendRequest, Void> param) {

                TableCell<FriendRequest, Void> cell = new TableCell<FriendRequest, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            FriendRequest selected = requestsSentTable.getSelectionModel().getSelectedItem();
                            if(selected != null)
                            {
                                try
                                {
                                    friendRequestService.remove(HomeController.activeUser.getId(), selected.getTo());
                                    messageToUserForRequest.setText("Friend request removed!");
                                    messageToUserForRequest.setTextFill(Color.DARKGREEN);
                                } catch (MyException e)
                                {
                                    messageToUserForRequest.setText(e.getMessage());
                                    messageToUserForRequest.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUserForRequest.setText("No request selected!");
                                messageToUserForRequest.setTextFill(Color.DARKRED);
                            }
                        });
                        InputStream input = getClass().getResourceAsStream("/images/remove.png");
                        Image image = new Image(input, 25,25, true, true);
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

        Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>> cellFactoryAccept = new Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>>() {
            @Override
            public TableCell<FriendRequest, Void> call(TableColumn<FriendRequest, Void> param) {

                TableCell<FriendRequest, Void> cell = new TableCell<FriendRequest, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            FriendRequest selected = requestsReceivedTable.getSelectionModel().getSelectedItem();
                            if (selected != null)
                            {
                                try
                                {
                                    friendRequestService.accept(selected.getFrom(), HomeController.activeUser.getId());
                                    messageToUserForRequest.setText("Friend request accepted!");
                                    messageToUserForRequest.setTextFill(Color.DARKGREEN);
                                } catch (MyException e)
                                {
                                    messageToUserForRequest.setText(e.getMessage());
                                    messageToUserForRequest.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUserForRequest.setText("No request selected!");
                                messageToUserForRequest.setTextFill(Color.DARKRED);
                            }
                        });
                        InputStream input = getClass().getResourceAsStream("/images/accept.png");
                        Image image = new Image(input, 25,25, true, true);
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

        Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>> cellFactoryReject = new Callback<TableColumn<FriendRequest, Void>, TableCell<FriendRequest, Void>>() {
            @Override
            public TableCell<FriendRequest, Void> call(TableColumn<FriendRequest, Void> param) {

                TableCell<FriendRequest, Void> cell = new TableCell<FriendRequest, Void>() {

                    final Button btn = new Button("");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            FriendRequest selected = requestsReceivedTable.getSelectionModel().getSelectedItem();
                            if(selected != null)
                            {
                                try
                                {
                                    friendRequestService.reject(selected.getFrom(), HomeController.activeUser.getId());
                                    messageToUserForRequest.setText("Friend request rejected!");
                                    messageToUserForRequest.setTextFill(Color.DARKGREEN);
                                } catch (MyException e)
                                {
                                    messageToUserForRequest.setText(e.getMessage());
                                    messageToUserForRequest.setTextFill(Color.DARKRED);
                                }
                            }
                            else
                            {
                                messageToUserForRequest.setText("No request selected!");
                                messageToUserForRequest.setTextFill(Color.DARKRED);
                            }
                        });
                        InputStream input = getClass().getResourceAsStream("/images/reject.png");
                        Image image = new Image(input, 25,25, true, true);
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

        InputStream inputBack = getClass().getResourceAsStream("/images/home2.png");
        Image imageBack = new Image(inputBack, 40,40, true, true);
        buttonBack.setBackground(Background.EMPTY);
        buttonBack.setGraphic(new ImageView(imageBack));

        buttonColumnSent.setCellFactory(cellFactoryRemove);
        requestsSentTable.getColumns().add(buttonColumnSent);

        buttonColumnReceivedAccept.setCellFactory(cellFactoryAccept);
        requestsReceivedTable.getColumns().add(buttonColumnReceivedAccept);

        buttonColumnReceivedReject.setCellFactory(cellFactoryReject);
        requestsReceivedTable.getColumns().add(buttonColumnReceivedReject);


        this.requestsSentTable.setItems(this.modelSent);
        this.initSent();

        this.requestsReceivedTable.setItems(this.modelReceived);
        this.initReceived();


        this.friendRequestService.addObserver(this);
    }

    public void initSent()
    {
        this.modelSent.setAll(superService.getAll(HomeController.activeUser.getId()).getFriendRequestsSend());
    }

    public void initReceived()
    {
        this.modelReceived.setAll(superService.getAll(HomeController.activeUser.getId()).getFriendRequestsReceived());
    }

    @Override
    public void update(FriendRequestEvent friendRequestEvent)
    {
        this.initSent();
        this.initReceived();
    }

    public void handleAcceptFriendRequest(ActionEvent actionEvent)
    {
        FriendRequest selected = (FriendRequest) this.requestsReceivedTable.getSelectionModel().getSelectedItem();
        if (selected != null)
        {
            try
            {
                this.friendRequestService.accept(selected.getFrom(), HomeController.activeUser.getId());
                this.messageToUserForRequest.setText("Friend request accepted!");
                this.messageToUserForRequest.setTextFill(Color.DARKGREEN);
            } catch (MyException e)
            {
                this.messageToUserForRequest.setText(e.getMessage());
                this.messageToUserForRequest.setTextFill(Color.DARKRED);
            }
        }
        else
        {
            this.messageToUserForRequest.setText("No request selected!");
            this.messageToUserForRequest.setTextFill(Color.DARKRED);
        }
    }

    public void handleRejectFriendRequest(ActionEvent actionEvent)
    {
        FriendRequest selected = (FriendRequest) this.requestsReceivedTable.getSelectionModel().getSelectedItem();
        if(selected != null)
        {
            try
            {
                this.friendRequestService.reject(selected.getFrom(), HomeController.activeUser.getId());
                this.messageToUserForRequest.setText("Friend request rejected!");
                this.messageToUserForRequest.setTextFill(Color.DARKGREEN);
            } catch (MyException e)
            {
                this.messageToUserForRequest.setText(e.getMessage());
                this.messageToUserForRequest.setTextFill(Color.DARKRED);
            }
        }
        else
        {
            this.messageToUserForRequest.setText("No request selected!");
            this.messageToUserForRequest.setTextFill(Color.DARKRED);
        }
    }

    public void handleRemove(ActionEvent actionEvent)
    {
        FriendRequest selected = (FriendRequest) this.requestsSentTable.getSelectionModel().getSelectedItem();
        if(selected != null)
        {
            try
            {
                this.friendRequestService.remove(HomeController.activeUser.getId(), selected.getTo());
                this.messageToUserForRequest.setText("Friend request removed!");
                this.messageToUserForRequest.setTextFill(Color.DARKGREEN);
            } catch (MyException e)
            {
                this.messageToUserForRequest.setText(e.getMessage());
                this.messageToUserForRequest.setTextFill(Color.DARKRED);
            }
        }
        else
        {
            this.messageToUserForRequest.setText("No request selected!");
            this.messageToUserForRequest.setTextFill(Color.DARKRED);
        }
    }

    public void handleBack(ActionEvent actionEvent) {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Home.fxml"));
            GridPane root = loader.load();
            HomeController controller = loader.getController();
            Scene scene = new Scene(root, 600, 300);
            stage.setTitle("Home");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/Home.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        }
        catch (MyException e) {
            messageToUserForRequest.setText(e.getMessage());
            messageToUserForRequest.setTextFill(Color.DARKRED);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
