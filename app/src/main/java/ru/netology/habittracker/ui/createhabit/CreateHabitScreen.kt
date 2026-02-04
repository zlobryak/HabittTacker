package ru.netology.habittracker.ui.createhabit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import ru.netology.habittracker.viewmodels.CreateHabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateHabitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Новая привычка",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Заголовок
            Text(
                text = "Создайте новую привычку",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Начните отслеживать свой прогресс уже сегодня!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Поле ввода названия
            OutlinedTextField(
                value = uiState.habitName,
                onValueChange = viewModel::onHabitNameChange,
                label = { Text("Название привычки") },
                placeholder = { Text("Например: Утренняя зарядка") },
                isError = uiState.isNameError,
                supportingText = if (uiState.isNameError) {
                    { Text(uiState.errorMessage) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сохранения
            Button(
                onClick = {
                    if (viewModel.saveHabit()) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Создать привычку",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Подсказка
            Text(
                text = "Совет: Выбирайте конкретные и достижимые цели",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Preview функции

@Preview(name = "Пустая форма", showBackground = true, showSystemUi = true)
@Composable
private fun CreateHabitScreenEmptyPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Заполненная форма", showBackground = true, showSystemUi = true)
@Composable
private fun CreateHabitScreenFilledPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(
            onNavigateBack = {}
        )
    }
}

@Preview(name = "С ошибкой валидации", showBackground = true, showSystemUi = true)
@Composable
private fun CreateHabitScreenErrorPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun CreateHabitScreenDarkPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Планшет", device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Composable
private fun CreateHabitScreenTabletPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(
            onNavigateBack = {}
        )
    }
}
