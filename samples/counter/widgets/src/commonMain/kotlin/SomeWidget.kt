import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SomeWidget() {
    Card {
        println("A")

        Card {
            println("B")

            Card {
                println("C")

                Surface {
                    println("D")
                }
            }
        }
    }
}
