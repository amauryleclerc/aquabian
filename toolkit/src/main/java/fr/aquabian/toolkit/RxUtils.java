package fr.aquabian.toolkit;

import io.reactivex.functions.Consumer;
import org.slf4j.Logger;


public class RxUtils {
    private RxUtils(){

    }

    public static Consumer<? super Throwable> logError(Logger logger) {
        return e -> logger.error("Exception is :", e);
    }
}
