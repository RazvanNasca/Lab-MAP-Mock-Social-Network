package socialnetwork.service;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

public class UserService
{
    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long,Long>, Friendship> friendshipRepository;
    private final Validator<User> validator;

    public UserService(Repository<Long,User> userRepository, Repository<Tuple<Long,Long>,Friendship> friendshipRepository, Validator<User> validator)
    {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.validator = validator;
    }

    public User addUser(User user)
    {
        this.validator.validate(user);
        return userRepository.save(user);
    }

    public User deleteUser(long id)
    {
        User user = this.userRepository.findOne(id);
        if(user == null)
            throw new ServiceException("There is no user with given ID!");

        return this.userRepository.delete(id);
    }

    public Iterable<User> getAll()
    {
        List<User> users = new ArrayList<>();

        for(User user : this.userRepository.findAll())
        {
            Map<User, LocalDateTime> friends = this.getFriends(user.getId());
            for(User friend : friends.keySet()) { user.getFriends().add(friend); }
            users.add(user);
        }
        return users;
    }

    public Map<User, LocalDateTime> getFriends(long id)
    {
        Map<User, LocalDateTime> dto = new HashMap<>();

        Iterable<Friendship> iterable = this.friendshipRepository.findAll();
        StreamSupport.stream(iterable.spliterator(), false)
                .filter(x -> (x.getId().getLeft().equals(id) || x.getId().getRight().equals(id)))
                .forEach( friendship -> {
                            User friend;
                            if(friendship.getId().getLeft().equals(id))
                                friend = this.userRepository.findOne(friendship.getId().getRight());
                            else
                                friend = this.userRepository.findOne(friendship.getId().getLeft());
                            dto.put(friend, friendship.getDate());
                        }
                );

        return dto;
    }

    public Map<User, LocalDateTime> getFriendsByMonth(long id, long month)
    {
        Map<User, LocalDateTime> dto = new HashMap<>();
        this.getFriends(id).forEach(
                (k, v) -> { long userMonth = Long.parseLong(v.toString().split("-")[1]);
                            if(month == userMonth) { dto.put(k, v); }});
        return dto;
    }

    public User findUser(long id)
    {
        return this.userRepository.findOne(id);
    }
}