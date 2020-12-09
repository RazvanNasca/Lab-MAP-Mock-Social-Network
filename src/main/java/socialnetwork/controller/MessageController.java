package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.AccountValidator;
import socialnetwork.domain.validators.FriendshipValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.AccountDatabaseRepository;
import socialnetwork.repository.database.FriendRequestDatabaseRepository;
import socialnetwork.repository.database.FriendshipDatabaseRepository;
import socialnetwork.repository.database.UserDatabaseRepository;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageController {
    public TableView friendsTable;
    public TableColumn friendColumnFirstName;
    public TableColumn friendColumnLastName;

    private FriendshipService friendshipService;
    private UserService userService;
    ObservableList<User> modelFriends = FXCollections.observableArrayList();

    public void initFriends()
    {
        Map<User, LocalDateTime> allFriends = userService.getFriends(HomeController.activeUser.getId());
        List<User> friendsList = new ArrayList<>();
        for (Map.Entry<User,LocalDateTime> entry : allFriends.entrySet())
            friendsList.add(entry.getKey());
        modelFriends.setAll(friendsList);
    }

    @FXML
    public void initialize()
    {
        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

        Repository<Long, User> userDatabaseRepository = new UserDatabaseRepository(url, username, password);
        Repository<Tuple<Long, Long>, Friendship> friendshipDatabaseRepository = new FriendshipDatabaseRepository(url, username, password);
        Repository<String, Account> accountDatabaseRepository = new AccountDatabaseRepository(url, username, password);
        Repository<Tuple<Long, Long>, FriendRequest> friendRequestDatabaseRepository = new FriendRequestDatabaseRepository(url, username, password);

        this.userService = new UserService(userDatabaseRepository, friendshipDatabaseRepository, accountDatabaseRepository, new UserValidator(), new AccountValidator());
         this.friendshipService = new FriendshipService(userDatabaseRepository, friendRequestDatabaseRepository,friendshipDatabaseRepository, new FriendshipValidator());

        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));


        friendsTable.setItems(modelFriends);
        initFriends();
    }

}
