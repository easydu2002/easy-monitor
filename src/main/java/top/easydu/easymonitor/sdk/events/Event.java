package top.easydu.easymonitor.sdk.events;

public class Event<T> {

    private EventType type;
    private T eventData;

    public Event(EventType type, T eventData) {
        this.type = type;
        this.eventData = eventData;
    }

    public EventType getType() {
        return type;
    }

    public T getEventData() {
        return eventData;
    }
}
