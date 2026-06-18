package ec.fr.pmr_3d.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ec.fr.pmr_3d.models.MENU_ITEMS
import ec.fr.pmr_3d.models.MenuItem
import ec.fr.pmr_3d.models.Screen

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // En-tête
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
                text = "Augmenté",
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
            text = "",
            color = Color(0xFF4A9EFF),
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
