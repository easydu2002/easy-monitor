package top.easydu.easymonitor.sdk.events;

import top.easydu.easymonitor.sdk.events.listener.EventListener;
import top.easydu.easymonitor.sdk.platform.hik.common.constants.AlarmType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * https://zhuanlan.zhihu.com/p/361143647
 */
public class EventDispatcher {

    private static Map<EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();


    public static void addEventListener(EventType type, EventListener listener) {
        if (listener == null) return;

        List<EventListener> handlersForEventType = listeners.get(type);
        if (handlersForEventType == null) {
            handlersForEventType = new ArrayList<>();
            listeners.put(type, handlersForEventType);
        }

        if (handlersForEventType.contains(listener)) return;
        handlersForEventType.add(listener);
    }


    public static void dispatchEvent(EventType type, Object data) {
        List<EventListener> listenersForEvent = listeners.get(type);
        if (listenersForEvent == null || listenersForEvent.isEmpty()) return;

        listenersForEvent.forEach(listener -> {
            try {
                listener.on(new Event(type, data));
            } catch (Exception e) {
                System.err.println("Listener threw exception for event " + type);
            }
        });
    }
}
