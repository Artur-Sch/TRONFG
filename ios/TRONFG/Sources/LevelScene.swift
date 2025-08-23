import SpriteKit

final class LevelScene: SKScene {
	private let levelId: Int
	private var levelTimer: TimeInterval = 0
	private var timerLabel = SKLabelNode(fontNamed: "Tr2n")
	private var lastUpdate: TimeInterval = 0

	init(size: CGSize, levelId: Int) {
		self.levelId = levelId
		super.init(size: size)
	}
	required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

	override func didMove(to view: SKView) {
		backgroundColor = .black
		let label = SKLabelNode(fontNamed: "Tr2n")
		label.text = "LEVEL \(levelId)"
		label.fontSize = 36
		label.position = CGPoint(x: size.width/2, y: size.height*0.78)
		addChild(label)

		timerLabel.fontSize = 22
		timerLabel.position = CGPoint(x: size.width/2, y: size.height*0.72)
		addChild(timerLabel)

		let complete = SKLabelNode(fontNamed: "Tr2n")
		complete.text = "COMPLETE"
		complete.fontSize = 26
		complete.name = "complete"
		complete.position = CGPoint(x: size.width/2, y: 60)
		addChild(complete)

		let quit = SKLabelNode(fontNamed: "Tr2n")
		quit.text = "QUIT"
		quit.fontSize = 22
		quit.name = "quit"
		quit.position = CGPoint(x: size.width/2, y: 30)
		addChild(quit)

		MediaManager.shared.stopMusic(nil)
		MediaManager.shared.playMusic(name: randomGameMusicName(), loop: true)
	}

	private func randomGameMusicName() -> String { "new_music\(Int.random(in: 1...3)).ogg" }

	override func update(_ currentTime: TimeInterval) {
		if lastUpdate == 0 { lastUpdate = currentTime }
		let delta = currentTime - lastUpdate
		lastUpdate = currentTime
		levelTimer += delta
		timerLabel.text = String(format: "%.2fs", levelTimer)
	}

	override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
		guard let touch = touches.first else { return }
		let location = touch.location(in: self)
		let nodes = nodes(at: location)
		if nodes.contains(where: { $0.name == "complete" }) {
			DataStore.shared.saveLevelTime(levelId: levelId, time: Float(levelTimer))
			if levelId >= 16 {
				let scene = GameCompletedScene(size: size)
				scene.scaleMode = scaleMode
				view?.presentScene(scene, transition: .flipHorizontal(withDuration: 0.35))
			} else {
				let next = LevelScene(size: size, levelId: levelId + 1)
				next.scaleMode = scaleMode
				view?.presentScene(next, transition: .moveIn(with: .right, duration: 0.25))
			}
		} else if nodes.contains(where: { $0.name == "quit" }) {
			let list = LevelListScene(size: size)
			list.scaleMode = scaleMode
			view?.presentScene(list, transition: .moveIn(with: .left, duration: 0.25))
		}
	}
}