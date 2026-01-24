package com.example.klarity.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityShapes

/**
 * Shimmer effect modifier for loading skeletons
 * Creates an animated gradient that simulates content loading
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    return this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, translateAnim),
            end = Offset(translateAnim + 200f, translateAnim + 200f)
        )
    )
}

/**
 * Skeleton placeholder for task cards in Kanban board
 * Shows approximate structure while content loads
 */
@Composable
fun TaskCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = KlarityShapes.Medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Priority badge skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(20.dp)
                        .clip(KlarityShapes.ExtraSmall)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
            
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
            
            // Description skeleton (2 lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
            
            Spacer(Modifier.weight(1f))
            
            // Footer skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(16.dp)
                        .clip(KlarityShapes.Small)
                        .shimmerEffect()
                )
            }
        }
    }
}

/**
 * Skeleton placeholder for Kanban column while loading
 */
@Composable
fun KanbanColumnSkeleton(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight(),
        shape = KlarityShapes.Medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Column header skeleton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .clip(KlarityShapes.Small)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
            
            // Task card skeletons (3 cards)
            repeat(3) {
                TaskCardSkeleton()
            }
        }
    }
}

/**
 * Simple skeleton box for generic content placeholders
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = KlarityShapes.Small
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect()
    )
}

/**
 * Skeleton placeholder for note list items
 */
@Composable
fun NoteListItemSkeleton(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = KlarityShapes.Medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
            
            // Preview text skeleton (2 lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(KlarityShapes.ExtraSmall)
                    .shimmerEffect()
            )
        }
    }
}
