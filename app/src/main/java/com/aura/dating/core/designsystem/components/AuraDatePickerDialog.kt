package com.aura.dating.core.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBorder
import com.aura.dating.core.designsystem.theme.DarkSurface
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraDatePickerDialog(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val maxDate = Calendar.getInstance().apply {
                    add(Calendar.YEAR, -18)
                }.timeInMillis
                return utcTimeMillis <= maxDate
            }

            override fun isSelectableYear(year: Int): Boolean {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                return year <= (currentYear - 18) && year >= (currentYear - 100)
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "Confirm",
                    color = AuraRose,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = DatePickerDefaults.colors(
            containerColor = DarkSurface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = DarkSurface,
                titleContentColor = Color.White,
                headlineContentColor = AuraRose,
                weekdayContentColor = Color.White.copy(alpha = 0.6f),
                subheadContentColor = Color.White.copy(alpha = 0.8f),
                yearContentColor = Color.White,
                currentYearContentColor = AuraRose,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = AuraRose,
                dayContentColor = Color.White,
                disabledDayContentColor = Color.White.copy(alpha = 0.2f),
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = AuraRose,
                todayDateBorderColor = AuraRose,
                todayContentColor = AuraRose,
                dividerColor = DarkBorder.copy(alpha = 0.5f),
                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraRose,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        )
    }
}
