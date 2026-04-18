package enums;

public enum ReservationStatus {
    PENDING,    // أول ما الحجز يتعمل وقبل ما يتأكد
    CONFIRMED,  // بعد ما الضيف يدفع أو الإدارة تأكد الحجز
    CANCELLED,  // لو الحجز اتلغى
    COMPLETED,  // بعد ما الضيف يعمل Check-out ويدفع الفاتورة
    STAYING
}
