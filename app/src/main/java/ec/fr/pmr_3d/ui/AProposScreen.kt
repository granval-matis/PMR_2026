package ec.fr.pmr_3d.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                Text("←", color = Color(0xFF4A9EFF), fontSize = 20.sp)
            }
            Text(
                "À propos du projet",
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
                Text("🥽", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Assemblage Augmenté",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Assistance mains-libres en Réalité Augmentée\npour le montage de structures",
                    color = Color(0xFF8A94A6),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Équipe
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1D2E), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "👥  Équipe",
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
                    Text("· $nom", color = Color(0xFFE2E8F4), fontSize = 14.sp)
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
                    "🛠   Stack technique",
                    color = Color(0xFF4A9EFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                listOf(
                    "Magic Leap 2"      to "Lunettes AR",
                    "Unity + AR Foundation" to "Rendu 3D",
                    "Kotlin / Android"  to "App compagnon",
                    "TensorFlow Lite"   to "Tracking pièces (à venir)",
                    "Google Cloud"      to "Base de données (à venir)"
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
