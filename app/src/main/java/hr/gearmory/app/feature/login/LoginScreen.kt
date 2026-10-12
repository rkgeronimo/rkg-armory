package hr.gearmory.app.feature.login

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import hr.gearmory.app.BuildConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.gearmory.app.R

@Composable
internal fun LoginScreen(
    error: String?,
    busy: Boolean,
    onLogin: (String, String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val portrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val phone = configuration.smallestScreenWidthDp < 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        BubbleField(Modifier.fillMaxSize())
        if (portrait) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LoginBranding(
                        centered = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(36.dp))
                    LoginCard(
                        error = error,
                        busy = busy,
                        onLogin = onLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 440.dp),
                    )
                }
            }
        } else if (phone) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LoginBranding(
                        centered = true,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(16.dp))
                    LoginCard(
                        error = error,
                        busy = busy,
                        onLogin = onLogin,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoginBranding(modifier = Modifier.weight(1.15f))
                Spacer(Modifier.width(48.dp))
                LoginCard(
                    error = error,
                    busy = busy,
                    onLogin = onLogin,
                    modifier = Modifier
                        .weight(0.85f)
                        .widthIn(max = 420.dp),
                )
            }
        }
    }
}

@Composable
private fun LoginBranding(
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    compact: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_blue),
            contentDescription = "Ronilački klub Geronimo",
            modifier = Modifier.size(120.dp),
        )
        Spacer(Modifier.height(if (compact) 8.dp else 22.dp))
        Text(
            text = "RKG Oružarstvo",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = if (compact) 26.sp else 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        if (!compact) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Jednostavno upravljanje opremom ronilačkog kluba.",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                fontSize = 17.sp,
                lineHeight = 25.sp,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .then(if (centered) Modifier.fillMaxWidth() else Modifier),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun LoginCard(
    error: String?,
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf(if (BuildConfig.DEBUG) "demo" else "") }
    var password by remember { mutableStateOf("") }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(if (compact) 16.dp else 36.dp)) {
            Text(
                text = "Prijava",
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(if (compact) 12.dp else 28.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !busy,
                label = { Text("Korisnik") },
                colors = loginFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !busy,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Lozinka aplikacije") },
                colors = loginFieldColors(),
            )
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(if (compact) 12.dp else 20.dp))
            Button(
                onClick = { if (!busy) onLogin(username, password) },
                enabled = busy || (username.isNotBlank() && password.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else 56.dp),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Prijavi se")
                }
            }
        }
    }
}

private data class Bubble(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val alpha: Float,
    val stroked: Boolean,
)

private val bubbles = listOf(
    Bubble(0.06f, 0.08f, 18f, 0.45f, true),
    Bubble(0.18f, 0.22f, 42f, 0.22f, false),
    Bubble(0.08f, 0.72f, 26f, 0.28f, false),
    Bubble(0.22f, 0.88f, 14f, 0.55f, true),
    Bubble(0.34f, 0.12f, 12f, 0.4f, true),
    Bubble(0.42f, 0.78f, 34f, 0.16f, false),
    Bubble(0.58f, 0.08f, 22f, 0.2f, false),
    Bubble(0.72f, 0.18f, 10f, 0.5f, true),
    Bubble(0.86f, 0.1f, 30f, 0.18f, false),
    Bubble(0.92f, 0.42f, 16f, 0.35f, true),
    Bubble(0.8f, 0.9f, 46f, 0.14f, false),
    Bubble(0.94f, 0.78f, 12f, 0.45f, true),
    Bubble(0.5f, 0.42f, 8f, 0.35f, true),
    Bubble(0.14f, 0.48f, 9f, 0.4f, true),
)

@Composable
private fun BubbleField(modifier: Modifier = Modifier) {
    val fill = Color.White
    val line = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        bubbles.forEach { bubble ->
            val center = Offset(bubble.x * size.width, bubble.y * size.height)
            val radius = bubble.radiusDp.dp.toPx()
            if (bubble.stroked) {
                drawCircle(
                    color = line.copy(alpha = bubble.alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            } else {
                drawCircle(
                    color = fill.copy(alpha = bubble.alpha),
                    radius = radius,
                    center = center,
                )
            }
        }
    }
}
