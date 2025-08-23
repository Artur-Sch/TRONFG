package ru.schneider_dev.tronfg;

import android.util.Log;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import okhttp3.*;
import ru.schneider_dev.tronfg.services.LeaderboardService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class AndroidLeaderboardService implements LeaderboardService {
	private static final String TAG = "LeaderboardService";
	private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
	private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
	private static final String API_URL = SUPABASE_URL + "/rest/v1";
	
	private final OkHttpClient client;
	private final Json json;
	
	public AndroidLeaderboardService() {
		client = new OkHttpClient.Builder()
				.connectTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.build();
		
		json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
	}
	
	@Override
	public void submitResult(String userId, int level, float time, LeaderboardCallback callback) {
		String url = API_URL + "/rpc/submit_result";
		String requestBody = json.toJson(new SubmitRequest(userId, level, time));
		
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(requestBody, MediaType.parse("application/json")))
				.addHeader("Content-Type", "application/json")
				.addHeader("apikey", SUPABASE_ANON_KEY)
				.addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
				.build();
		
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Network error", e);
				callback.onError("Network error: " + e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body() != null ? response.body().string() : "";
						Log.d(TAG, "Submit response: " + responseBody);
						RankData[] list = json.fromJson(RankData[].class, responseBody);
						if (list != null && list.length > 0) {
							RankData data = list[0];
							// Обрабатываем null значения для best_time
							float bestTime = data.best_time != null ? data.best_time : 0f;
							callback.onSuccess((int) data.rank, (int) data.total_players, bestTime);
						} else {
							callback.onError("No rank data received");
						}
					} catch (Exception e) {
						Log.e(TAG, "Failed to parse response", e);
						callback.onError("Failed to parse response: " + e.getMessage());
					}
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Unknown error";
					Log.e(TAG, "Submit failed: " + response.code() + " - " + errorBody);
					callback.onError("Submit failed: " + response.code());
				}
				response.close();
			}
		});
	}

	@Override
	public void submitResultImmediately(String userId, int level, float time, ImmediateSubmitCallback callback) {
		Log.d(TAG, "Submitting result immediately - User: " + userId + ", Level: " + level + ", Time: " + time);
		
		// Используем существующий метод submitResult
		submitResult(userId, level, time, new LeaderboardCallback() {
			@Override
			public void onSuccess(int rank, int totalPlayers, float bestTime) {
				callback.onSuccess(rank, totalPlayers, bestTime);
			}
			
			@Override
			public void onError(String error) {
				callback.onError(error);
			}
		});
	}

	@Override
	public void getRank(String userId, int level, LeaderboardCallback callback) {
		String url = API_URL + "/rpc/get_user_rank";
		String requestBody = json.toJson(new RankRequest(userId, level));
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(requestBody, MediaType.parse("application/json")))
				.addHeader("Content-Type", "application/json")
				.addHeader("apikey", SUPABASE_ANON_KEY)
				.addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Network error", e);
				callback.onError("Network error: " + e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body() != null ? response.body().string() : "";
						RankData[] list = json.fromJson(RankData[].class, responseBody);
						if (list != null && list.length > 0) {
							RankData data = list[0];
							callback.onSuccess((int) data.rank, (int) data.total_players, data.best_time);
						} else {
							callback.onError("No rank data received");
						}
					} catch (Exception e) {
						callback.onError("Failed to parse response: " + e.getMessage());
					}
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Unknown error";
					callback.onError("GetRank failed: " + response.code());
				}
				response.close();
			}
		});
	}
	
	@Override
	public void getBestTime(int level, LeaderboardCallback callback) {
		String url = API_URL + "/rpc/get_level_best_time";
		String requestBody = json.toJson(new BestTimeRequest(level));
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(requestBody, MediaType.parse("application/json")))
				.addHeader("Content-Type", "application/json")
				.addHeader("apikey", SUPABASE_ANON_KEY)
				.addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Network error", e);
				callback.onError("Network error: " + e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body() != null ? response.body().string() : "";
						Log.d(TAG, "GetBestTime response: " + responseBody);
						BestTimeData[] list = json.fromJson(BestTimeData[].class, responseBody);
						if (list != null && list.length > 0) {
							BestTimeData data = list[0];
							// Обрабатываем null значения для best_time
							float bestTime = data.best_time != null ? data.best_time : 0f;
							callback.onSuccess(0, 0, bestTime); // rank и total не важны для лучшего времени
						} else {
							callback.onError("No best time data received");
						}
					} catch (Exception e) {
						Log.e(TAG, "Failed to parse response", e);
						callback.onError("Failed to parse response: " + e.getMessage());
					}
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Unknown error";
					Log.e(TAG, "GetBestTime failed: " + response.code() + " - " + errorBody);
					callback.onError("GetBestTime failed: " + response.code());
				}
				response.close();
			}
		});
	}

	@Override
	public void getAllLevelsBestTimes(AllLevelsBestTimesCallback callback) {
		String url = API_URL + "/rpc/get_all_levels_best_times";
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create("{}", MediaType.parse("application/json")))
				.addHeader("Content-Type", "application/json")
				.addHeader("apikey", SUPABASE_ANON_KEY)
				.addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Network error", e);
				callback.onError("Network error: " + e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body() != null ? response.body().string() : "";
						Log.d(TAG, "GetAllLevelsBestTimes response: " + responseBody);
						AllLevelsBestTimesData[] list = json.fromJson(AllLevelsBestTimesData[].class, responseBody);
						if (list != null && list.length > 0) {
							Map<Integer, Float> levelToBestTime = new HashMap<>();
							Map<Integer, Integer> levelToTotalPlayers = new HashMap<>();
							
							for (AllLevelsBestTimesData data : list) {
								// Обрабатываем null значения для best_time
								float bestTime = data.best_time != null ? data.best_time : 0f;
								levelToBestTime.put(data.level_id, bestTime);
								levelToTotalPlayers.put(data.level_id, (int)data.total_players);
							}
							
							callback.onSuccess(levelToBestTime, levelToTotalPlayers);
						} else {
							callback.onError("No best times data received");
						}
					} catch (Exception e) {
						Log.e(TAG, "Failed to parse response", e);
						callback.onError("Failed to parse response: " + e.getMessage());
					}
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Unknown error";
					Log.e(TAG, "GetAllLevelsBestTimes failed: " + response.code() + " - " + errorBody);
					callback.onError("GetAllLevelsBestTimes failed: " + response.code());
				}
				response.close();
			}
		});
	}

	@Override
	public void getAllUserRanks(String userId, AllUserRanksCallback callback) {
		String url = API_URL + "/rpc/get_all_user_ranks_optimized";
		String requestBody = json.toJson(new AllUserRanksRequest(userId));
		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(requestBody, MediaType.parse("application/json")))
				.addHeader("Content-Type", "application/json")
				.addHeader("apikey", SUPABASE_ANON_KEY)
				.addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Network error", e);
				callback.onError("Network error: " + e.getMessage());
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body() != null ? response.body().string() : "";
						Log.d(TAG, "GetAllUserRanks response: " + responseBody);
						AllUserRanksData[] list = json.fromJson(AllUserRanksData[].class, responseBody);
						if (list != null && list.length > 0) {
							Map<Integer, Integer> levelToRank = new HashMap<>();
							Map<Integer, Integer> levelToTotalPlayers = new HashMap<>();
							Map<Integer, Float> levelToUserBestTime = new HashMap<>();
							
							for (AllUserRanksData data : list) {
								levelToRank.put(data.level_id, (int)data.user_rank);
								levelToTotalPlayers.put(data.level_id, (int)data.total_players);
								// Обрабатываем null значения для user_best_time
								float bestTime = data.user_best_time != null ? data.user_best_time : 0f;
								levelToUserBestTime.put(data.level_id, bestTime);
							}
							
							callback.onSuccess(levelToRank, levelToTotalPlayers, levelToUserBestTime);
						} else {
							callback.onError("No user ranks data received");
						}
					} catch (Exception e) {
						Log.e(TAG, "Failed to parse response", e);
						callback.onError("Failed to parse response: " + e.getMessage());
					}
				} else {
					String errorBody = response.body() != null ? response.body().string() : "Unknown error";
					Log.e(TAG, "GetAllUserRanks failed: " + response.code() + " - " + errorBody);
					callback.onError("GetAllUserRanks failed: " + response.code());
				}
				response.close();
			}
		});
	}
	
	// Запрос для RPC
	private static class SubmitRequest {
		public String p_user_id;
		public int p_level;
		public float p_time;
		SubmitRequest(String userId, int level, float time) {
			this.p_user_id = userId;
			this.p_level = level;
			this.p_time = time;
		}
	}
	
	// Ответ RPC
	private static class RankData {
		public long rank;
		public long total_players;
		public Float best_time; // Изменено на Float для поддержки null
	}
	
	private static class RankRequest {
		public String p_user_id;
		public int p_level;
		RankRequest(String userId, int level) {
			this.p_user_id = userId;
			this.p_level = level;
		}
	}

	private static class BestTimeRequest {
		public int p_level;
		BestTimeRequest(int level) {
			this.p_level = level;
		}
	}

	private static class BestTimeData {
		public Float best_time; // Изменено на Float для поддержки null
		public long total_players;
		public int level_id;
	}

	private static class AllLevelsBestTimesData {
		public Float best_time; // Изменено на Float для поддержки null
		public long total_players;
		public int level_id;
	}

	private static class AllUserRanksRequest {
		public String p_user_id;
		
		AllUserRanksRequest(String userId) {
			this.p_user_id = userId;
		}
	}
	
	private static class AllUserRanksData {
		public int level_id;
		public long user_rank;
		public long total_players;
		public Float user_best_time; // Уже правильный тип
	}
}
