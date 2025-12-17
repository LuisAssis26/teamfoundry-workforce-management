import { apiFetch } from "../auth/client";

export async function getNotifications() {
    const response = await apiFetch("/api/notifications");
    if (!response.ok) throw new Error("Failed to fetch notifications");
    return response.json();
}

export async function markAsRead(id) {
    const response = await apiFetch(`/api/notifications/${id}/read`, { method: "PATCH" });
    if (!response.ok) throw new Error("Failed to mark as read");
    // response body might be empty
    return true;
}

export async function markAllAsRead() {
    const response = await apiFetch("/api/notifications/read-all", { method: "PATCH" });
    if (!response.ok) throw new Error("Failed to mark all as read");
    return true;
}
