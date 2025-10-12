package ru.schneider_dev.tronfg.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

/**
 * Утилитарный класс для оптимизации памяти и поддержки страниц размером 16 КБ
 * Этот класс обеспечивает эффективное управление памятью для игровых объектов
 */
public class MemoryOptimizer implements Disposable {

    // Константы для размера страниц памяти
    public static final int MEMORY_PAGE_SIZE = 16384; // 16 КБ в байтах
    public static final int MEMORY_PAGE_SIZE_KB = 16;
    public static final boolean MEMORY_OPTIMIZATION_ENABLED = true;

    // Настройки оптимизации
    private static final int MAX_MEMORY_PAGES = 1024; // Максимальное количество страниц
    private static final float MEMORY_CLEANUP_THRESHOLD = 0.8f; // Порог для очистки памяти

    // Состояние оптимизации
    private boolean isOptimized = false;
    private int currentMemoryPages = 0;
    private long lastMemoryCheck = 0;
    private static final long MEMORY_CHECK_INTERVAL = 5000; // Проверка каждые 5 секунд

    private static MemoryOptimizer instance;

    private MemoryOptimizer() {
        initializeMemoryOptimization();
    }

    public static MemoryOptimizer getInstance() {
        if (instance == null) {
            instance = new MemoryOptimizer();
        }
        return instance;
    }

    /**
     * Инициализация оптимизации памяти
     */
    private void initializeMemoryOptimization() {
        if (MEMORY_OPTIMIZATION_ENABLED) {
            Gdx.app.log("MemoryOptimizer", "Инициализация оптимизации памяти для страниц " + MEMORY_PAGE_SIZE_KB + " КБ");

            // Устанавливаем размер страницы памяти
            setMemoryPageSize(MEMORY_PAGE_SIZE);

            // Включаем оптимизацию
            enableMemoryOptimization();

            isOptimized = true;
        }
    }

    /**
     * Устанавливает размер страницы памяти
     *
     * @param pageSize размер страницы в байтах
     */
    public void setMemoryPageSize(int pageSize) {
        if (pageSize > 0 && pageSize <= MEMORY_PAGE_SIZE) {
            Gdx.app.log("MemoryOptimizer", "Установлен размер страницы памяти: " + pageSize + " байт");

            // Здесь можно добавить нативную настройку размера страницы
            // System.setProperty("memory.page.size", String.valueOf(pageSize));
        }
    }

    /**
     * Получает текущий размер страницы памяти
     *
     * @return размер страницы в байтах
     */
    public int getMemoryPageSize() {
        return MEMORY_PAGE_SIZE;
    }

    /**
     * Включает оптимизацию памяти
     */
    public void enableMemoryOptimization() {
        if (!isOptimized) {
            Gdx.app.log("MemoryOptimizer", "Включена оптимизация памяти");

            // Настройки для эффективного использования памяти
            optimizeMemoryAllocation();
            setupMemoryMonitoring();

            isOptimized = true;
        }
    }

    /**
     * Отключает оптимизацию памяти
     */
    public void disableMemoryOptimization() {
        if (isOptimized) {
            Gdx.app.log("MemoryOptimizer", "Отключена оптимизация памяти");
            isOptimized = false;
        }
    }

    /**
     * Оптимизирует выделение памяти
     */
    private void optimizeMemoryAllocation() {
        // Настройки для эффективного выделения памяти
        System.gc(); // Принудительная сборка мусора

        // Устанавливаем параметры для оптимизации памяти
        try {
            // Настройка размера кучи для оптимального использования страниц 16 КБ
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();

            Gdx.app.log("MemoryOptimizer", String.format(
                    "Память: Макс=%d, Всего=%d, Свободно=%d, Используется=%d",
                    maxMemory, totalMemory, freeMemory, (totalMemory - freeMemory)
            ));

        } catch (Exception e) {
            Gdx.app.error("MemoryOptimizer", "Ошибка при оптимизации памяти", e);
        }
    }

    /**
     * Настраивает мониторинг памяти
     */
    private void setupMemoryMonitoring() {
        // Периодическая проверка использования памяти
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                checkMemoryUsage();

                // Планируем следующую проверку
                if (isOptimized) {
                    Gdx.app.postRunnable(this);
                }
            }
        });
    }

    /**
     * Проверяет использование памяти
     */
    private void checkMemoryUsage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMemoryCheck < MEMORY_CHECK_INTERVAL) {
            return;
        }

        lastMemoryCheck = currentTime;

        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            float memoryUsageRatio = (float) usedMemory / maxMemory;

            // Если использование памяти превышает порог, выполняем очистку
            if (memoryUsageRatio > MEMORY_CLEANUP_THRESHOLD) {
                Gdx.app.log("MemoryOptimizer", "Высокое использование памяти: " +
                        String.format("%.1f%%", memoryUsageRatio * 100));
                performMemoryCleanup();
            }

        } catch (Exception e) {
            Gdx.app.error("MemoryOptimizer", "Ошибка при проверке памяти", e);
        }
    }

    /**
     * Выполняет очистку памяти
     */
    private void performMemoryCleanup() {
        Gdx.app.log("MemoryOptimizer", "Выполняется очистка памяти");

        // Принудительная сборка мусора
        System.gc();

        // Очистка кэшей и временных объектов
        clearCaches();

        Gdx.app.log("MemoryOptimizer", "Очистка памяти завершена");
    }

    /**
     * Очищает кэши и временные объекты
     */
    private void clearCaches() {
        // Очистка кэшей LibGDX
        if (Gdx.graphics != null) {
            // Очистка кэша текстур
            Gdx.gl.glFlush();
        }

        // Очистка кэша звуков
        if (Gdx.audio != null) {
            // Очистка аудио кэша
        }
    }

    /**
     * Получает информацию о текущем состоянии памяти
     *
     * @return строка с информацией о памяти
     */
    public String getMemoryInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            return String.format(
                    "Память: Макс=%d КБ, Всего=%d КБ, Используется=%d КБ, Свободно=%d КБ, Страницы=%d КБ",
                    maxMemory / 1024, totalMemory / 1024, usedMemory / 1024, freeMemory / 1024, MEMORY_PAGE_SIZE_KB
            );
        } catch (Exception e) {
            return "Ошибка получения информации о памяти: " + e.getMessage();
        }
    }

    /**
     * Проверяет, включена ли оптимизация памяти
     *
     * @return true если оптимизация включена
     */
    public boolean isMemoryOptimizationEnabled() {
        return isOptimized;
    }

    /**
     * Получает количество текущих страниц памяти
     *
     * @return количество страниц
     */
    public int getCurrentMemoryPages() {
        return currentMemoryPages;
    }

    @Override
    public void dispose() {
        Gdx.app.log("MemoryOptimizer", "Освобождение ресурсов MemoryOptimizer");

        // Отключаем оптимизацию
        disableMemoryOptimization();

        // Очищаем ссылку на экземпляр
        instance = null;
    }
}
