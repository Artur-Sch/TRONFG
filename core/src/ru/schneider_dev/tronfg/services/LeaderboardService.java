package ru.schneider_dev.tronfg.services;

public interface LeaderboardService {
	void submitResult(String userId, int level, float time, LeaderboardCallback callback);
	void getRank(String userId, int level, LeaderboardCallback callback);
	
	interface LeaderboardCallback {
		void onSuccess(int rank, int totalPlayers, float bestTime);
		void onError(String error);
	}
}
