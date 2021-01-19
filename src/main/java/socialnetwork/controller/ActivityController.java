package socialnetwork.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityController implements Observer<MessageEvent> {

    private static DatePicker date1 = new DatePicker();
    private static DatePicker date2 = new DatePicker();
    public TableView <User> friendsTable;
    public TableColumn <User, String> friendColumnFirstName;
    public TableColumn <User, String> friendColumnLastName;
    public ListView<String> messageList;
    public Button Generate;
    public Label messageToUser;
    public Button buttonBack;
    public Button buttonExport;
    public Label labelForTable;
    public Label labelForActivity;

    private MessageService messageService;
    private UserService userService;
    private List<String> list = new ArrayList<>();
    private List<User> friendsList;
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<String> messages = FXCollections.observableArrayList();

    public DatePicker calendar1 = new DatePicker();
    public DatePicker calendar2 = new DatePicker();
    public DatePicker getDate1(){return calendar1;}
    public DatePicker getDate2(){return calendar2;}

    private Stage stage = new Stage();
    public void setStage(Stage stage1)
    {
        this.stage = stage1;
    }

    public static void setDate1(DatePicker date1){ActivityController.date1 = date1;}
    public static void setDate2(DatePicker date2){ActivityController.date2 = date2;}

    public void initFriends()
    {
        Map<User, LocalDateTime> allFriends = userService.getFriends(HomeController.activeUser.getId());
        friendsList = new ArrayList<>();
        for (Map.Entry<User,LocalDateTime> entry : allFriends.entrySet())
            if(entry.getValue().toLocalDate().isAfter(date1.getValue()) && entry.getValue().toLocalDate().isBefore(date2.getValue()))
            {
                friendsList.add(entry.getKey());
                initMessages(entry.getKey(), date1, date2);
            }
        modelFriends.setAll(friendsList);
    }


    public void initMessages(User friend, DatePicker date1, DatePicker date2)
    {
        List<Message> conversation = this.messageService.getConversation(HomeController.activeUser.getId(), friend.getId());
        for (Message x : conversation)
        {
            if(x.getDate().toLocalDate().isAfter(date1.getValue()) && x.getDate().toLocalDate().isBefore(date2.getValue()))
                if(x.getFrom().equals(friend.getId()))
                    list.add(friend.getFirstName() + " to " + HomeController.activeUser.getFirstName() + ": " + x.getMessage());
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

        buttonBack.setTooltip(new Tooltip("Home page"));
        buttonExport.setTooltip(new Tooltip("Export to PDF"));
        Generate.setTooltip(new Tooltip("View activity"));

        calendar1.setPromptText("Start date");
        calendar2.setPromptText("End date");

        InputStream inputBack = getClass().getResourceAsStream("/images/home2.png");
        Image imageBack = new Image(inputBack, 45,45, true, true);
        buttonBack.setBackground(Background.EMPTY);
        buttonBack.setGraphic(new ImageView(imageBack));

        InputStream inputPDF = getClass().getResourceAsStream("/images/PDF.png");
        Image imagePDF = new Image(inputPDF, 45,45, true, true);
        buttonExport.setBackground(Background.EMPTY);
        buttonExport.setGraphic(new ImageView(imagePDF));

        InputStream inputRaport = getClass().getResourceAsStream("/images/raport.png");
        Image imageRaport = new Image(inputRaport, 40,40, true, true);
        Generate.setBackground(Background.EMPTY);
        Generate.setGraphic(new ImageView(imageRaport));


        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));

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
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void handleExport(ActionEvent actionEvent) throws FileNotFoundException, DocumentException {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("PDFActivity.pdf"));

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("New friends: ", font);
        document.add(chunk);
        document.add(new Paragraph("\n"));

        for(User it : friendsList)
        {
            Chunk chunk1 = new Chunk(it.getFirstName(), font);
            document.add(chunk1);
            document.add(new Paragraph("\n"));
        }

        Chunk chunk2 = new Chunk("New Message: ", font);
        document.add(chunk2);
        document.add(new Paragraph("\n"));

        for(String it : list)
        {
            Chunk chunk3 = new Chunk(it, font);
            document.add(chunk3);
            document.add(new Paragraph("\n"));
        }

        document.close();

    }

    @Override
    public void update(MessageEvent messageEvent) {

    }

    public void handleGenerate(ActionEvent actionEvent) {

        try {
            setDate1(calendar1);
            setDate2(calendar2);

            friendsTable.setItems(modelFriends);
            initFriends();
            messageList.setItems(messages);
            labelForActivity.setPrefWidth(200);

            labelForActivity.setText("Activities during " + calendar1.getValue() + " - " + calendar2.getValue());
            labelForActivity.setWrapText(true);
        }
        catch (Exception e)
        {
            this.messageToUser.setText("No dates selected!");
            this.messageToUser.setTextFill(Color.DARKRED);
        }
    }
}
