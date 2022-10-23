package md.vnastasi.vtp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@BenchmarkMode(Mode.AverageTime)
@Fork(warmups = 1, value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
public class MatrixSum {

    private static final int[][] matrix = Sampler.randomMatrix(10_000);

    public static void main(String[] args) throws RunnerException {
        var options = new OptionsBuilder().include(MatrixSum.class.getName()).build();
        new Runner(options).run();
    }

    @Benchmark
    public void sumUsingNestedLoops(Blackhole blackhole) {
        int sum = 0;
        for (var row : matrix) {
            for (var element : row) {
                sum += element;
            }
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void sumUsingSequentialStreams(Blackhole blackhole) {
        var sum = Arrays.stream(matrix).flatMapToInt(Arrays::stream).sum();
        blackhole.consume(sum);
    }

    @Benchmark
    public void sumUsingParallelStreams(Blackhole blackhole) {
        var sum = Arrays.stream(matrix).parallel().flatMapToInt(Arrays::stream).sum();
        blackhole.consume(sum);
    }

    @Benchmark
    public void sumUsingPlatformThreads(Blackhole blackhole) {
        sumUsingExecutor(Executors.newCachedThreadPool(), blackhole);
    }

    @Benchmark
    public void sumUsingVirtualThreads(Blackhole blackhole) {
        sumUsingExecutor(Executors.newVirtualThreadPerTaskExecutor(), blackhole);
    }

    private void sumUsingExecutor(ExecutorService executor, Blackhole blackhole) {
        try (executor) {
            var value = Arrays.stream(matrix)
                    .map(row -> executor.submit(() -> Arrays.stream(row).sum()))
                    .toList().stream()
                    .mapToInt(future -> {
                        try {
                            return future.get();
                        } catch (ExecutionException | InterruptedException e) {
                            throw new IllegalStateException(e);
                        }
                    }).sum();
            blackhole.consume(value);
        }
    }
}
