package com.example.musicapplicationse114.ui.screen.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicapplicationse114.model.SongResponse

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MiniPlayer(
    song: SongResponse,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        // 🔹 Nền mờ lấy từ hình ảnh thumbnail
        AsyncImage(
            model = song.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp) // Giảm độ blur để giữ chi tiết
                .drawBehind {
                    // Thêm lớp phủ để điều chỉnh độ trong suốt
                    drawRect(color = Color.Black.copy(alpha = 0.85f))
                }
        )

        // 🔹 Lớp nội dung
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Title + Artist
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artistName,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play/Pause Button
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

    }
}
