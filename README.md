# TRONFG Game

## Настройка проекта

### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd TRONFG
```

### 2. Настройка Supabase
Для работы с лидербордом необходимо настроить Supabase:

1. Скопируйте файл `local.properties.example` в `local.properties`
2. Заполните в `local.properties` ваши Supabase данные:
   ```properties
   supabase.url=https://your-project-id.supabase.co
   supabase.anon.key=your-anon-key-here
   ```

### 3. Сборка проекта
```bash
./gradlew assembleDebug
```

## Структура проекта
- `core/` - основная логика игры
- `android/` - Android-специфичный код
- `desktop/` - Desktop-версия

## Безопасность
⚠️ **ВАЖНО**: Никогда не коммитьте `local.properties` в репозиторий!
Файл уже добавлен в `.gitignore` для вашей безопасности.

## iOS (Swift, SpriteKit) port

A minimal SpriteKit-based iOS version is added on branch `ios-swift-port` in `ios/TRONFG`.

- Project files are defined via XcodeGen YAML: `ios/TRONFG/Configs/project.yml`.
- Source code lives in `ios/TRONFG/Sources`.
- Resources (images, fonts, audio) are in `ios/TRONFG/Resources`.

Build steps:
1. Install XcodeGen (if needed): `brew install xcodegen`
2. Generate Xcode project:
   - `cd ios/TRONFG`
   - `xcodegen generate --spec Configs/project.yml`
3. Open `TRONFG.xcodeproj` in Xcode and run on a device/simulator.

Notes:
- iOS does not natively play `.ogg`. Convert audio to `.m4a`/`.mp3` and update names in `MediaManager` and scenes if needed.
- This port is a simplified gameplay shell (Intro, Level List, Level placeholder, Score, Completed) to mirror app flow. Physics/TMX logic from libGDX is not yet implemented.
- Bundle identifier is `ru.schneider-dev.tronfg`. Set your Development Team in the project settings before building on device.
