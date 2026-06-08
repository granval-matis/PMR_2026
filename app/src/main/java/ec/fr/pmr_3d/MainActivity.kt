package ec.fr.pmr_3d

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currentAnimation = remember { mutableStateOf("animation1") } // "animation1" ou "animation2"
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
    val fondOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val gaucheOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }
    val droiteOriginalTransform = remember { mutableStateOf<FloatArray?>(null) }

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

    LaunchedEffect(modelNode) {
        modelNode?.let { node ->
            val asset = node.modelInstance.asset

            val entity_fond = asset.getFirstEntityByName("fond")
            val entity_gauche = asset.getFirstEntityByName("gauche")
            val entity_droite = asset.getFirstEntityByName("droite")

            val tm = engine.transformManager

            // Initialise les entités
            if (entity_fond != 0) {
                fondEntity.value = entity_fond
                debugLines.add("🎯 Entité 'fond' trouvée (id=$entity_fond)")
            } else {
                debugLines.add("⚠️ Entité 'fond' introuvable")
            }

            if (entity_gauche != 0) {
                gaucheEntity.value = entity_gauche
                debugLines.add("🎯 Entité 'gauche' trouvée (id=$entity_gauche)")
            } else {
                debugLines.add("⚠️ Entité 'gauche' introuvable")
            }

            if (entity_droite != 0) {
                droiteEntity.value = entity_droite
                debugLines.add("🎯 Entité 'droite' trouvée (id=$entity_droite)")
            } else {
                debugLines.add("⚠️ Entité 'droite' introuvable")
            }

            // Récupère les matrices de transformation **pour chaque entité**
            if (entity_fond != 0) {
                val tmInstanceFond = tm.getInstance(entity_fond)
                val matFond = tm.getTransform(tmInstanceFond)
                fondOriginalTransform.value = floatArrayOf(
                    matFond.x.x, matFond.x.y, matFond.x.z, matFond.x.w,
                    matFond.y.x, matFond.y.y, matFond.y.z, matFond.y.w,
                    matFond.z.x, matFond.z.y, matFond.z.z, matFond.z.w,
                    matFond.w.x, matFond.w.y, matFond.w.z, matFond.w.w
                )
                debugLines.add("📐 Transform d'origine sauvegardée pour 'fond'")
            }

            if (entity_gauche != 0) {
                val tmInstanceGauche = tm.getInstance(entity_gauche)
                val matGauche = tm.getTransform(tmInstanceGauche)
                gaucheOriginalTransform.value = floatArrayOf(
                    matGauche.x.x, matGauche.x.y, matGauche.x.z, matGauche.x.w,
                    matGauche.y.x, matGauche.y.y, matGauche.y.z, matGauche.y.w,
                    matGauche.z.x, matGauche.z.y, matGauche.z.z, matGauche.z.w,
                    matGauche.w.x, matGauche.w.y, matGauche.w.z, matGauche.w.w
                )
                debugLines.add("📐 Transform d'origine sauvegardée pour 'gauche'")
            }

            if (entity_droite != 0) {
                val tmInstanceDroite = tm.getInstance(entity_droite)
                val matDroite = tm.getTransform(tmInstanceDroite)
                droiteOriginalTransform.value = floatArrayOf(
                    matDroite.x.x, matDroite.x.y, matDroite.x.z, matDroite.x.w,
                    matDroite.y.x, matDroite.y.y, matDroite.y.z, matDroite.y.w,
                    matDroite.z.x, matDroite.z.y, matDroite.z.z, matDroite.z.w,
                    matDroite.w.x, matDroite.w.y, matDroite.w.z, matDroite.w.w
                )
                debugLines.add("📐 Transform d'origine sauvegardée pour 'droite'")
            }

            // Affiche les noms des nœuds disponibles pour le debug
            debugLines.add("📋 Nœuds disponibles :")
            asset.renderableNames.forEach { name ->
                debugLines.add("  • '$name'")
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // --- Scene 3D ---
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

                val entity_fond = fondEntity.value
                val entity_gauche = gaucheEntity.value
                val entity_droite = droiteEntity.value
                val originalTransformFond = fondOriginalTransform.value
                val originalTransformGauche = gaucheOriginalTransform.value
                val originalTransformDroite = droiteOriginalTransform.value

                if (entity_fond != null && entity_gauche != null && entity_droite != null &&
                    originalTransformFond != null && originalTransformGauche != null && originalTransformDroite != null) {

                    val elapsed = (frameTimeNanos - startTimeNanos.value) / 1_000_000_000f

                    when (currentAnimation.value) {
                        "animation1" -> {
                            // Logique actuelle (translation en Z)
                            val t = elapsed * (2f * Math.PI.toFloat())
                            val factor = (1f - kotlin.math.cos(t)) / 2f
                            val offsetZ = factor * 0.08f

                            val tm = engine.transformManager

                            val tmInstanceFond = tm.getInstance(entity_fond)
                            val newTransformFond = originalTransformFond.copyOf()
                            newTransformFond[14] += offsetZ
                            tm.setTransform(tmInstanceFond, newTransformFond)

                            val tmInstanceGauche = tm.getInstance(entity_gauche)
                            val newTransformGauche = originalTransformGauche.copyOf()
                            newTransformGauche[14] -= offsetZ
                            tm.setTransform(tmInstanceGauche, newTransformGauche)

                            val tmInstanceDroite = tm.getInstance(entity_droite)
                            val newTransformDroite = originalTransformDroite.copyOf()
                            newTransformDroite[14] -= offsetZ
                            tm.setTransform(tmInstanceDroite, newTransformDroite)
                        }

                        "animation2" -> {
                            // TODO: Implémenter l'animation 2 ici
                            // Exemple : rotation ou autre mouvement
                            debugLines.add("⏳ Animation 2 non encore implémentée")
                        }
                    }
                }
            }
        )

        // --- Bouton pour basculer ---
        Button(
            onClick = {
                currentAnimation.value =
                    if (currentAnimation.value == "animation1") "animation2"
                    else "animation1"
                debugLines.add("🔄 Animation changée : ${currentAnimation.value}")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Basculer Animation (${currentAnimation.value})")
        }

        // --- Overlay debug ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            // ... (ton code existant pour les logs)
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