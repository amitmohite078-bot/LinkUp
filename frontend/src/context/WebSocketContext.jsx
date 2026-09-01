import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { useAuth } from './AuthContext';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
  const { user } = useAuth();
  const [connected, setConnected] = useState(false);
  const [liveNotifications, setLiveNotifications] = useState([]);
  const stompClientRef = useRef(null);
  const subscriptionsRef = useRef(new Map());

  useEffect(() => {
    if (!user) {
      disconnect();
      return;
    }

    connect();

    return () => {
      disconnect();
    };
  }, [user]);

  const connect = () => {
    try {
      const socket = new SockJS('http://localhost:8080/ws-linkup');
      const client = Stomp.over(socket);
      
      // Disable debug console logging to keep console clean unless needed
      client.debug = () => {};

      client.connect({}, (frame) => {
        setConnected(true);
        stompClientRef.current = client;

        // 1. Notify platform that user is online
        client.send('/app/chat.online', {}, JSON.stringify({
          userId: user.id,
          isOnline: true
        }));

        // 2. Subscribe to user-specific notifications
        const notificationSub = client.subscribe(`/topic/notifications/${user.id}`, (message) => {
          const notification = JSON.parse(message.body);
          setLiveNotifications(prev => [notification, ...prev]);
        });
        subscriptionsRef.current.set(`notifications_${user.id}`, notificationSub);

      }, (error) => {
        // Auto-reconnect after 5 seconds if connection drops
        setTimeout(connect, 5000);
      });
    } catch (e) {
      setTimeout(connect, 5000);
    }
  };

  const disconnect = () => {
    const client = stompClientRef.current;
    if (client && client.connected) {
      try {
        // Send offline indicator before dropping
        client.send('/app/chat.online', {}, JSON.stringify({
          userId: user.id,
          isOnline: false
        }));
        
        // Unsubscribe all active channels
        subscriptionsRef.current.forEach(sub => sub.unsubscribe());
        subscriptionsRef.current.clear();
        
        client.disconnect();
      } catch (e) {
        console.error("Disconnect error:", e);
      }
    }
    stompClientRef.current = null;
    setConnected(false);
    setLiveNotifications([]);
  };

  // Dynamic channel subscriber
  const subscribeChannel = (topic, callback) => {
    const client = stompClientRef.current;
    if (!client || !client.connected) {
      return null;
    }

    if (subscriptionsRef.current.has(topic)) {
      return subscriptionsRef.current.get(topic);
    }

    const sub = client.subscribe(topic, (msg) => {
      callback(JSON.parse(msg.body));
    });
    subscriptionsRef.current.set(topic, sub);
    return sub;
  };

  const unsubscribeChannel = (topic) => {
    if (subscriptionsRef.current.has(topic)) {
      subscriptionsRef.current.get(topic).unsubscribe();
      subscriptionsRef.current.delete(topic);
    }
  };

  const sendPayload = (destination, payload) => {
    const client = stompClientRef.current;
    if (client && client.connected) {
      client.send(destination, {}, JSON.stringify(payload));
    }
  };

  return (
    <WebSocketContext.Provider value={{
      connected,
      liveNotifications,
      setLiveNotifications,
      subscribeChannel,
      unsubscribeChannel,
      sendPayload
    }}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
