package socialnetwork.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
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
import java.util.ArrayList;
import java.util.List;



public class MessageViewController implements Observer<MessageEvent> {

    private static User friend;
    private static DatePicker date1;
    private static DatePicker date2;
    public ListView<String> messageList;
    public Button buttonExport;
    public Button buttonBack;
    private MessageService messageService;
    private UserService userService;
    private List<String> list;
    ObservableList<String> messages = FXCollections.observableArrayList();

    private Stage stage = new Stage();
    public void setStage(Stage stage1)
    {
        this.stage = stage1;
    }

    public static void setFriend(User friend){MessageViewController.friend = friend;}
    public static void setDate1(DatePicker date1){MessageViewController.date1 = date1;}
    public static void setDate2(DatePicker date2){MessageViewController.date2 = date2;}


    public void initMessages(User friend, DatePicker date1, DatePicker date2)
    {
        List<Message> conversation = this.messageService.getConversation(HomeController.activeUser.getId(), friend.getId());
        list = new ArrayList<>();
        for (Message x : conversation)
        {
            if(x.getDate().toLocalDate().isAfter(date1.getValue()) && x.getDate().toLocalDate().isBefore(date2.getValue()))
                if(x.getFrom().equals(HomeController.activeUser.getId()))
                    list.add(HomeController.activeUser.getFirstName() + ": " + x.getMessage());
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
        Repository<String, Account> accountDatabaseRepository = new AccountDatabaseRepository(url, username, password);
        Repository<Long, Message> messageDatabaseRepository = new MessageDatabaseRepository(url, username, password);
        Repository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);

        this.userService = new UserService(userDatabaseRepository, friendshipDatabaseRepository, accountDatabaseRepository, new UserValidator(), new AccountValidator());
        this.messageService = new MessageService(messageDatabaseRepository, friendshipDatabaseRepository, new MessageValidator());

        InputStream inputPDF = getClass().getResourceAsStream("/images/PDF.png");
        Image imagePDF = new Image(inputPDF, 45,45, true, true);
        buttonExport.setBackground(Background.EMPTY);
        buttonExport.setGraphic(new ImageView(imagePDF));

        InputStream inputBack = getClass().getResourceAsStream("/images/back.png");
        Image imageBack = new Image(inputBack, 45,45, true, true);
        buttonBack.setBackground(Background.EMPTY);
        buttonBack.setGraphic(new ImageView(imageBack));

        buttonBack.setTooltip(new Tooltip("Back to messages"));
        buttonExport.setTooltip(new Tooltip("Export to PDF"));

        initMessages(friend, date1, date2);
        messageList.setItems(messages);

    }

    @Override
    public void update(MessageEvent messageEvent) {

    }

    public void handleBack(ActionEvent actionEvent) {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Messages.fxml"));
            GridPane root = loader.load();
            Scene scene = new Scene(root, 800, 500);
            MessageController controller = loader.getController();
            //Stage stage = new Stage();
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

    public void handleExport(ActionEvent actionEvent) throws FileNotFoundException, DocumentException {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("PDFMessage.pdf"));

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("Conversations between " + HomeController.activeUser.getFirstName() + " and " + friend.getFirstName(), font);
        document.add(chunk);
        document.add(new Paragraph("\n"));

        for(String it : list)
        {
            Chunk chunk1 = new Chunk(it, font);
            document.add(chunk1);
            document.add(new Paragraph("\n"));
        }

        document.close();
    }
}
