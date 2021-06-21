package com.kuo.easychat.common.event;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2021/6/21.
 *
 * @author Fagan Wang
 */
public class EventPool {

    private ConcurrentHashMap<String, IEvent> events;

    private static final EventPool INSTANCE = new EventPool();

    private EventPool() {
        events = new ConcurrentHashMap<>();
    }
    public static EventPool getInstance() {
        return INSTANCE;
    }

    /**
     * 事件注册
     *
     * @param key     标识
     * @param handler 事件
     */
    public void registe(final String key, final IEvent handler) {
        if (null == key || key.isEmpty()) {
            System.out.println("key is empty!");
            return;
        }
        if (null == handler) {
            System.out.println("handle is empty!");
            return;
        }
        events.put(key, handler);
    }

    /**
     * 查找事件对象
     *
     * @param key 标识
     * @return 结果
     */
    public IEvent find(final String key) {
        if (null == key || key.isEmpty()) {
            System.out.println("key is empty!");
            return null;
        }
        return events.get(key);
    }
}
