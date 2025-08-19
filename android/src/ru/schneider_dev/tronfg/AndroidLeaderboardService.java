package ru.schneider_dev.tronfg;

import android.util.Log;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import okhttp3.*;
import ru.schneider_dev.tronfg.services.LeaderboardService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
							callback.onSuccess((int) data.rank, (int) data.total_players, data.best_time);
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
		public float best_time;
	}
	
	private static class RankRequest {
		public String p_user_id;
		public int p_level;
		RankRequest(String userId, int level) {
			this.p_user_id = userId;
			this.p_level = level;
		}
	}
}
