package com.hotel.dto;

/** Item del checklist de un servicio (US11). Se guarda como lista JSON en servicio.checklist. */
public record ChecklistItem(String item, Boolean obligatorio, Boolean hecho) {
}
