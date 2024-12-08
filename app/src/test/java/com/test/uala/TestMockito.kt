package com.test.uala

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TestMockito {
    //Test para probar si instale bien mockito
    interface MyService {
        fun fetchData(): String
    }

    @Test
    fun `test mockito`() {
        val mockService: MyService = mock()
        whenever(mockService.fetchData()).thenReturn("Hello Mockito!")

        val result = mockService.fetchData()

        verify(mockService).fetchData()
        assert(result == "Hello Mockito!")
    }
}
