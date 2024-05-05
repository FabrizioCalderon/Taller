package com.movil.myapp3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskManagerApp()
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TaskManagerApp() {
    val navController = rememberNavController()
    val tasks = remember { mutableStateListOf<Task>() }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                tasks = tasks,
                onAddTask = { navController.navigate("new") },
                navController = navController
            )
        }
        composable("new") {
            NewTaskScreen(
                tasks = tasks,
                onTaskCreated = { navController.navigateUp() },
                onCancel = { navController.navigateUp() },
                onIconSelected = {  },
                onCategorySelected = {}
            )
        }
        composable(
            "details/{taskIndex}",
            arguments = listOf(navArgument("taskIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskIndex = backStackEntry.arguments?.getInt("taskIndex")
            if (taskIndex != null && taskIndex in tasks.indices) {
                TaskDetailsScreen(
                    task = tasks[taskIndex],
                    onDismiss = { navController.navigateUp() },
                    onTaskCompleted = {
                        tasks[taskIndex].isCompleted = !tasks[taskIndex].isCompleted
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}


@Composable
fun MainScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    navController: NavController
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = Color(0xFF003C71)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar tarea"
                )
            }
        }
    ) { paddingValues ->
        TaskList(
            tasks = tasks,
            paddingValues = paddingValues,
            onTaskClick = { task ->
                navController.navigate("details/${tasks.indexOf(task)}")},
            onTaskCompleted = { }
        )
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    paddingValues: PaddingValues,
    onTaskClick: (Task) -> Unit,
    onTaskCompleted: (Task) -> Unit

) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onTaskClick = { onTaskClick(task) },
                onTaskCompleted = { onTaskCompleted(task) }
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun NewTaskScreen(
    tasks: MutableList<Task>,
    onTaskCreated: () -> Unit,
    onCancel: () -> Unit,
    onIconSelected: (ImageVector) -> Unit,
    onCategorySelected: (TaskCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedIcon by remember { mutableStateOf(Icons.Default.Add) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.WORK) }
    var showDatePicker by remember { mutableStateOf(false) }

    val icons = listOf(
        Icons.Default.Info,
        Icons.Default.Person,
        Icons.Default.Place,
        Icons.Default.Phone,
        Icons.Default.ShoppingCart
    )

    val categories = TaskCategory.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva tarea") },
                navigationIcon = {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                IconButton(
                    onClick = { showDatePicker = true }
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
                if (showDatePicker) {
                    DatePicker(selectedDate = remember { mutableStateOf(dueDate) }, showdate = remember { mutableStateOf(true) })
                }
            }

            Row {
                icons.forEach { icon ->
                    IconButton(
                        onClick = { selectedIcon = icon },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            }

            Row {
                categories.forEach { category ->
                    Button(
                        onClick = {
                            selectedCategory = category
                            onCategorySelected(category) // Llama al callback cuando se selecciona una categoría
                        },
                        modifier = Modifier
                            .size(70.dp)
                            .background(category.color),
                    ) {
                        Text(category.name,
                            fontSize = 8.sp)
                    }
                }
            }

            Button(
                onClick = {
                    tasks.add(
                        Task(
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            icon = selectedIcon,
                            category = selectedCategory,
                            isCompleted = false
                        )
                    )
                    onTaskCreated()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear tarea")
            }
        }
    }
}



@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onTaskCompleted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onTaskClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = task.icon,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = task.dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    color = Color.Black
                )
            }
        }

        val taskStatusText = if (task.isCompleted) "Completada" else "Pendiente"
        Text(
            text = taskStatusText,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

data class Task(

val title: String,
val description: String,
val dueDate: LocalDate,
val icon: ImageVector,
val category: TaskCategory,
var isCompleted: Boolean = false
)

enum class TaskCategory(val color: Color) {
    WORK(Color(0xFF3F51B5)),
    PERSONAL(Color(0xFF009688)),
    SHOPPING(Color(0xFFFFEB3B)),
    STUDY(Color(0xFFFF5722)),
    OTHER(Color(0xFF9C27B0))
}


@Composable
fun TaskDetailsScreen(
    task: Task,
    onTaskCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = task.category.color,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = task.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = task.description,
                color = Color.White
            )
            Text(
                text = task.dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onTaskCompleted()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = task.category.color
                )
            ) {
                Text(if (task.isCompleted) "Marcar como pendiente" else "Marcar como completada")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = task.category.color
                )
            ) {
                Text("Cerrar")
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(selectedDate:MutableState<LocalDate>,showdate:MutableState<Boolean>){
    Column(modifier=Modifier
        .background(colorResource(id = R.color.purple_700))
    ){
        CalendarDialog(
            state = rememberUseCaseState(visible = true,true, onCloseRequest = {}),
            config= CalendarConfig(
                yearSelection = true,
                style = CalendarStyle.MONTH,
            ),
            selection = CalendarSelection.Date(
                selectedDate=selectedDate.value
            ){
                    newDate->
                selectedDate.value=newDate
                showdate.value=false

            }
        )


    }
}










fun String.toTaskCategory(): TaskCategory {
    return when (this) {
        "Urgente" -> TaskCategory.WORK
        "Importante" -> TaskCategory.PERSONAL
        "Personal" -> TaskCategory.PERSONAL
        "Finanzas" -> TaskCategory.SHOPPING
        "Universidad" -> TaskCategory.STUDY
        else -> TaskCategory.OTHER
    }
}