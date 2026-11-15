package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.Difficulty
import com.example.domain.model.TrailSortOption
import com.example.domain.model.TrekRegion
import com.example.ui.viewmodel.TrailFilterState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filterState: TrailFilterState,
    availableStates: List<String>,
    onSelectRegion: (TrekRegion?) -> Unit,
    onSelectState: (String?) -> Unit,
    onSelectDifficulty: (Difficulty?) -> Unit,
    onSelectSort: (TrailSortOption) -> Unit,
    onToggleSavedOnly: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = rememberTrailHaptics()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Indian Trails",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedButton(
                    onClick = {
                        haptics.heavyAction()
                        onClearAll()
                    },
                    modifier = Modifier.testTag("clear_filters_button")
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Sort By", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrailSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = filterState.sortOption == option,
                        onClick = { onSelectSort(option) },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Region", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterState.selectedRegion == null,
                    onClick = { onSelectRegion(null) },
                    label = { Text("All Regions") }
                )
                TrekRegion.entries.forEach { region ->
                    FilterChip(
                        selected = filterState.selectedRegion == region,
                        onClick = { onSelectRegion(region) },
                        label = { Text(region.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Difficulty", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = filterState.selectedDifficulty == diff,
                        onClick = { onSelectDifficulty(diff) },
                        label = { Text(diff.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("State / Territory", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterState.selectedState == null,
                    onClick = { onSelectState(null) },
                    label = { Text("All States") }
                )
                availableStates.forEach { stateName ->
                    FilterChip(
                        selected = filterState.selectedState == stateName,
                        onClick = {
                            onSelectState(if (filterState.selectedState == stateName) null else stateName)
                        },
                        label = { Text(stateName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            FilterChip(
                selected = filterState.savedOnly,
                onClick = onToggleSavedOnly,
                label = { Text("Show Saved & Offline Ready Only") }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    haptics.confirm()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_filters_button")
            ) {
                Text("Show Trails")
            }
        }
    }
}
