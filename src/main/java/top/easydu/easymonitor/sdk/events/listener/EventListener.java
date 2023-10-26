package top.easydu.easymonitor.sdk.events.listener;

import top.easydu.easymonitor.sdk.events.Event;

public interface EventListener<T> {

    void on(Event<T> event);

}
