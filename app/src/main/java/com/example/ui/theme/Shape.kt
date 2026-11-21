package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardCornerShape = RoundedCornerShape(14.dp)
val ButtonCornerShape = RoundedCornerShape(9.dp)
val InputCornerShape = RoundedCornerShape(9.dp)
val BadgeCornerShape = RoundedCornerShape(6.dp)
val DialogCornerShape = RoundedCornerShape(20.dp)
val SheetCornerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

val AppShapes = Shapes(
    small = BadgeCornerShape,
    medium = ButtonCornerShape,
    large = CardCornerShape,
    extraLarge = DialogCornerShape
)
