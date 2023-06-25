package client_server;

public class Storage {

    private static int numberOfProducts = 0;

    public Storage(int numberOfProducts) {
        Storage.numberOfProducts = numberOfProducts;
    }

    public static int getNumberOfProducts() {
        return numberOfProducts;
    }

    public static void addProducts(int numberOfProducts) {
        Storage.numberOfProducts += numberOfProducts;
    }
    public static void subtractProducts(int numberOfProducts) {
        Storage.numberOfProducts -= numberOfProducts;
    }

}
