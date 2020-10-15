package com.aak1247.example;


import com.aak1247.promise.Promise;

public class Test {
    public static void main(String[] args) throws Exception {
//        Promise<Integer> promise = new Promise<>(
//                ()->100,
//                i-> {
//                    System.out.println(i);
//                    return i;
//                },
//                err-> System.out.println(err)
//        );
//        promise.then(i->{
//            System.out.println(promise.getStatus().toString());
//            System.out.println(i);
//            Integer res = (Integer)i;
//            res +=1 ;
//            return res;
//        },System.out::println)
//        .then(i -> {
//              System.out.println(i);
//              throw new Throwable("xxxxx");
////              return i;
//          }, System.out::println)
//        .then(
//                i->{
//                   return i;
//                },
//                e->((Throwable)e).printStackTrace()
//        );


        for (int t = 0; t < 100; ++t) {
            int m = t;
            Promise<Integer, Integer, RuntimeException> promise = new Promise<>((resolver, rejector) -> resolver.resolve(m));
            promise.then((i) -> {
                System.out.println(i);
                return "" + i;
            }).then(s -> {
                System.out.println(s);
                Thread.sleep(1);
                throw new Exception(s);
            }).onCatch(e -> {
                return "fuck" + e.getMessage();
            }, Exception.class).then(s -> {
                System.out.println("last:" + s);
                return null;
            });
        }
        Thread.sleep(1000000000);
    }
}
