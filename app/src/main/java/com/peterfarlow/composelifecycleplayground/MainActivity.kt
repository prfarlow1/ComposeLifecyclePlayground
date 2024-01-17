package com.peterfarlow.composelifecycleplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: BasicViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewStateData by viewModel.viewState.collectAsState("initialValue")
            LaunchedEffect(Unit) {
                viewModel.onViewEvent("LaunchedEffect")
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Text(viewStateData)
                SideEffect {
                    Log.d(TAG, "displaying $viewStateData in Text")
                }
                ComposableLifecycle { _, event ->
                    Log.d(TAG, "ComposableLifecycle $event")
                    if (event == Lifecycle.Event.ON_START) {
                        viewModel.onViewEvent("ComposableLifecycle $event")
                    }
                }
            }
        }
    }
}

class BasicViewModel : ViewModel() {
    private val eventFlow = MutableSharedFlow<String>(replay = 0)
    val viewState = eventFlow.asSharedFlow()

    fun onViewEvent(value: String) {
        viewModelScope.launch {
            Log.d(TAG, "1 emit value $value while subscriptionCount=${eventFlow.subscriptionCount.value}")
            eventFlow.emit(value)
            Log.d(TAG, "2 emit value $value while subscriptionCount=${eventFlow.subscriptionCount.value}")
        }
    }
}

const val TAG = "Test"

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
