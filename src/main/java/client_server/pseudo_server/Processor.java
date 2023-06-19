package client_server.pseudo_server;

import client_server.Message;

public class Processor {

    public static void process(Message message) {
        Object[] requestInfo = extractValues(message.getUsefulInfo());
        switch ((int) requestInfo[0]) {
            case 1:
                System.out.println("Number of products in storage = " + Storage.getNumberOfProducts());
                System.out.println("Ok");
                break;
            case 2:
                Storage.subtractProducts((int) requestInfo[1]);
                System.out.println("Number of products in storage after subtracting = " + Storage.getNumberOfProducts());
                System.out.println("Ok");
                break;
            case 3:
                Storage.addProducts((int) requestInfo[1]);
                System.out.println("Number of products in storage after adding = " + Storage.getNumberOfProducts());
                System.out.println("Ok");
                break;
            case 4:
                ProductGroup productGroup = new ProductGroup((String) requestInfo[1]);
                System.out.println("Created " + productGroup.getProductGroupName() + " product group");
                System.out.println("Ok");
                break;
            case 5:
                Product.setPrice((int) requestInfo[1]);
                System.out.println("Set price to be " + Product.getPrice());
                System.out.println("Ok");
                break;
        }
    }

    public static Object[] extractValues(String input) {
        String[] parts = input.split("/");

        if (parts.length != 2) {
            return null;
        }

        try {
            Object beforeSlash;
            if (isNumeric(parts[0])) {
                beforeSlash = Integer.parseInt(parts[0]);
            } else {
                beforeSlash = parts[0];
            }

            Object afterSlash;
            if (isNumeric(parts[1])) {
                afterSlash = Integer.parseInt(parts[1]);
            } else {
                afterSlash = parts[1];
            }

            return new Object[]{beforeSlash, afterSlash};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }
}
