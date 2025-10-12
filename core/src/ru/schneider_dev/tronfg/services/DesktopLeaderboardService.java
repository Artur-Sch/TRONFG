package ru.schneider_dev.tronfg.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DesktopLeaderboardService implements LeaderboardService {
    private static final String TAG = "DesktopLeaderboardService";
     
    private final Json json;
    private String supabaseUrl;
    private String supabaseAnonKey;
    private String apiUrl;
    
    public DesktopLeaderboardService() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        loadSupabaseConfig();
    }
    
    private void loadSupabaseConfig() {
        try {
            // Пытаемся загрузить из local.properties
            FileHandle localProps = Gdx.files.external("local.properties");
            if (!localProps.exists()) {
                // Если файл не найден, пробуем загрузить из корня проекта
                localProps = Gdx.files.local("local.properties");
            }
            
            if (localProps.exists()) {
                BufferedReader reader = new BufferedReader(localProps.reader());
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("supabase.url=")) {
                        supabaseUrl = line.substring("supabase.url=".length()).trim();
                    } else if (line.startsWith("supabase.anon.key=")) {
                        supabaseAnonKey = line.substring("supabase.anon.key=".length()).trim();
                    }
                }
                reader.close();
                
                if (supabaseUrl != null && supabaseAnonKey != null) {
                    apiUrl = supabaseUrl + "/rest/v1";
                    Gdx.app.log(TAG, "Supabase config loaded successfully");
                    Gdx.app.log(TAG, "URL: " + supabaseUrl);
                    Gdx.app.log(TAG, "Key: " + supabaseAnonKey.substring(0, Math.min(20, supabaseAnonKey.length())) + "...");
                } else {
                    Gdx.app.error(TAG, "Failed to load Supabase config from local.properties");
                    setDefaultConfig();
                }
            } else {
                Gdx.app.error(TAG, "local.properties not found, using default config");
                setDefaultConfig();
            }
        } catch (IOException e) {
            Gdx.app.error(TAG, "Error reading local.properties", e);
            setDefaultConfig();
        }
    }
    
    private void setDefaultConfig() {
        // Используем значения по умолчанию из local.properties
        supabaseUrl = "https://zsbyhtziksjucudcpfma.supabase.co";
        supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpzYnlodHppa3NqdWN1ZGNwZm1hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTU1MjM4NzEsImV4cCI6MjA3MTA5OTg3MX0.LnBQFaRQn7LuQzZXff1s4Y4mxK6LHVE-3sicjutKX8s";
        apiUrl = supabaseUrl + "/rest/v1";
        Gdx.app.log(TAG, "Using default Supabase config");
    }
    
    @Override
    public void submitResult(String userId, int level, float time, LeaderboardCallback callback) {
        String url = apiUrl + "/rpc/submit_result";
        String requestBody = json.toJson(new SubmitRequest(userId, level, time));
        
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl(url);
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("apikey", supabaseAnonKey);
        httpRequest.setHeader("Authorization", "Bearer " + supabaseAnonKey);
        httpRequest.setContent(requestBody);
        
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    try {
                        String responseBody = httpResponse.getResultAsString();
                        Gdx.app.log(TAG, "Submit response: " + responseBody);
                        RankData[] list = json.fromJson(RankData[].class, responseBody);
                        if (list != null && list.length > 0) {
                            RankData data = list[0];
                            float bestTime = data.best_time != null ? data.best_time : 0f;
                            callback.onSuccess((int) data.rank, (int) data.total_players, bestTime);
                        } else {
                            callback.onError("No rank data received");
                        }
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Failed to parse response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = httpResponse.getResultAsString();
                    Gdx.app.error(TAG, "Submit failed: " + statusCode + " - " + errorBody);
                    callback.onError("Submit failed: " + statusCode);
                }
            }
            
            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
            
            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }
    
    @Override
    public void getRank(String userId, int level, LeaderboardCallback callback) {
        String url = apiUrl + "/rpc/get_user_rank";
        String requestBody = json.toJson(new RankRequest(userId, level));
        
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl(url);
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("apikey", supabaseAnonKey);
        httpRequest.setHeader("Authorization", "Bearer " + supabaseAnonKey);
        httpRequest.setContent(requestBody);
        
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    try {
                        String responseBody = httpResponse.getResultAsString();
                        RankData[] list = json.fromJson(RankData[].class, responseBody);
                        if (list != null && list.length > 0) {
                            RankData data = list[0];
                            float bestTime = data.best_time != null ? data.best_time : 0f;
                            callback.onSuccess((int) data.rank, (int) data.total_players, bestTime);
                        } else {
                            callback.onError("No rank data received");
                        }
                    } catch (Exception e) {
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    callback.onError("GetRank failed: " + statusCode);
                }
            }
            
            @Override
            public void failed(Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
            
            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }
    
    @Override
    public void getBestTime(int level, LeaderboardCallback callback) {
        String url = apiUrl + "/rpc/get_level_best_time";
        String requestBody = json.toJson(new BestTimeRequest(level));
        
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl(url);
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("apikey", supabaseAnonKey);
        httpRequest.setHeader("Authorization", "Bearer " + supabaseAnonKey);
        httpRequest.setContent(requestBody);
        
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    try {
                        String responseBody = httpResponse.getResultAsString();
                        Gdx.app.log(TAG, "GetBestTime response: " + responseBody);
                        BestTimeData[] list = json.fromJson(BestTimeData[].class, responseBody);
                        if (list != null && list.length > 0) {
                            BestTimeData data = list[0];
                            float bestTime = data.best_time != null ? data.best_time : 0f;
                            callback.onSuccess(0, 0, bestTime);
                        } else {
                            callback.onError("No best time data received");
                        }
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Failed to parse response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = httpResponse.getResultAsString();
                    Gdx.app.error(TAG, "GetBestTime failed: " + statusCode + " - " + errorBody);
                    callback.onError("GetBestTime failed: " + statusCode);
                }
            }
            
            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
            
            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }
    
    @Override
    public void getAllLevelsBestTimes(AllLevelsBestTimesCallback callback) {
        String url = apiUrl + "/rpc/get_all_levels_best_times";
        String requestBody = "{}"; // Пустой JSON для POST
        
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl(url);
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("apikey", supabaseAnonKey);
        httpRequest.setHeader("Authorization", "Bearer " + supabaseAnonKey);
        httpRequest.setContent(requestBody);
        
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    try {
                        String responseBody = httpResponse.getResultAsString();
                        Gdx.app.log(TAG, "GetAllLevelsBestTimes response: " + responseBody);
                        AllLevelsBestTimesData[] list = json.fromJson(AllLevelsBestTimesData[].class, responseBody);
                        if (list != null && list.length > 0) {
                            Map<Integer, Float> levelToBestTime = new HashMap<>();
                            Map<Integer, Integer> levelToTotalPlayers = new HashMap<>();
                            for (AllLevelsBestTimesData data : list) {
                                float bestTime = data.best_time != null ? data.best_time : 0f;
                                levelToBestTime.put(data.level_id, bestTime);
                                levelToTotalPlayers.put(data.level_id, (int)data.total_players);
                            }
                            callback.onSuccess(levelToBestTime, levelToTotalPlayers);
                        } else {
                            callback.onError("No best times data received");
                        }
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Failed to parse response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = httpResponse.getResultAsString();
                    Gdx.app.error(TAG, "GetAllLevelsBestTimes failed: " + statusCode + " - " + errorBody);
                    callback.onError("GetAllLevelsBestTimes failed: " + statusCode);
                }
            }
            
            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
            
            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }
    
    @Override
    public void getAllUserRanks(String userId, AllUserRanksCallback callback) {
        String url = apiUrl + "/rpc/get_all_user_ranks_optimized";
        String requestBody = json.toJson(new AllUserRanksRequest(userId));
        
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl(url);
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("apikey", supabaseAnonKey);
        httpRequest.setHeader("Authorization", "Bearer " + supabaseAnonKey);
        httpRequest.setContent(requestBody);
        
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode == 200) {
                    try {
                        String responseBody = httpResponse.getResultAsString();
                        Gdx.app.log(TAG, "GetAllUserRanks response: " + responseBody);
                        AllUserRanksData[] list = json.fromJson(AllUserRanksData[].class, responseBody);
                        if (list != null && list.length > 0) {
                            Map<Integer, Integer> levelToRank = new HashMap<>();
                            Map<Integer, Integer> levelToTotalPlayers = new HashMap<>();
                            Map<Integer, Float> levelToUserBestTime = new HashMap<>();
                            for (AllUserRanksData data : list) {
                                levelToRank.put(data.level_id, (int)data.user_rank);
                                levelToTotalPlayers.put(data.level_id, (int)data.total_players);
                                float bestTime = data.user_best_time != null ? data.user_best_time : 0f;
                                levelToUserBestTime.put(data.level_id, bestTime);
                            }
                            callback.onSuccess(levelToRank, levelToTotalPlayers, levelToUserBestTime);
                        } else {
                            callback.onError("No user ranks data received");
                        }
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Failed to parse response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = httpResponse.getResultAsString();
                    Gdx.app.error(TAG, "GetAllUserRanks failed: " + statusCode + " - " + errorBody);
                    callback.onError("GetAllUserRanks failed: " + statusCode);
                }
            }
            
            @Override
            public void failed(Throwable t) {
                Gdx.app.error(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
            
            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }
    
    @Override
    public void submitResultImmediately(String userId, int level, float time, ImmediateSubmitCallback callback) {
        Gdx.app.log(TAG, "Submitting result immediately - User: " + userId + ", Level: " + level + ", Time: " + time);
        
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
    
    // Классы для запросов и ответов
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
    
    private static class RankData {
        public long rank;
        public long total_players;
        public Float best_time;
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
        public Float best_time;
        public long total_players;
        public int level_id;
    }
    
    private static class AllLevelsBestTimesData {
        public Float best_time;
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
        public Float user_best_time;
    }
}
