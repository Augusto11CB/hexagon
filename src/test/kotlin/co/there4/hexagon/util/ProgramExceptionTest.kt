package co.there4.hexagon.util

import co.there4.hexagon.util.ProgramException
import org.testng.annotations.Test

@Test class ProgramExceptionTest {
    fun program_exception_contains_a_list_of_causes () {
        val causes = (0..9).map { RuntimeException (it.toString()) }
        val exception = ProgramException (1, "Program exception", *causes.toTypedArray())
        assert (exception.causes.size == 10)
        assert (exception.code == 1)
    }
}