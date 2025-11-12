import java.util.concurrent.*;
import java.util.function.DoubleUnaryOperator;

public class MonteCarloIntegrator {
    private final double a; 
    private final double b; 
    private final double minY;
    private final double maxY;
    private final DoubleUnaryOperator function;
    private final int totalPoints; 
    private final int numThreads; 

    public MonteCarloIntegrator(double a, double b, double minY, double maxY, DoubleUnaryOperator function, int totalPoints, int numThreads) {
        this.a = a;
        this.b = b;
        this.minY = minY;
        this.maxY = maxY;
        this.function = function;
        this.totalPoints = totalPoints;
        this.numThreads = numThreads;
    }

    public double integrate() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        java.util.List<Future<IntegralResult>> futures = new java.util.ArrayList<>();
        
        int pointsPerThread = totalPoints / numThreads;
        int remainingPoints = totalPoints % numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            int pointsForThisThread = pointsPerThread;
            if (i == 0) {
                pointsForThisThread += remainingPoints; 
            }
            
            Callable<IntegralResult> task = new MonteCarloTask(
                a, b, minY, maxY, function, pointsForThisThread, i);
            futures.add(executor.submit(task));
        }
        
        int totalPointsInside = 0;
        int totalPointsProcessed = 0;
        
        for (Future<IntegralResult> future : futures) {
            IntegralResult result = future.get();
            totalPointsInside += result.getPointsInside();
            totalPointsProcessed += result.getPointsProcessed();
        }
        
        executor.shutdown();
        
        double rectangleArea = (b - a) * (maxY - minY);
        double ratio = (double) totalPointsInside / totalPointsProcessed;
        
        return rectangleArea * ratio;
    }

    private static class IntegralResult {
        private final int pointsInside;
        private final int pointsProcessed;
        
        public IntegralResult(int pointsInside, int pointsProcessed) {
            this.pointsInside = pointsInside;
            this.pointsProcessed = pointsProcessed;
        }
        
        public int getPointsInside() { return pointsInside; }
        public int getPointsProcessed() { return pointsProcessed; }
    }

    private static class MonteCarloTask implements Callable<IntegralResult> {
        private final double a;
        private final double b;
        private final double minY;
        private final double maxY;
        private final DoubleUnaryOperator function;
        private final int pointsToProcess;
        private final int threadId;
        
        public MonteCarloTask(double a, double b, double minY, double maxY, DoubleUnaryOperator function, int pointsToProcess, int threadId) {
            this.a = a;
            this.b = b;
            this.minY = minY;
            this.maxY = maxY;
            this.function = function;
            this.pointsToProcess = pointsToProcess;
            this.threadId = threadId;
        }
        
        @Override
        public IntegralResult call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int pointsInside = 0;
            
            for (int i = 0; i < pointsToProcess; i++) {
                double x = a + random.nextDouble() * (b - a);
                double y = minY + random.nextDouble() * (maxY - minY);
                
                double fx = function.applyAsDouble(x);
                
                if ((fx >= 0 && y >= 0 && y <= fx) || (fx < 0 && y < 0 && y >= fx)) {
                    pointsInside++;
                }
            }
            
            System.out.printf("%d: %d / %d\n", threadId, pointsInside, pointsToProcess);
            
            return new IntegralResult(pointsInside, pointsToProcess);
        }
    }

    public static void main(String[] args) {
        DoubleUnaryOperator function = x -> Math.cos(x);
        double a = 0;
        double b = 3.14 / 2;
        double minY = 0;
        double maxY = 1;
        int totalPoints = (int)1e6;
        int numThreads = 4;
        
        MonteCarloIntegrator integrator = new MonteCarloIntegrator(
            a, b, minY, maxY, function, totalPoints, numThreads);
        
        try {
            double result = integrator.integrate();
            double exactResult = 1.0;
            
            System.out.printf("mine: %.6f\n", result);
            System.out.printf("must be: %.6f\n", exactResult);
            System.out.printf("abs error: %.6f\n", Math.abs(result - exactResult));
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}