package com.example

import com.example.model.DeviceAccessMode
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun deviceAccessMode_valuesAreValid() {
    val modes = DeviceAccessMode.values()
    assertEquals(3, modes.size)
    assertTrue(modes.contains(DeviceAccessMode.LAPTOP))
    assertTrue(modes.contains(DeviceAccessMode.HANDHELD))
    assertTrue(modes.contains(DeviceAccessMode.AUTO))
    assertEquals("Mode Akses Laptop", DeviceAccessMode.LAPTOP.label)
    assertEquals("💻", DeviceAccessMode.LAPTOP.icon)
  }
}
