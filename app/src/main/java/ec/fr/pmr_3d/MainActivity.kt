package ec.fr.pmr_3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import dev.romainguy.kotlin.math.Float3
import ec.fr.pmr_3d.ui.theme.Pmr_3dTheme
import io.github.sceneview.Scene
import io.github.sceneview.managers.getTransform
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.model.renderableNames

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pmr_3dTheme {
                FilamentScreen()
            }
        }
    }
}

@Composable
fun FilamentScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Viewer3D()
    }
}

@Composable
fun Viewer3D() {
    val currentAnimation = remember { mutableStateOf("animation1") }
    val pendingAnimation = remember { mutableStateOf<String?>(null) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val debugLines = remember { mutableStateListOf<String>() }

    val cameraNode = rememberCameraNode(engine).apply {
        position = Float3(0f, 1f, 4f)
    }
    val mainLightNode = rememberMainLightNode(engine).apply {
        intensity = 100_000f
    }

    val fondEntity = remember { mutableStateOf<Int?>(null) }
    val gaucheEntity = remember { mutableStateOf<Int?>(null) }
    val droiteEntity = remember { mutableStateOf<Int?>(null) }
    val hautEntity = remember { mutableStateOf<Int?>(null) }
    val basEntity = remember { mutableStateOf<Int?>(null) }
    val bitoniau5Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau6Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau7Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau8Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau9Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau10Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau11Entity = remember { mutableStateOf<Int?>(null) }
    val bitoniau12Entity = remember { mutableStateOf<Int?>(null) }

    val fondOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val gaucheOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val droiteOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }

    val hautOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val basOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }

    val bitoniau5OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau6OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau7OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau8OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau9OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau10OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau11OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val bitoniau12OriginalTransform = remember { mutableStateOf<FloatArray?>(null) }

    val startTimeNanos = remember { mutableStateOf(0L) }

    val modelNode = remember {
        try {
            debugLines.add("⏳ Chargement du modèle...")
            val instance = modelLoader.createModelInstance("pmr_assembly.glb")
            debugLines.add("✅ Modèle chargé")
            ModelNode(modelInstance = instance, scaleToUnits = 1f)
        } catch (e: Exception) {
            debugLines.add("❌ ${e.javaClass.simpleName}: ${e.message}")
            null
        }
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
                    debugLines.add("🎯 '$name' trouvé (id=$entity)")
                    val transform = extractTransform(tm, entity)
                    when (name) {
                        "fond"       -> { fondEntity.value = entity; fondOriginalTransform.value = transform }
                        "gauche"     -> { gaucheEntity.value = entity; gaucheOriginalTransform.value = transform }
                        "droite"     -> { droiteEntity.value = entity; droiteOriginalTransform.value = transform }
                        "haut"       -> { hautEntity.value = entity; hautOriginalTransform.value = transform }
                        "bas"        -> { basEntity.value = entity; basOriginalTransform.value = transform }
                        "bitoniau_5" -> { bitoniau5Entity.value = entity; bitoniau5OriginalTransform.value = transform }
                        "bitoniau_6" -> { bitoniau6Entity.value = entity; bitoniau6OriginalTransform.value = transform }
                        "bitoniau_7" -> { bitoniau7Entity.value = entity; bitoniau7OriginalTransform.value = transform }
                        "bitoniau_8" -> { bitoniau8Entity.value = entity; bitoniau8OriginalTransform.value = transform }
                        "bitoniau_9" -> { bitoniau9Entity.value = entity; bitoniau9OriginalTransform.value = transform }
                        "bitoniau_10"-> { bitoniau10Entity.value = entity; bitoniau10OriginalTransform.value = transform }
                        "bitoniau_11"-> { bitoniau11Entity.value = entity; bitoniau11OriginalTransform.value = transform }
                        "bitoniau_12"-> { bitoniau12Entity.value = entity; bitoniau12OriginalTransform.value = transform }
                    }
                } else {
                    debugLines.add("⚠️ '$name' introuvable")
                }
            }

            hautEntity.value?.let { setEntityVisible(it, false) }
            basEntity.value?.let { setEntityVisible(it, false) }
            (5..12).forEach { i ->
                asset.getFirstEntityByName("bitoniau_$i").takeIf { it != 0 }?.let {
                    setEntityVisible(it, false)
                }
            }

            hautEntity.value?.let { setEntityVisible(it, false) }
            basEntity.value?.let { setEntityVisible(it, false) }
            bitoniau5Entity.value?.let { setEntityVisible(it, false) }
            bitoniau6Entity.value?.let { setEntityVisible(it, false) }
            bitoniau7Entity.value?.let { setEntityVisible(it, false) }
            bitoniau8Entity.value?.let { setEntityVisible(it, false) }
            bitoniau9Entity.value?.let { setEntityVisible(it, false) }
            bitoniau10Entity.value?.let { setEntityVisible(it, false) }
            bitoniau11Entity.value?.let { setEntityVisible(it, false) }
            bitoniau12Entity.value?.let { setEntityVisible(it, false) }

            debugLines.add("📋 Nœuds disponibles :")
            asset.renderableNames.forEach { debugLines.add("  • '$it'") }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            childNodes = rememberNodes { modelNode?.let { add(it) } },
            onFrame = { frameTimeNanos ->
                if (startTimeNanos.value == 0L) {
                    startTimeNanos.value = frameTimeNanos
                }

                val elapsed = (frameTimeNanos - startTimeNanos.value) / 1_000_000_000f
                val t = elapsed * (Math.PI.toFloat())
                val factor = (1f - kotlin.math.cos(t)) / 2f


                val cycleProgress = elapsed % 2.0f
                val isEndOfCycle = cycleProgress > 1.97f || cycleProgress < 0.03f

                if (pendingAnimation.value != null && isEndOfCycle) {
                    val next = pendingAnimation.value!!

                    hautEntity.value?.let { setEntityVisible(it, next != "animation1") }
                    basEntity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau5Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau6Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau7Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau8Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau9Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau10Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau11Entity.value?.let { setEntityVisible(it, next != "animation1") }
                    bitoniau12Entity.value?.let { setEntityVisible(it, next != "animation1") }

                    currentAnimation.value = next
                    pendingAnimation.value = null
                    startTimeNanos.value = frameTimeNanos
                    debugLines.add("✅ Switched to $next")
                }

                when (currentAnimation.value) {
                    "animation1" -> {
                        val entity_fond = fondEntity.value ?: return@Scene
                        val entity_gauche = gaucheEntity.value ?: return@Scene
                        val entity_droite = droiteEntity.value ?: return@Scene
                        val origFond = fondOriginalTransform.value ?: return@Scene
                        val origGauche = gaucheOriginalTransform.value ?: return@Scene
                        val origDroite = droiteOriginalTransform.value ?: return@Scene

                        val offsetZ = factor * 0.08f
                        val tm = engine.transformManager

                        val newFond = origFond.copyOf()
                        newFond[14] += offsetZ
                        tm.setTransform(tm.getInstance(entity_fond), newFond)

                        val newGauche = origGauche.copyOf()
                        newGauche[14] -= offsetZ
                        tm.setTransform(tm.getInstance(entity_gauche), newGauche)

                        val newDroite = origDroite.copyOf()
                        newDroite[14] -= offsetZ
                        tm.setTransform(tm.getInstance(entity_droite), newDroite)
                    }

                    "animation2" -> {
                        val tm = engine.transformManager
                        val t = elapsed * (2f * Math.PI.toFloat())
                        val factor = (1f - kotlin.math.cos(t)) / 2f  // 0 → 1 → 0 en 1s

                        hautEntity.value?.let { entity ->
                            hautOriginalTransform.value?.let { orig ->
                                val newT = orig.copyOf()
                                newT[13] -= factor * 0.08f
                                tm.setTransform(tm.getInstance(entity), newT)
                            }
                        }

                        listOf(
                            bitoniau9Entity to bitoniau9OriginalTransform,
                            bitoniau10Entity to bitoniau10OriginalTransform,
                            bitoniau11Entity to bitoniau11OriginalTransform,
                            bitoniau12Entity to bitoniau12OriginalTransform
                        ).forEach { (entityState, transformState) ->
                            entityState.value?.let { entity ->
                                transformState.value?.let { orig ->
                                    val newT = orig.copyOf()
                                    newT[13] -= factor * 0.04f
                                    tm.setTransform(tm.getInstance(entity), newT)
                                }
                            }
                        }

                        basEntity.value?.let { entity ->
                            basOriginalTransform.value?.let { orig ->
                                val newT = orig.copyOf()
                                newT[13] += factor * 0.08f
                                tm.setTransform(tm.getInstance(entity), newT)
                            }
                        }

                        listOf(
                            bitoniau5Entity to bitoniau5OriginalTransform,
                            bitoniau6Entity to bitoniau6OriginalTransform,
                            bitoniau7Entity to bitoniau7OriginalTransform,
                            bitoniau8Entity to bitoniau8OriginalTransform
                        ).forEach { (entityState, transformState) ->
                            entityState.value?.let { entity ->
                                transformState.value?.let { orig ->
                                    val newT = orig.copyOf()
                                    newT[13] += factor * 0.04f
                                    tm.setTransform(tm.getInstance(entity), newT)
                                }
                            }
                        }
                    }
                }
            }
        )

        Button(
            onClick = {
                val target = if (currentAnimation.value == "animation1") "animation2" else "animation1"
                pendingAnimation.value = target
                debugLines.add("🕐 En attente de fin de cycle pour passer à $target")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                if (pendingAnimation.value != null)
                    "⏳ En attente... (${currentAnimation.value})"
                else
                    "Basculer Animation (${currentAnimation.value})"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Text("=== DEBUG ===", color = Color.Yellow, fontWeight = FontWeight.Bold)
            if (debugLines.isEmpty()) Text("(aucun log)", color = Color.Gray, fontSize = 12.sp)
            debugLines.forEach { Text(it, color = Color.White, fontSize = 13.sp) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pmr_3dTheme {
        Text("Prévisualisation non disponible pour la vue 3D")
    }
}