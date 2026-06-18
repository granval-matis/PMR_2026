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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                Text("←", color = Color(0xFF4A9EFF), fontSize = 20.sp)
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
                emoji = "👓",
                titre = "Matériel requis",
                texte = "Lunettes Magic Leap 2 connectées et chargées. Assurez-vous d'être dans un espace bien éclairé."
            )
            NoticeSection(
                emoji = "📦",
                titre = "Avant de commencer",
                texte = "Sortez toutes les pièces de leur emballage et vérifiez qu'elles correspondent à la liste fournie."
            )
            NoticeSection(
                emoji = "🕹️",
                titre = "Navigation",
                texte = "Utilisez les boutons Précédent / Suivant pour avancer dans les étapes. L'animation 3D se lance automatiquement."
            )
            NoticeSection(
                emoji = "⚠️",
                titre = "Sécurité",
                texte = "Ne forcez jamais les pièces. En cas de doute, revenez à l'étape précédente."
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
