package ec.fr.pmr_3d

import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import ec.fr.pmr_3d.models.Screen
import ec.fr.pmr_3d.ui.*
import ec.fr.pmr_3d.ui.theme.Pmr_3dTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pmr_3dTheme {
                val currentScreen = remember { mutableStateOf(Screen.HOME) }

                // Active/désactive la transparence selon l'écran (uniquement sur Magic Leap)
                LaunchedEffect(currentScreen.value) {
                    if (currentScreen.value == Screen.ASSEMBLAGE && isMagicLeap()) {
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
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
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

@Composable
fun AppRoot(currentScreen: MutableState<Screen>) {
    when (currentScreen.value) {
        Screen.HOME        -> HomeScreen(onNavigate = { currentScreen.value = it })
        Screen.NOTICE      -> NoticeScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.SCAN_MEUBLE -> FurnitureScanScreen(
            onScanSuccess = { currentScreen.value = Screen.ASSEMBLAGE },
            onBack = { currentScreen.value = Screen.HOME }
        )
        Screen.ASSEMBLAGE  -> AssemblageScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.PARAMETRES  -> ParametresScreen(onBack = { currentScreen.value = Screen.HOME })
        Screen.APROPOS     -> AProposScreen(onBack = { currentScreen.value = Screen.HOME })
    }
}
