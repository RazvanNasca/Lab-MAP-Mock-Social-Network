package socialnetwork.event;

public class EventEvent implements Event {

    private ChangeEventType type;
    private Event event, oldEvent;

    public EventEvent(ChangeEventType type, Event event) {
        this.type = type;
        this.event = event;
    }
    public EventEvent(ChangeEventType type, Event event, Event oldEvent) {
        this.type = type;
        this.event = event;
        this.oldEvent = oldEvent;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Event getEvent() {
        return event;
    }

    public Event getOldEvent() {
        return oldEvent;
    }

}
