# Plan d'implémentation — AssemblageScreen Magic Leap 2
## Fond transparent & Scan QR codes d'orientation

> **Contexte du code existant :** `AssemblageScreen` utilise SceneView (Filament) pour piloter
> un modèle `.glb` avec des entités nommées (`fond`, `gauche`, `droite`, `haut`, `bas`,
> `bitoniau_5…12`). Les animations sont manuelles (transforms sur `onFrame`). Le fond est
> actuellement opaque (`Color.Black`). La cible est Magic Leap 2 (LuminOS 2 / Android 10 AOSP).

---

## Table des matières

1. [Architecture cible](#1-architecture-cible)
2. [Fond transparent — Passthrough AR](#2-fond-transparent--passthrough-ar)
3. [Scan QR codes — Vérification d'orientation](#3-scan-qr-codes--vérification-dorientation)
4. [Intégration dans AssemblageScreen](#4-intégration-dans-assemblagescreen)
5. [Flux utilisateur complet](#5-flux-utilisateur-complet)
6. [Dépendances Gradle](#6-dépendances-gradle)
7. [Points d'attention Magic Leap 2](#7-points-dattention-magic-leap-2)

---

## 1. Architecture cible

```
AssemblageScreen
│
├── [Couche 0] Passthrough réel (monde physique visible à travers les lentilles)
├── [Couche 1] SceneView / Filament — modèle 3D avec fond transparent
│                └─ fond clair = 0x00000000 (alpha = 0)
├── [Couche 2] QrScanOverlay (Composable)
│                ├── CameraX ImageAnalysis (traitement sans preview affiché)
│                └── Résultat : badge ✅/❌ ancré à la pièce concernée
└── [Couche 3] UI Compose — header, barre de progression, description, boutons
```

**Principe Magic Leap 2 :** le waveguide additionne la lumière projetée par l'app sur la
scène réelle. Un pixel totalement noir (alpha = 0 en mode transparent) est invisible —
l'utilisateur voit le monde physique à travers. Les pixels colorés (3D, UI) apparaissent
en surimpression. Il n'y a donc **pas** besoin d'afficher un flux caméra ; l'affichage
physique est le passthrough natif.

---

## 2. Fond transparent — Passthrough AR

### 2.1 Principe de la chaîne de transparence

Pour qu'un pixel soit transparent sur Magic Leap 2, **trois couches** doivent toutes
avoir alpha = 0 :

```
AndroidManifest (style)
    └── Window flags (Activity)
          └── Filament Renderer clearOptions (alpha = 0f)
                └── Compose background = Color.Transparent
```

Si l'une des couches est opaque, le fond sera noir.

---

### 2.2 `AndroidManifest.xml` — Style de l'Activity

Créer (ou modifier) `res/values/themes.xml` :

```xml
<!-- res/values/themes.xml -->
<resources>
    <!-- Thème de base de l'app (écrans HOME, NOTICE, etc.) -->
    <style name="Theme.PMR3D" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="android:windowBackground">@color/dark_bg</item>
    </style>

    <!-- Thème AR transparent — uniquement pour AssemblageActivity si on la sépare,
         ou appliqué dynamiquement via window flags dans MainActivity -->
    <style name="Theme.PMR3D.AR" parent="Theme.PMR3D">
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:backgroundDimEnabled">false</item>
        <item name="android:windowTranslucentNavigation">true</item>
        <item name="android:windowTranslucentStatus">true</item>
    </style>
</resources>
```

**Option A — Activity unique (recommandé pour rester proche du code existant) :**
Appliquer les flags window dynamiquement selon l'écran actif.

**Option B — Activity dédiée** pour `AssemblageScreen` avec `Theme.PMR3D.AR` dans le
manifest. Plus propre mais implique une navigation Activity-to-Activity.

---

### 2.3 `MainActivity.kt` — Window flags dynamiques

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pmr_3dTheme {
                val currentScreen = remember { mutableStateOf(Screen.HOME) }

                // Active/désactive la transparence selon l'écran
                LaunchedEffect(currentScreen.value) {
                    if (currentScreen.value == Screen.ASSEMBLAGE) {
                        enableArTransparency()
                    } else {
                        disableArTransparency()
                    }
                }

                AppRoot(currentScreen)
            }
        }
    }

    private fun enableArTransparency() {
        window.apply {
            // Fond de la window en transparent
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            // Conserver le canal alpha dans le rendu
            addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            // Magic Leap 2 : désactive le dimmer segmenté (rendu voir-à-travers)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            // Attributs de format pour alpha
            attributes = attributes.also { attrs ->
                attrs.format = PixelFormat.TRANSLUCENT
            }
        }
    }

    private fun disableArTransparency() {
        window.apply {
            setBackgroundDrawableResource(android.R.color.black)
            attributes = attributes.also { attrs ->
                attrs.format = PixelFormat.OPAQUE
            }
        }
    }
}
```

---

### 2.4 Configuration Filament — Renderer transparent

C'est le point **le plus critique** : si Filament efface le framebuffer avec une couleur
opaque, rien de ce qui précède ne fonctionnera.

```kotlin
// À appeler une fois, après que rememberEngine() a fourni l'engine
LaunchedEffect(engine) {
    engine.renderer.clearOptions = Renderer.ClearOptions.Builder()
        .clearColor(0f, 0f, 0f, 0f)   // RGBA : noir pur, alpha = 0
        .clear(true)
        .discard(true)
        .build()
}
```

**Note :** `Renderer.ClearOptions` est exposé dans Filament Android via
`com.google.android.filament.Renderer`. Vérifier la version de Filament incluse dans
SceneView pour la disponibilité du Builder.

---

### 2.5 Modifications dans `AssemblageScreen`

```kotlin
// AVANT
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)  // ← supprimer
) { ... }

// APRÈS
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent)  // fond Compose transparent
) {
    Scene(
        modifier = Modifier.fillMaxSize(),
        // ... paramètres existants ...
    )

    // Les overlays UI gardent leur propre fond semi-transparent
    // (ex. Color(0xDD0F1117) ou Color(0xCC0F1117)) pour rester lisibles
    // sur le monde réel.
}
```

Les panneaux UI existants (header `0xDD0F1117`, description `0xCC0F1117`, boutons
`0xFF1A1D2E`) fonctionnent bien en AR : ils ont déjà un fond semi-opaque qui les rend
lisibles quelle que soit la scène derrière.

---

### 2.6 Spécificité Magic Leap 2 — MLSegmentedDimmer (optionnel, v2)

Pour un contrôle précis de l'opacité par zone (ex. rendre le fond de la description
plus sombre pour la lisibilité sans impacter le reste), Magic Leap 2 expose
`MLSegmentedDimmer` via le SDK :

```kotlin
// Dépendance SDK ML2 (disponible dans le repo Maven de Magic Leap)
// com.magicleap:sdk-android:X.Y.Z

import com.magicleap.sdk.MLSegmentedDimmer

// Dans AssemblageScreen
val dimmerEnabled = remember { mutableStateOf(false) }

DisposableEffect(Unit) {
    MLSegmentedDimmer.addView(localView, 1.0f)  // force le dimmer sur une View spécifique
    onDispose { MLSegmentedDimmer.removeView(localView) }
}
```

**Recommandation :** implémenter d'abord sans `MLSegmentedDimmer` (window flags + Filament
suffisent pour la transparence de base). Ajouter le dimmer en itération v2 si la lisibilité
des panneaux UI est insuffisante sur fond lumineux.

---

## 3. Scan QR codes — Vérification d'orientation

### 3.1 Philosophie du système

Chaque pièce du meuble porte **un QR code par face significative**. Lorsqu'une pièce
est dans le bon sens, c'est la face "attendue" qui se présente naturellement vers
l'utilisateur (et donc vers la caméra Magic Leap). Inverser la pièce expose une face
différente → QR code différent → l'app détecte l'erreur.

```
Pièce physique : "fond"
  ├── Face avant  → QR code : {"piece":"fond","face":"avant","v":1}
  ├── Face arrière→ QR code : {"piece":"fond","face":"arriere","v":1}
  └── Face haut   → QR code : {"piece":"fond","face":"haut","v":1}
```

---

### 3.2 Modèle de données QR

#### Payload encodé dans chaque QR code

```kotlin
// QrPayload.kt
import kotlinx.serialization.Serializable

@Serializable
data class QrPayload(
    val piece: String,    // "fond" | "gauche" | "droite" | "haut" | "bas" | "bitoniau_N"
    val face: String,     // "avant" | "arriere" | "haut" | "bas" | "gauche" | "droite"
    val v: Int = 1        // version du schema (pour évolution future)
)
```

Exemple de contenu JSON d'un QR code :
```json
{"piece":"fond","face":"avant","v":1}
```

---

### 3.3 Table des orientations attendues

```kotlin
// AssemblyOrientations.kt

data class OrientationCheck(
    val pieceId: String,
    val faceAttendue: String,
    val stepId: Int,
    val labelErreur: String  // message affiché si mauvaise orientation
)

val ORIENTATION_CHECKS = listOf(
    // Étape 1 : panneaux latéraux + fond
    OrientationCheck("fond",   "avant",  stepId = 1,
        labelErreur = "Retournez le fond : la face gravée doit être visible"),
    OrientationCheck("gauche", "interieure", stepId = 1,
        labelErreur = "Panneau gauche à l'envers : face intérieure vers le centre"),
    OrientationCheck("droite", "interieure", stepId = 1,
        labelErreur = "Panneau droit à l'envers : face intérieure vers le centre"),

    // Étape 2 : haut, bas et chevilles
    OrientationCheck("haut",  "bas",   stepId = 2,
        labelErreur = "Panneau du haut à l'envers : face inférieure vers le bas"),
    OrientationCheck("bas",   "haut",  stepId = 2,
        labelErreur = "Panneau du bas à l'envers : face supérieure vers le haut"),
    OrientationCheck("bitoniau_5",  "tete", stepId = 2,
        labelErreur = "Cheville 5 dans le mauvais sens"),
    OrientationCheck("bitoniau_6",  "tete", stepId = 2,
        labelErreur = "Cheville 6 dans le mauvais sens"),
    // … idem pour bitoniau_7 à bitoniau_12
)

fun getOrientationCheck(pieceId: String, stepId: Int): OrientationCheck? =
    ORIENTATION_CHECKS.find { it.pieceId == pieceId && it.stepId == stepId }
```

---

### 3.4 Architecture — State & ViewModel

```kotlin
// QrScanState.kt

sealed class ScanResult {
    object Idle : ScanResult()
    object Scanning : ScanResult()
    data class Correct(val pieceId: String, val face: String) : ScanResult()
    data class Incorrect(
        val pieceId: String,
        val faceLue: String,
        val faceAttendue: String,
        val messageErreur: String
    ) : ScanResult()
    data class UnknownPiece(val rawValue: String) : ScanResult()
    data class ParseError(val rawValue: String) : ScanResult()
}

data class QrScanState(
    val isActive: Boolean = false,
    val lastResult: ScanResult = ScanResult.Idle,
    val currentStepId: Int = 1
)
```

```kotlin
// QrScanViewModel.kt
class QrScanViewModel : ViewModel() {
    private val _state = MutableStateFlow(QrScanState())
    val state: StateFlow<QrScanState> = _state.asStateFlow()

    fun activateScanner(stepId: Int) {
        _state.update { it.copy(isActive = true, currentStepId = stepId,
                                lastResult = ScanResult.Scanning) }
    }

    fun deactivateScanner() {
        _state.update { it.copy(isActive = false, lastResult = ScanResult.Idle) }
    }

    fun onBarcodeDetected(rawValue: String) {
        val payload = try {
            Json.decodeFromString<QrPayload>(rawValue)
        } catch (e: Exception) {
            _state.update { it.copy(lastResult = ScanResult.ParseError(rawValue)) }
            return
        }

        val check = getOrientationCheck(payload.piece, _state.value.currentStepId)
        if (check == null) {
            _state.update { it.copy(lastResult = ScanResult.UnknownPiece(rawValue)) }
            return
        }

        val result = if (payload.face == check.faceAttendue) {
            ScanResult.Correct(payload.piece, payload.face)
        } else {
            ScanResult.Incorrect(
                pieceId = payload.piece,
                faceLue = payload.face,
                faceAttendue = check.faceAttendue,
                messageErreur = check.labelErreur
            )
        }
        _state.update { it.copy(lastResult = result) }

        // Auto-désactivation après résultat (délai 3s)
        viewModelScope.launch {
            delay(3_000)
            deactivateScanner()
        }
    }
}
```

---

### 3.5 CameraX — Analyse sans preview

Sur Magic Leap 2, l'utilisateur **voit le monde réel directement** à travers les lentilles.
Le flux caméra n'a pas besoin d'être affiché — seul l'`ImageAnalysis` est nécessaire.

```kotlin
// QrAnalyzer.kt
class QrAnalyzer(
    private val onResult: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { onResult(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
```

```kotlin
// QrScanManager.kt  — encapsule CameraX pour usage en Composable
class QrScanManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onBarcodeDetected: (String) -> Unit
) {
    private var cameraProvider: ProcessCameraProvider? = null

    fun startScanning() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Caméra orientée vers le monde (BACK = caméra RGB world-facing sur ML2)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(Executors.newSingleThreadExecutor(),
                                       QrAnalyzer(onBarcodeDetected)) }

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis           // pas de Preview use-case : pas d'affichage
            )
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopScanning() {
        cameraProvider?.unbindAll()
    }
}
```

---

### 3.6 Composable `QrScanOverlay`

Ce composable gère l'activation du scanner et affiche le feedback visuel :

```kotlin
@Composable
fun QrScanOverlay(
    viewModel: QrScanViewModel,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    // Créer le manager une seule fois
    val scanManager = remember {
        QrScanManager(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onBarcodeDetected = viewModel::onBarcodeDetected
        )
    }

    // Démarrer/arrêter le scanner selon l'état
    LaunchedEffect(state.isActive) {
        if (state.isActive) scanManager.startScanning()
        else scanManager.stopScanning()
    }

    // Nettoyer à la destruction
    DisposableEffect(Unit) {
        onDispose { scanManager.stopScanning() }
    }

    Box(modifier = modifier) {
        // Bouton d'activation
        if (!state.isActive) {
            FloatingActionButton(
                onClick = { viewModel.activateScanner(currentStepIndex + 1) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 96.dp),
                containerColor = Color(0xFF252840)
            ) {
                Text("📷", fontSize = 22.sp)
            }
        }

        // Feedback du résultat de scan
        AnimatedVisibility(
            visible = state.lastResult !is ScanResult.Idle,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit  = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            ScanResultCard(result = state.lastResult)
        }

        // Indicateur "scan actif" (cercle pulsant)
        if (state.isActive && state.lastResult is ScanResult.Scanning) {
            ScanningIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ScanResultCard(result: ScanResult) {
    val (icon, title, subtitle, color) = when (result) {
        is ScanResult.Correct -> Quadruple(
            "✅", "Pièce correctement orientée",
            "${result.pieceId} — face ${result.face}", Color(0xFF22C55E)
        )
        is ScanResult.Incorrect -> Quadruple(
            "❌", "Mauvaise orientation",
            result.messageErreur, Color(0xFFEF4444)
        )
        is ScanResult.UnknownPiece -> Quadruple(
            "⚠️", "Pièce non reconnue",
            "Ce QR code n'appartient pas à cette étape", Color(0xFFF59E0B)
        )
        else -> Quadruple("🔍", "Scan en cours…", "Pointez vers un QR code", Color(0xFF4A9EFF))
    }

    Column(
        modifier = Modifier
            .background(Color(0xF00F1117), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 40.sp)
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
             textAlign = TextAlign.Center)
        Text(subtitle, color = Color(0xFF8A94A6), fontSize = 13.sp,
             textAlign = TextAlign.Center, lineHeight = 18.sp)
    }
}

@Composable
fun ScanningIndicator(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .size(80.dp)
            .border(2.dp, Color(0xFF4A9EFF).copy(alpha = alpha), RoundedCornerShape(12.dp))
    )
}
```

---

## 4. Intégration dans `AssemblageScreen`

### 4.1 Modifications de la signature

```kotlin
@Composable
fun AssemblageScreen(
    onBack: () -> Unit,
    viewModel: QrScanViewModel = viewModel()   // ← ajout
) {
```

### 4.2 Ajout de `QrScanOverlay` dans le `Box` principal

```kotlin
// Dans le Box principal d'AssemblageScreen, ENTRE la Scene et les UI overlays :

Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

    // [1] Scene 3D existante (inchangée, sauf fond transparent)
    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        // ... paramètres existants ...
        onFrame = { /* … code existant … */ }
    )

    // [2] NOUVEAU — Overlay QR scan
    QrScanOverlay(
        viewModel = viewModel,
        currentStepIndex = currentStepIndex.value,
        modifier = Modifier.fillMaxSize()
    )

    // [3] Header existant (inchangé)
    Row(modifier = Modifier.align(Alignment.TopCenter) /* … */) { /* … */ }

    // [4] Barre de progression existante (inchangée)
    Row(modifier = Modifier.align(Alignment.TopCenter).padding(top = 108.dp) /* … */) { /* … */ }

    // [5] Description existante
    // Optionnel : ajouter un indicateur "✅ pièce vérifiée" ici si ScanResult.Correct
    val scanState by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
            .background(Color(0xCC0F1117), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = currentStep.description, color = Color(0xFFE2E8F4),
                 fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
            // Badge de validation QR
            if (scanState.lastResult is ScanResult.Correct) {
                Text("✅", fontSize = 18.sp)
            }
        }
    }

    // [6] Boutons Précédent/Suivant existants (inchangés)
    Row(modifier = Modifier.align(Alignment.BottomCenter) /* … */) { /* … */ }
}
```

### 4.3 Permission caméra

Dans `AndroidManifest.xml` :
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

Demande de permission dans `AssemblageScreen` au montage :
```kotlin
val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
LaunchedEffect(Unit) {
    if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
}
```

---

## 5. Flux utilisateur complet

```
[Utilisateur arrive sur AssemblageScreen]
        │
        ▼
[Fond devient transparent — monde réel visible]
[Animation 3D de l'étape 1 commence]
        │
        ▼
[Utilisateur prend physiquement la pièce "fond"]
        │
        ▼
[Appuie sur 📷 dans le coin bas-droit]
        │
        ▼
[Scanner activé — indicateur pulsant affiché]
[CameraX ImageAnalysis démarre silencieusement]
        │
        ├──[QR détecté — pièce correcte]──► Badge ✅ "fond - face avant" (3s)
        │                                   └► Badge ✅ sur la description de l'étape
        │
        └──[QR détecté — pièce retournée]──► Badge ❌ + message "Retournez le fond"
                                             └► Scanner reste actif pour re-scanner

[Bouton Suivant]──► Étape 2 / animation 2 démarre
                    Badge QR remis à zéro (Idle)
```

---

## 6. Dépendances Gradle

```kotlin
// build.gradle.kts (module :app)

dependencies {
    // Existantes
    implementation("io.github.sceneview:sceneview:2.x.x")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.x")
    implementation("androidx.camera:camera-camera2:1.4.x")
    implementation("androidx.camera:camera-lifecycle:1.4.x")

    // ML Kit — Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.x")

    // Serialisation JSON (QR payload)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.x")

    // Compose permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.x")

    // ViewModel + Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.x")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.x")

    // Magic Leap 2 SDK (via repo Maven ML)
    // implementation("com.magicleap:sdk-android:2.x.x")  // optionnel pour MLSegmentedDimmer
}

// Activer la sérialisation Kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}
```

---

## 7. Points d'attention Magic Leap 2

### 7.1 Caméra world-facing

Magic Leap 2 expose trois caméras. Via CameraX/Camera2, la caméra `DEFAULT_BACK_CAMERA`
correspond à la **caméra RGB centrale** (world-facing, résolution 1280×960). C'est la
bonne pour lire des QR codes sur les pièces posées devant l'utilisateur.

### 7.2 Résolution et distance de scan

Les QR codes doivent être **au minimum 3×3 cm** sur les pièces pour être lisibles à 40-80 cm
de distance (distance de travail typique pour l'assemblage). Prévoir une densité de
modules adaptée lors de la génération (outils : `zxing-android-embedded` ou génération
serveur/offline).

### 7.3 Éclairage et contraste

Le waveguide Magic Leap 2 est additif : les pixels noirs sont transparents. Les textes et
panneaux UI trop sombres peuvent être difficiles à lire en environnement très lumineux.
**Solution :** utiliser des textes clairs sur fond semi-opaque (déjà le cas dans le code,
ex. `Color(0xDD0F1117)`). Pour des environnements extrêmement clairs, augmenter l'opacité
du fond (`0xFF` au lieu de `0xDD`).

### 7.4 Navigation spatiale et boutons

Sur Magic Leap 2, l'interaction peut se faire via le **controller** (touchpad, boutons)
ou le **eye-tracking + hand tracking** selon la version SDK activée. Les boutons Compose
répondent au pointeur du controller ML2 nativement via le driver d'entrée ML Android.
Aucune adaptation spécifique n'est nécessaire pour les `Button` Compose existants.

### 7.5 Performances Filament

Le modèle GLB avec 13 entités animées à la main dans `onFrame` est léger. Conserver
cette approche. Ne pas activer le bloom ou les ombres portées si non nécessaires : le
waveguide a une luminosité limitée et les effets post-processing sont peu perceptibles
en AR additif.

### 7.6 Ordre d'implémentation suggéré

```
Sprint 1 ── Fond transparent
              ├── Window flags dans MainActivity
              ├── Filament clearOptions (alpha = 0)
              └── Tester sur device : fond noir ➜ transparent ✓

Sprint 2 ── QR codes offline
              ├── Générer les QR codes pour chaque pièce/face
              ├── Intégrer CameraX + ML Kit (ImageAnalysis uniquement)
              └── Tester la détection sans UI (logs)

Sprint 3 ── Intégration UI
              ├── QrScanOverlay + ScanResultCard
              ├── QrScanViewModel + state
              └── Bouton 📷 dans AssemblageScreen

Sprint 4 ── Polish
              ├── MLSegmentedDimmer (lisibilité des panneaux)
              ├── Animations de feedback (flash vert/rouge sur l'entité 3D)
              └── Permissions caméra + gestion d'erreurs
```
