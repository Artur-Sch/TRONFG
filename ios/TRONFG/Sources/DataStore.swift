import Foundation

final class DataStore {
	static let shared = DataStore()
	private let defaults = UserDefaults.standard
	private let progressKey = "progress"
	private let timesKeyPrefix = "level_time_"
	private init() {}

	var progress: Int {
		get { max(1, defaults.integer(forKey: progressKey)) }
		set { defaults.set(newValue, forKey: progressKey) }
	}

	func saveLevelTime(levelId: Int, time: Float) {
		let key = timesKeyPrefix + String(levelId)
		let current = defaults.float(forKey: key)
		if current == 0 || time < current {
			defaults.set(time, forKey: key)
		}
	}

	func getLevelTime(levelId: Int) -> Float? {
		let key = timesKeyPrefix + String(levelId)
		let value = defaults.float(forKey: key)
		return value == 0 ? nil : value
	}
}