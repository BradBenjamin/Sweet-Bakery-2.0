package generators;

import domain.Cake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CakeGenerator {
    public static List<Cake> generateCakes(int count) {
        List<Cake> cakes = new ArrayList<>();
        Random random = new Random();
        String[] flavors = {"vanilla", "chocolate", "strawberry", "lemon"};
        for (int i = 0; i < count; i++) {
            String flavor = flavors[random.nextInt(flavors.length)];
            double price = 10 + (100 - 10) * random.nextDouble();  // Random price between 10 and 100
            int size = random.nextInt(3) + 1;  // Random size: 1, 2, or 3
            cakes.add(new Cake(i + 1, "Cake " + (i + 1), price, flavor, size));
        }
        return cakes;
    }
}