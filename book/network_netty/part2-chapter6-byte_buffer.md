# Part2 Chapter6 바이트 버퍼

## 6.1 자바 NIO 바이트 버퍼

자바 NIO 바이트 버퍼

- 데이터를 저장하고 읽는 저장소
- 배열을 멤버 변수로 가지고 있으며 배열에 대한 읽고 쓰기를 추상화한 메서드를 제공
- 추상화한 메서드로 인해서 개발자가 배열의 인덱스에 대한 계산 없이 데이터의 변경 처리를 수행
- ByteBuffer, CharBuffer, IntBuffer, ShortBuffer, LongBuffer, FloatBuffer, DoubleBuffer가 있으며 저장되는 데이터형에 따라 클래스를 선택하여 사용

내부 배열 상태를 관리하는 세 가지 속성

- capacity
    - 버퍼에 저장할 수 있는 데이터의 최대 크기로 한 번 정하면 변경 불가
    - 버퍼 생성시 생성자 인수로 입력
- position
    - 읽기 또는 쓰기 작업 중인 위치를 나타냄
    - 생성시 0으로 초기화
    - limit, capacity 보다 작거나 같다
- limit
    - 읽고 쓸 수 있는 버퍼 공간의 최대치
    - capacity 보다 크게 설정할 수 없다

### 6.1.1 자바 바이트 버퍼 생성

- static ByteBuffer allocate(int capacity)
    - JVM 힙 영역에 바이트 버퍼를 생성 (힙 버퍼)
    - capacity 인자로 받으며 초기 바이트 버퍼 값은 모두 0으로 초기화
- public static ByteBuffer allocateDirect(int capacity)
    - JVM 힙 영역이 아닌 운영체제의 커널 영역에 바이트 버퍼를 생성 (다이렉트 버퍼)
    - 다이렉트 버퍼는 ByteBuffer로만 생성
    - capacity 인자로 받으며 초기 바이트 버퍼 값은 모두 0으로 초기화
    - 다이렉트 버퍼는 힙 버퍼에 비해서 생성 시간은 길지만 더 빠른 읽기 쓰기 성능을 제공
- public static ByteBuffer wrap(byte[] array, int offset, int length)
    - 입력된 바이트 배열을 사용하여 바이트 버퍼 생성
    - 입력에 사용된 바이트 배열이 변경되면 wrap 메서드를 사용해서 생성한 바이트 버퍼의 내용도 변경된다

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

import org.junit.Test;

public class CreateByteBufferTest {
    @Test
    public void createTest() {
        CharBuffer heapBuffer = CharBuffer.allocate(11); // 11개 char형 데이터 저장할 수 있는 힙 버퍼 생성
        assertEquals(11, heapBuffer.capacity());
        assertEquals(false, heapBuffer.isDirect());

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(11); // 11개 byte형 데이터 저장할 수 있는 다이렉트 버퍼 생성
        assertEquals(11, directBuffer.capacity());
        assertEquals(true, directBuffer.isDirect());

        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0};
        IntBuffer intHeapBuffer = IntBuffer.wrap(array); // array 배열을 감싸는 int형 버퍼 생성, JVM 힙 영역에 생성
        assertEquals(11, intHeapBuffer.capacity());
        assertEquals(false, intHeapBuffer.isDirect());
    }
}
```

### 6.1.2 버퍼 사용

```java
package com.github.nettybook.ch6;

import java.nio.ByteBuffer;

public class ByteBufferTest2 {
    public static void main(String[] args) {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        byte[] source = "Hello world".getBytes();
        firstBuffer.put(source);
        System.out.println("현재 상태 : " + firstBuffer);
    }
}
```

```log
초기 상태 : java.nio.HeapByteBuffer[pos=0  lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=11 lim=11 cap=11]
```

초기상태

| buffer |                 |
|--------|-----------------|
| ㅤ      | position        |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      | limit, capacity |

현재 상태

| buffer |                           |
|--------|---------------------------|
| H      |                           | 
| e      |                           | 
| l      |                           | 
| l      |                           | 
| o      |                           | 
| ㅤ      |                           | 
| w      |                           | 
| o      |                           | 
| r      |                           | 
| l      |                           | 
| d      | position, limit, capacity | 
