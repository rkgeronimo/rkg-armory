package hr.rkgarmory.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hr.rkgarmory.app.remote.Reservation

/**
 * Waiting requests (Issuance, Returns), grouped by excursion in the order
 * they come: the excursion name, then one card with a row per member. The
 * row and its [action] button both open the request.
 */
@Composable
internal fun RequestGroups(
    reservations: List<Reservation>,
    action: String,
    onOpen: (Reservation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        reservations
            .groupBy { it.excursion?.takeIf(String::isNotBlank) ?: "Bez izleta" }
            .forEach { (trip, group) ->
                Text(
                    text = trip,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    group.forEachIndexed { index, reservation ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 72.dp)
                                .clickable(onClickLabel = action) { onOpen(reservation) }
                                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(reservation.userName)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = reservation.userName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.width(12.dp))
                            FilledTonalButton(
                                onClick = { onOpen(reservation) },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(action, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
    }
}
