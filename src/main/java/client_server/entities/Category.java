package client_server.entities;

public class Category {

    private String name;
    private String characteristics;


    public Category(String name, String characteristics) {
        this.name = name;
        this.characteristics = characteristics;
    }

    public Category() {

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

}
