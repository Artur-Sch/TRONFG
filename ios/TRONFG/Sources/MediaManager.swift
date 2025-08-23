import Foundation
import AVFoundation

final class MediaManager {
	static let shared = MediaManager()
	private var musicPlayer: AVAudioPlayer?
	private var soundPlayer: AVAudioPlayer?
	private init() {}

	func playMusic(name: String, loop: Bool) {
		guard let url = Bundle.main.url(forResource: name, withExtension: nil) else {
			// iOS does not support OGG by default. Convert to m4a/mp3 and adjust names.
			return
		}
		do {
			musicPlayer = try AVAudioPlayer(contentsOf: url)
			musicPlayer?.numberOfLoops = loop ? -1 : 0
			musicPlayer?.prepareToPlay()
			musicPlayer?.play()
		} catch {
			print("Failed to play music: \(error)")
		}
	}

	func stopMusic(_ name: String? = nil) {
		musicPlayer?.stop()
	}

	func playSound(name: String) {
		guard let url = Bundle.main.url(forResource: name, withExtension: nil) else { return }
		do {
			soundPlayer = try AVAudioPlayer(contentsOf: url)
			soundPlayer?.prepareToPlay()
			soundPlayer?.play()
		} catch {
			print("Failed to play sound: \(error)")
		}
	}
}