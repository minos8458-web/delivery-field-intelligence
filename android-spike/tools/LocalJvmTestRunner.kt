import java.lang.reflect.InvocationTargetException

/**
 * Minimal reflective runner for the spike's pure-JVM unit tests.
 *
 * Purpose: run the geometry and gesture tests in environments without the
 * Android SDK / Gradle (e.g. CI-less review sandboxes) using only kotlinc
 * and kotlin-test.jar:
 *
 *   kotlinc <geometry sources> <gesture sources> <test sources> \
 *           tools/KotlinTestAnnotationShim.kt tools/LocalJvmTestRunner.kt \
 *           -Xallow-kotlin-package -cp kotlin-test.jar -d out/
 *   java -cp out:kotlin-test.jar:kotlin-stdlib.jar LocalJvmTestRunnerKt
 *
 * On a normal dev machine the same tests run through Gradle:
 *   ./gradlew :app:testDebugUnitTest
 */
fun main() {
    val testClasses = listOf(
        "com.dfi.spike.geometry.RadialGeometryTest",
        "com.dfi.spike.gesture.GestureStateMachineTest",
    )
    var passed = 0
    val failures = mutableListOf<String>()

    for (className in testClasses) {
        val cls = Class.forName(className)
        val instanceCtor = cls.getDeclaredConstructor()
        val testMethods = cls.declaredMethods
            .filter { m -> m.annotations.any { it.annotationClass.simpleName == "Test" } }
            .sortedBy { it.name }
        for (m in testMethods) {
            val label = "${cls.simpleName}.${m.name}"
            try {
                m.invoke(instanceCtor.newInstance())
                println("PASS $label")
                passed++
            } catch (e: InvocationTargetException) {
                println("FAIL $label: ${e.cause?.message}")
                failures += label
            }
        }
    }

    println("---")
    println("passed=$passed failed=${failures.size}")
    if (failures.isNotEmpty()) {
        failures.forEach { println("FAILED: $it") }
        kotlin.system.exitProcess(1)
    }
}
