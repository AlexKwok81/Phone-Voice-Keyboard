package com.phonemic.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TouchPadMode(
    onMove: (Int, Int) -> Unit,
    onClick: () -> Unit,
    onRightClick: () -> Unit,
    onScroll: (Int) -> Unit,
    enabled: Boolean = true
) {
    if (!enabled) return

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 触控板主区域（75%宽度）
        Box(
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F0F0))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onMove(dragAmount.x.toInt(), dragAmount.y.toInt())
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onRightClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("触控板", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("滑动移动 · 点击左键 · 长按右键", fontSize = 10.sp, color = Color.LightGray)
            }
        }

        // 滚轮区域（1份宽度）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F0F0))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // 竖向滑动控制滚动，每次滚动固定步长
                        val scrollStep = 3
                        if (dragAmount.y < 0) {
                            onScroll(scrollStep)
                        } else if (dragAmount.y > 0) {
                            onScroll(-scrollStep)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("↕", fontSize = 28.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("滚动", fontSize = 10.sp, color = Color.LightGray, textAlign = TextAlign.Center)
            }
        }
    }
}

