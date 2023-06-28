package client_server.entities;

import client_server.entities.Category;

public class Product {

    String name;
    String characteristics;
    String manufacturer;
    Integer quantity;
    Integer price;
    Category category;

    public Product(String name, String characteristics, String manufacturer, Integer quantity, Integer price, Category category) {
        this.name = name;
        this.characteristics = characteristics;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    public Product() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setCategory(String name) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" + '\'' +
                "name='" + name + '\'' +
                ", characteristics='" + characteristics + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", category=" + category.getName() +
                '}' + '\n';
    }
}
