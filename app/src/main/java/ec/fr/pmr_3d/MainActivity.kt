package ec.fr.pmr_3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// MODÃˆLE DE DONNÃ‰ES
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

enum class Screen { HOME, NOTICE, ASSEMBLAGE, PARAMETRES, APROPOS }

data class AssemblyStep(
    val id: Int,
    val titre: String,
    val description: String,
    val animationKey: String,
    val icone: String
)

val ASSEMBLY_STEPS = listOf(
    AssemblyStep(1, "Panneaux latÃ©raux", "Assemblez les panneaux gauche et droit avec le fond.", "animation1", "ðŸªµ"),
    AssemblyStep(2, "Fixation haut/bas", "Positionnez les panneaux haut et bas avec les chevilles.", "animation2", "ðŸ”©"),
)

data class MenuItem(
    val icone: String,
    val titre: String,
    val description: String,
    val screen: Screen
)

val MENU_ITEMS = listOf(
    MenuItem("ðŸ“–", "Notice d'utilisation", "Lisez les instructions avant de commencer", Screen.NOTICE),
    MenuItem("ðŸ”§", "Assemblage du meuble", "Suivez les Ã©tapes d'assemblage en 3D", Screen.ASSEMBLAGE),
    MenuItem("âš™ï¸", "ParamÃ¨tres", "Configurer l'affichage et les prÃ©fÃ©rences", Screen.PARAMETRES),
    MenuItem("â„¹ï¸", "Ã€ propos du projet", "Ã‰quipe, contexte et technologies utilisÃ©es", Screen.APROPOS),
)

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// ACTIVITÃ‰ PRINCIPALE
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pmr_3dTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val currentScreen = remember { mutableStateOf(Screen.HOME) }

    when (currentScreen.value) {
        Screen.HOME       -> HomeScreen(onNavigate = { currentScreen.value = it })
        Screen.NOTICE     -> NoticeScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.ASSEMBLAGE -> AssemblageScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.PARAMETRES -> ParametresScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.APROPOS    -> AProposScreen(onBack = { currentScreen.value = Screen.HOME })
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Ã‰CRAN D'ACCUEIL
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // En-tÃªte
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "Assemblage",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AugmentÃ©",
                color = Color(0xFF4A9EFF),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Assistance mains-libres pour le montage",
                color = Color(0xFF8A94A6),
                fontSize = 14.sp
            )
        }

        HorizontalDivider(color = Color(0xFF1E2130), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Liste des sections
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MENU_ITEMS.forEach { item ->
                MenuItemCard(item = item, onClick = { onNavigate(item.screen) })
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1D2E), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF252840), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icone, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.titre,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = item.description,
                    color = Color(0xFF8A94A6),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Text(
            text = "â€º",
            color = Color(0xFF4A9EFF),
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Ã‰CRAN NOTICE
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun NoticeScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("â†", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                text = "Notice d'utilisation",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color(0xFF1E2130), thickness = 1.dp)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NoticeSection(
                emoji = "ðŸ‘“",
                titre = "MatÃ©riel requis",
                texte = "Lunettes Magic Leap 2 connectÃ©es et chargÃ©es. Assurez-vous d'Ãªtre dans un espace bien Ã©clairÃ©."
            )
            NoticeSection(
                emoji = "ðŸ“¦",
                titre = "Avant de commencer",
                texte = "Sortez toutes les piÃ¨ces de leur emballage et vÃ©rifiez qu'elles correspondent Ã  la liste fournie."
            )
            NoticeSection(
                emoji = "â–¶",
                titre = "Navigation",
                texte = "Utilisez les boutons PrÃ©cÃ©dent / Suivant pour avancer dans les Ã©tapes. L'animation 3D se lance automatiquement."
            )
            NoticeSection(
                emoji = "âš ï¸",
                titre = "SÃ©curitÃ©",
                texte = "Ne forcez jamais les piÃ¨ces. En cas de doute, revenez Ã  l'Ã©tape prÃ©cÃ©dente."
            )
        }
    }
}

@Composable
fun NoticeSection(emoji: String, titre: String, texte: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titre, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(texte, color = Color(0xFF8A94A6), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Ã‰CRAN ASSEMBLAGE
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun AssemblageScreen(onBack: () -> Unit) {
    val currentStepIndex = remember { mutableStateOf(0) }
    val currentStep = ASSEMBLY_STEPS[currentStepIndex.value]

    val currentAnimation = remember { mutableStateOf("animation1") }
    val pendingAnimation = remember { mutableStateOf<String?>(null) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine).apply { position = Float3(0f, 1f, 4f) }
    val mainLightNode = rememberMainLightNode(engine).apply { intensity = 100_000f }

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

    val startTimeNanos = remember { mutableStateOf(0L) }

    val modelNode = remember {
        try {
            val instance = modelLoader.createModelInstance("pmr_assembly.glb")
            ModelNode(modelInstance = instance, scaleToUnits = 1f)
        } catch (e: Exception) { null }
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

    // Charge les entitÃ©s du modÃ¨le
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
            // Masque tout sauf animation1 au dÃ©part
            hautEntity.value?.let { setEntityVisible(it, false) }
            basEntity.value?.let { setEntityVisible(it, false) }
            listOf(
                bitoniau5Entity, bitoniau6Entity, bitoniau7Entity, bitoniau8Entity,
                bitoniau9Entity, bitoniau10Entity, bitoniau11Entity, bitoniau12Entity
            ).forEach { it.value?.let { e -> setEntityVisible(e, false) } }
        }
    }

    // Quand l'Ã©tape change â†’ dÃ©clenche l'animation correspondante
    LaunchedEffect(currentStepIndex.value) {
        pendingAnimation.value = currentStep.animationKey
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // â”€â”€ ScÃ¨ne 3D â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            childNodes = rememberNodes { modelNode?.let { add(it) } },
            onFrame = { frameTimeNanos ->
                if (startTimeNanos.value == 0L) startTimeNanos.value = frameTimeNanos
                val elapsed = (frameTimeNanos - startTimeNanos.value) / 1_000_000_000f
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
                    startTimeNanos.value = frameTimeNanos
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

        // â”€â”€ Header : retour + titre sur UNE ligne â”€â”€â”€â”€â”€
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
                Text("â†", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                text = "${currentStep.icone}  ${currentStep.titre}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (pendingAnimation.value != null) "â³" else "${currentStep.id}/${ASSEMBLY_STEPS.size}",
                color = Color(0xFF8A94A6),
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        // â”€â”€ Barre de progression SOUS le header â”€â”€â”€â”€â”€â”€â”€
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
                                index < currentStepIndex.value  -> Color(0xFF4A9EFF)
                                index == currentStepIndex.value -> Color(0xFF4A9EFF).copy(alpha = 0.6f)
                                else -> Color(0xFF2A2D3E)
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // â”€â”€ Description de l'Ã©tape â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
                .background(Color(0xCC0F1117), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = currentStep.description,
                color = Color(0xFFE2E8F4),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        // â”€â”€ Boutons PrÃ©cÃ©dent / Suivant â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentStepIndex.value > 0) currentStepIndex.value-- },
                enabled = currentStepIndex.value > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1D2E),
                    disabledContainerColor = Color(0xFF12141E)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    "â—€  PrÃ©cÃ©dent",
                    color = if (currentStepIndex.value > 0) Color(0xFF4A9EFF) else Color(0xFF3A3D4E)
                )
            }
            Button(
                onClick = { if (currentStepIndex.value < ASSEMBLY_STEPS.size - 1) currentStepIndex.value++ },
                enabled = currentStepIndex.value < ASSEMBLY_STEPS.size - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A9EFF),
                    disabledContainerColor = Color(0xFF2A3A4E)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    if (currentStepIndex.value < ASSEMBLY_STEPS.size - 1) "Suivant  â–¶" else "âœ“ TerminÃ©",
                    color = Color.White
                )
            }
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Ã‰CRAN PARAMÃˆTRES
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun ParametresScreen(onBack: () -> Unit) {
    val vitesseAnim = remember { mutableStateOf(1f) }
    val afficherDescription = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("â†", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                "ParamÃ¨tres",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        HorizontalDivider(color = Color(0xFF1E2130), thickness = 1.dp)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Toggle description
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Afficher la description",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Texte explicatif sous la scÃ¨ne 3D",
                        color = Color(0xFF8A94A6),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = afficherDescription.value,
                    onCheckedChange = { afficherDescription.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4A9EFF)
                    )
                )
            }

            // Vitesse animation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Vitesse d'animation",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when {
                            vitesseAnim.value < 0.8f -> "Lente"
                            vitesseAnim.value > 1.2f -> "Rapide"
                            else -> "Normale"
                        },
                        color = Color(0xFF4A9EFF),
                        fontSize = 13.sp
                    )
                }
                Text(
                    "Ajuste la vitesse des animations 3D",
                    color = Color(0xFF8A94A6),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = vitesseAnim.value,
                    onValueChange = { vitesseAnim.value = it },
                    valueRange = 0.5f..2f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4A9EFF),
                        activeTrackColor = Color(0xFF4A9EFF),
                        inactiveTrackColor = Color(0xFF2A2D3E)
                    )
                )
            }

            // Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Version", color = Color.White, fontSize = 15.sp)
                Text("0.1.0 â€” prototype", color = Color(0xFF8A94A6), fontSize = 13.sp)
            }
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Ã‰CRAN Ã€ PROPOS
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun AProposScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("â†", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                "Ã€ propos du projet",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        HorizontalDivider(color = Color(0xFF1E2130), thickness = 1.dp)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Titre projet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ðŸ¥½", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Assemblage AugmentÃ©",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Assistance mains-libres en RÃ©alitÃ© AugmentÃ©e\npour le montage de structures",
                    color = Color(0xFF8A94A6),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Ã‰quipe
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "ðŸ‘¥  Ã‰quipe",
                    color = Color(0xFF4A9EFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                listOf(
                    "Wenrui Zhou",
                    "Matis Granval",
                    "Noa Le Roux Mbaye",
                    "Paul Delachaux",
                    "Karl Garrido Andersson"
                ).forEach { nom ->
                    Text("Â· $nom", color = Color(0xFFE2E8F4), fontSize = 14.sp)
                }
            }

            // Stack technique
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "ðŸ›   Stack technique",
                    color = Color(0xFF4A9EFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                listOf(
                    "Magic Leap 2"      to "Lunettes AR",
                    "Unity + AR Foundation" to "Rendu 3D",
                    "Kotlin / Android"  to "App compagnon",
                    "TensorFlow Lite"   to "Tracking piÃ¨ces (Ã  venir)",
                    "Google Cloud"      to "Base de donnÃ©es (Ã  venir)"
                ).forEach { (techno, role) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(techno, color = Color(0xFFE2E8F4), fontSize = 13.sp)
                        Text(role, color = Color(0xFF8A94A6), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// PREVIEW
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pmr_3dTheme {
        Text("PrÃ©visualisation non disponible pour la vue 3D")
    }
}
