package service;

public class TelegramNotifcationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("[Telegram] " + message);
    }
}
