package hr.rkgarmory.app.feature.login

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.rkgarmory.app.BuildConfig
import hr.rkgarmory.app.R
import hr.rkgarmory.app.ui.SystemBarIcons

/** Dark wash over the photo, strongest where the white title sits. */
private val photoScrim = Brush.verticalGradient(
    0f to Color(0x33061E32),
    0.3f to Color(0x00061E32),
    0.6f to Color(0xD9061E32),
    1f to Color(0xF2061E32),
)

/** Keeps white text readable when the keyboard pushes it up onto bright water. */
private val photoTextShadow = Shadow(color = Color(0x99000000), blurRadius = 12f)

/**
 * Underwater photo behind everything. Portrait: logo and title over the
 * photo, the form in a white sheet at the bottom. Landscape: title and
 * form side by side on the photo.
 */
@Composable
internal fun LoginScreen(
    error: String?,
    busy: Boolean,
    onLogin: (String, String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val portrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val phone = configuration.smallestScreenWidthDp < 600
    // Dark icons over the bright water at the top. At the bottom sits the sheet
    // in portrait and the darkest part of the wash in landscape.
    SystemBarIcons(
        darkBehindStatusBar = false,
        darkBehindNavigationBar = !portrait || MaterialTheme.colorScheme.surface.luminance() < 0.5f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF061E32)),
    ) {
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0.3f, -1f),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(photoScrim),
        )
        if (portrait) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LoginBranding(
                        modifier = Modifier
                            .widthIn(max = 488.dp)
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 28.dp),
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Box(contentAlignment = Alignment.TopCenter) {
                            LoginForm(
                                error = error,
                                busy = busy,
                                onLogin = onLogin,
                                modifier = Modifier
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 28.dp),
                            )
                        }
                    }
                }
            }
        } else {
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
                        .padding(horizontal = if (phone) 20.dp else 64.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LoginBranding(
                        compact = phone,
                        modifier = Modifier.weight(1.15f),
                    )
                    Spacer(Modifier.width(if (phone) 16.dp else 48.dp))
                    Card(
                        modifier = Modifier
                            .weight(0.85f)
                            .widthIn(max = 420.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        LoginForm(
                            error = error,
                            busy = busy,
                            onLogin = onLogin,
                            compact = phone,
                            modifier = Modifier.padding(if (phone) 16.dp else 36.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginBranding(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.logo_blue),
            contentDescription = "Ronilački klub Geronimo",
            modifier = Modifier.size(if (compact) 64.dp else 80.dp),
            colorFilter = ColorFilter.tint(Color.White),
        )
        Spacer(Modifier.height(if (compact) 8.dp else 16.dp))
        Text(
            text = "RKG Oružarstvo",
            color = Color.White,
            fontSize = if (compact) 26.sp else 32.sp,
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(shadow = photoTextShadow),
        )
        if (!compact) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Jednostavno upravljanje opremom ronilačkog kluba.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                style = LocalTextStyle.current.copy(shadow = photoTextShadow),
                modifier = Modifier.widthIn(max = 360.dp),
            )
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
)

@Composable
private fun LoginForm(
    error: String?,
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var username by remember { mutableStateOf(if (BuildConfig.DEBUG) "demo" else "") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = "Prijava",
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(if (compact) 12.dp else 20.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !busy,
            label = { Text("Korisnik") },
            shape = RoundedCornerShape(14.dp),
            colors = loginFieldColors(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !busy,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            label = { Text("Lozinka aplikacije") },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showPassword) "Sakrij lozinku" else "Prikaži lozinku",
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = loginFieldColors(),
        )
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Start,
            )
        }
        Spacer(Modifier.height(if (compact) 12.dp else 20.dp))
        Button(
            onClick = { if (!busy) onLogin(username, password) },
            enabled = busy || (username.isNotBlank() && password.isNotBlank()),
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 48.dp else 56.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Prijavi se", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
