/**
 * Local-run shim ONLY (see LocalJvmTestRunner.kt).
 *
 * kotlin-test.jar from the compiler distribution provides kotlin.test
 * assertions but the @Test annotation is supplied by a framework artifact
 * (kotlin-test-junit / -junit5). In sandboxed environments without Maven
 * access this shim supplies a runtime-retained kotlin.test.Test so the same
 * test sources compile and run via LocalJvmTestRunner.
 *
 * This file is NOT part of the Gradle build. Gradle resolves the real
 * annotation through `testImplementation(kotlin("test"))` + JUnit4.
 */
package kotlin.test

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Test
