package com.aak1247.example;

import com.aak1247.promise.Promise;


public class Test {
    public static void main(String args[]) throws Exception{
        Promise<Integer> promise = new Promise<>(
                ()->100,
                i-> {
                    System.out.println(i);
                    return i;
                },
                err-> System.out.println(err)
        );
        promise.then(i->{
            System.out.println(promise.getStatus().toString());
            System.out.println(i);
            Integer res = (Integer)i;
            res +=1 ;
            return res;
        },System.out::println)
        .then(i -> {
              System.out.println(i);
              throw new Throwable("xxxxx");
//              return i;
          }, System.out::println)
        .then(
                i->{
                   return i;
                },
                e->((Throwable)e).printStackTrace()
        );
    }
}
