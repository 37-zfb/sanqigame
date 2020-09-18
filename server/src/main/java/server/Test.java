package server;

public class Test {
    public static void main(String[] args) {
//        System.out.println(fun(6));

//        ReferenceQueue referenceQueue = new ReferenceQueue<ByteBuffer>();
//        PhantomReference phantomReference = new PhantomReference<ByteBuffer>(ByteBuffer.allocateDirect(1024),referenceQueue);

        System.out.println(Runtime.getRuntime().maxMemory());

    }

    static void test(Integer n) {
        n = new Integer(9);
    }

    static int fun(int n) {
        if (n <= 1) {
            return n;
        }

        int result = 3;
        n = n - 1;
        while (n > 1) {
            result = (result % 2) * 3 + (result / 2) * 5;
            n = n - 1;
        }
        return result;
    }

}
