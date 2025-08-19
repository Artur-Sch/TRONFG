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

/**
 * Экран отображения результатов игрока
 * 
 * Логика работы:
 * 1. При открытии экрана загружается кеш для быстрого отображения данных
 * 2. Кнопка UPDATE всегда работает и обновляет данные из Supabase
 * 3. Кеш не блокирует обновления, а используется только для быстрого отображения
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
				}
				if (bestTime > 0f) {
					cachedBestTimes.put(i, bestTime);
				}
			}
		}
		
		// Применяем кешированные данные если они есть
		if (!cachedRanks.isEmpty() || !cachedBestTimes.isEmpty()) {
			applyCachedData();
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
			}
			for (Map.Entry<Integer, Float> e : cachedBestTimes.entrySet()) {
				data.saveFloat("cache_best_" + e.getKey(), e.getValue());
			}
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
		Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
		addBackground(bg, true, false);
	}

	private void setupTitle() {
		titleLabel = new TextButton("SCORE BOARD", TRONgame.tr2nFont, Color.WHITE, Color.GOLD);
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
		Label totalTimeHeader = new Label("BEST", headerStyle);

		rankHeader.setAlignment(Align.center);
		levelHeader.setAlignment(Align.center);
		timeHeader.setAlignment(Align.center);
		totalTimeHeader.setAlignment(Align.center);

		scoreTable.add(rankHeader).width(100).height(40).padRight(40);
		scoreTable.add(levelHeader).width(140).height(40).padRight(10);
		scoreTable.add(timeHeader).width(140).height(40).padRight(30);
		scoreTable.add(totalTimeHeader).width(140).height(40);
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
				
				Label rankLabel = new Label("-", scoreStyle);
				Label levelLabel = new Label("Level " + entry.levelId, scoreStyle);
				Label timeLabel = new Label(formatTime(entry.levelTime), scoreStyle);
				Label bestLabel = new Label("-", scoreStyle);

				rankLabel.setAlignment(Align.center);
				levelLabel.setAlignment(Align.center);
				timeLabel.setAlignment(Align.center);
				bestLabel.setAlignment(Align.center);

				if (i == 0) {
					rankLabel.setColor(Color.WHITE);
					levelLabel.setColor(Color.WHITE);
					timeLabel.setColor(Color.WHITE);
					bestLabel.setColor(Color.WHITE);
				}

				scoreTable.add(rankLabel).width(100).height(30).padRight(40);
				scoreTable.add(levelLabel).width(140).height(30).padRight(20);
				scoreTable.add(timeLabel).width(140).height(30).padRight(20);
				scoreTable.add(bestLabel).width(140).height(30);
				scoreTable.row();

				// Карты для обновления из БД
				levelIdToRankLabel.put(entry.levelId, rankLabel);
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
		updateButton = new TextButton("UPDATE RANKINGS", TRONgame.tr2nFont, Color.WHITE, Color.BLUE);
		addChild(updateButton);
		centerHorizontally(updateButton);
		updateButton.setY(getHeight() - 120);
		
		updateButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				updateRankings();
				TRONgame.media.playSound("new_click.ogg");
			}
		});
	}

	private void updateRankings() {
		// Кнопка UPDATE всегда работает - убираем проверку кеша
		updateButton.setTouchable(Touchable.disabled);
		updateButton.setText("UPDATING...");
		
		List<ScoreEntry> scores = getScoreData();
		if (scores.isEmpty()) {
			updateButton.setText("NO RESULTS");
			updateButton.setTouchable(Touchable.enabled);
			return;
		}
		
		String userId = TRONgame.data.getUserId();
		
		// Сначала отправляем результаты для всех пройденных уровней
		sendAllResults(userId, scores, 0);
	}
	
	private void applyCachedData() {
		for (Map.Entry<Integer, String> entry : cachedRanks.entrySet()) {
			Label rankLabel = levelIdToRankLabel.get(entry.getKey());
			if (rankLabel != null) {
				rankLabel.setText(entry.getValue());
			}
		}
		
		for (Map.Entry<Integer, Float> entry : cachedBestTimes.entrySet()) {
			int lvl = entry.getKey();
			float globalBest = entry.getValue();
			Label bestLabel = levelIdToBestLabel.get(lvl);
			if (bestLabel != null) {
				bestLabel.setText(formatTime(globalBest));
			}
		}
	}
	
	private void sendAllResults(String userId, List<ScoreEntry> scores, int currentIndex) {
		if (currentIndex >= scores.size()) {
			// Все результаты отправлены, теперь получаем ранги
			getAllRanks(userId, scores, 0);
			return;
		}
		
		ScoreEntry entry = scores.get(currentIndex);
		// Получаем лучшее время пользователя для этого уровня
		float bestUserTime = getBestUserTimeForLevel(entry.levelId, entry.levelTime);
		
		leaderboardService.submitResult(userId, entry.levelId, bestUserTime, 
			new LeaderboardService.LeaderboardCallback() {
				@Override
				public void onSuccess(int rank, int totalPlayers, float bestTime) {
					// Отправляем следующий результат
					sendAllResults(userId, scores, currentIndex + 1);
				}
				
				@Override
				public void onError(String error) {
					// Продолжаем с следующим уровнем даже при ошибке
					sendAllResults(userId, scores, currentIndex + 1);
				}
			});
	}
	
	private void getAllRanks(String userId, List<ScoreEntry> scores, int currentIndex) {
		if (currentIndex >= scores.size()) {
			// Все ранги получены, обновляем кеш и кнопку
			lastUpdateTime = System.currentTimeMillis();
			saveCachedData();
			Gdx.app.postRunnable(() -> {
				updateButton.setText("RANKINGS UPDATED");
				updateButton.setTouchable(Touchable.enabled);
			});
			return;
		}
		
		ScoreEntry entry = scores.get(currentIndex);
		final int levelId = entry.levelId;
		
		leaderboardService.getRank(userId, levelId, new LeaderboardService.LeaderboardCallback() {
			@Override
			public void onSuccess(int rank, int total, float best) {
				// Сохраняем в кеш
				cachedRanks.put(levelId, rank + "/" + total);
				cachedBestTimes.put(levelId, best);
				
				Label rankLbl = levelIdToRankLabel.get(levelId);
				Label bestLbl = levelIdToBestLabel.get(levelId);
				if (rankLbl != null || bestLbl != null) {
					Gdx.app.postRunnable(() -> {
						if (rankLbl != null) rankLbl.setText(rank + "/" + total);
						if (bestLbl != null) bestLbl.setText(formatTime(best));
					});
				}
				// Получаем ранг для следующего уровня
				getAllRanks(userId, scores, currentIndex + 1);
			}
			
			@Override
			public void onError(String error) {
				// Продолжаем с следующим уровнем даже при ошибке
				getAllRanks(userId, scores, currentIndex + 1);
			}
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
				onClickBack();
				TRONgame.media.playSound("new_click.ogg");
			}
		});
	}

	private void centerHorizontally(TextButton button) {
		button.updateSize();
		button.setX((getWidth() - button.getWidth()) / 2);
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
			List<Data.LevelResult> levelResults = data.getAllLevelResults();
			
			if (levelResults.isEmpty()) {
				return scores;
			}
			
			levelResults.sort((o1, o2) -> Integer.compare(o1.levelId, o2.levelId));
			
			for (Data.LevelResult result : levelResults) {
				// TIME: лучшее локально сохранённое время пользователя
				float bestUserTime = getBestUserTimeForLevel(result.levelId, result.time);
				// bestTime пока оставляем пустым, он заполнится из Supabase
				scores.add(new ScoreEntry(result.levelId, bestUserTime, 0f));
			}
		}
		
		return scores;
	}

	private String formatTime(float timeInSeconds) {
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
