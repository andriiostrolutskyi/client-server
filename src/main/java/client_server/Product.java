package client_server;

public class Product {

    String name;
    static int price = 10;
    ProductGroup productGroup;

    public Product(String name, int price, ProductGroup productGroup) {
        this.name = name;
        this.price = price;
        this.productGroup = productGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public static int getPrice() {
        return price;
    }

    public static void setPrice(int price) {
        Product.price = price;
    }
    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }
}
