import React from "react";
import { Link } from "react-router-dom";
import PropTypes from "prop-types";

// Helper to determine link based on type
function getLink(type) {
    switch (type) {
        case "JOB_OFFER":
            return "/candidato/ofertas";
        case "REQUEST_COMPLETED":
            return "/empresa/requisicoes";
        default:
            return "#";
    }
}

function getIcon(type) {
    switch (type) {
        case "JOB_OFFER":
            return "bi-briefcase";
        case "REQUEST_COMPLETED":
            return "bi-check-circle";
        default:
            return "bi-bell";
    }
}

export default function NotificationItem({ notification, onClick }) {
    const { isRead: read, message, type, createdAt } = notification;
    // Note: DTO uses 'isRead' or 'read'? backend DTO says 'isRead', context mapped 'read' if I used response.json().
    // Let's check DTO in backend: NotificationDTO has `boolean isRead`.
    // Jackson usually serializes boolean `isRead` as `read` if it's a getter `isRead()`? Or strictly `isRead`?
    // Spring Boot default Jackson: `isRead` -> `read` usually. 
    // Wait, DTO fields: `private boolean isRead;`. Lombok `@Data` generates `isRead()` getter.
    // Jackson treats `isRead()` as property `read`.
    // So valid property is `read`.

    // Wait, I should verify this. If I use `isRead` property in DTO, Lombok generates `isRead()` (for boolean) or `getIsRead()` (for Boolean wrapper)?
    // `private boolean isRead;` -> Lombok `isRead()`. Jackson matches `isRead()` to json field `read`.
    // So frontend receives `read`.

    const link = getLink(type);
    const icon = getIcon(type);
    const dateStr = new Date(createdAt).toLocaleDateString() + " " + new Date(createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    return (
        <div
            className={`relative flex flex-col gap-1 border-b border-base-200 p-3 hover:bg-base-200 transition-colors cursor-pointer ${!notification.read ? "bg-base-100/50" : "opacity-80"}`}
        >
            <Link to={link} onClick={onClick} className="absolute inset-0 z-10" />
            <div className="flex items-start gap-3">
                <div className={`mt-1 flex h-2 w-2 shrink-0 rounded-full ${!notification.read ? "bg-error" : "bg-transparent"}`} />
                <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                        <i className={`bi ${icon} text-primary`} />
                        <span className="text-xs text-base-content/60">{dateStr}</span>
                    </div>
                    <p className={`text-sm ${!notification.read ? "font-semibold text-base-content" : "text-base-content/80"}`}>
                        {message}
                    </p>
                </div>
            </div>
        </div>
    );
}

NotificationItem.propTypes = {
    notification: PropTypes.shape({
        id: PropTypes.number.isRequired,
        message: PropTypes.string.isRequired,
        type: PropTypes.string.isRequired,
        read: PropTypes.bool,
        createdAt: PropTypes.string,
    }).isRequired,
    onClick: PropTypes.func,
};
