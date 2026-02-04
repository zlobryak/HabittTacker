package ru.netology.habittracker.ui.habitlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.netology.habittracker.ui.components.HabitCard
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import ru.netology.habittracker.viewmodels.HabitListViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateToCreate: () -> Unit,
    viewModel: HabitListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HabitListTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить привычку"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Заголовок недели с навигацией
            WeekHeader(
                weekStart = uiState.currentWeekStart,
                onPreviousWeek = { viewModel.navigateWeek(forward = false) },
                onNextWeek = { viewModel.navigateWeek(forward = true) }
            )

            // Список привычек
            if (uiState.habits.isEmpty()) {
                EmptyState(
                    searchQuery = uiState.searchQuery,
                    onAddHabit = onNavigateToCreate
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.habits,
                        key = { it.id }
                    ) { habit ->
                        HabitCard(
                            habit = habit,
                            weekStart = uiState.currentWeekStart,
                            onDayClick = { date ->
                                viewModel.toggleHabitCompletion(habit.id, date)
                            },
                            onDelete = {
                                viewModel.showDeleteDialog(habit)
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления
    val habitToDelete = uiState.habitToDelete
    if (uiState.showDeleteDialog && habitToDelete != null) {
        DeleteConfirmationDialog(
            habitName = habitToDelete.name,
            onConfirm = {
                viewModel.deleteHabit(habitToDelete.id)
            },
            onDismiss = {
                viewModel.hideDeleteDialog()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitListTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск привычек...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Мои привычки",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        onSearchQueryChange("")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Закрыть поиск" else "Поиск",
                    tint = if (isSearchActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val weekEnd = weekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("ru"))
    val weekText = "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}"

    val isCurrentWeek = weekStart == HabitListViewModel.getWeekStart(LocalDate.now())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Предыдущая неделя",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weekText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (isCurrentWeek) {
                    Text(
                        text = "Текущая неделя",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onNextWeek) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Следующая неделя",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    searchQuery: String,
    onAddHabit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (searchQuery.isBlank()) {
                "📝"
            } else {
                "🔍"
            },
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isBlank()) {
                "У вас пока нет привычек"
            } else {
                "Ничего не найдено"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isBlank()) {
                "Начните отслеживать свои привычки,\nнажав на кнопку +"
            } else {
                "Попробуйте изменить поисковый запрос"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (searchQuery.isBlank()) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddHabit,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить первую привычку")
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = "Удалить привычку?")
        },
        text = {
            Text(
                text = "Вы действительно хотите удалить привычку \"$habitName\"? Это действие нельзя отменить.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    "Удалить",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Preview функции
@Preview(name = "Список с привычками", showBackground = true, showSystemUi = true)
@Composable
private fun HabitListScreenPreview() {
    HabitTrackerTheme {
        HabitListScreen(
            onNavigateToCreate = {}
        )
    }
}

@Preview(name = "Пустой список", showBackground = true, showSystemUi = true)
@Composable
private fun HabitListScreenEmptyPreview() {
    HabitTrackerTheme {
        HabitListScreen(
            onNavigateToCreate = {}
        )
    }
}

@Preview(name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun HabitListScreenDarkPreview() {
    HabitTrackerTheme {
        HabitListScreen(
            onNavigateToCreate = {}
        )
    }
}
