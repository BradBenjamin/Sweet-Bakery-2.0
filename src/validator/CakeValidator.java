package validator;

import domain.Cake;
import exceptions.ValidationException;

public class CakeValidator implements Validator<Cake> {
    @Override
    public void validate(Cake cake) throws ValidationException {
        if (cake.getName() == null || cake.getName().isEmpty()) {
            throw new ValidationException("Patient name cannot be null or empty.");
        }
        if (cake.getFlavor() == null || cake.getFlavor().isEmpty()) {
            throw new ValidationException("Patient flavour cannot be null or empty.");
        }
        if (cake.getPrice()<0) {
            throw new ValidationException("The price must be a positive integer.");
        }

    }
}