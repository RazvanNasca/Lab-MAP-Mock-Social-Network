package socialnetwork.domain;

import java.util.List;

public class PageDTO {

    List<User> users;
    List<User> friendships;
    List<FriendRequest> friendRequestsSend;
    List<FriendRequest> friendRequestsReceived;
    List<Event> events;
    List<Event> eventsUser;

    public PageDTO(List<User> users, List<User> friendships, List<FriendRequest> friendRequestsSend, List<FriendRequest> friendRequestsReceived, List<Event> events, List<Event> eventsUser) {
        this.users = users;
        this.friendships = friendships;
        this.friendRequestsSend = friendRequestsSend;
        this.friendRequestsReceived = friendRequestsReceived;
        this.events = events;
        this.eventsUser = eventsUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getFriendships() {
        return friendships;
    }

    public void setFriendships(List<User> friendships) {
        this.friendships = friendships;
    }

    public List<FriendRequest> getFriendRequests() {
        return friendRequestsSend;
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        this.friendRequestsSend = friendRequests;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEventsUser() {
        return eventsUser;
    }

    public void setEventsUser(List<Event> eventsUser) {
        this.eventsUser = eventsUser;
    }

    public List<FriendRequest> getFriendRequestsSend() {
        return friendRequestsSend;
    }

    public void setFriendRequestsSend(List<FriendRequest> friendRequestsSend) {
        this.friendRequestsSend = friendRequestsSend;
    }

    public List<FriendRequest> getFriendRequestsReceived() {
        return friendRequestsReceived;
    }

    public void setFriendRequestsReceived(List<FriendRequest> friendRequestsReceived) {
        this.friendRequestsReceived = friendRequestsReceived;
    }
}
