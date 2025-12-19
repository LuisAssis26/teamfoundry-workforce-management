import React, { useRef, useState, useEffect } from "react";
import { useNotification } from "../../../context/NotificationContext";
import NotificationItem from "./NotificationItem";
import { Link } from "react-router-dom";

export default function NotificationDropdown() {
    const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotification();
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    // Close when clicking outside
    useEffect(() => {
        function handleClickOutside(event) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        }
        if (isOpen) document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [isOpen]);

    const handleItemClick = (id) => {
        markAsRead(id);
        setIsOpen(false);
    };

    const handleMarkAll = (e) => {
        e.stopPropagation();
        markAllAsRead();
    };

    // Limit to 5 in dropdown
    const displayedNotifications = notifications.slice(0, 5);
    const hasNotifications = notifications.length > 0;
    const hasUnread = unreadCount > 0;

    return (
        <div className="relative z-50" ref={dropdownRef}>
            <button
                type="button"
                className="btn btn-ghost btn-circle relative"
                onClick={() => setIsOpen(!isOpen)}
                aria-label="Notificações"
            >
                <i className="bi bi-bell text-xl" />
                {unreadCount > 0 && (
                    <span className="absolute top-2 right-2 h-3 w-3 rounded-full bg-error border-2 border-base-100" />
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 rounded-xl border border-base-300 bg-base-100 shadow-xl overflow-hidden animate-fade-in-down">
                    <div className="flex items-center justify-between border-b border-base-200 bg-base-100 px-4 py-3">
                        <h3 className="font-semibold text-base-content">Notificações</h3>
                        {hasUnread && (
                            <button
                                onClick={handleMarkAll}
                                className="btn btn-xs btn-ghost text-primary tooltip tooltip-left"
                                data-tip="Marcar todas como lidas"
                            >
                                <i className="bi bi-envelope-open" />
                            </button>
                        )}
                        {!hasUnread && hasNotifications && (
                            <i className="bi bi-envelope text-base-content/50" />
                        )}
                    </div>

                    <div className="max-h-[300px] overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-6 text-center text-sm text-base-content/60">
                                Sem notificações
                            </div>
                        ) : (
                            displayedNotifications.map((notif) => (
                                <NotificationItem
                                    key={notif.id}
                                    notification={notif}
                                    onClick={() => handleItemClick(notif.id)}
                                />
                            ))
                        )}
                    </div>

                    {/* Footer removed as per user request to delete Notifications Page */}
                </div>
            )}
        </div>
    );
}
