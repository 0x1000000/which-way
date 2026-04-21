package x100000.whichway.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import x100000.whichway.data.createGameDataRepository
import x100000.whichway.presentation.theme.WhichWayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = createGameDataRepository(applicationContext)
        setContent {
            WhichWayTheme {
                WhichWayApp(repository = repository)
            }
        }
    }
}
