# MyAndroidApp

Минимальный шаблон Android-приложения на Kotlin с автоматической сборкой APK через GitHub Actions.

## Что внутри

- `app/` — модуль приложения (Kotlin, ViewBinding, Material 3)
- `MainActivity.kt` — простой счётчик (демо-экран)
- `.github/workflows/build-apk.yml` — сборка debug APK в CI

## Как получить APK

1. Запушьте изменения в `main` (или запустите workflow вручную: **Actions → Build APK → Run workflow**).
2. Дождитесь завершения job **Assemble debug APK**.
3. Скачайте артефакт `app-debug-apk` со страницы запуска workflow — внутри `app-debug.apk`.

Чтобы получить APK в **Releases**, создайте тег:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Workflow соберёт APK и прикрепит его к релизу.

## Локальная сборка

```bash
# при необходимости сгенерировать Gradle wrapper
gradle wrapper --gradle-version 8.7

./gradlew :app:assembleDebug
```

APK появится в `app/build/outputs/apk/debug/app-debug.apk`.

Требования: JDK 17, Android SDK (compileSdk 34).

## Настройки

- `applicationId`: `com.example.myandroidapp`
- `minSdk`: 24, `targetSdk` / `compileSdk`: 34
- AGP 8.5.2, Kotlin 1.9.24, Gradle 8.7

## Подпись release-сборки

Debug APK подписывается автоматически и устанавливается на устройство.
Для публикации в Google Play нужен собственный keystore: добавьте его как base64 в
GitHub Secrets и настройте `signingConfigs` в `app/build.gradle.kts`.
