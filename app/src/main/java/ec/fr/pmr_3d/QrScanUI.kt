package ec.fr.pmr_3d

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView

@Composable
fun QrScanOverlay(
    viewModel: QrScanViewModel,
    currentStepIndex: Int,
    scanManager: QrScanManager,
    isML: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    // Démarrer/arrêter le scanner selon l'état (uniquement sur Magic Leap)
    // Sur téléphone, le scanner est géré par AssemblageScreen pour le fond AR
    LaunchedEffect(state.isActive) {
        if (isML) {
            if (state.isActive) scanManager.startScanning()
            else scanManager.stopScanning()
        }
    }

    // Nettoyer à la destruction
    DisposableEffect(Unit) {
        onDispose { 
            if (isML) scanManager.stopScanning()
        }
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
            exit = fadeOut(),
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
fun CameraPreview(
    onPreviewViewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                // Force TextureView pour éviter les conflits de SurfaceView avec SceneView
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                onPreviewViewCreated(this)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ScanResultCard(result: ScanResult) {
    val icon: String
    val title: String
    val subtitle: String
    val color: Color

    when (result) {
        is ScanResult.Correct -> {
            icon = "✅"
            title = "Pièce correctement orientée"
            subtitle = "${result.pieceId} — face ${result.face}"
            color = Color(0xFF22C55E)
        }
        is ScanResult.Incorrect -> {
            icon = "❌"
            title = "Mauvaise orientation"
            subtitle = result.messageErreur
            color = Color(0xFFEF4444)
        }
        is ScanResult.UnknownPiece -> {
            icon = "⚠️"
            title = "Pièce non reconnue"
            subtitle = "Ce QR code n'appartient pas à cette étape"
            color = Color(0xFFF59E0B)
        }
        else -> {
            icon = "🔍"
            title = "Scan en cours…"
            subtitle = "Pointez vers un QR code"
            color = Color(0xFF4A9EFF)
        }
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
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            color = Color(0xFF8A94A6),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ScanningIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .size(80.dp)
            .border(2.dp, Color(0xFF4A9EFF).copy(alpha = alpha), RoundedCornerShape(12.dp))
    )
}
