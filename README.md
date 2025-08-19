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
