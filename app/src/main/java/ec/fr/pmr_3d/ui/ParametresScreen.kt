package ec.fr.pmr_3d.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                Text("←", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                "Paramètres",
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
                        "Texte explicatif sous la scène 3D",
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
                Text("0.1.0 — prototype", color = Color(0xFF8A94A6), fontSize = 13.sp)
            }
        }
    }
}
