package ru.schneider_dev.tronfg.services;

import java.util.Map;

public interface LeaderboardService {
	void submitResult(String userId, int level, float time, LeaderboardCallback callback);
	void getRank(String userId, int level, LeaderboardCallback callback);
	void getBestTime(int level, LeaderboardCallback callback);
	void getAllLevelsBestTimes(AllLevelsBestTimesCallback callback);
	void getAllUserRanks(String userId, AllUserRanksCallback callback);
	void submitResultImmediately(String userId, int level, float time, ImmediateSubmitCallback callback);
	
	interface LeaderboardCallback {
		void onSuccess(int rank, int totalPlayers, float bestTime);
		void onError(String error);
	}
	
	interface AllLevelsBestTimesCallback {
		void onSuccess(Map<Integer, Float> levelToBestTime, Map<Integer, Integer> levelToTotalPlayers);
		void onError(String error);
	}
	
	interface AllUserRanksCallback {
		void onSuccess(Map<Integer, Integer> levelToRank, Map<Integer, Integer> levelToTotalPlayers, Map<Integer, Float> levelToUserBestTime);
		void onError(String error);
	}
	
	interface ImmediateSubmitCallback {
		void onSuccess(int rank, int totalPlayers, float bestTime);
		void onError(String error);
	}
}
