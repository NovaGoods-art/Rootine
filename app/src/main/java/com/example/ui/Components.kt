package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun PlantCanvas(
    consistency: Float,
    plantType: String,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        
        // Colors
        val potColor = Color(0xFFC28E61) // terracotta clay
        val rimColor = Color(0xFFA67145)
        val stemColor = Color(0xFF5B8C5A) // EarthPrimary green
        val wiltColor = Color(0xFFA59E81) // gentle yellow-green wilt
        val petalColor = when (plantType) {
            "Sunflower" -> Color(0xFFE9C46A)
            "Tulip" -> Color(0xFFE76F51)
            "Cactus" -> Color(0xFFF4A261)
            else -> Color(0xFFA7C957)
        }
        
        val activeStemColor = if (consistency < 0.3f) wiltColor else stemColor
        
        // --- 1. Draw the flower pot at the bottom ---
        val potWidthBottom = width * 0.35f
        val potWidthTop = width * 0.45f
        val potHeight = height * 0.25f
        val potY = height - potHeight
        
        val potPath = Path().apply {
            moveTo(width / 2f - potWidthTop / 2f, potY)
            lineTo(width / 2f + potWidthTop / 2f, potY)
            lineTo(width / 2f + potWidthBottom / 2f, height)
            lineTo(width / 2f - potWidthBottom / 2f, height)
            close()
        }
        drawPath(path = potPath, color = potColor)
        
        // Pot rim
        drawRect(
            color = rimColor,
            topLeft = Offset(width / 2f - potWidthTop * 0.55f, potY - 4f),
            size = Size(potWidthTop * 1.1f, 8.dp.toPx())
        )
        
        // --- 2. Draw Plant Stem based on consistency ---
        // Growth height scales from 0.1 to 0.9 of potY (available space)
        val maxGrowthHeight = potY * 0.9f
        val growthHeight = maxGrowthHeight * consistency.coerceIn(0.1f, 1.0f)
        
        val stemBottomX = width / 2f
        val stemBottomY = potY
        val stemTopY = potY - growthHeight
        
        // Wilt offset (curves stem to the side if consistency is low)
        val cvX = if (consistency < 0.30f) {
            // heavy curve to the right/left to look wilted
            stemBottomX + (width * 0.15f)
        } else {
            // straight or slight organic curve
            stemBottomX - (width * 0.03f)
        }
        val cvY = potY - (growthHeight * 0.5f)
        
        val stemTopX = if (consistency < 0.30f) cvX + 15f else stemBottomX
        
        val stemPath = Path().apply {
            moveTo(stemBottomX, stemBottomY)
            quadraticTo(cvX, cvY, stemTopX, stemTopY)
        }
        
        drawPath(
            path = stemPath,
            color = activeStemColor,
            style = Stroke(width = 6.dp.toPx())
        )
        
        // --- 3. Draw Leaves & Branches ---
        if (consistency > 0.15f) {
            // First pair of leaves at 1/3 height
            val leaf1Y = potY - (growthHeight * 0.35f)
            val leaf1X = stemBottomX - (width * 0.08f)
            drawOval(
                color = activeStemColor,
                topLeft = Offset(leaf1X, leaf1Y),
                size = Size(14.dp.toPx(), 7.dp.toPx())
            )
        }
        if (consistency > 0.45f) {
            // Second pair of leaves at 2/3 height
            val leaf2Y = potY - (growthHeight * 0.65f)
            val leaf2X = stemBottomX + (width * 0.06f)
            drawOval(
                color = activeStemColor,
                topLeft = Offset(leaf2X, leaf2Y),
                size = Size(12.dp.toPx(), 6.dp.toPx())
            )
        }
        
        // --- 4. Draw Blooms (if consistency passes 60%) ---
        if (consistency >= 0.60f && plantType != "Fern") {
            val bloomScale = ((consistency - 0.60f) / 0.40f).coerceIn(0.1f, 1.0f)
            val flowerRadius = 16.dp.toPx() * bloomScale
            
            if (plantType == "Sunflower") {
                // Petals
                val numPetals = 8
                for (i in 0 until numPetals) {
                    val angle = (i * 2 * Math.PI / numPetals).toFloat()
                    val petalX = stemTopX + (flowerRadius * 0.7f * kotlin.math.cos(angle))
                    val petalY = stemTopY + (flowerRadius * 0.7f * sin(angle))
                    drawCircle(
                        color = petalColor,
                        radius = flowerRadius * 0.5f,
                        center = Offset(petalX, petalY)
                    )
                }
                // Center
                drawCircle(
                    color = Color(0xFF6F4E37), // brown seed center
                    radius = flowerRadius * 0.4f,
                    center = Offset(stemTopX, stemTopY)
                )
            } else if (plantType == "Tulip") {
                // Tulip cup shape
                val tulipPath = Path().apply {
                    moveTo(stemTopX - flowerRadius * 0.8f, stemTopY)
                    lineTo(stemTopX + flowerRadius * 0.8f, stemTopY)
                    lineTo(stemTopX + flowerRadius * 0.5f, stemTopY - flowerRadius * 1.2f)
                    lineTo(stemTopX, stemTopY - flowerRadius * 0.4f)
                    lineTo(stemTopX - flowerRadius * 0.5f, stemTopY - flowerRadius * 1.2f)
                    close()
                }
                drawPath(path = tulipPath, color = petalColor)
            } else if (plantType == "Cactus") {
                // Friendly spines or top flower
                drawCircle(
                    color = Color(0xFFE56B6F), // pink cactus blossom
                    radius = flowerRadius * 0.6f,
                    center = Offset(stemTopX, stemTopY - 4.dp.toPx())
                )
            }
        } else if (plantType == "Fern") {
            // Ferns grow more branches instead of flowers
            if (consistency > 0.75f) {
                val leaf3Y = potY - (growthHeight * 0.85f)
                val leaf3X = stemTopX - (width * 0.05f)
                drawOval(
                    color = activeStemColor,
                    topLeft = Offset(leaf3X, leaf3Y),
                    size = Size(10.dp.toPx(), 5.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun ConsistencyLineChart(
    weeklyData: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val gridLines = 4
        val rowHeight = height / gridLines
        val labelColor = Color(0xFF7C8C7C)
        val lineColor = Color(0xFF4C754B) // EarthPrimary green
        val shadowColor = Color(0xFF4C754B).copy(alpha = 0.15f)
        
        // 1. Draw grid background lines
        for (i in 0..gridLines) {
            val y = i * rowHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // 2. Plot lines and filled path
        if (weeklyData.isNotEmpty()) {
            val numPoints = weeklyData.size
            val stepX = width / (numPoints - 1).coerceAtLeast(1)
            
            val path = Path()
            val fillPath = Path()
            
            weeklyData.forEachIndexed { index, score ->
                val x = index * stepX
                // invert because canvas y goes down
                val y = height - (score * height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                if (index == numPoints - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }
            
            // Draw gradient shadow
            drawPath(path = fillPath, color = shadowColor)
            
            // Draw main path line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Draw connection dots
            weeklyData.forEachIndexed { index, score ->
                val x = index * stepX
                val y = height - (score * height)
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
