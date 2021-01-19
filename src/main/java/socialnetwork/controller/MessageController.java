package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import socialnetwork.MyException;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.AccountValidator;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.event.MessageEvent;
import socialnetwork.observer.Observer;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.AccountDatabaseRepository;
import socialnetwork.repository.database.FriendshipDatabaseRepository;
import socialnetwork.repository.database.MessageDatabaseRepository;
import socialnetwork.repository.database.UserDatabaseRepository;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageController implements Observer<MessageEvent> {

    public TableView<User> friendsTable;
    public TableColumn<User, String> friendColumnFirstName;
    public TableColumn<User, String> friendColumnLastName;
    public ListView<String> messageList;
    public TextField messageField;
    public Label errorMessage;
    public DatePicker calendar1;
    public DatePicker calendar2;

    public Button buttonBack;
    public Button buttonView;
    public Button buttonSendToAll;

    private MessageService messageService;
    private UserService userService;
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<String> messages = FXCollections.observableArrayList();

    private User friend;
    private Stage stage = new Stage();

    public void setStage(Stage stage1) { this.stage = stage1; }

    public DatePicker getDate1(){return calendar1;}
    public DatePicker getDate2(){return calendar2;}
    public User getFriend(){return friend;}


    public void initFriends()
    {
        Map<User, LocalDateTime> allFriends = userService.getFriends(HomeController.activeUser.getId());
        List<User> friendsList = new ArrayList<>();
        for (Map.Entry<User,LocalDateTime> entry : allFriends.entrySet())
            friendsList.add(entry.getKey());
        modelFriends.setAll(friendsList);
    }

    public void initMessages(User friend)
    {
        List<Message> conversation = this.messageService.getConversation(HomeController.activeUser.getId(), friend.getId());
        List<String> list = new ArrayList<>();
        for (Message x : conversation)
        {
            if(x.getFrom().equals(HomeController.activeUser.getId()))
                list.add("Me: " + x.getMessage());
            else
                list.add(this.userService.findUser(x.getFrom()).getFirstName() + ": " + x.getMessage());
        }
        messages.setAll(list);
    }

    @FXML
    public void initialize()
    {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

        PagingRepository<Long, User> userDatabaseRepository = new UserDatabaseRepository(url, username, password);
        Repository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
        Repository<String, Account> accountDatabaseRepository = new AccountDatabaseRepository(url, username, password);
        Repository<Long, Message> messageDatabaseRepository = new MessageDatabaseRepository(url, username, password);

        this.userService = new UserService(userDatabaseRepository, friendshipDatabaseRepository, accountDatabaseRepository, new UserValidator(), new AccountValidator());
        this.messageService = new MessageService(messageDatabaseRepository, friendshipDatabaseRepository, new MessageValidator());
        this.messageService.addObserver(this);

        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));

        friendsTable.setItems(modelFriends);
        initFriends();

        this.errorMessage.setText("");

        buttonView.setTooltip(new Tooltip("View activity"));
        buttonSendToAll.setTooltip(new Tooltip("Send to all"));
        buttonBack.setTooltip(new Tooltip("Home page"));

        friendsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        messageField.setPromptText("Type a message...");
        calendar1.setPromptText("Start date");
        calendar2.setPromptText("End date");

        InputStream inputBack = getClass().getResourceAsStream("/images/home2.png");
        Image imageBack = new Image(inputBack, 40,40, true, true);
        buttonBack.setBackground(Background.EMPTY);
        buttonBack.setGraphic(new ImageView(imageBack));

        InputStream inputView = getClass().getResourceAsStream("/images/activity.png");
        Image imageView = new Image(inputView, 35,35, true, true);
        buttonView.setBackground(Background.EMPTY);
        buttonView.setGraphic(new ImageView(imageView));

        InputStream inputSendToAll = getClass().getResourceAsStream("/images/send.png");
        Image imageSendToAll = new Image(inputSendToAll, 35,35, true, true);
        buttonSendToAll.setBackground(Background.EMPTY);
        buttonSendToAll.setGraphic(new ImageView(imageSendToAll));


        this.friendsTable.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                friend = (User)friendsTable.getSelectionModel().getSelectedItem();
                if(friend != null)
                {
                    initMessages(friend);
                    messageList.setItems(messages);
                }
            }
        });

        this.messageField.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event)
            {
                if(event.getCode().equals(KeyCode.ENTER))
                {
                    String text = messageField.getText();
                    ArrayList<Long> to = new ArrayList<>();
                    to.add(friend.getId());
                    Message message = new Message(HomeController.activeUser.getId(), to, text, LocalDateTime.now());
                    try
                    {
                        messageService.sendMessage(message);
                        errorMessage.setText("");
                    }catch (MyException e)
                    {
                        errorMessage.setText(e.getMessage());
                        errorMessage.setTextFill(Color.DARKRED);
                    }
                    messageField.clear();
                }
            }
        });
    }

    @Override
    public void update(MessageEvent messageEvent) {
        initMessages(friend);
        initFriends();
    }


    public void handleSendToAll(ActionEvent actionEvent) {
        List<User> all = friendsTable.getSelectionModel().getSelectedItems();

        String text = messageField.getText();
        ArrayList<Long> to = new ArrayList<>();

        for(User it: all)
            to.add(it.getId());

        Message message = new Message(HomeController.activeUser.getId(), to, text, LocalDateTime.now());
        try
        {
            messageService.sendMessage(message);
            errorMessage.setText("");
        }catch (MyException e)
        {
            errorMessage.setText(e.getMessage());
            errorMessage.setTextFill(Color.DARKRED);
        }
        messageField.clear();

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
            stage.getIcons().add(new Image("/images/home.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        }
        catch (MyException e) {
            errorMessage.setText(e.getMessage());
            errorMessage.setTextFill(Color.DARKRED);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void handleView(ActionEvent actionEvent) {
        try
        {
            MessageViewController.setFriend(friend);

            if(friend == null)
            {
                this.errorMessage.setText("No item selected!");
                this.errorMessage.setTextFill(Color.DARKRED);
            }
            else {
                if(getDate1() == null || getDate2() == null)
                {
                    this.errorMessage.setText("No date selected!");
                    this.errorMessage.setTextFill(Color.DARKRED);
                }
                else {
                    MessageViewController.setDate1(getDate1());
                    MessageViewController.setDate2(getDate2());
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/view/MessageViewPDF.fxml"));
                    GridPane root = loader.load();
                    Scene scene = new Scene(root, 600, 300);
                    MessageViewController controller = loader.getController();
                    stage.setTitle("Messages View");
                    stage.setResizable(false);
                    stage.getIcons().add(new Image("/images/message.png"));
                    stage.setScene(scene);
                    controller.setStage(stage);
                    stage.show();
                }
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void handleActivity(ActionEvent actionEvent) {

        try
        {
            ActivityController.setDate1(getDate1());
            ActivityController.setDate2(getDate2());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/ActivityView.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 400, 500);
            ActivityController controller = loader.getController();
            //Stage stage = new Stage();
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
