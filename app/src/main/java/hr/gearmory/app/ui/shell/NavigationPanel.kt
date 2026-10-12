package hr.gearmory.app.ui.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.gearmory.app.R

@Composable
internal fun NavigationPanel(
    selected: Destination,
    destinations: List<Destination>,
    darkTheme: Boolean,
    onSelect: (Destination) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_blue),
            contentDescription = "Ronilački klub Geronimo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
                .size(52.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            destinations.forEach { destination ->
                NavigationDestination(
                    title = destination.title,
                    icon = destination.icon,
                    selected = destination == selected,
                    darkTheme = darkTheme,
                    onClick = { onSelect(destination) },
                )
            }
        }
        HorizontalDivider()
        NavigationUtility(
            title = "Tema",
            icon = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = "Promijeni temu",
            onClick = onToggleTheme,
        )
        NavigationUtility(
            title = "Odjava",
            icon = Icons.Rounded.Logout,
            contentDescription = "Odjava",
            onClick = onLogout,
        )
    }
}

@Composable
private fun NavigationDestination(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .height(72.dp)
            .clickable(role = Role.Tab, onClick = onClick),
        color = when {
            !selected -> Color.Transparent
            darkTheme -> Color(0xFF334155)
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = color,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun NavigationUtility(
    title: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .height(48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
