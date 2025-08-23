import SpriteKit

enum ScoreSource { case intro, list }

final class ScoreScene: SKScene {
	private let source: ScoreSource
	init(size: CGSize, source: ScoreSource) {
		self.source = source
		super.init(size: size)
	}
	required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

	override func didMove(to view: SKView) {
		backgroundColor = .black
		let title = SKLabelNode(fontNamed: "Tr2n")
		title.text = "SCORE"
		title.fontSize = 36
		title.position = CGPoint(x: size.width/2, y: size.height*0.8)
		addChild(title)

		let startY = size.height*0.65
		for level in 1...16 {
			let label = SKLabelNode(fontNamed: "Tr2n")
			let time = DataStore.shared.getLevelTime(levelId: level)
			label.text = String(format: "Level %02d: %@", level, time.map { String(format: "%.2fs", $0) } ?? "--")
			label.fontSize = 18
			label.horizontalAlignmentMode = .center
			label.position = CGPoint(x: size.width/2, y: startY - CGFloat(level-1)*22)
			addChild(label)
		}

		let back = SKLabelNode(fontNamed: "Tr2n")
		back.text = "BACK"
		back.fontSize = 26
		back.name = "back"
		back.position = CGPoint(x: size.width/2, y: 40)
		addChild(back)
	}

	override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
		guard let touch = touches.first else { return }
		let location = touch.location(in: self)
		if nodes(at: location).contains(where: { $0.name == "back" }) {
			switch source {
			case .intro:
				let scene = IntroScene(size: size)
				scene.scaleMode = scaleMode
				view?.presentScene(scene, transition: .moveIn(with: .down, duration: 0.25))
			case .list:
				let scene = LevelListScene(size: size)
				scene.scaleMode = scaleMode
				view?.presentScene(scene, transition: .moveIn(with: .down, duration: 0.25))
			}
		}
	}
}