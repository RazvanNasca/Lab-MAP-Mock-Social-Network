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
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.paging.PagingRepository;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.AccountDatabaseRepository;
import socialnetwork.repository.database.FriendshipDatabaseRepository;
import socialnetwork.repository.database.UserDatabaseRepository;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.io.InputStream;

public class LoginController
{
    public TextField usernameField;
    public PasswordField passwordField;
    public Label messageToUser;
    public Hyperlink hyperLink;
    public Button signInButton;
    private UserService userService;

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

        usernameField.setText("");
        passwordField.setText("");

        usernameField.setPromptText("username");
        passwordField.setPromptText("password");

        signInButton.setTooltip(new Tooltip("Login"));

        InputStream inputLogIn = getClass().getResourceAsStream("/images/loginImagine.png");
        Image imageLogIn = new Image(inputLogIn, 35,35, true, true);
        signInButton.setBackground(Background.EMPTY);
        signInButton.setGraphic(new ImageView(imageLogIn));

    }

    public void handleSubmitButtonAction(ActionEvent event)
    {
        try{
            Account account = userService.findAccount(usernameField.getText());
            if(!account.getPassword().equals(passwordField.getText()))
                throw new ValidationException("Invalid password!");

            HomeController.setActiveUser(userService.findUser(account.getUserID()));

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Home.fxml"));
            GridPane root = loader.load();
            HomeController controller = loader.getController();
            Scene scene = new Scene(root, 650, 300);
            Stage stage = new Stage();
            stage.setTitle("Home");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/images/home.png"));
            stage.setScene(scene);
            controller.setStage(stage);
            stage.show();
            controller.startEvents();
        }
        catch (MyException e) {
            messageToUser.setText(e.getMessage());
            messageToUser.setTextFill(Color.DARKRED);

            passwordField.setText("");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void handleSignUpAction(ActionEvent actionEvent)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/Signup.fxml"));
            GridPane root = loader.load();
            SignupController controller = loader.getController();
            Scene scene = new Scene(root, 400, 500);
            //Stage stage = new Stage();
            stage.setTitle("Sign up");
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
