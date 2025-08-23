import SpriteKit

final class IntroScene: SKScene {
	private let titleLabel = SKLabelNode(fontNamed: "Tr2n")
	private let playLabel = SKLabelNode(fontNamed: "Tr2n")
	private let scoreLabel = SKLabelNode(fontNamed: "Tr2n")

	override func didMove(to view: SKView) {
		backgroundColor = .black
		setupUI()
		MediaManager.shared.playMusic(name: "new_menu.ogg", loop: true)
	}

	private func setupUI() {
		titleLabel.text = "TRON FG"
		titleLabel.fontSize = 48
		titleLabel.position = CGPoint(x: size.width/2, y: size.height*0.7)
		addChild(titleLabel)

		playLabel.text = "PLAY"
		playLabel.fontSize = 40
		playLabel.position = CGPoint(x: size.width/2, y: size.height*0.45)
		addChild(playLabel)

		scoreLabel.text = "SCORE"
		scoreLabel.fontSize = 32
		scoreLabel.position = CGPoint(x: size.width/2, y: 40)
		addChild(scoreLabel)
	}

	override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
		guard let touch = touches.first else { return }
		let location = touch.location(in: self)
		let nodes = nodes(at: location)
		if nodes.contains(playLabel) {
			MediaManager.shared.playSound(name: "new_click.ogg")
			let next = LevelListScene(size: size)
			next.scaleMode = scaleMode
			view?.presentScene(next, transition: .moveIn(with: .up, duration: 0.3))
		} else if nodes.contains(scoreLabel) {
			MediaManager.shared.playSound(name: "new_click.ogg")
			let next = ScoreScene(size: size, source: .intro)
			next.scaleMode = scaleMode
			view?.presentScene(next, transition: .moveIn(with: .up, duration: 0.3))
		}
	}
}