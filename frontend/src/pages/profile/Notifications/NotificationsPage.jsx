import React, { useEffect } from "react";
import { useNotification } from "../../../context/NotificationContext";
import NotificationItem from "../../../components/ui/Notification/NotificationItem";
import { Link } from "react-router-dom";

export default function NotificationsPage() {
    const { notifications, fetchNotifications, markAsRead, markAllAsRead } = useNotification();

    useEffect(() => {
        fetchNotifications();
    }, [fetchNotifications]);

    const handleItemClick = (id) => {
        markAsRead(id);
    };

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
            <div className="mb-6 flex items-center justify-between">
                <h1 className="text-2xl font-bold text-base-content">Notificações</h1>
                {notifications.some(n => !n.read) && (
                    <button onClick={markAllAsRead} className="btn btn-sm btn-outline btn-primary">
                        <i className="bi bi-envelope-open me-2" />
                        Marcar todas como lidas
                    </button>
                )}
            </div>

            <div className="rounded-2xl border border-base-200 bg-base-100 shadow-sm overflow-hidden">
                {notifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-16 text-center">
                        <div className="mb-4 rounded-full bg-base-200 p-4">
                            <i className="bi bi-bell-slash text-3xl text-base-content/40" />
                        </div>
                        <p className="text-lg font-medium text-base-content/70">Tudo limpo por aqui!</p>
                        <p className="text-sm text-base-content/50">Você não tem novas notificações.</p>
                    </div>
                ) : (
                    <div className="divide-y divide-base-200">
                        {notifications.map((notif) => (
                            <NotificationItem
                                key={notif.id}
                                notification={notif}
                                onClick={() => handleItemClick(notif.id)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
