package ru.schneider_dev.tronfg.utils;

import com.boontaran.DataManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.services.LeaderboardService;
import ru.schneider_dev.tronfg.services.LeaderboardServiceFactory;

public class Data {

    private DataManagerInterface manager;
    private String userId; // Кешируем ID пользователя

    private static final String PREFERENCE_NAME = "tron_data";
    private static final String PROGRESS_KEY = "progress";
    private static final String LEVEL_TIME_PREFIX = "level_time_";
    private static final String TOTAL_TIME_KEY = "total_time";
    private static final String USER_ID_KEY = "user_id";
    
    // Константы для настроек звука
    private static final String SOUND_MUTED_KEY = "sound_muted";
    private static final String MUSIC_MUTED_KEY = "music_muted";

    public Data() {
        // Используем LibGDX Preferences для Android, DataManager для других платформ
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
            manager = new AndroidDataManager();
        } else {
            manager = new LegacyDataManager();
        }
        
        // Инициализируем ID пользователя только один раз при создании объекта
        initializeUserId();
    }
    
    /**
     * Инициализирует ID пользователя только один раз
     */
    private void initializeUserId() {
		// Пытаемся восстановить существующий ID пользователя
		int userIdHash = manager.getInt(USER_ID_KEY, 0);
		
		if (userIdHash == 0) {
			// Пытаемся восстановить из backup ключа
			userIdHash = restoreUserIdFromBackup();
			
			if (userIdHash == 0) {
				// Генерируем новый ID
				userIdHash = generateNewUserIdHash();
			}
		}
		
		this.userId = "user_" + Math.abs(userIdHash);
	}
    
    /**
     * Получает уникальный идентификатор пользователя (из кеша)
     * @return userId
     */
    public String getUserId() {
		// Просто возвращаем сохранённый ID
		return this.userId;
	}
    
    /**
     * Восстанавливает ID из резервных копий
     * @return восстановленный ID или 0 если не найден
     */
    private int restoreUserIdFromBackup() {
        // Пробуем восстановить из резервной копии
        int backupHash = manager.getInt(USER_ID_KEY + "_backup", 0);
        if (backupHash != 0) {
            // Восстанавливаем основной ID
            manager.saveInt(USER_ID_KEY, backupHash);
            return backupHash;
        }
        
        // Пробуем восстановить из строкового хеша
        int stringHash = manager.getInt(USER_ID_KEY + "_string", 0);
        if (stringHash != 0) {
            // Пытаемся найти оригинальный ID по строковому хешу
            // Это сложно, поэтому просто возвращаем 0
            return 0;
        }
        
        return 0;
    }

    private int generateNewUserIdHash() {
		// Генерируем новый ID
		int userIdHash = java.util.UUID.randomUUID().hashCode();
		
		// Сохраняем ID в нескольких местах для надёжности
		manager.saveInt(USER_ID_KEY, userIdHash);
		manager.saveInt(USER_ID_KEY + "_backup", userIdHash);
		
		// Также сохраняем как строку для совместимости
		String newUserId = "user_" + Math.abs(userIdHash);
		int stringHash = newUserId.hashCode();
		manager.saveInt(USER_ID_KEY + "_string", stringHash);
		
		return userIdHash;
	}

    public int getProgress() {
        return  manager.getInt(PROGRESS_KEY, 1);
    }

    public void setProgress(int progress) {
        manager.saveInt(PROGRESS_KEY, progress);
    }

    /**
     * Сохраняет время прохождения уровня
     * @param levelId ID уровня
     * @param time время в секундах
     */
    public void saveLevelTime(int levelId, float time) {
        String key = LEVEL_TIME_PREFIX + levelId;
        // Текущее сохранённое (лучшее) время в сотых
        int prevInHundredths = manager.getInt(key, 0);
        
        // Если уже есть время и новое не лучше - не сохраняем и не отправляем
        if (prevInHundredths > 0 && time >= (prevInHundredths / 100f)) {
            return;
        }
        
        // Сохраняем время в сотых долях секунды для точности
        int timeInHundredths = Math.round(time * 100);
        manager.saveInt(key, timeInHundredths);
        
        // Автоматически отправляем лучший результат в Supabase
        submitBestResultToLeaderboard(levelId, time);
    }
    
    /**
     * Автоматически отправляет лучший результат в лидерборд
     * @param levelId ID уровня
     * @param time время прохождения
     */
    private void submitBestResultToLeaderboard(int levelId, float time) {
        try {
            // Получаем userId из TRONgame
            String userId = TRONgame.data.getUserId();
            if (userId != null && !userId.isEmpty()) {
                // Создаем сервис для отправки результата
                LeaderboardService leaderboardService = LeaderboardServiceFactory.createLeaderboardService();
                
                // Отправляем результат асинхронно
                leaderboardService.submitResult(userId, levelId, time, new LeaderboardService.LeaderboardCallback() {
                    @Override
                    public void onSuccess(int rank, int totalPlayers, float bestTime) {
                        // Логируем успешную отправку
                        System.out.println("✅ Level " + levelId + " result submitted automatically: " + 
                                         formatTime(time) + " (Rank: " + rank + "/" + totalPlayers + ")");
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Логируем ошибку, но не блокируем игру
                        System.err.println("❌ Failed to submit level " + levelId + " result: " + error);
                    }
                });
            }
        } catch (Exception e) {
            // Игнорируем ошибки, чтобы не блокировать игру
            System.err.println("Error submitting level " + levelId + " result: " + e.getMessage());
        }
    }
    
    /**
     * Форматирует время для логирования
     */
    private String formatTime(float timeInSeconds) {
        if (timeInSeconds <= 0) return "0:00";
        int minutes = (int) (timeInSeconds / 60);
        int seconds = (int) (timeInSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Получает время прохождения уровня
     * @param levelId ID уровня
     * @return время в секундах или 0 если уровень не пройден
     */
    public float getLevelTime(int levelId) {
        String key = LEVEL_TIME_PREFIX + levelId;
        int timeInHundredths = manager.getInt(key, 0);
        return timeInHundredths / 100f;
    }

    /**
     * Получает все результаты по уровням
     * @return список результатов
     */
    public List<LevelResult> getAllLevelResults() {
        List<LevelResult> results = new ArrayList<>();
        
        // Проверяем все 16 уровней
        for (int i = 1; i <= 16; i++) {
            float time = getLevelTime(i);
            if (time > 0) { // Если уровень пройден
                results.add(new LevelResult(i, time));
            }
        }
        
        return results;
    }

    /**
     * Получает общее время прохождения всех уровней
     * @return общее время в секундах
     */
    public float getTotalTime() {
        int totalInHundredths = manager.getInt(TOTAL_TIME_KEY, 0);
        return totalInHundredths / 100f;
    }

    // Методы для кеширования
    public void saveFloat(String key, float value) {
        int valueInHundredths = Math.round(value * 100);
        manager.saveInt(key, valueInHundredths);
    }

    public float getFloat(String key, float defaultValue) {
        int valueInHundredths = manager.getInt(key, Math.round(defaultValue * 100));
        return valueInHundredths / 100f;
    }

    public void saveInt(String key, int value) {
        manager.saveInt(key, value);
    }

    public int getInt(String key, int defaultValue) {
        return manager.getInt(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        // Преобразуем long в int для совместимости с DataManager
        // Используем битовую маску для сохранения старших и младших битов
        int highBits = (int) (value >> 32);
        int lowBits = (int) (value & 0xFFFFFFFFL);
        
        manager.saveInt(key + "_high", highBits);
        manager.saveInt(key + "_low", lowBits);
    }

    public long getLong(String key, long defaultValue) {
        int highBits = manager.getInt(key + "_high", 0);
        int lowBits = manager.getInt(key + "_low", 0);
        
        if (highBits == 0 && lowBits == 0) {
            return defaultValue;
        }
        
        return ((long) highBits << 32) | (lowBits & 0xFFFFFFFFL);
    }

    public void saveString(String key, String value) {
        // Сохраняем строку как хеш для совместимости с DataManager
        int hash = value.hashCode();
        manager.saveInt(key, hash);
    }

    public String getString(String key, String defaultValue) {
        int hash = manager.getInt(key, 0);
        if (hash == 0) {
            return defaultValue;
        }
        // Возвращаем хеш как строку, так как оригинальную строку восстановить нельзя
        return String.valueOf(hash);
    }

    /**
     * Сохраняет настройку звука (включен/выключен)
     * @param muted true - звук выключен, false - звук включен
     */
    public void saveSoundMuted(boolean muted) {
        manager.saveInt(SOUND_MUTED_KEY, muted ? 1 : 0);
    }

    /**
     * Загружает настройку звука
     * @return true если звук выключен, false если включен
     */
    public boolean isSoundMuted() {
        return manager.getInt(SOUND_MUTED_KEY, 0) == 1;
    }

    /**
     * Сохраняет настройку музыки (включена/выключена)
     * @param muted true - музыка выключена, false - музыка включена
     */
    public void saveMusicMuted(boolean muted) {
        manager.saveInt(MUSIC_MUTED_KEY, muted ? 1 : 0);
    }

    /**
     * Загружает настройку музыки
     * @return true если музыка выключена, false если включена
     */
    public boolean isMusicMuted() {
        return manager.getInt(MUSIC_MUTED_KEY, 0) == 1;
    }

    /**
     * Сохраняет все настройки звука
     * @param soundMuted настройка звука
     * @param musicMuted настройка музыки
     */
    public void saveAudioSettings(boolean soundMuted, boolean musicMuted) {
        saveSoundMuted(soundMuted);
        saveMusicMuted(musicMuted);
    }

    /**
     * Загружает все настройки звука
     * @return массив [soundMuted, musicMuted]
     */
    public boolean[] getAudioSettings() {
        return new boolean[]{
            isSoundMuted(),
            isMusicMuted()
        };
    }

    /**
     * Класс для хранения результата уровня
     */
    public static class LevelResult {
        public final int levelId;
        public final float time;

        public LevelResult(int levelId, float time) {
            this.levelId = levelId;
            this.time = time;
        }
    }

    /**
     * Интерфейс для работы с данными
     */
    private interface DataManagerInterface {
        void saveInt(String key, int value);
        int getInt(String key, int defaultValue);
    }

    /**
     * Реализация для Android с использованием LibGDX Preferences
     */
    private static class AndroidDataManager implements DataManagerInterface {
        private Preferences prefs;

        public AndroidDataManager() {
            prefs = Gdx.app.getPreferences(PREFERENCE_NAME);
        }

        @Override
        public void saveInt(String key, int value) {
            prefs.putInteger(key, value);
            prefs.flush(); // Принудительно сохраняем
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return prefs.getInteger(key, defaultValue);
        }
    }

    /**
     * Реализация для других платформ с использованием DataManager
     */
    private static class LegacyDataManager implements DataManagerInterface {
        private DataManager manager;

        public LegacyDataManager() {
            manager = new DataManager(PREFERENCE_NAME);
        }

        @Override
        public void saveInt(String key, int value) {
            manager.saveInt(key, value);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return manager.getInt(key, defaultValue);
        }
    }
}
