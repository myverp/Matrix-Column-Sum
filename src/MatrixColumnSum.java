import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class MatrixColumnSum {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Введення розмірності матриці користувачем
        System.out.print("Enter number of rows: ");
        int rows = scanner.nextInt();
        System.out.print("Enter number of columns: ");
        int cols = scanner.nextInt();

        // Введення мінімального та максимального значення
        System.out.print("Enter minimum value: ");
        int min = scanner.nextInt();
        System.out.print("Enter maximum value: ");
        int max = scanner.nextInt();

        // Генерація матриці
        int[][] matrix = generateMatrix(rows, cols, min, max);
        System.out.println("\nGenerated Matrix:");
        printMatrix(matrix);

        // Виконання задачі за допомогою Fork/Join Framework
        long startTimeForkJoin = System.currentTimeMillis();
        int[] resultForkJoin = calculateColumnSumsForkJoin(matrix);
        long endTimeForkJoin = System.currentTimeMillis();
        System.out.println("\nColumn Sums (Fork/Join):");
        printArray(resultForkJoin);
        System.out.println("Execution Time (Fork/Join): " + (endTimeForkJoin - startTimeForkJoin) + " ms");

        // Виконання задачі за допомогою ThreadPool
        long startTimeThreadPool = System.currentTimeMillis();
        int[] resultThreadPool = calculateColumnSumsThreadPool(matrix);
        long endTimeThreadPool = System.currentTimeMillis();
        System.out.println("\nColumn Sums (ThreadPool):");
        printArray(resultThreadPool);
        System.out.println("Execution Time (ThreadPool): " + (endTimeThreadPool - startTimeThreadPool) + " ms");
    }

    // Генерація матриці
    public static int[][] generateMatrix(int rows, int cols, int min, int max) {
        Random random = new Random();
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(max - min + 1) + min;
            }
        }
        return matrix;
    }

    // Виведення матриці
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }

    // Виведення масиву
    public static void printArray(int[] array) {
        for (int value : array) {
            System.out.print(value + "\t");
        }
        System.out.println();
    }

    // Розрахунок суми стовпців за допомогою Fork/Join Framework
    public static int[] calculateColumnSumsForkJoin(int[][] matrix) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new ColumnSumTask(matrix, 0, matrix[0].length - 1));
    }

    // Завдання для Fork/Join
    static class ColumnSumTask extends RecursiveTask<int[]> {
        private static final int THRESHOLD = 1; // Порогове значення для розбиття
        private final int[][] matrix;
        private final int startCol;
        private final int endCol;

        public ColumnSumTask(int[][] matrix, int startCol, int endCol) {
            this.matrix = matrix;
            this.startCol = startCol;
            this.endCol = endCol;
        }

        @Override
        protected int[] compute() {
            if (endCol - startCol <= THRESHOLD) {
                int[] sums = new int[endCol - startCol + 1];
                for (int col = startCol; col <= endCol; col++) {
                    for (int[] row : matrix) {
                        sums[col - startCol] += row[col];
                    }
                }
                return sums;
            } else {
                int mid = (startCol + endCol) / 2;
                ColumnSumTask leftTask = new ColumnSumTask(matrix, startCol, mid);
                ColumnSumTask rightTask = new ColumnSumTask(matrix, mid + 1, endCol);
                invokeAll(leftTask, rightTask);
                int[] leftResult = leftTask.join();
                int[] rightResult = rightTask.join();
                return mergeResults(leftResult, rightResult);
            }
        }

        private int[] mergeResults(int[] left, int[] right) {
            int[] result = new int[left.length + right.length];
            System.arraycopy(left, 0, result, 0, left.length);
            System.arraycopy(right, 0, result, left.length, right.length);
            return result;
        }
    }

    // Розрахунок суми стовпців за допомогою ThreadPool
    public static int[] calculateColumnSumsThreadPool(int[][] matrix) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int cols = matrix[0].length;
        int[] results = new int[cols];
        Future<Integer>[] futures = new Future[cols];

        for (int col = 0; col < cols; col++) {
            final int column = col;
            futures[col] = executor.submit(() -> {
                int sum = 0;
                for (int[] row : matrix) {
                    sum += row[column];
                }
                return sum;
            });
        }

        for (int col = 0; col < cols; col++) {
            try {
                results[col] = futures[col].get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return results;
    }
}
