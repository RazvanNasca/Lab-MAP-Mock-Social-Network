package socialnetwork.service;

import socialnetwork.domain.Account;
import socialnetwork.domain.Event;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.event.ChangeEventType;
import socialnetwork.event.EventEvent;
import socialnetwork.event.UserEvent;
import socialnetwork.observer.Observable;
import socialnetwork.observer.Observer;
import socialnetwork.paging.Page;
import socialnetwork.paging.Pageable;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.EventDatabaseRepository;

import java.util.ArrayList;
import java.util.List;

public class EventService implements Observable<EventEvent>
{
    private final Repository<Long, Event> eventRepository;
    private final Validator<Event> eventValidator;

    private List<Observer<EventEvent>> observers;

    public EventService(Repository<Long, Event> eventRepository, Validator<Event> eventValidator)
    {
        this.eventRepository = eventRepository;
        this.eventValidator = eventValidator;
        this.observers = new ArrayList<>();
    }

    public Iterable<Event> getAll(){ return eventRepository.findAll();}

    public Iterable<Event> getUserEvent(User user)
    {
        List<Event> list = new ArrayList<>();
        Iterable<Event> all = getAll();

        for(Event it : all)
            for(Long  u : it.getParticipanti())
                if(u.equals(user.getId()))
                    list.add(it);

        return list;
    }

    public Event addEvent(Event event)
    {
        this.eventValidator.validate(event);

        if(this.eventRepository.findOne(event.getId()) != null)
        {
            throw new ServiceException("Email taken!");
        }

        Event savedEvent = this.eventRepository.save(event);

        return savedEvent;
    }

    public Long generateEventID()
    {
        Long eventID = 1L;
        Iterable <Event> all = getAll();

        for(Event e : all)
            eventID++;

        return eventID;
    }

    public Tuple<Long, Long> addSub(Tuple<Long, Long> tuple)
    {
        return ((EventDatabaseRepository) eventRepository).addSub(tuple);
    }

    public void deleteSub(Tuple<Long, Long> tuple)
    {
         ((EventDatabaseRepository) eventRepository).deleteSub(tuple);
    }

    public String findNotification(Tuple<Long, Long> tuple)
    {
        return ((EventDatabaseRepository) eventRepository).findNotification(tuple);
    }

    public void updateEvent(Tuple<Long, Long> tuple, String not)
    {
        ((EventDatabaseRepository) eventRepository).updateEvent(tuple, not);
    }

    @Override
    public void addObserver(Observer<EventEvent> e) { observers.add(e); }

    @Override
    public void removeObserver(Observer<EventEvent> e) { observers.remove(e);}

    @Override
    public void notifyObservers(EventEvent t) { observers.forEach(x -> x.update(t));}


    public Page<Event> findAll(Pageable pageable)
    {
        return ((EventDatabaseRepository) eventRepository).findAll(pageable);
    }

    public Page<Event> findAllForUser(Long id, Pageable pageable)
    {
        return ((EventDatabaseRepository) eventRepository).findAllForUser(id, pageable);
    }

    public Iterable<Event> getAllForUser(Long id)
    {
        return ((EventDatabaseRepository) eventRepository).getAllForUser(id);
    }

}
