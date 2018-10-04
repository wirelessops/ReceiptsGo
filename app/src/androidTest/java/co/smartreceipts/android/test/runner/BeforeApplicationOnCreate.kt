package co.smartreceipts.android.test.runner

/**
 * <p>Sometimes several tests need to perform computationally expensive setup before the Android
 * <code>Application</code> object is created (like pre-loading a database). Annotating a
 * <code>public static void</code> no-arg method with <code>@BeforeApplicationOnCreate</code> causes
 * it to be run once before EACH of the test methods in the class. The static rule was applied to
 * reduce the reliance on <code>Context</code> methods, since many of these will not be available
 * before the application is created. Please note that the <code>@BeforeApplicationOnCreate</code>
 * methods of superclasses will be ignored.</p>
 *
 * For example:
 * <pre>
 * public class Example {
 *    &#064;BeforeApplicationOnCreate public static void onceBeforeEachTest() {
 *       ...
 *    }
 *    &#064;Test public void one() {
 *       ...
 *    }
 *    &#064;Test public void two() {
 *       ...
 *    }
 * }
 * </pre>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BeforeApplicationOnCreate