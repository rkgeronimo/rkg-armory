package hr.rkgarmory.app.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.rkgarmory.app.R
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.excursionsOutLabel
import hr.rkgarmory.app.ui.Avatar
import hr.rkgarmory.app.ui.IssueIcon
import hr.rkgarmory.app.ui.ReturnIcon
import hr.rkgarmory.app.ui.StatusChip
import hr.rkgarmory.app.ui.theme.HomeHeader
import hr.rkgarmory.app.ui.theme.StatusIssued
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** How far the summary cards hang below the header; the rest of them overlaps it. */
private val CardOverlap = 36.dp
private const val HomeRowLimit = 6

/**
 * Header with the club logo, a greeting and the date; two summary cards
 * over its lower edge; the waiting issuances and returns, grouped by
 * excursion. [accountMenu] sits in the header where the shell has no top
 * bar (phone).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onOpenIssuance: (Long) -> Unit,
    onOpenReturns: (Long) -> Unit,
    onShowIssuance: () -> Unit,
    onShowReturns: () -> Unit,
    accountMenu: (@Composable () -> Unit)? = null,
    viewModel: HomeViewModel = viewModel(),
) {
    val pending = viewModel.pending
    val active = viewModel.active
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(Unit) {
        viewModel.open()
    }

    PullToRefreshBox(
        isRefreshing = viewModel.refreshing,
        onRefresh = viewModel::reload,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box {
                Column {
                    HomeHero(accountMenu)
                    Spacer(Modifier.height(CardOverlap))
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryCard(
                        value = pending?.size?.toString() ?: "…",
                        label = "Izdavanje",
                        icon = IssueIcon,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onShowIssuance,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCard(
                        value = active?.size?.toString() ?: "…",
                        label = "Razduživanje",
                        icon = ReturnIcon,
                        color = StatusIssued,
                        onClick = onShowReturns,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (viewModel.notice != null) {
                    Text(
                        text = viewModel.notice.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
                val issuance = @Composable { modifier: Modifier ->
                    RequestSection(
                        title = "Izdavanje",
                        rows = pending,
                        action = "Otvori izdavanje",
                        onOpen = onOpenIssuance,
                        onShowAll = onShowIssuance,
                        modifier = modifier,
                    )
                }
                val returns = @Composable { modifier: Modifier ->
                    RequestSection(
                        title = "Razduživanje",
                        rows = active,
                        action = "Otvori razduživanje",
                        onOpen = onOpenReturns,
                        onShowAll = onShowReturns,
                        trailing = { reservation -> ExcursionsOut(reservation) },
                        modifier = modifier,
                    )
                }
                if (portrait) {
                    issuance(Modifier)
                    returns(Modifier)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        issuance(Modifier.weight(1f))
                        returns(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHero(accountMenu: (@Composable () -> Unit)?) {
    val now = LocalDateTime.now()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeHeader)
            .clipToBounds()
            .drawBehind {
                // Faint ring off the top right corner, as in the mockup.
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 96.dp.toPx(),
                    center = Offset(size.width - 50.dp.toPx(), 60.dp.toPx()),
                    style = Stroke(width = 28.dp.toPx()),
                )
            }
            .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 24.dp + CardOverlap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo_blue),
                contentDescription = "Ronilački klub Geronimo",
                modifier = Modifier.size(56.dp),
                colorFilter = ColorFilter.tint(Color.White),
            )
            Spacer(Modifier.weight(1f))
            if (accountMenu != null) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    accountMenu()
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = greeting(now.hour),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = dateLine(now),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun greeting(hour: Int): String = when (hour) {
    in 5..10 -> "Dobro jutro"
    in 11..17 -> "Dobar dan"
    else -> "Dobra večer"
}

private val dateFormat = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.forLanguageTag("hr"))

private fun dateLine(now: LocalDateTime): String =
    now.format(dateFormat).replaceFirstChar { it.titlecase(Locale.forLanguageTag("hr")) }

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = lerp(color, MaterialTheme.colorScheme.onSurface, 0.4f),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Title with a "Sve" link, then up to [HomeRowLimit] reservations in one
 * card, grouped by excursion. A row opens that reservation.
 */
@Composable
private fun RequestSection(
    title: String,
    rows: List<Reservation>?,
    action: String,
    onOpen: (Long) -> Unit,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (Reservation) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onShowAll) {
                Text("Sve", fontWeight = FontWeight.SemiBold)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            when {
                rows == null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                rows.isEmpty() -> Text(
                    text = "Nema zahtjeva",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> {
                    val sorted = rows.sortedWith(
                        compareBy({ it.excursion.isNullOrBlank() }, { it.excursion.orEmpty() }, { it.userName }),
                    )
                    sorted.take(HomeRowLimit)
                        .groupBy { it.excursion?.takeIf(String::isNotBlank) ?: "Bez izleta" }
                        .entries
                        .forEachIndexed { index, (trip, group) ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            Text(
                                text = trip,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 4.dp,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            group.forEach { reservation ->
                                RequestRow(
                                    reservation = reservation,
                                    action = action,
                                    onOpen = onOpen,
                                    trailing = trailing,
                                )
                            }
                        }
                    if (sorted.size > HomeRowLimit) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TextButton(
                            onClick = onShowAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        ) {
                            Text("Prikaži sve (${sorted.size})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestRow(
    reservation: Reservation,
    action: String,
    onOpen: (Long) -> Unit,
    trailing: @Composable (Reservation) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClickLabel = action) { onOpen(reservation.id) }
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(reservation.userName)
        Spacer(Modifier.width(12.dp))
        Text(
            text = reservation.userName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing(reservation)
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Excursions the member signed up for while the gear is out; nothing when none. */
@Composable
private fun ExcursionsOut(reservation: Reservation) {
    StatusChip(
        text = reservation.excursionsOutLabel() ?: return,
        color = StatusIssued,
        modifier = Modifier.padding(start = 8.dp),
    )
}
