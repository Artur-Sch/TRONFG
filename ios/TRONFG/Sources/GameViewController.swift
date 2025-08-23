import UIKit
import SpriteKit

final class GameViewController: UIViewController {
	override func viewDidLoad() {
		super.viewDidLoad()
		let skView = SKView(frame: view.bounds)
		skView.ignoresSiblingOrder = true
		skView.preferredFramesPerSecond = 60
		view.addSubview(skView)

		let scene = IntroScene(size: view.bounds.size)
		scene.scaleMode = .aspectFill
		skView.presentScene(scene)
	}

	override var prefersStatusBarHidden: Bool { true }
}