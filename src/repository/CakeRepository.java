package repository;

import domain.Cake;
import filters.AbstractFilter;

import java.util.List;

public class CakeRepository extends FilteredFileRepository<Cake, Integer> {

    public CakeRepository() {
        super();
    }
    public CakeRepository(String filePath) {
        super(filePath);
    }
    public List<Cake> findWithFilter(AbstractFilter<Cake> filter) {
        return super.findWithFilter(filter);
    }
}