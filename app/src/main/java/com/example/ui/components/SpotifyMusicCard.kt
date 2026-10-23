package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpotifyDark
import com.example.ui.theme.SpotifyGreen

data class FocusPlaylist(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val uri: String
)

val CURATED_SPOTIFY_PLAYLISTS = listOf(
    FocusPlaylist(
        title = "Deep Focus",
        subtitle = "Atmospheric reading beats",
        iconEmoji = "🎧",
        uri = "spotify:playlist:37i9dQZF1DWZeKCadgRdKQ"
    ),
    FocusPlaylist(
        title = "Peaceful Piano",
        subtitle = "Calm classical & modern solo",
        iconEmoji = "🎹",
        uri = "spotify:playlist:37i9dQZF1DX4sWSpwq3LiO"
    ),
    FocusPlaylist(
        title = "Reading Chill",
        subtitle = "Soft acoustic & ambient vibes",
        iconEmoji = "📚",
        uri = "spotify:playlist:37i9dQZF1DX8Uebhn9wzrS"
    ),
    FocusPlaylist(
        title = "Lofi Beats",
        subtitle = "Gentle study rhythms",
        iconEmoji = "☕",
        uri = "spotify:playlist:37i9dQZF1DXdLEN7aqioXM"
    ),
    FocusPlaylist(
        title = "Nature Focus",
        subtitle = "Rain, stream & woodland sounds",
        iconEmoji = "🌲",
        uri = "spotify:playlist:37i9dQZF1DX3Ogo9pFvBkY"
    )
)

@Composable
fun SpotifyMusicCard(
    onLaunchSpotify: (Context, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SpotifyDark
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("spotify_music_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SpotifyGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Spotify Music",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Spotify Reading Music",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Light background music for focus",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB3B3B3))
                        )
                    }
                }

                Button(
                    onClick = { onLaunchSpotify(context, "spotify:app") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("open_spotify_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Open Spotify",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CURATED READING PLAYLISTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = SpotifyGreen
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CURATED_SPOTIFY_PLAYLISTS) { playlist ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF282828),
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { onLaunchSpotify(context, playlist.uri) }
                            .testTag("playlist_item_${playlist.title.replace(" ", "_")}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playlist.iconEmoji,
                                    fontSize = 22.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(SpotifyGreen.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = playlist.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFFA0A0A0)
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
