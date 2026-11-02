package service;

public class EmailNotificationService implements NotificationService {
    @Override
    public void send(String string) {
        System.out.println(String.format("[EMAIL] %s", string));
    }
}
