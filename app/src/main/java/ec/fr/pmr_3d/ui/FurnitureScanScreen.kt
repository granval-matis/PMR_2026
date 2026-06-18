package ec.fr.pmr_3d.ui

import androidx.camera.view.PreviewView
import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ec.fr.pmr_3d.CameraPreview
import ec.fr.pmr_3d.QrScanManager
import ec.fr.pmr_3d.ScanningIndicator
import ec.fr.pmr_3d.isMagicLeap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FurnitureScanScreen(
    onScanSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isML = remember { isMagicLeap() }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val scope = rememberCoroutineScope()
    
    val scanManager = remember(previewView) {
        QrScanManager(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onBarcodeDetected = { rawValue ->
                if (successMessage != null) return@QrScanManager

                if (rawValue == "Mon meuble") {
                    successMessage = "Meuble reconnu ! Redirection en cours..."
                    scope.launch {
                        delay(2000)
                        onScanSuccess()
                    }
                } else {
                    errorMessage = "QR code non valide : $rawValue"
                }
            }
        )
    }

    LaunchedEffect(previewView, isML) {
        // Sur Magic Leap on n'a pas besoin de previewView pour démarrer le scanManager 
        // (il n'affichera juste rien mais fera l'analyse)
        if (isML || previewView != null) {
            scanManager.startScanning()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            errorMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fond caméra (uniquement sur téléphone)
        if (!isML) {
            CameraPreview(
                onPreviewViewCreated = { previewView = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Color(0xFF4A9EFF), fontSize = 24.sp)
                }
                Text(
                    "Scanner le meuble",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Zone de visée / Instruction
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Pointez vers le QR code\ndu meuble",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                
                // Coins de visée fictifs
                ScanningIndicator(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Messages de retour (Succès / Erreur)
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = successMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFF22C55E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFFEF4444),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
