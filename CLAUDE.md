# EhFacturas! Android — Guía para Claude

## RESTRICCIONES
- **NO hacer `git push`** hasta que el usuario lo diga explícitamente.
- Solo commit local permitido, pero **NUNCA push sin permiso**.

## Proyecto
Versión Android nativa de EhFacturas! — app de facturación voice-first para autónomos y pequeñas empresas en España.
Paridad total de features con la versión iOS.

## Stack
- Kotlin 2.0 + Jetpack Compose + Material 3
- Room Database (SQLite)
- MediaPipe LLM Inference + Gemma 2B (IA on-device, gratis, sin límites)
- Claude API + OpenAI API (cloud fallback)
- Android SpeechRecognizer (voz → texto)
- Android TextToSpeech (texto → voz)
- Android PdfDocument + Canvas (PDF A4)
- ZXing (QR) + ML Kit (OCR)
- OkHttp (SOAP AEAT + API calls)
- Google Play Billing (suscripciones)
- Firebase Firestore (sync entre dispositivos)
- WorkManager (background tasks)
- CameraX + ML Kit (escáner)
- Glance (widget)
- Hilt (dependency injection)

## Arquitectura
- MVVM + Clean Architecture
- data/ → Room entities, DAOs, repositories
- domain/ → actions, verifactu, importador, pdf
- ai/ → AIProvider interface, Gemma, Claude, OpenAI
- ui/ → Compose screens (main, bandeja, factura, importador, scanner)
- service/ → WorkManager workers, offline queue
- di/ → Hilt modules

## Versión iOS de referencia
- Proyecto iOS en: `/Users/xaron/Desktop/EhFacturas!/FacturaApp/`
- Plan Android: `/Users/xaron/Desktop/EhFacturas!/docs/android/ANDROID_PLAN.md`
- Reutilizable: system prompts, tool schemas JSON, VeriFactu logic, CSV sinónimos, traducciones

## Reglas de código
- Min SDK: API 26 (Android 8.0)
- Kotlin con coroutines (no callbacks)
- Compose sin XML layouts
- Room con KSP (no KAPT)
- Hilt para DI
- StateFlow/SharedFlow para estado reactivo
- Idioma de la app y comentarios: español
- Traducciones: es, en, ca, eu, gl

## Fases de implementación
1. Base (Room + entidades + navegación + theme)
2. CRUD (clientes, artículos, facturas, gastos)
3. IA + Voz (MediaPipe Gemma + Speech + TTS)
4. PDF + VeriFactu
5. Features avanzadas (importador, OCR, fotos, firma)
6. Infra (Firebase, Billing, Widget, notificaciones)

## Errores a evitar (aprendidos en iOS)
- Los enums en Room deben usar TypeConverters
- Siempre guardar después de insert/update
- No hacer trabajo pesado en el main thread (usar coroutines + Dispatchers.IO)
- El modelo Gemma 2B necesita ~1.5GB — descargar al primer uso, no al instalar
- Las traducciones van en res/values-XX/strings.xml
- Los permisos de cámara/micrófono se piden al usar, no al instalar
