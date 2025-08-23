import SpriteKit

final class GameCompletedScene: SKScene {
	override func didMove(to view: SKView) {
		backgroundColor = .black
		let label = SKLabelNode(fontNamed: "Tr2n")
		label.text = "GAME COMPLETED!"
		label.fontSize = 40
		label.position = CGPoint(x: size.width/2, y: size.height/2)
		addChild(label)

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
			let scene = IntroScene(size: size)
			scene.scaleMode = scaleMode
			view?.presentScene(scene, transition: .moveIn(with: .down, duration: 0.25))
		}
	}
}