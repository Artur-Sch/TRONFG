import SpriteKit

final class LevelListScene: SKScene {
	private let columns = 4
	private let totalLevels = 16
	private let spacing: CGFloat = 16
	private let buttonSize = CGSize(width: 120, height: 60)

	override func didMove(to view: SKView) {
		backgroundColor = .black
		layoutGrid()
	}

	private func layoutGrid() {
		let startX = (size.width - (CGFloat(columns) * buttonSize.width + CGFloat(columns - 1) * spacing)) / 2
		let startY = size.height * 0.65
		for level in 1...totalLevels {
			let col = (level - 1) % columns
			let row = (level - 1) / columns
			let x = startX + CGFloat(col) * (buttonSize.width + spacing) + buttonSize.width / 2
			let y = startY - CGFloat(row) * (buttonSize.height + spacing)
			let node = makeLevelNode(level)
			node.position = CGPoint(x: x, y: y)
			addChild(node)
		}
	}

	private func makeLevelNode(_ level: Int) -> SKNode {
		let container = SKNode()
		container.name = "level_\(level)"
		let bg = SKShapeNode(rectOf: buttonSize, cornerRadius: 10)
		bg.fillColor = .darkGray
		bg.strokeColor = .white
		bg.lineWidth = 2
		bg.name = container.name
		let label = SKLabelNode(fontNamed: "Tr2n")
		label.text = "LEVEL \(level)"
		label.fontSize = 22
		label.verticalAlignmentMode = .center
		label.name = container.name
		container.addChild(bg)
		container.addChild(label)
		return container
	}

	override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
		guard let touch = touches.first else { return }
		let location = touch.location(in: self)
		if let name = nodes(at: location).first?.name, name.hasPrefix("level_") {
			let parts = name.split(separator: "_")
			if let id = Int(parts.last ?? "1") {
				let scene = LevelScene(size: size, levelId: id)
				scene.scaleMode = scaleMode
				view?.presentScene(scene, transition: .moveIn(with: .right, duration: 0.25))
			}
		}
	}
}