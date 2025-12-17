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

                    {hasNotifications && (
                        <div className="border-t border-base-200 bg-base-50 p-2 text-center">
                            <Link
                                to={
                                    // Determine generic link, or dynamic based on user role?
                                    // User roles are: /candidato/... or /empresa/...
                                    // Since we are inside EmployeeLayout or CompanyLayout, we can use a generic logic 
                                    // or just rely on the Navbar knowing the context? 
                                    // Wait, Navbar is shared.
                                    // I can look at URL to guess prefix (/candidato or /empresa) or use context.
                                    // But simpliest: The requirement said "Create section for NOTIFICATIONS in Account/Profile".
                                    // So I should link to a dedicated page.
                                    // Let's assume "/notifications" route or "/conta/notificacoes"?
                                    // No, user said "Account/Profile ... create a secção".
                                    // EmployeeLayout has /candidato/definicoes etc.
                                    // I will create /candidato/notificacoes and /empresa/notificacoes.
                                    // I will detect path to choose link.
                                    window.location.pathname.startsWith("/empresa") ? "/empresa/notificacoes" : "/candidato/notificacoes"
                                }
                                className="text-xs font-semibold text-primary hover:underline"
                                onClick={() => setIsOpen(false)}
                            >
                                Ver todas as notificações
                            </Link>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
