package com.cognitiveassistant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitiveassistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSelectionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.cognitive_tests),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TestCard(
            title = stringResource(R.string.mmse_title),
            description = stringResource(R.string.mmse_description),
            onClick = {  }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TestCard(
            title = stringResource(R.string.moca_title),
            description = stringResource(R.string.moca_description),
            onClick = {  }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TestCard(
            title = stringResource(R.string.rorschach_title),
            description = stringResource(R.string.rorschach_description),
            onClick = {  }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}