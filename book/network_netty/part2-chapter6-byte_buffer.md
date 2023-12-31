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

초기 상태

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

#### 오버플로우 예제

- 바이트 버퍼를 사용할 때는 항상 버퍼의 크기에 유의하면서 사용해야 한다

```java
package com.github.nettybook.ch6;

import java.nio.ByteBuffer;

public class ByteBufferTest2 {
    public static void main(String[] args) {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        byte[] source = "Hello world!".getBytes();

        for (byte item : source) {
            firstBuffer.put(item);
            System.out.println("현재 상태 : " + firstBuffer);
        }
    }
}
```

```log
초기 상태 : java.nio.HeapByteBuffer[pos=0 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=1 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=2 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=3 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=4 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=5 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=6 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=7 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=8 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=9 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=10 lim=11 cap=11]
현재 상태 : java.nio.HeapByteBuffer[pos=11 lim=11 cap=11]

java.nio.BufferOverflowException // 바이트 버퍼 크기인 capacity보다 position 값이 커졌기 때문에 BufferOverflowException 발생
	at java.base/java.nio.Buffer.nextPutIndex(Buffer.java:722)
	at java.base/java.nio.HeapByteBuffer.put(HeapByteBuffer.java:209)
```

#### 바이트 버퍼 데이터 쓰기 읽기

```java
package com.github.nettybook.ch6;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferTest3 {
    @Test
    public void test() {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        firstBuffer.put((byte) 1);
        System.out.println(firstBuffer.get());
        System.out.println(firstBuffer);
    }
}
```

```log
초기 상태 : java.nio.HeapByteBuffer[pos=0 lim=11 cap=11]
0
java.nio.HeapByteBuffer[pos=2 lim=11 cap=11]
```

- 바이트 버퍼의 get 메서드는 현재 position 속성이 지시하는 위치에서 데이터를 읽기 때문에 두 번째 요소의 초기값인 0이 출력
- get 메서드가 바이트 버퍼의 position 속성을 1 증가 시켜 마지막 pos 값이 2가 된다

#### Rewind를 사용한 데이터 쓰기 읽기

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class RightByteBufferTest3 {
    @Test
    public void test() {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        firstBuffer.put((byte) 1);
        firstBuffer.put((byte) 2);
        assertEquals(2, firstBuffer.position());

        firstBuffer.rewind();
        assertEquals(0, firstBuffer.position());

        assertEquals(1, firstBuffer.get());
        assertEquals(1, firstBuffer.position());

        System.out.println(firstBuffer);
    }
}
```

```log
초기 상태 : java.nio.HeapByteBuffer[pos=0 lim=11 cap=11]
java.nio.HeapByteBuffer[pos=1 lim=11 cap=11]
```

- rewind 메서드를 호출하여 position 속성을 0으로 변경
- 버퍼에 저장된 첫 번째 값을 조회하여 기대한 대로 동작

#### Flip을 사용한 바이트 버퍼 데이터 쓰기 읽기

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class WriteByteBufferTest {
    @Test
    public void writeTest() {
        ByteBuffer firstBuffer = ByteBuffer.allocateDirect(11);
        assertEquals(0, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());
        assertEquals(11, firstBuffer.capacity());

        firstBuffer.put((byte) 1);
        firstBuffer.put((byte) 2);
        firstBuffer.put((byte) 3);
        firstBuffer.put((byte) 4);
        assertEquals(4, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.flip();
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());
    }
}
```

- flip 메서드를 호출하면 limit 속성값이 마지막에 기록한 데이터 위치로 변경

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ReadByteBufferTest {
    @Test
    public void readTest() {
        byte[] tempArray = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0};
        ByteBuffer firstBuffer = ByteBuffer.wrap(tempArray);
        assertEquals(0, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        assertEquals(1, firstBuffer.get());
        assertEquals(2, firstBuffer.get());
        assertEquals(3, firstBuffer.get());
        assertEquals(4, firstBuffer.get());
        assertEquals(4, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.flip();
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());

        firstBuffer.get(3);
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());
    }
}
```

- flip 메서드는 이전에 작업한 마지막 위치를 limit 속성으로 변경
- 즉 쓰기 작업 완료 이후에 데이터의 처음부터 읽을 수 있도록 현재 포인터의 위치를 변경
- 읽기에서 쓰기 또는 쓰기에서 읽기로 작업을 전환

데이터 기록 후

| buffer |                 |
|--------|-----------------|
| 1      |                 |
| 2      |                 |
| 3      |                 |
| 4      | position        |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      |                 |
| ㅤ      | limit, capacity |

flip 호출 후

| buffer |          |
|--------|----------|
| 1      | position | 
| 2      |          | 
| 3      |          | 
| 4      | limit    | 
| ㅤ      |          | 
| ㅤ      |          | 
| ㅤ      |          | 
| ㅤ      |          | 
| ㅤ      |          | 
| ㅤ      |          | 
| ㅤ      | capacity |

```java
public class BufferUnderflowExceptionTest {

    @Test
    void test() {
        byte[] tempArray = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0};
        ByteBuffer firstBuffer = ByteBuffer.wrap(tempArray);
        assertEquals(0, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        assertEquals(1, firstBuffer.get());
        assertEquals(2, firstBuffer.get());
        assertEquals(3, firstBuffer.get());
        assertEquals(4, firstBuffer.get());
        assertEquals(4, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.flip();
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());

        assertEquals(1, firstBuffer.get());
        assertEquals(2, firstBuffer.get());
        assertEquals(3, firstBuffer.get());
        assertEquals(4, firstBuffer.get());
        assertEquals(5, firstBuffer.get()); // limit 초과 BufferUnderflowException 발생
    }

}
```

```log
java.nio.BufferUnderflowException
	at java.base/java.nio.Buffer.nextGetIndex(Buffer.java:699)
	at java.base/java.nio.HeapByteBuffer.get(HeapByteBuffer.java:165)
```

- limit 넘을 경우 BufferUnderflowException 발생

#### 바이트 버퍼 사용시 주의점

- 읽기 쓰기를 분리해서 생각해야함
- 다중 스레드 환경에서 바이트 버퍼를 공유하지 않아야 한다

## 6.2 네티 바이트 버퍼

바이트 버퍼만 사용하고자 한다면 netty-buffer-${version}.jar 라이브러리 참조

네티 바이트 버퍼 특징

- 별도의 읽기 인덱스와 쓰기 인덱스
- flip 메서드 없이 읽기 쓰기 가능
- 가변 바이트 버퍼
- 바이트 버퍼 풀
- 복합 버퍼
- 자바의 바이트 버퍼와 네티 바이트 버퍼 상호 변환

바이트 버퍼 풀을 제공, 빈번한 바이트 버퍼 할당과 해제에 대한 부담을 줄여주어 GC 부담을 줄여줌  
저장되는 데이터형에 따른 별도의 바이트 버퍼를 제공하지 않음, 각 데이터형에 대한 읽기 쓰기 메서드 제공  
읽기, 쓰기 메서드가 실행될 때 각각 읽기 인덱스 쓰기 인덱스를 증가  
읽기 인덱스와 쓰기 인덱스가 분리되어 있기 때문에 별도 메서드 호출 없이 읽기, 쓰기를 수행
하나의 바이트 버퍼에 대하여 쓰기 작업과 읽기 작업을 병행할 수 있다

네티 바이트 버퍼의 초기 상태

| buffer |                         |
|--------|-------------------------|
| ㅤ      | readerIndex, writeIndex |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      |                         |
| ㅤ      | capacity                |

### 6.2.1 네티 바이트 버퍼 생성

네티 바이트 버퍼 풀 제공, 이를 통한 바이트 버퍼 재사용  
바이트 버퍼 풀에 할당하려면 ByteBufAllocator 인터페이스 사용, 즉 ByteBufAllocator 하위 추상 구현체 PooledByteBufAllocator 클래스로 바이트 버퍼 생성  
바이트 버퍼 생성시 인수 지정하지 않을 경우 기본값인 256 바이트 크기의 바이트 버퍼 생성

<details>
<summary>ByteBufAllocator</summary>

```java
package io.netty.buffer;

/**
 * Implementations are responsible to allocate buffers. Implementations of this interface are expected to be
 * thread-safe.
 */
public interface ByteBufAllocator {

    ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;

    // ...

}
```

```java
package io.netty.buffer;

/**
 * A collection of utility methods that is related with handling {@link ByteBuf},
 * such as the generation of hex dump and swapping an integer's byte order.
 */
public final class ByteBufUtil {

    // ...

    static final ByteBufAllocator DEFAULT_ALLOCATOR;

    static {
        String allocType = SystemPropertyUtil.get(
                "io.netty.allocator.type", PlatformDependent.isAndroid() ? "unpooled" : "pooled");
        allocType = allocType.toLowerCase(Locale.US).trim();

        ByteBufAllocator alloc;
        if ("unpooled".equals(allocType)) {
            alloc = UnpooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else if ("pooled".equals(allocType)) {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: pooled (unknown: {})", allocType);
        }

        DEFAULT_ALLOCATOR = alloc;

        THREAD_LOCAL_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 0);
        logger.debug("-Dio.netty.threadLocalDirectBufferSize: {}", THREAD_LOCAL_BUFFER_SIZE);

        MAX_CHAR_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.maxThreadLocalCharBufferSize", 16 * 1024);
        logger.debug("-Dio.netty.maxThreadLocalCharBufferSize: {}", MAX_CHAR_BUFFER_SIZE);
    }

    // ...

}
```

```java
package io.netty.buffer;

/**
 * Creates a new {@link ByteBuf} by allocating new space or by wrapping
 * or copying existing byte arrays, byte buffers and a string.
 *
 * <h3>Use static import</h3>
 * This classes is intended to be used with Java 5 static import statement:
 *
 * <pre>
 * import static io.netty.buffer.{@link Unpooled}.*;
 *
 * {@link ByteBuf} heapBuffer    = buffer(128);
 * {@link ByteBuf} directBuffer  = directBuffer(256);
 * {@link ByteBuf} wrappedBuffer = wrappedBuffer(new byte[128], new byte[256]);
 * {@link ByteBuf} copiedBuffer  = copiedBuffer({@link ByteBuffer}.allocate(128));
 * </pre>
 *
 * <h3>Allocating a new buffer</h3>
 *
 * Three buffer types are provided out of the box.
 *
 * <ul>
 * <li>{@link #buffer(int)} allocates a new fixed-capacity heap buffer.</li>
 * <li>{@link #directBuffer(int)} allocates a new fixed-capacity direct buffer.</li>
 * </ul>
 *
 * <h3>Creating a wrapped buffer</h3>
 *
 * Wrapped buffer is a buffer which is a view of one or more existing
 * byte arrays and byte buffers.  Any changes in the content of the original
 * array or buffer will be visible in the wrapped buffer.  Various wrapper
 * methods are provided and their name is all {@code wrappedBuffer()}.
 * You might want to take a look at the methods that accept varargs closely if
 * you want to create a buffer which is composed of more than one array to
 * reduce the number of memory copy.
 *
 * <h3>Creating a copied buffer</h3>
 *
 * Copied buffer is a deep copy of one or more existing byte arrays, byte
 * buffers or a string.  Unlike a wrapped buffer, there's no shared data
 * between the original data and the copied buffer.  Various copy methods are
 * provided and their name is all {@code copiedBuffer()}.  It is also convenient
 * to use this operation to merge multiple buffers into one buffer.
 */
public final class Unpooled {

    private static final ByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;

    // ...

}
```

```java
package io.netty.buffer;

/**
 * Simplistic {@link ByteBufAllocator} implementation that does not pool anything.
 */
public final class UnpooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {

    /**
     * Default instance which uses leak-detection for direct buffers.
     */
    public static final UnpooledByteBufAllocator DEFAULT =
            new UnpooledByteBufAllocator(PlatformDependent.directBufferPreferred());

    // ...

}
```

</details>

네티 바이트 버퍼의 종류

|         | 풀링                  | 풀링 안함                 |
|---------|---------------------|-----------------------|
| 힙 버퍼    | PooledHeapByteBuf   | UnpooledHeapByteBuf   |
| 다이렉트 버퍼 | PooledDirectByteBuf | UnpooledDirectByteBuf |

네티 바이트 버퍼 생성 방법

|         | 풀링                                      | 풀링 안함                   |
|---------|-----------------------------------------|-------------------------|
| 힙 버퍼    | ByteBufAllocator.DEFAULT.heapBuffer()   | Unpooled.buffer()       |
| 다이렉트 버퍼 | ByteBufAllocator.DEFAULT.directBuffer() | Unpooled.directBuffer() |

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;

import org.junit.Test;

public class CreateByteBufferByNettyTest {
    @Test
    public void createUnpooledHeapBufferTest() {
        ByteBuf buf = Unpooled.buffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createUnpooledDirectBufferTest() {
        ByteBuf buf = Unpooled.directBuffer(11);

        testBuffer(buf, true);
    }

    @Test
    public void createPooledHeapBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createPooledDirectBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(11);

        testBuffer(buf, true);
    }

    private void testBuffer(ByteBuf buf, boolean isDirect) {
        assertEquals(11, buf.capacity());

        assertEquals(isDirect, buf.isDirect());

        assertEquals(0, buf.readableBytes());
        assertEquals(11, buf.writableBytes());
    }
}
```

### 6.2.2 버퍼 사용

#### 바이트 버퍼 읽기 쓰기

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;

import org.junit.Test;

public class ReadWriteByteBufferByNettyTest {
    @Test
    public void createUnpooledHeapBufferTest() {
        ByteBuf buf = Unpooled.buffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createUnpooledDirectBufferTest() {
        ByteBuf buf = Unpooled.directBuffer(11);

        testBuffer(buf, true);
    }

    @Test
    public void createPooledHeapBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createPooledDirectBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(11);

        testBuffer(buf, true);
    }

    private void testBuffer(ByteBuf buf, boolean isDirect) {
        assertEquals(11, buf.capacity());

        assertEquals(isDirect, buf.isDirect());

        buf.writeInt(65537); // 4바이트 크기 정수 65537 기록, 데이터를 기록하는 write 메서드는 기록한 데이터 크기만큼 writeIndex 속성값을 증가
        assertEquals(4, buf.readableBytes()); // 4 바이트를 기록했으므로 읽을 수 있는 바이트 수는 4
        assertEquals(7, buf.writableBytes()); // 4 바이트를 기록했으므로 남은 바이트 수는 7

        assertEquals(1, buf.readShort()); // 2 바이트 정수를 읽어 1 확인, 65537(0x10001) 4바이트 패딩을 하면 0x00010001 이 된다. 그러므로 앞쪽 2바이트는 1이다
        assertEquals(2, buf.readableBytes()); // 4 바이트가 기록된 버퍼에서 2바이트를 읽었으므로 남은 바이트 수 2
        assertEquals(7, buf.writableBytes()); // 읽기 인덱스와 쓰기 인덱스를 별도로 관리하므로 읽기 인덱스가 변경된다고 해서 쓰기 인덱스가 변경되지 않는다

        assertEquals(true, buf.isReadable()); // 바이트 버퍼에 읽지 않은 데이터가 남았는지 확인, 쓰기 인덱스가 읽기 인덱스보다 큰지 검사

        buf.clear(); // 바이트 버퍼를 초기화, 읽기 인덱스와 쓰기 인덱스 값을 모두 0으로 변경, 남은 데이터를 읽지 않고 버린다

        assertEquals(0, buf.readableBytes());
        assertEquals(11, buf.writableBytes());
    }
}
```

#### 가변 크기 버퍼

네티 바이트 버퍼는 생성된 버퍼의 크기를 동적으로 변경할 수 있다  
버퍼 크기를 변경해도 저장된 데이터는 보존된다

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

import org.junit.Test;

public class DynamicByteBufferTest {
    @Test
    public void createUnpooledHeapBufferTest() {
        ByteBuf buf = Unpooled.buffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createUnpooledDirectBufferTest() {
        ByteBuf buf = Unpooled.directBuffer(11);

        testBuffer(buf, true);
    }

    @Test
    public void createPooledHeapBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(11);

        testBuffer(buf, false);
    }

    @Test
    public void createPooledDirectBufferTest() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(11);

        testBuffer(buf, true);
    }

    private void testBuffer(ByteBuf buf, boolean isDirect) {
        assertEquals(11, buf.capacity());
        assertEquals(isDirect, buf.isDirect());

        String sourceData = "hello world";

        buf.writeBytes(sourceData.getBytes()); // "hello world" 문자열 저장
        assertEquals(11, buf.readableBytes()); // 문자열 길이 11 반환 
        assertEquals(0, buf.writableBytes()); // 버퍼 남은 공간 0 

        assertEquals(sourceData, buf.toString(Charset.defaultCharset())); // 버퍼 저장된 문자열 확인

        buf.capacity(6); // 바이트 버퍼 크기 6으로 줄임, 저장된 데이터보다 작은 크기로 조절시 나머지 데이터 잘림
        assertEquals("hello ", buf.toString(Charset.defaultCharset())); // 버퍼의 뒤 "world" 잘린것 확인
        assertEquals(6, buf.capacity());

        buf.capacity(13); // 바이트 버퍼 크기 13으로 증가, 이전 데이터 보존됨
        assertEquals("hello ", buf.toString(Charset.defaultCharset())); // 데이터 보존 확인

        buf.writeBytes("world".getBytes()); // "world" 문자열 추가, 따라서 원 문자열 "hello world" 복구
        assertEquals(sourceData, buf.toString(Charset.defaultCharset())); // 초기 데이터 복구 확인

        assertEquals(13, buf.capacity());
        assertEquals(2, buf.writableBytes()); // 13 바이트 크기에 11 바이트를 기록하였으므로 남은 바이트 수 2
    }
}
```

#### 바이트 버퍼 풀링

네티는 프레임워크에서 바이트 버퍼 풀을 제공  
다이렉트 버퍼, 힙 버퍼 모두 풀링 가능

버퍼를 빈번히 할당하고 해제 할 때 일어나는 GC 횟수의 감소  
버퍼 풀링을 사용하여 GC 수행 횟수를 줄인다

네티 바이트 버퍼 풀링은 ByteBufAllocator를 사용하여 바이트 버퍼를 생성 할 때 자동으로 수행

네티는 바이트 버퍼를 풀링하기 위해서 바이트 버퍼에 참조 수를 기록한다  
참조 수를 관리하기 위해 ReferenceCountUtil 클래스에 정의된 retain 메서드와 release 메서드를 사용  
참조 수가 0일 때 release 호출시 IllegalReferenceCountException 발생

#### 부호 없는 값 읽기

자바는 C언어와 달리 부호 없는 데이터형이 없다

자바에서 1바이트 데이터를 부호 없는 데이터로 변환하는 방법은 2바이트 데이터형에 데이터를 저장하는 것  
부호 없는 데이터를 읽을 때는 읽을 데이터보다 큰 데이터형에 할당한다

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Test;

public class UnsignedByteBufferTest {
    @Test
    public void unsignedBufferToJavaBuffer() {
        ByteBuf buf = Unpooled.buffer(11);

        buf.writeShort(-1);

        assertEquals(65535, buf.getUnsignedShort(0));
    }
}
```

빈 바이트 버퍼에 음수 1 기록  
-1은 부호 있는 16진수 표기법에서 0xFFFF -2는 0xFFFE로 표현  
0xFFFF를 부호 없는 정수로 표현하면 65535  
그러므로 getUnsignedShort 메서드로 바이트 버퍼에 저장된 데이터의 0번째 바이트 부터 2바이트를 읽어서 4바이트 데이터인 int로 읽어들이면 65535가 된다


| 메서드               | 원본 데이터형 | 리턴 데이터형 |
|-------------------|---------|---------|
| getUnsignedByte   | byte    | short   |
| getUnsignedShort  | short   | int     |
| getUnsignedMedium | medium  | int     |
| getUnsignedInt    | int     | long    |

getUnsignedXXX 메서드를 제공  
각 메서드 응답은 읽을 데이터형보다 한 단계 더 큰 데이터형

#### 엔디안 변환

Endian. Endianness의 줄임말

reference
- [https://ko.wikipedia.org/wiki/엔디언](https://ko.wikipedia.org/wiki/%EC%97%94%EB%94%94%EC%96%B8)
- [https://tcpschool.com/c/c_refer_endian](https://tcpschool.com/c/c_refer_endian)

네티의 바이트 버퍼의 기본 엔디안은 자바와 동일하게 빅엔디안  
특별한 상황에서 리틀엔디안의 바이트 버퍼가 필요, 이때 바이트 버퍼의 order 메서드를 사용하여 엔디안을 변환  
**네티 바이트 버퍼의 order 메서드는 새로운 바이트 버퍼를 생성하는 것이 아니라 주어진 바이트 버퍼의 내용을 공유하는 파생 바이트 버퍼 객체를 생성하므로 유의하여 사용**  

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;

import org.junit.Test;

public class OrderedByteBufferTest {
    @Test
    public void pooledHeapBufferTest() {
        ByteBuf buf = Unpooled.buffer(11);
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order()); // 옵션 없이 생성한 네티 바이트 버퍼의 엔디안 확인

        buf.writeShort(1); // 바이트 버퍼의 기본 엔디안이 빅엔디안이므로 0x0001 이 저장된다

        buf.markReaderIndex(); // 현재 바이트 버퍼의 읽기 인덱스 위치를 표시
        // markReaderIndex 로 표시한 읽기 인덱스로 돌아가려면 resetReaderIndex를 사용
        assertEquals(1, buf.readShort()); // 저장한 데이터 1 확인, 빅엔디안으로 저장된 데이터를 그대로 읽는다

        buf.resetReaderIndex(); // 읽기 인덱스의 위치를 markReaderIndex를 사용하여 표시한 위치로 이동

        ByteBuf lettleEndianBuf = buf.order(ByteOrder.LITTLE_ENDIAN); // 바이트 버퍼 order 메서드로 리틀엔디안의 바이트 버퍼 생성
        // 생성된 바이트 버퍼는 바이트 버퍼 내부의 배열과 읽기 인덱스, 쓰기 인덱스를 공유한다
        // 즉 내용은 동일하지만 리틀엔디안에 해당하는 읽기 쓰기 메서드를 제공하는 바이트 버퍼 객체를 얻을 수 있다
        assertEquals(256, lettleEndianBuf.readShort()); // 리틀엔디안에 해당하는 2바이트 Short형 데이터를 읽고 그 값이 256인지 확인  
        // 빅엔디안인 0x0001을 리틀엔디안으로 변환하면 0x0100이 되므로 십진수 256이 된다
    }
}
```

#### 바이트 버퍼 상호 변환

네티 바이트 버퍼는 nioBuffer 메서드를 사용하여 자바 NIO 버퍼로 변환 가능  
변환된 NIO 바이트 버퍼는 네티 바이트 버퍼의 내부 바이트 배열을 공유한다

```java
package com.github.nettybook.ch6;

import static org.junit.Assert.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Test;

public class ConvertByteBufferTest {
    final String source = "Hello world";
    
    @Test
    public void convertNettyBufferToJavaBuffer() {
        ByteBuf buf = Unpooled.buffer(11);
        
        buf.writeBytes(source.getBytes());
        assertEquals(source, buf.toString(Charset.defaultCharset()));

        ByteBuffer nioByteBuffer = buf.nioBuffer();
        assertNotNull(nioByteBuffer);
        assertEquals(source, new String(nioByteBuffer.array()));
        // assertEquals(source, new String(nioByteBuffer.array(), nioByteBuffer.arrayOffset(), nioByteBuffer.remaining()));
    }

    @Test
    public void convertJavaBufferToNettyBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(source.getBytes());
        ByteBuf nettyBuffer = Unpooled.wrappedBuffer(byteBuffer); // Unpooled 클래스의 wrappedBuffer 메서드에 자바 바이트 버퍼를 입력하여 네티 바이트 버퍼 생성
        // 생성한 자바 바이트 버퍼와 네티 바이트 버퍼의 내부 배열은 서로 공유된다
        // 이 같이 내부 배열을 공유하는 바이트 버퍼를 뷰 버퍼라고 한다

        assertEquals(source, nettyBuffer.toString(Charset.defaultCharset()));
    }
}
```

#### 채널과 바이트 버퍼 풀

네티 내부에서 데이터 처리할 때 네티 바이트 버퍼 사용  
channelRead 와 같이 이벤트 메서드 인수로 네티 바이트 버퍼를 사용하고  
실행 이후 네티 바이트 버퍼는 바이트 버퍼 풀로 돌아간다  

네티 바이트 버퍼 풀은 네티 어플리케이션의 서버 소켓 채널이 초기화될 때 같이 초기화  
ChannelHandlerContext 인터페이스의 alloc 메서드로 생성된 바이트 버퍼 풀을 참조할 수 있다


```java
package com.github.nettybook.ch6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * Handler implementation for the echo server.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf readMessage = (ByteBuf) msg;
        System.out.println("channelRead : " + readMessage.toString(Charset.defaultCharset()));

        ByteBufAllocator byteBufAllocator = ctx.alloc(); // ChannelHandlerContext 를 통해서 네티 프레임워크에서 초기화된 ByteBufAllocator를 참조할 수 있다
        // ByteBufAllocator 는 바이트 버퍼 풀을 관리하는 인터페이스이며
        // 플랫폼의 지원 여부에 따라 다이렉트 버퍼와 힙 버퍼 풀을 생성한다
        // 기본적으로 다이렉트 버퍼 풀을 생성하며 어플리케이션 개발자의 필요에 따라 힙 버퍼 풀을 생성할 수도 있다
        ByteBuf newBuffer = byteBufAllocator.buffer(); // buffer 메서드를 사용하여 생성된 바이트 버퍼는 ByteBufAllocator 풀에서 관리됨
        // 바이트 버퍼를 채널에 기록하거나 명시적으로 release 메서드를 호출하면 바이트 버퍼 풀로 돌아간다
        
        // newBuffer 사용.

        ctx.write(msg); // write 메서드의 인수로 바이트 버퍼가 입력되면 데이터를 채널에 기록하고 난 뒤에 버퍼 풀로 돌아간다
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```
