package ec.fr.pmr_3d.ui

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.filament.Renderer
import dev.romainguy.kotlin.math.Float3
import ec.fr.pmr_3d.*
import ec.fr.pmr_3d.models.ASSEMBLY_STEPS
import io.github.sceneview.Scene
import io.github.sceneview.managers.getTransform
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AssemblageScreen(
    onBack: () -> Unit,
    qrViewModel: QrScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isML = remember { isMagicLeap() }
    
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    val currentStepIndex = remember { mutableIntStateOf(0) }
    val currentStep = ASSEMBLY_STEPS[currentStepIndex.intValue]

    val showTerminerDialog = remember { mutableStateOf(false) }

    if (showTerminerDialog.value) {
        AlertDialog(
            onDismissRequest = { showTerminerDialog.value = false },
            title = { Text("Assemblage terminé") },
            text = { Text("Voulez-vous vraiment terminer l'assemblage et revenir à l'accueil ?") },
            confirmButton = {
                TextButton(onClick = {
                    showTerminerDialog.value = false
                    onBack()
                }) {
                    Text("Confirmer", color = Color(0xFF4A9EFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminerDialog.value = false }) {
                    Text("Annuler", color = Color(0xFF8A94A6))
                }
            },
            containerColor = Color(0xFF1A1D2E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F4)
        )
    }

    val currentAnimation = remember { mutableStateOf("animation1") }
    val pendingAnimation = remember { mutableStateOf<String?>(null) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine).apply { position = Float3(0f, 1f, 4f) }
    val mainLightNode = rememberMainLightNode(engine).apply { intensity = 100_000f }
    val renderer = io.github.sceneview.rememberRenderer(engine)

    // On phone, we need to pass a PreviewView to QrScanManager
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    val scanManager = remember(previewView) {
        QrScanManager(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onBarcodeDetected = qrViewModel::onBarcodeDetected
        )
    }

    // Configure Filament for transparency
    LaunchedEffect(renderer) {
        renderer.clearOptions = Renderer.ClearOptions().apply {
            clearColor = floatArrayOf(0f, 0f, 0f, 0f)
            clear = true
        }
    }

    // Démarrer la caméra immédiatement sur téléphone pour le fond AR
    LaunchedEffect(isML, previewView, cameraPermission.status.isGranted) {
        if (!isML && previewView != null && cameraPermission.status.isGranted) {
            scanManager.startScanning()
        }
    }

    // Entities
    val fondEntity        = remember { mutableStateOf<Int?>(null) }
    val gaucheEntity      = remember { mutableStateOf<Int?>(null) }
    val droiteEntity      = remember { mutableStateOf<Int?>(null) }
    val hautEntity        = remember { mutableStateOf<Int?>(null) }
    val basEntity         = remember { mutableStateOf<Int?>(null) }
    val bitoniau5Entity   = remember { mutableStateOf<Int?>(null) }
    val bitoniau6Entity   = remember { mutableStateOf<Int?>(null) }
    val bitoniau7Entity   = remember { mutableStateOf<Int?>(null) }
    val bitoniau8Entity   = remember { mutableStateOf<Int?>(null) }
    val bitoniau9Entity   = remember { mutableStateOf<Int?>(null) }
    val bitoniau10Entity  = remember { mutableStateOf<Int?>(null) }
    val bitoniau11Entity  = remember { mutableStateOf<Int?>(null) }
    val bitoniau12Entity  = remember { mutableStateOf<Int?>(null) }

    // Transforms originaux
    val fondOriginalTransform       = remember { mutableStateOf<FloatArray?>(null) }
    val gaucheOriginalTransform     = remember { mutableStateOf<FloatArray?>(null) }
    val droiteOriginalTransform     = remember { mutableStateOf<FloatArray?>(null) }
    val hautOriginalTransform       = remember { mutableStateOf<FloatArray?>(null) }
    val basOriginalTransform        = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau5OriginalTransform  = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau6OriginalTransform  = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau7OriginalTransform  = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau8OriginalTransform  = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau9OriginalTransform  = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau10OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau11OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau12OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }

    val startTimeNanos = remember { mutableLongStateOf(0L) }

    val modelNode = remember {
        try {
            val instance = modelLoader.createModelInstance("pmr_assembly.glb")
            ModelNode(modelInstance = instance, scaleToUnits = 0.9f).apply {
                position = Float3(0f, 0.5f, 0f)
            }
        } catch (_: Exception) { null }
    }

    fun extractTransform(tm: com.google.android.filament.TransformManager, entity: Int): FloatArray {
        val tmInstance = tm.getInstance(entity)
        val mat = tm.getTransform(tmInstance)
        return floatArrayOf(
            mat.x.x, mat.x.y, mat.x.z, mat.x.w,
            mat.y.x, mat.y.y, mat.y.z, mat.y.w,
            mat.z.x, mat.z.y, mat.z.z, mat.z.w,
            mat.w.x, mat.w.y, mat.w.z, mat.w.w
        )
    }

    fun setEntityVisible(entity: Int, visible: Boolean) {
        val rm = engine.renderableManager
        if (rm.hasComponent(entity)) {
            val ri = rm.getInstance(entity)
            rm.setLayerMask(ri, 0xFF, if (visible) 0xFF else 0x00)
        }
    }

    // Charge les entités du modèle
    LaunchedEffect(modelNode) {
        modelNode?.let { node ->
            val asset = node.modelInstance.asset
            val tm = engine.transformManager
            listOf(
                "fond", "gauche", "droite", "haut", "bas",
                "bitoniau_5", "bitoniau_6", "bitoniau_7", "bitoniau_8",
                "bitoniau_9", "bitoniau_10", "bitoniau_11", "bitoniau_12"
            ).forEach { name ->
                val entity = asset.getFirstEntityByName(name)
                if (entity != 0) {
                    val transform = extractTransform(tm, entity)
                    when (name) {
                        "fond"         -> { fondEntity.value = entity;        fondOriginalTransform.value = transform }
                        "gauche"       -> { gaucheEntity.value = entity;      gaucheOriginalTransform.value = transform }
                        "droite"       -> { droiteEntity.value = entity;      droiteOriginalTransform.value = transform }
                        "haut"         -> { hautEntity.value = entity;        hautOriginalTransform.value = transform }
                        "bas"          -> { basEntity.value = entity;         basOriginalTransform.value = transform }
                        "bitoniau_5"   -> { bitoniau5Entity.value = entity;   bitoniau5OriginalTransform.value = transform }
                        "bitoniau_6"   -> { bitoniau6Entity.value = entity;   bitoniau6OriginalTransform.value = transform }
                        "bitoniau_7"   -> { bitoniau7Entity.value = entity;   bitoniau7OriginalTransform.value = transform }
                        "bitoniau_8"   -> { bitoniau8Entity.value = entity;   bitoniau8OriginalTransform.value = transform }
                        "bitoniau_9"   -> { bitoniau9Entity.value = entity;   bitoniau9OriginalTransform.value = transform }
                        "bitoniau_10"  -> { bitoniau10Entity.value = entity;  bitoniau10OriginalTransform.value = transform }
                        "bitoniau_11"  -> { bitoniau11Entity.value = entity;  bitoniau11OriginalTransform.value = transform }
                        "bitoniau_12"  -> { bitoniau12Entity.value = entity;  bitoniau12OriginalTransform.value = transform }
                    }
                }
            }
            // Masque tout sauf animation1 au départ et initialise les positions
            hautEntity.value?.let { setEntityVisible(it, false) }
            basEntity.value?.let { setEntityVisible(it, false) }
            listOf(
                bitoniau5Entity, bitoniau6Entity, bitoniau7Entity, bitoniau8Entity,
                bitoniau9Entity, bitoniau10Entity, bitoniau11Entity, bitoniau12Entity
            ).forEach { it.value?.let { e -> setEntityVisible(e, false) } }

            // Force l'initialisation de l'animation 1 pour éviter le saut au démarrage
            val eF = fondEntity.value
            val eG = gaucheEntity.value
            val eD = droiteEntity.value
            val oF = fondOriginalTransform.value
            val oG = gaucheOriginalTransform.value
            val oD = droiteOriginalTransform.value
            if (eF != null && eG != null && eD != null && oF != null && oG != null && oD != null) {
                tm.setTransform(tm.getInstance(eF), oF)
                tm.setTransform(tm.getInstance(eG), oG)
                tm.setTransform(tm.getInstance(eD), oD)
            }
        }
    }

    // Quand l'étape change, déclenche l'animation correspondante
    LaunchedEffect(currentStepIndex.intValue) {
        pendingAnimation.value = currentStep.animationKey
        qrViewModel.deactivateScanner()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Flux caméra en arrière-plan sur téléphone
        if (!isML) {
            CameraPreview(
                onPreviewViewCreated = { previewView = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            renderer = renderer,
            isOpaque = false,
            childNodes = rememberNodes { modelNode?.let { add(it) } },
            onFrame = { frameTimeNanos ->
                if (startTimeNanos.longValue == 0L) startTimeNanos.longValue = frameTimeNanos
                val elapsed = (frameTimeNanos - startTimeNanos.longValue) / 1_000_000_000f
                val t = elapsed * Math.PI.toFloat()
                val factor = (1f - kotlin.math.cos(t)) / 2f
                val cycleProgress = elapsed % 2.0f
                val isEndOfCycle = cycleProgress > 1.97f || cycleProgress < 0.03f

                if (pendingAnimation.value != null && isEndOfCycle) {
                    val next = pendingAnimation.value!!
                    val isAnim2 = next != "animation1"
                    hautEntity.value?.let { setEntityVisible(it, isAnim2) }
                    basEntity.value?.let { setEntityVisible(it, isAnim2) }
                    listOf(
                        bitoniau5Entity, bitoniau6Entity, bitoniau7Entity, bitoniau8Entity,
                        bitoniau9Entity, bitoniau10Entity, bitoniau11Entity, bitoniau12Entity
                    ).forEach { it.value?.let { e -> setEntityVisible(e, isAnim2) } }
                    currentAnimation.value = next
                    pendingAnimation.value = null
                    startTimeNanos.longValue = frameTimeNanos
                }

                when (currentAnimation.value) {
                    "animation1" -> {
                        val eF = fondEntity.value ?: return@Scene
                        val eG = gaucheEntity.value ?: return@Scene
                        val eD = droiteEntity.value ?: return@Scene
                        val oF = fondOriginalTransform.value ?: return@Scene
                        val oG = gaucheOriginalTransform.value ?: return@Scene
                        val oD = droiteOriginalTransform.value ?: return@Scene
                        val offsetZ = factor * 0.08f
                        val tm = engine.transformManager
                        oF.copyOf().also { it[14] += offsetZ; tm.setTransform(tm.getInstance(eF), it) }
                        oG.copyOf().also { it[14] -= offsetZ; tm.setTransform(tm.getInstance(eG), it) }
                        oD.copyOf().also { it[14] -= offsetZ; tm.setTransform(tm.getInstance(eD), it) }
                    }
                    "animation2" -> {
                        val tm = engine.transformManager
                        hautEntity.value?.let { entity ->
                            hautOriginalTransform.value?.let { orig ->
                                orig.copyOf().also { it[13] -= factor * 0.08f; tm.setTransform(tm.getInstance(entity), it) }
                            }
                        }
                        listOf(
                            bitoniau9Entity  to bitoniau9OriginalTransform,
                            bitoniau10Entity to bitoniau10OriginalTransform,
                            bitoniau11Entity to bitoniau11OriginalTransform,
                            bitoniau12Entity to bitoniau12OriginalTransform
                        ).forEach { (eS, oS) ->
                            eS.value?.let { e -> oS.value?.let { o ->
                                o.copyOf().also { it[13] -= factor * 0.04f; tm.setTransform(tm.getInstance(e), it) }
                            }}
                        }
                        basEntity.value?.let { entity ->
                            basOriginalTransform.value?.let { orig ->
                                orig.copyOf().also { it[13] += factor * 0.08f; tm.setTransform(tm.getInstance(entity), it) }
                            }
                        }
                        listOf(
                            bitoniau5Entity to bitoniau5OriginalTransform,
                            bitoniau6Entity to bitoniau6OriginalTransform,
                            bitoniau7Entity to bitoniau7OriginalTransform,
                            bitoniau8Entity to bitoniau8OriginalTransform
                        ).forEach { (eS, oS) ->
                            eS.value?.let { e -> oS.value?.let { o ->
                                o.copyOf().also { it[13] += factor * 0.04f; tm.setTransform(tm.getInstance(e), it) }
                            }}
                        }
                    }
                }
            }
        )

        // Overlay QR scan
        QrScanOverlay(
            viewModel = qrViewModel,
            currentStepIndex = currentStepIndex.intValue,
            scanManager = scanManager,
            isML = isML,
            modifier = Modifier.fillMaxSize()
        )

        // Header : retour + titre sur UNE ligne ─────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0xDD0F1117))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                text = "${currentStep.icone}  ${currentStep.titre}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (pendingAnimation.value != null) "" else "${currentStep.id}/${ASSEMBLY_STEPS.size}",
                color = Color(0xFF8A94A6),
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        // Barre de progression SOUS le header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 108.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ASSEMBLY_STEPS.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = when {
                                index < currentStepIndex.intValue  -> Color(0xFF4A9EFF)
                                index == currentStepIndex.intValue -> Color(0xFF4A9EFF).copy(alpha = 0.6f)
                                else -> Color(0xFF2A2D3E)
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // Description de l'étape
        val scanState by qrViewModel.state.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
                .background(Color(0xCC0F1117), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentStep.description,
                    color = Color(0xFFE2E8F4),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                if (scanState.lastResult is ScanResult.Correct) {
                    Text("✅", fontSize = 18.sp)
                }
            }
        }

        // ── Boutons Précédent / Suivant ────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentStepIndex.intValue > 0) currentStepIndex.intValue-- },
                enabled = currentStepIndex.intValue > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1D2E),
                    disabledContainerColor = Color(0xFF12141E)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    "◀  Précédent",
                    color = if (currentStepIndex.intValue > 0) Color(0xFF4A9EFF) else Color(0xFF3A3D4E)
                )
            }
            Button(
                onClick = {
                    if (currentStepIndex.intValue < ASSEMBLY_STEPS.size - 1) {
                        currentStepIndex.intValue++
                    } else {
                        showTerminerDialog.value = true
                    }
                },
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A9EFF),
                    disabledContainerColor = Color(0xFF2A3A4E)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    if (currentStepIndex.intValue < ASSEMBLY_STEPS.size - 1) "Suivant  ▶" else "✓ Terminé",
                    color = Color.White
                )
            }
        }
    }
}
