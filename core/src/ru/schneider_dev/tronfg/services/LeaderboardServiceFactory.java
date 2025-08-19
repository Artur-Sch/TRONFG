package ru.schneider_dev.tronfg.services;

public class LeaderboardServiceFactory {
    
    /**
     * Создает экземпляр LeaderboardService для текущей платформы
     * @return экземпляр LeaderboardService
     */
    public static LeaderboardService createLeaderboardService() {
        try {
            // Пытаемся создать Android-реализацию
            Class<?> androidServiceClass = Class.forName("ru.schneider_dev.tronfg.AndroidLeaderboardService");
            return (LeaderboardService) androidServiceClass.getConstructor().newInstance();
        } catch (Exception e) {
            // Если не удалось, используем Desktop-реализацию
            return new DesktopLeaderboardService();
        }
    }
}
