package socialnetwork.service;

import socialnetwork.domain.*;
import socialnetwork.paging.*;
import socialnetwork.repository.database.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SuperService {

    public PagingRepository<Long, User> userDatabaseRepository;
    public PagingRepository<Tuple<Long,Long>, Friendship> friendDatabaseRepository;
    public PagingRepository<Tuple<Long,Long>, FriendRequest> friendshipDatabaseRepository;
    public PagingRepository<Long, Event> eventDatabaseRepository;

    public SuperService(PagingRepository<Long, User> userDatabaseRepository, PagingRepository<Tuple<Long,Long>, Friendship> friendDatabaseRepository, PagingRepository<Tuple<Long,Long>, FriendRequest> friendshipDatabaseRepository, PagingRepository<Long, Event> eventDatabaseRepository) {
        this.userDatabaseRepository = userDatabaseRepository;
        this.friendDatabaseRepository = friendDatabaseRepository;
        this.friendshipDatabaseRepository = friendshipDatabaseRepository;
        this.eventDatabaseRepository = eventDatabaseRepository;
    }

    public Page<User> getFriends(Long id, Pageable pageable)
    {
        Map<User, LocalDateTime> dto = new HashMap<>();

        Iterable<Friendship> iterable = friendDatabaseRepository.findAll();
        StreamSupport.stream(iterable.spliterator(), false)
                .filter(x -> (x.getId().getLeft().equals(id) || x.getId().getRight().equals(id)))
                .forEach( friendship -> {
                            User friend;
                            if(friendship.getId().getLeft().equals(id))
                                friend = userDatabaseRepository.findOne(friendship.getId().getRight());
                            else
                                friend = userDatabaseRepository.findOne(friendship.getId().getLeft());
                            dto.put(friend, friendship.getDate());
                        }
                );

        List<User> friendsList = new ArrayList<>();
        for (Map.Entry<User,LocalDateTime> entry : dto.entrySet())
            friendsList.add(entry.getKey());

        Paginator<User> paginator = new Paginator<>(pageable, friendsList);

        return paginator.paginate();
    }


    public PageDTO getAll(Long id)
    {
        return new PageDTO(((UserDatabaseRepository) userDatabaseRepository).findAllPotentialFriends(id,new PageableImplementation(0,2)).getContent().collect(Collectors.toList()),
                            getFriends(id, new PageableImplementation(0,2)).getContent().collect(Collectors.toList()),
                            ((FriendRequestDatabaseRepository) friendshipDatabaseRepository).findAllReguestSend(id, new PageableImplementation(0,2)).getContent().collect(Collectors.toList()),
                            ((FriendRequestDatabaseRepository) friendshipDatabaseRepository).findAllReguestReceive(id, new PageableImplementation(0,2)).getContent().collect(Collectors.toList()),
                            ((EventDatabaseRepository) eventDatabaseRepository).findAllForUser( id ,new PageableImplementation(0,2)).getContent().collect(Collectors.toList()),
                            eventDatabaseRepository.findAll(new PageableImplementation(0,2)).getContent().collect(Collectors.toList())
                          );
    }
}
