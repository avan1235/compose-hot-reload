import org.jetbrains.compose.reload.core.topologicalSort
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TopologicalSortTest {

    class Node(val name: String) {
        val dependencies = mutableListOf<Node>()

        override fun toString(): String {
            return name
        }
    }

    @Test
    fun `test - simple linear`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")

        a.dependencies.add(b)
        b.dependencies.add(c)

        val expected = listOf(a, b, c)
        assertEquals(expected, listOf(a, b, c).topologicalSort { it.dependencies })
        assertEquals(expected, listOf(c, b, a).topologicalSort { it.dependencies })
        assertEquals(expected, listOf(b, c, a).topologicalSort { it.dependencies })
    }

    /**
     * a
     * | \
     * b   c – +
     * |    \  \
     * d     e   f
     *        \
     *         g
     */
    @Test
    fun `test - simple graph`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")
        val e = Node("e")
        val f = Node("f")
        val g = Node("g")

        a.dependencies.add(b)
        a.dependencies.add(c)

        b.dependencies.add(d)

        c.dependencies.add(e)
        c.dependencies.add(f)

        e.dependencies.add(g)

        assertEquals(
            listOf(a, b, c, d, e, f, g),
            listOf(a, b, c, d, e, f, g).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(a, c, b, f, e, d, g),
            listOf(g, f, e, d, c, b, a).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(a, c, b, e, f, d, g),
            listOf(c, e, a, g, b, f, d).topologicalSort { it.dependencies }
        )
    }

    /**
     *    +-> a       |        b
     *   /  / \       |      a   d
     *  +- b   c      |    c
     *     |          |
     *     d          |
     */
    @Test
    fun `test - cycle`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")

        a.dependencies.add(b)
        a.dependencies.add(c)

        b.dependencies.add(d)
        b.dependencies.add(a)

        assertEquals(
            listOf(a, b, c, d),
            listOf(a, b, c, d).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(b, d, a, c),
            listOf(d, c, b, a).topologicalSort { it.dependencies }
        )
    }

    /**
     *    +---> a
     *    |    / \
     *    |   b    c
     *    | /
     *    d
     *   / \
     *  e   f
     */
    @Test
    fun `test - cycle 2`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")
        val e = Node("e")
        val f = Node("f")

        a.dependencies.add(b)
        a.dependencies.add(c)

        b.dependencies.add(d)

        d.dependencies.add(a)
        d.dependencies.add(e)
        d.dependencies.add(f)

        assertEquals(
            listOf(a, b, c, d, e, f),
            listOf(a, b, c, d, e, f).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(d, f, e, a, c, b),
            listOf(f, e, d, c, b, a).topologicalSort { it.dependencies }
        )

        run {
            var cycle = emptyList<Node>()
            listOf(f, e, d, c, b, a).topologicalSort(onCycle = { cycle = it.toList() }) { it.dependencies }
            assertEquals(listOf(d, a, b, d), cycle)
        }

        run {
            var cycle = emptyList<Node>()

            assertEquals(
                listOf(a, c, b, d, f, e),
                listOf(a, f, d, e, c, b).topologicalSort(onCycle = { cycle = it.toList() }) { it.dependencies }
            )

            assertEquals(listOf(a, b, d, a), cycle)
        }
    }

    /**
     *      +-> a
     *     |   / \
     *     b<-+---c
     *    /   |   |
     *   d    +---x
     */
    @Test
    fun `test - cycle 3`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")
        val x = Node("x")

        a.dependencies.add(b)
        a.dependencies.add(c)

        b.dependencies.add(a)
        b.dependencies.add(d)

        c.dependencies.add(b)
        c.dependencies.add(x)

        x.dependencies.add(b)

        run {
            var cycle = emptyList<Node>()
            assertEquals(
                listOf(a, c, x, b, d),
                listOf(a, b, c, d, x).topologicalSort(onCycle = { cycle = it.toList() }) { it.dependencies }
            )

            assertEquals(listOf(a, b, a), cycle)
        }
    }

    /**
     *    x --> a
     *    |  /   \
     *    b <-+-- c
     *        |   |
     *        +-- d
     */
    @Test
    fun `test - cycle 4`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")

        a.dependencies.add(b)
        a.dependencies.add(c)

        c.dependencies.add(b)

    }

    /**
     *        a       x
     *       / \     / \
     *      b  c    y   z
     */
    @Test
    fun `test - multiple roots`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val x = Node("x")
        val y = Node("y")
        val z = Node("z")

        a.dependencies.add(b)
        a.dependencies.add(c)

        x.dependencies.add(y)
        x.dependencies.add(z)

        assertEquals(
            listOf(a, x, b, c, y, z),
            listOf(a, b, c, x, y, z).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(x, a, y, z, b, c),
            listOf(x, y, z, a, b, c).topologicalSort { it.dependencies }
        )

        assertEquals(
            listOf(x, a, y, b, z, c),
            listOf(y, b, x, a, z, c).topologicalSort { it.dependencies }
        )
    }

    /**
     *       a
     *       ^
     *       v
     *  d <> x <> b
     *       ^
     *       v
     *       c
     */
    @Test
    fun `test - star`() {
        val a = Node("a")
        val b = Node("b")
        val c = Node("c")
        val d = Node("d")
        val x = Node("x")

        x.dependencies.add(a)
        x.dependencies.add(b)
        x.dependencies.add(c)
        x.dependencies.add(d)

        a.dependencies.add(x)
        b.dependencies.add(x)
        c.dependencies.add(x)
        d.dependencies.add(x)

        assertEquals(
            listOf(x, a, b, c, d),
            listOf(x, a, b, c, d).topologicalSort { it.dependencies }
        )
    }

    @Test
    fun `test - smoke 1`() = runSmokeTest(1902)

    @Test
    fun `test - smoke 2`() = runSmokeTest(2411)


    private fun runSmokeTest(seed: Int) {
        val random = Random(seed)
        val nodes = buildList { repeat(1024) { add(Node("Node $it")) } }
        nodes.forEach { node ->
            repeat(random.nextInt(0, 12)) {
                node.dependencies.add(nodes.random(random))
            }
        }

        val sorted = nodes.topologicalSort { it.dependencies }
        assertEquals(nodes.size, sorted.size)
        assertEquals(nodes.toSet(), sorted.toSet())
    }
}
