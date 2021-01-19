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
import socialnetwork.MyException;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.Account;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.AccountValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.AccountDatabaseRepository;
import socialnetwork.repository.database.FriendshipDatabaseRepository;
import socialnetwork.repository.database.UserDatabaseRepository;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.io.InputStream;

public class SignupController
{
    public TextField usernameField;
    public PasswordField passwordField1;
    public PasswordField passwordField2;
    public UserService userService;
    public TextField firstNameField;
    public TextField lastNameField;
    public Label messageToUser;
    public Hyperlink hyperLink;
    public Button signInButton;

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
        Repository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
        Repository<String, Account> accountDatabaseRepository = new AccountDatabaseRepository(url, username, password);

        this.userService = new UserService(userDatabaseRepository, friendshipDatabaseRepository, accountDatabaseRepository, new UserValidator(), new AccountValidator());

        this.usernameField.setText("");
        this.passwordField1.setText("");
        this.passwordField2.setText("");
        this.firstNameField.setText("");
        this.lastNameField.setText("");

        usernameField.setPromptText("username");
        passwordField1.setPromptText("password");
        passwordField2.setPromptText("password again");
        firstNameField.setPromptText("firstname");
        lastNameField.setPromptText("lastname");

        signInButton.setTooltip(new Tooltip("Sign up"));

        InputStream inputSigIn = getClass().getResourceAsStream("/images/signupImagine.png");
        Image imageSignIn = new Image(inputSigIn, 35,35, true, true);
        signInButton.setBackground(Background.EMPTY);
        signInButton.setGraphic(new ImageView(imageSignIn));


    }

    public void handleSubmitButtonAction(ActionEvent actionEvent)
    {
        String username = this.usernameField.getText();
        String password1 = this.passwordField1.getText();
        String password2 = this.passwordField2.getText();
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();

        if(password1.compareTo(password2) != 0)
        {
            messageToUser.setText("Passwords do not match!");
            messageToUser.setTextFill(Color.DARKRED);

            this.passwordField1.setText("");
            this.passwordField2.setText("");
        }

        try
        {
            User user = new User(firstName, lastName, username);
            user.setId(this.userService.generateUserID());
            Account account = new Account(username, password1, user.getId());
            account.setId(username);

            this.userService.addUser(user, account);

            messageToUser.setText("Account created successfully!");
            messageToUser.setTextFill(Color.DARKGREEN);
            this.firstNameField.setText("");
            this.lastNameField.setText("");
            this.usernameField.setText("");
            this.passwordField1.setText("");
            this.passwordField2.setText("");
        }catch (MyException e)
        {
            messageToUser.setText(e.getMessage());
            messageToUser.setTextFill(Color.DARKRED);
        }
    }

    public void handleBack(ActionEvent actionEvent) {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Login.fxml"));
            GridPane root = loader.load();
            LoginController controller = loader.getController();
            Scene scene = new Scene(root, 400, 500);
            //Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/login.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
