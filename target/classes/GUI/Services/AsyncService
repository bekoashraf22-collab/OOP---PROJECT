package GUI.Services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AsyncService {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("hotel-gui-worker-" + THREAD_COUNTER.getAndIncrement());
            return thread;
        }
    });

    private AsyncService() {}

    public static <T> void runAsync(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() {
                return work.get();
            }
        };

        task.setOnSucceeded(event -> {
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            if (onError != null) onError.accept(error == null ? new RuntimeException("Unknown background task error.") : error);
        });

        EXECUTOR.submit(task);
    }

    public static boolean isFxThread() {
        return Platform.isFxApplicationThread();
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
