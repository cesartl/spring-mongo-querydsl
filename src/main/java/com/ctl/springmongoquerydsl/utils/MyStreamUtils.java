package com.ctl.springmongoquerydsl.utils;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class to work on Java 8 Stream
 * Created by Cesar on 22/08/2016.
 */
public final class MyStreamUtils {
    private MyStreamUtils() {
    }

    public static <T> Stream<T> fromIterator(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
