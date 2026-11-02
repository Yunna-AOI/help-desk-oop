package service;

public class TelegramNotifcationService implements NotificationService{
    @Override
    public void send(String string) {
        System.out.println(String.format("[Telegram] %s", string));
    }
}
