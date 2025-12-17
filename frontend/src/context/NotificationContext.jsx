import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { useAuthContext } from "../auth/AuthContext";
import { getNotifications, markAsRead as apiMarkAsRead, markAllAsRead as apiMarkAllAsRead } from "../api/notification/notification";

const NotificationContext = createContext();

export function NotificationProvider({ children }) {
    const { isAuthenticated } = useAuthContext();
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(false);

    const fetchNotifications = useCallback(async () => {
        if (!isAuthenticated) return;
        try {
            setLoading(true);
            const data = await getNotifications();
            setNotifications(data);
            setUnreadCount(data.filter((n) => !n.read).length);
        } catch (err) {
            console.error("Failed to fetch notifications", err);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        fetchNotifications();
        // Poll every 30 seconds
        const interval = setInterval(fetchNotifications, 30000);
        return () => clearInterval(interval);
    }, [fetchNotifications]);

    const markAsRead = async (id) => {
        try {
            await apiMarkAsRead(id);
            setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, read: true } : n))
            );
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch (err) {
            console.error("Failed to mark notification as read", err);
        }
    };

    const markAllAsRead = async () => {
        try {
            await apiMarkAllAsRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch (err) {
            console.error("Failed to mark all notifications as read", err);
        }
    };

    return (
        <NotificationContext.Provider
            value={{
                notifications,
                unreadCount,
                loading,
                fetchNotifications,
                markAsRead,
                markAllAsRead,
            }}
        >
            {children}
        </NotificationContext.Provider>
    );
}

export function useNotification() {
    return useContext(NotificationContext);
}
