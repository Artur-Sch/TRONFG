package ru.schneider_dev.tronfg.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.boontaran.games.StageGame;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.TextButton;
import ru.schneider_dev.tronfg.utils.Data;
import ru.schneider_dev.tronfg.services.LeaderboardService;
import ru.schneider_dev.tronfg.services.LeaderboardServiceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static ru.schneider_dev.tronfg.controls.TextButton.*;

/**
 * Экран отображения результатов игрока
 * 
 * Структура таблицы SCORE:
 * | RANK | LEVEL | USER TIME | LEVEL BEST |
 * |------|-------|-----------|------------|
 * | 2/5  |   1   |   0:18    |   0:18     | ← Ранг пользователя, уровень, лучшее время пользователя, лучшее время уровня
 * | 5/5  |   2   |   0:25    |   0:15     | ← Пользователь на 5-м месте из 5, его время 25 сек, лучшее время уровня 15 сек
 * 
 * Логика работы:
 * 1. При открытии экрана загружается кеш для быстрого отображения данных
 * 2. Кнопка REFRESH RANKINGS обновляет данные из Supabase
 * 3. Кеш не блокирует обновления, а используется только для быстрого отображения
 * 
 * 🚀 ОПТИМИЗАЦИЯ ЗАПРОСОВ (v2.0):
 * - РАНЬШЕ: N вызовов getRank() + N вызовов getBestTime() = до 32 запросов
 * - ТЕПЕРЬ: 1 вызов getAllUserRanks() + 1 вызов getAllLevelsBestTimes() = 2 запроса
 * - ЭКОНОМИЯ: до 30 сетевых запросов! (15x быстрее)
 * 
 * 🎯 АВТОМАТИЧЕСКАЯ ОТПРАВКА РЕЗУЛЬТАТОВ (v2.1):
 * - Результаты отправляются автоматически при завершении каждого уровня
 * - Отправляется только ЛУЧШИЙ результат пользователя для каждого уровня
 * - ScoreScreen теперь только получает данные для отображения
 * - Нет дублирования отправки результатов!
 */
public class ScoreScreen extends StageGame {

	public static final int ON_BACK = 1;
	public static final int ON_BACK_TO_INTRO = 2;
	public static final int ON_BACK_TO_LEVEL_LIST = 3;

	private TextButton backButton;
	private TextButton titleLabel;
	private TextButton updateButton;

	private Table scoreTable;
	private ScrollPane scrollPane;
	private LeaderboardService leaderboardService;
	private Map<Integer, Label> levelIdToRankLabel = new HashMap<>();
	private Map<Integer, Label> levelIdToBestLabel = new HashMap<>();
	private Map<Integer, Label> levelIdToTimeLabel = new HashMap<>();
	private Map<Integer, String> cachedRanks = new HashMap<>();
	private Map<Integer, Float> cachedBestTimes = new HashMap<>();
	private long lastUpdateTime = 0;
	private static final long CACHE_TTL_MS = 5 * 60 * 1000L; // 5 минут в миллисекундах
	private int sourceScreen; // Откуда пришли: 1 - Intro, 2 - LevelList
	private int[] completedRequests = {0}; // Счетчик завершенных запросов для оптимизации

	public ScoreScreen(int sourceScreen) {
		this.sourceScreen = sourceScreen;
		setupBackground();
		setupTitle();
		setupUpdateButton();
		
		leaderboardService = LeaderboardServiceFactory.createLeaderboardService();
		
		// Инициализируем кеш ПЕРЕД созданием таблицы
		loadCachedData();
		
		setupScoreTable();
		setupBackButton();

		updateRankings();
		// Если есть кешированные лучшие времена, применяем их сразу
		if (!cachedBestTimes.isEmpty()) {
			applyCachedData();
		}
		
		Gdx.app.log("ScoreScreen", "ScoreScreen setup completed");
	}
	
	private void loadCachedData() {
		// Загружаем кеш из постоянной памяти
		Data data = TRONgame.data;
		if (data != null) {
			lastUpdateTime = data.getLong("cache_last_update_time", 0L);
			
			for (int i = 1; i <= 16; i++) {
				int rankHash = data.getInt("cache_rank_" + i, 0);
				float bestTime = data.getFloat("cache_best_" + i, 0f);
				
				if (rankHash != 0) {
					String rank = decodeRankFromHash(rankHash);
					cachedRanks.put(i, rank);
					Gdx.app.log("ScoreScreen", "Loaded cached rank for level " + i + ": " + rank);
				}
				if (bestTime > 0f) {
					cachedBestTimes.put(i, bestTime);
					Gdx.app.log("ScoreScreen", "Loaded cached best time for level " + i + ": " + bestTime);
				}
			}
			
			Gdx.app.log("ScoreScreen", "Cache loaded: " + cachedRanks.size() + " ranks, " + cachedBestTimes.size() + " best times");
		}
	}
	
	private void saveCachedData() {
		// Сохраняем кеш в постоянную память
		Data data = TRONgame.data;
		if (data != null) {
			data.saveLong("cache_last_update_time", lastUpdateTime);
			
			for (Map.Entry<Integer, String> e : cachedRanks.entrySet()) {
				int hash = encodeRankToHash(e.getValue());
				data.saveInt("cache_rank_" + e.getKey(), hash);
				Gdx.app.log("ScoreScreen", "Saved cached rank for level " + e.getKey() + ": " + e.getValue());
			}
			for (Map.Entry<Integer, Float> e : cachedBestTimes.entrySet()) {
				data.saveFloat("cache_best_" + e.getKey(), e.getValue());
				Gdx.app.log("ScoreScreen", "Saved cached best time for level " + e.getKey() + ": " + e.getValue());
			}
			
			Gdx.app.log("ScoreScreen", "Cache saved: " + cachedRanks.size() + " ranks, " + cachedBestTimes.size() + " best times");
		}
	}
	
	private int encodeRankToHash(String rank) {
		// Простая схема кодирования ранга в хеш
		// Формат: "rank/total" -> сохраняем как rank * 1000 + total
		try {
			String[] parts = rank.split("/");
			if (parts.length == 2) {
				int rankNum = Integer.parseInt(parts[0]);
				int totalNum = Integer.parseInt(parts[1]);
				return rankNum * 1000 + totalNum;
			}
		} catch (NumberFormatException e) {
			// Игнорируем ошибки парсинга
		}
		return rank.hashCode(); // Fallback
	}
	
	private String decodeRankFromHash(int hash) {
		// Простая схема декодирования хеша в ранг
		// Формат: rank * 1000 + total -> "rank/total"
		if (hash > 1000 && hash < 999999) {
			int rank = hash / 1000;
			int total = hash % 1000;
			return rank + "/" + total;
		}
		return "1/1"; // Fallback
	}

	public void clearCache() {
		cachedRanks.clear();
		cachedBestTimes.clear();
		lastUpdateTime = 0;
	}

	private void setupBackground() {
		Gdx.app.log("ScoreScreen", "Setting up background");
		Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
		addBackground(bg, true, false);
		Gdx.app.log("ScoreScreen", "Background added");
	}

	private void setupTitle() {
		titleLabel = new TextButton("SCORE BOARD", TRONgame.tr2nFont, Color.GOLD, Color.GOLD);
		titleLabel.setFontScale(1.5f);
		addChild(titleLabel);

		centerHorizontally(titleLabel);
		titleLabel.setY(getHeight() - 60);
	}

	private void setupScoreTable() {
		scoreTable = new Table();
		scoreTable.pad(10);

		Label.LabelStyle headerStyle = new Label.LabelStyle(TRONgame.tr2nFont, Color.GOLD);
		headerStyle.font.setColor(Color.GOLD);

		Label rankHeader = new Label("RANK", headerStyle);
		Label levelHeader = new Label("LEVEL", headerStyle);
		Label timeHeader = new Label("TIME", headerStyle);
		Label levelBest = new Label("BEST", headerStyle);

		// Уменьшаем размер заголовков
		rankHeader.setFontScale(0.8f);
		levelHeader.setFontScale(0.8f);
		timeHeader.setFontScale(0.8f);
		levelBest.setFontScale(0.8f);

		rankHeader.setAlignment(Align.center);
		levelHeader.setAlignment(Align.center);
		timeHeader.setAlignment(Align.center);
		levelBest.setAlignment(Align.center);

		scoreTable.add(rankHeader).width(100).height(40).padRight(40);
		scoreTable.add(levelHeader).width(140).height(40).padRight(10);
		scoreTable.add(timeHeader).width(140).height(40).padRight(30);
		scoreTable.add(levelBest).width(140).height(40);
		scoreTable.row();

		List<ScoreEntry> scores = getScoreData();
		
		if (scores.isEmpty()) {
			Label.LabelStyle noDataStyle = new Label.LabelStyle(TRONgame.font24, Color.GRAY);
			Label noDataLabel = new Label("Нет результатов", noDataStyle);
			noDataLabel.setAlignment(Align.center);
			scoreTable.add(noDataLabel).colspan(4).width(400).height(100);
		} else {
			Collections.sort(scores, new Comparator<ScoreEntry>() {
				@Override
				public int compare(ScoreEntry o1, ScoreEntry o2) {
					return Integer.compare(o1.levelId, o2.levelId);
				}
			});

			Label.LabelStyle scoreStyle = new Label.LabelStyle(TRONgame.font24, Color.WHITE);
			for (int i = 0; i < scores.size(); i++) {
				ScoreEntry entry = scores.get(i);
				
				// Определяем стиль в зависимости от того, пройден ли уровень
				boolean isCompleted = entry.levelTime > 0;
				Color textColor = isCompleted ? Color.WHITE : Color.GRAY;
				
				Label rankLabel = new Label("-", scoreStyle);
				Label levelLabel = new Label("Level " + entry.levelId, scoreStyle);
				Label timeLabel = new Label(isCompleted ? formatTime(entry.levelTime) : "---", scoreStyle);
				Label bestLabel = new Label("-", scoreStyle); // Для всех уровней показываем "-", который заменится на реальное время

				rankLabel.setAlignment(Align.center);
				levelLabel.setAlignment(Align.center);
				timeLabel.setAlignment(Align.center);
				bestLabel.setAlignment(Align.center);

				// Применяем цвета
				rankLabel.setColor(textColor);
				levelLabel.setColor(textColor);
				timeLabel.setColor(textColor);
				bestLabel.setColor(textColor);

				scoreTable.add(rankLabel).width(100).height(30).padRight(40);
				scoreTable.add(levelLabel).width(140).height(30).padRight(20);
				scoreTable.add(timeLabel).width(140).height(30).padRight(20);
				scoreTable.add(bestLabel).width(140).height(30);
				scoreTable.row();

				// Карты для обновления из БД (ранги только для пройденных, лучшие времена для всех)
				if (isCompleted) {
					levelIdToRankLabel.put(entry.levelId, rankLabel);
				}
				// Лучшие времена нужны для всех уровней
				levelIdToBestLabel.put(entry.levelId, bestLabel);
				levelIdToTimeLabel.put(entry.levelId, timeLabel);
			}
		}

		scrollPane = new ScrollPane(scoreTable);
		
		// Располагаем таблицу ПОД кнопкой UPDATE и НАД кнопкой BACK
		float updateButtonY = updateButton.getY();
		float backButtonY = 30;
		float topMarginFromUpdate = 20f; // было 40f — уменьшаем отступ под кнопкой UPDATE
		float bottomMarginFromBack = 40f;
		float availableHeight = (updateButtonY - topMarginFromUpdate) - (backButtonY + bottomMarginFromBack);
		if (availableHeight < 100) availableHeight = 100;
		
		scrollPane.setSize(800, availableHeight);
		// Поднимаем таблицу выше - увеличиваем отступ от кнопки BACK
		scrollPane.setPosition((getWidth() - scrollPane.getWidth()) / 2, backButtonY + bottomMarginFromBack + 20);
		scrollPane.setScrollingDisabled(true, false);
		addChild(scrollPane);
		
		// Применяем кешированные данные если они есть (только для быстрого отображения при открытии экрана)
		if (!cachedRanks.isEmpty() || !cachedBestTimes.isEmpty()) {
			applyCachedData();
		}
	}

	private void setupUpdateButton() {
		updateButton = new TextButton("REFRESH RANKINGS", TRONgame.tr2nFont, TextButton.NEON_WHITE, TextButton.NEON_BLUE);
		addChild(updateButton);
		centerHorizontally(updateButton);
		updateButton.setY(getHeight() - 120);
		
		updateButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TRONgame.playSoundSafe("new_click.ogg");
				updateRankings();
			}
		});
	}

	private void updateRankings() {
		// Кнопка UPDATE всегда работает - убираем проверку кеша
		updateButton.setTouchable(Touchable.disabled);
		updateButton.setColor(NEON_BLUE);
		updateButton.setText("REFRESHING...");

		// Теперь результаты уже отправлены автоматически при завершении уровней,
		// поэтому сразу получаем обновленные данные
		String userId = TRONgame.data.getUserId();
		getAllDataOptimized(userId);
	}
	
	private void applyCachedData() {
		Gdx.app.log("ScoreScreen", "Applying cached data...");
		
		for (Map.Entry<Integer, String> entry : cachedRanks.entrySet()) {
			Label rankLabel = levelIdToRankLabel.get(entry.getKey());
			if (rankLabel != null) {
				rankLabel.setText(entry.getValue());
				Gdx.app.log("ScoreScreen", "Applied cached rank for level " + entry.getKey() + ": " + entry.getValue());
			}
		}
		
		// Применяем кешированные лучшие времена для всех уровней
		for (int levelId = 1; levelId <= 16; levelId++) {
			if (cachedBestTimes.containsKey(levelId)) {
				float globalBest = cachedBestTimes.get(levelId);
				Label bestLabel = levelIdToBestLabel.get(levelId);
				if (bestLabel != null) {
					bestLabel.setText(formatTime(globalBest));
					Gdx.app.log("ScoreScreen", "Applied cached best time for level " + levelId + ": " + globalBest);
				}
			}
		}
	}
	
	/**
	 * Новый оптимизированный метод для получения ВСЕХ данных за два запроса:
	 * 1. getAllUserRanks - ранги пользователя по всем уровням
	 * 2. getAllLevelsBestTimes - лучшие времена всех уровней
	 * 
	 * Вместо множественных вызовов getRank() и getBestTime() для каждого уровня,
	 * теперь делаем только 2 запроса к базе данных!
	 * 
	 * Результаты уже отправлены автоматически при завершении уровней,
	 * поэтому здесь мы только получаем обновленные данные для отображения.
	 */
	private void getAllDataOptimized(String userId) {
		Gdx.app.log("ScoreScreen", "🚀 Starting optimized data retrieval: user ranks + best times in 2 requests");
		Gdx.app.log("ScoreScreen", "💡 Note: Results are already submitted automatically when levels are completed!");
		
		// Сбрасываем счетчик для отслеживания завершения обоих запросов
		completedRequests[0] = 0;
		final int totalRequests = 2;
		
		// 1. Получаем ранги пользователя по всем уровням за один запрос
		Gdx.app.log("ScoreScreen", "📊 Request 1/2: Getting user ranks for all levels...");
		leaderboardService.getAllUserRanks(userId, new LeaderboardService.AllUserRanksCallback() {
			@Override
			public void onSuccess(Map<Integer, Integer> levelToRank, Map<Integer, Integer> levelToTotalPlayers, Map<Integer, Float> levelToUserBestTime) {
				Gdx.app.log("ScoreScreen", "✅ User ranks received: " + levelToRank.size() + " levels");
				
				// Обновляем кеш и отображение для всех уровней
				for (int levelId = 1; levelId <= 16; levelId++) {
					if (levelToRank.containsKey(levelId)) {
						final int rank = levelToRank.get(levelId);
						final int totalPlayers = levelToTotalPlayers.get(levelId);
						final float userBestTime = levelToUserBestTime.get(levelId);
						final int finalLevelId = levelId;
						
						// Сохраняем ранг в кеш
						cachedRanks.put(levelId, formatRank(rank, totalPlayers));
						
						// Обновляем отображение РАНГА
						Label rankLabel = levelIdToRankLabel.get(levelId);
						if (rankLabel != null) {
							Gdx.app.postRunnable(() -> {
								rankLabel.setText(formatRank(rank, totalPlayers));
								Gdx.app.log("ScoreScreen", "Updated rank label for level " + finalLevelId + " to: " + formatRank(rank, totalPlayers));
							});
						}
						
						// Обновляем отображение ВРЕМЕНИ ПОЛЬЗОВАТЕЛЯ (колонка TIME)
						Label timeLabel = levelIdToTimeLabel.get(levelId);
						if (timeLabel != null && userBestTime > 0) {
							Gdx.app.postRunnable(() -> {
								timeLabel.setText(formatTime(userBestTime));
								Gdx.app.log("ScoreScreen", "Updated user time label for level " + finalLevelId + " to: " + formatTime(userBestTime));
							});
						}
					}
				}
				
				// Отмечаем завершение первого запроса
				completedRequests[0]++;
				Gdx.app.log("ScoreScreen", "📊 Request 1/2 completed successfully");
				checkAllRequestsCompleted();
			}
			
			@Override
			public void onError(String error) {
				Gdx.app.log("ScoreScreen", "❌ Error in user ranks request: " + error);
				// Отмечаем завершение первого запроса (с ошибкой)
				completedRequests[0]++;
				checkAllRequestsCompleted();
			}
		});
		
		// 2. Получаем лучшие времена всех уровней за один запрос
		Gdx.app.log("ScoreScreen", "⏱️ Request 2/2: Getting best times for all levels...");
		leaderboardService.getAllLevelsBestTimes(new LeaderboardService.AllLevelsBestTimesCallback() {
			@Override
			public void onSuccess(Map<Integer, Float> levelToBestTime, Map<Integer, Integer> levelToTotalPlayers) {
				Gdx.app.log("ScoreScreen", "✅ Best times received: " + levelToBestTime.size() + " levels");
				
				// Обновляем кеш и отображение для всех уровней
				for (int levelId = 1; levelId <= 16; levelId++) {
					if (levelToBestTime.containsKey(levelId)) {
						final float bestTime = levelToBestTime.get(levelId);
						final int finalLevelId = levelId;
						cachedBestTimes.put(levelId, bestTime);
						
						// Обновляем отображение ЛУЧШЕГО ВРЕМЕНИ УРОВНЯ (колонка BEST)
						Label bestLabel = levelIdToBestLabel.get(levelId);
						if (bestLabel != null) {
							Gdx.app.postRunnable(() -> {
								bestLabel.setText(formatTime(bestTime));
								Gdx.app.log("ScoreScreen", "Updated level best time label for level " + finalLevelId + " to: " + formatTime(bestTime));
							});
						}
					}
				}
				
				// Отмечаем завершение второго запроса
				completedRequests[0]++;
				Gdx.app.log("ScoreScreen", "⏱️ Request 2/2 completed successfully");
				checkAllRequestsCompleted();
			}
			
			@Override
			public void onError(String error) {
				Gdx.app.log("ScoreScreen", "❌ Error in best times request: " + error);
				// Отмечаем завершение второго запроса (с ошибкой)
				completedRequests[0]++;
				checkAllRequestsCompleted();
			}
		});
	}
	
	/**
	 * Проверяет, завершены ли все запросы, и обновляет UI
	 */
	private void checkAllRequestsCompleted() {
		if (completedRequests[0] >= 2) {
			// Все запросы завершены (успешно или с ошибками)
			lastUpdateTime = System.currentTimeMillis();
			saveCachedData();
			Gdx.app.log("ScoreScreen", "All requests completed, updating cache and button");
			Gdx.app.postRunnable(() -> {
				updateButton.setText("RANKINGS REFRESHED");
				updateButton.setColor(NEON_GREEN);
				updateButton.setTouchable(Touchable.enabled);
			});
		}
	}
	
	/**
	 * Обрабатывает ошибки в оптимизированных запросах
	 */
	private void handleOptimizedRequestError(String requestType, String error) {
		Gdx.app.log("ScoreScreen", "Error in " + requestType + ": " + error);
		
		// В случае ошибки, показываем пользователю информацию
		Gdx.app.postRunnable(() -> {
			updateButton.setText("UPDATE FAILED");
			updateButton.setColor(Color.RED);
			updateButton.setTouchable(Touchable.enabled);
		});
	}

	private void setupBackButton() {
		backButton = new TextButton("BACK", TRONgame.tr2nFont, Color.WHITE, Color.RED);
		addChild(backButton);

		centerHorizontally(backButton);
		backButton.setY(30);

		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TRONgame.playSoundSafe("new_click.ogg");
				onClickBack();
			}
		});
	}

	private void centerHorizontally(TextButton button) {
		button.updateSize();
		float centerX = (getWidth() - button.getWidth()) / 2;
		button.setX(centerX);
		Gdx.app.log("ScoreScreen", "Button centered at X: " + centerX + " (screen width: " + getWidth() + ", button width: " + button.getWidth() + ")");
	}

	private void onClickBack() {
		backButton.setTouchable(Touchable.disabled);
		backButton.addAction(Actions.fadeOut(0.3f));
		delayCall("back", 0.3f);
	}

	@Override
	protected void onDelayCall(String code) {
		if ("back".equals(code)) {
			// Возвращаемся туда, откуда пришли
			if (sourceScreen == 1) {
				call(ON_BACK_TO_INTRO);
			} else if (sourceScreen == 2) {
				call(ON_BACK_TO_LEVEL_LIST);
			} else {
				call(ON_BACK); // Fallback
			}
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
			onClickBack();
			return true;
		}
		return super.keyUp(keycode);
	}

	private List<ScoreEntry> getScoreData() {
		List<ScoreEntry> scores = new ArrayList<>();
		
		Data data = TRONgame.data;
		if (data != null) {
			// Создаем список всех 16 уровней
			for (int levelId = 1; levelId <= 16; levelId++) {
				float levelTime = data.getLevelTime(levelId);
				// Если уровень не пройден, время будет 0
				float bestUserTime = levelTime > 0 ? levelTime : 0f;
				scores.add(new ScoreEntry(levelId, bestUserTime, 0f));
			}
		}
		
		return scores;
	}

	private String formatTime(float timeInSeconds) {
		// Проверяем валидность времени
		if (timeInSeconds <= 0) {
			return "---";
		}
		
		int minutes = (int) (timeInSeconds / 60);
		int seconds = (int) (timeInSeconds % 60);
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	private float getBestUserTimeForLevel(int levelId, float fallbackTime) {
		// Лучшее пользовательское время храним локально в Data (минимум от всех попыток)
		Data data = TRONgame.data;
		if (data != null) {
			float t = data.getLevelTime(levelId);
			if (t > 0f) return t;
		}
		return fallbackTime;
	}

	private String formatRank(int rank, int total) {
		return rank + "/" + total;
	}

	private static class ScoreEntry {
		int levelId;
		float levelTime;
		float bestTime; // Лучшее время прохождения уровня

		ScoreEntry(int levelId, float levelTime, float bestTime) {
			this.levelId = levelId;
			this.levelTime = levelTime;
			this.bestTime = bestTime;
		}
	}
}
